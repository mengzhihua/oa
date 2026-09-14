package com.oa.attendance.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.attendance.dto.ClockRequest;
import com.oa.attendance.dto.LeaveRequest;
import com.oa.attendance.dto.OvertimeRequest;
import com.oa.attendance.dto.PatchRequest;
import com.oa.attendance.dto.TripRequest;
import com.oa.attendance.entity.AttClockRecord;
import com.oa.attendance.entity.AttDaily;
import com.oa.attendance.entity.AttLeaveBalance;
import com.oa.attendance.entity.AttLeaveRequest;
import com.oa.attendance.entity.AttOvertimeRequest;
import com.oa.attendance.entity.AttPatchRequest;
import com.oa.attendance.entity.AttShift;
import com.oa.attendance.entity.AttTripRequest;
import com.oa.attendance.vo.ClockTodayView;
import com.oa.common.BizException;
import com.oa.system.auth.CurrentUser;
import com.oa.workflow.WorkflowService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AttendanceService {
    @Value("${oa.attendance.schedule-enabled:true}")
    private boolean scheduleEnabled;
    private final JdbcTemplate jdbc;
    private final AttClockRecordService clockService;
    private final AttDailyService dailyService;
    private final AttLeaveRequestService leaveService;
    private final AttOvertimeRequestService overtimeService;
    private final AttPatchRequestService patchService;
    private final AttTripRequestService tripService;
    private final AttLeaveBalanceService balanceService;
    private final AttMonthlySummaryService monthlyService;
    private final WorkflowService workflowService;
    private final ObjectMapper objectMapper;

    public AttendanceService(JdbcTemplate jdbc,
                             AttClockRecordService clockService,
                             AttDailyService dailyService,
                             AttLeaveRequestService leaveService,
                             AttOvertimeRequestService overtimeService,
                             AttPatchRequestService patchService,
                             AttTripRequestService tripService,
                             AttLeaveBalanceService balanceService,
                             AttMonthlySummaryService monthlyService,
                             WorkflowService workflowService,
                             ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.clockService = clockService;
        this.dailyService = dailyService;
        this.leaveService = leaveService;
        this.overtimeService = overtimeService;
        this.patchService = patchService;
        this.tripService = tripService;
        this.balanceService = balanceService;
        this.monthlyService = monthlyService;
        this.workflowService = workflowService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AttClockRecord clock(Long userId, ClockRequest request, HttpServletRequest servletRequest) {
        Long employeeId = employeeId(userId);
        LocalDateTime now = LocalDateTime.now();
        String type = request.getClockType();
        if (type == null || type.trim().isEmpty()) {
            Integer count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM att_clock_record WHERE employee_id = ? "
                            + "AND clock_time >= ? AND clock_time < ? AND clock_type = 'IN'",
                    Integer.class, employeeId, now.toLocalDate().atStartOfDay(),
                    now.toLocalDate().plusDays(1).atStartOfDay());
            type = count != null && count > 0 ? "OUT" : "IN";
        }
        AttClockRecord record = new AttClockRecord();
        record.setEmployeeId(employeeId);
        record.setClockTime(now);
        record.setClockType(type);
        record.setSource(defaultValue(request.getSource(), "WEB"));
        record.setDevice(request.getDevice());
        record.setIp(servletRequest.getRemoteAddr());
        record.setRemark(request.getRemark());
        clockService.save(record);
        return record;
    }

    public ClockTodayView today(Long userId) {
        Long employeeId = employeeId(userId);
        LocalDate today = LocalDate.now();
        ClockTodayView view = new ClockTodayView();
        view.setRecords(clockService.lambdaQuery()
                .eq(AttClockRecord::getEmployeeId, employeeId)
                .ge(AttClockRecord::getClockTime, today.atStartOfDay())
                .lt(AttClockRecord::getClockTime, today.plusDays(1).atStartOfDay())
                .orderByAsc(AttClockRecord::getClockTime)
                .list());
        view.setDaily(dailyService.lambdaQuery()
                .eq(AttDaily::getEmployeeId, employeeId)
                .eq(AttDaily::getWorkDate, today)
                .one());
        return view;
    }

    @Transactional
    public AttLeaveRequest submitLeave(Long userId, LeaveRequest request) {
        Long employeeId = employeeId(userId);
        BigDecimal days = workingDays(request.getStartTime().toLocalDate(),
                request.getEndTime().toLocalDate());
        if (days.signum() <= 0) {
            throw new BizException("请假日期不包含工作日");
        }
        if ("ANNUAL".equals(request.getLeaveType())) {
            ensureBalance(employeeId, request.getLeaveType(), days);
        }
        AttLeaveRequest entity = new AttLeaveRequest();
        entity.setEmployeeId(employeeId);
        entity.setLeaveType(request.getLeaveType());
        entity.setStartTime(request.getStartTime());
        entity.setEndTime(request.getEndTime());
        entity.setDays(days);
        entity.setReason(request.getReason());
        entity.setStatus("PENDING");
        leaveService.save(entity);
        Map<String, Object> form = new HashMap<>();
        form.put("days", days);
        form.put("leaveType", request.getLeaveType());
        form.put("reason", request.getReason());
        Map<String, Object> instance = workflowService.start("LEAVE", userId,
                "请假：" + request.getReason(), form, "LEAVE", String.valueOf(entity.getId()));
        entity.setWfInstanceId(number(instance, "ID"));
        leaveService.updateById(entity);
        return entity;
    }

    @Transactional
    public AttOvertimeRequest submitOvertime(Long userId, OvertimeRequest request) {
        Long employeeId = employeeId(userId);
        BigDecimal hours = BigDecimal.valueOf(
                        ChronoUnit.MINUTES.between(request.getStartTime(), request.getEndTime()))
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        if (hours.signum() <= 0) {
            throw new BizException("加班时间必须大于零");
        }
        AttOvertimeRequest entity = new AttOvertimeRequest();
        entity.setEmployeeId(employeeId);
        entity.setStartTime(request.getStartTime());
        entity.setEndTime(request.getEndTime());
        entity.setHours(hours);
        entity.setType(request.getType());
        entity.setReason(request.getReason());
        entity.setStatus("PENDING");
        overtimeService.save(entity);
        Map<String, Object> form = new HashMap<>();
        form.put("hours", hours);
        form.put("type", request.getType());
        form.put("reason", request.getReason());
        Map<String, Object> instance = workflowService.start("OVERTIME", userId,
                "加班：" + request.getReason(), form, "OVERTIME", String.valueOf(entity.getId()));
        entity.setWfInstanceId(number(instance, "ID"));
        overtimeService.updateById(entity);
        return entity;
    }

    @Transactional
    public AttPatchRequest submitPatch(Long userId, PatchRequest request) {
        Long employeeId = employeeId(userId);
        AttPatchRequest entity = new AttPatchRequest();
        entity.setEmployeeId(employeeId);
        entity.setWorkDate(request.getWorkDate());
        entity.setClockType(request.getClockType());
        entity.setClockTime(request.getClockTime());
        entity.setReason(request.getReason());
        entity.setStatus("PENDING");
        patchService.save(entity);
        Map<String, Object> form = new HashMap<>();
        form.put("date", request.getWorkDate().toString());
        form.put("reason", request.getReason());
        Map<String, Object> instance = workflowService.start("PATCH_CLOCK", userId,
                "补卡：" + request.getWorkDate(), form, "PATCH_CLOCK", String.valueOf(entity.getId()));
        entity.setWfInstanceId(number(instance, "ID"));
        patchService.updateById(entity);
        return entity;
    }

    @Transactional
    public AttTripRequest submitTrip(Long userId, TripRequest request) {
        Long employeeId = employeeId(userId);
        BigDecimal days = workingDays(request.getStartDate(), request.getEndDate());
        if (days.signum() <= 0) {
            throw new BizException("出差日期不包含工作日");
        }
        AttTripRequest entity = new AttTripRequest();
        entity.setEmployeeId(employeeId);
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setDestination(request.getDestination());
        entity.setDays(days);
        entity.setReason(request.getReason());
        entity.setStatus("PENDING");
        tripService.save(entity);
        Map<String, Object> form = new HashMap<>();
        form.put("days", days);
        form.put("destination", request.getDestination());
        form.put("reason", request.getReason());
        Map<String, Object> instance = workflowService.start("BUSINESS_TRIP", userId,
                "出差：" + request.getDestination(), form, "BUSINESS_TRIP",
                String.valueOf(entity.getId()));
        entity.setWfInstanceId(number(instance, "ID"));
        tripService.updateById(entity);
        return entity;
    }

    @Transactional
    public void cancel(Long userId, String type, Long id) {
        Long instanceId;
        if ("LEAVE".equals(type)) {
            instanceId = leaveService.getById(id).getWfInstanceId();
        } else if ("OVERTIME".equals(type)) {
            instanceId = overtimeService.getById(id).getWfInstanceId();
        } else if ("PATCH_CLOCK".equals(type)) {
            instanceId = patchService.getById(id).getWfInstanceId();
        } else {
            instanceId = tripService.getById(id).getWfInstanceId();
        }
        workflowService.cancel(instanceId, userId);
    }

    @Transactional
    public AttDaily calcDaily(Long employeeId, LocalDate date) {
        AttShift shift = findShift(employeeId, date);
        String status = holidayStatus(date);
        List<AttClockRecord> records = clockService.lambdaQuery()
                .eq(AttClockRecord::getEmployeeId, employeeId)
                .ge(AttClockRecord::getClockTime, date.atStartOfDay())
                .lt(AttClockRecord::getClockTime, date.plusDays(1).atStartOfDay())
                .orderByAsc(AttClockRecord::getClockTime)
                .list();
        AttDaily daily = new AttDaily();
        daily.setEmployeeId(employeeId);
        daily.setWorkDate(date);
        daily.setShiftId(shift == null ? null : shift.getId());
        daily.setFirstIn(first(records, "IN"));
        daily.setLastOut(last(records, "OUT"));
        daily.setLateMinutes(0);
        daily.setEarlyMinutes(0);
        daily.setWorkMinutes(workMinutes(daily.getFirstIn(), daily.getLastOut()));
        if ("HOLIDAY".equals(status)) {
            daily.setStatus("HOLIDAY");
        } else if ("REST".equals(status)) {
            daily.setStatus("REST");
        } else if (approvedLeave(employeeId, date)) {
            daily.setStatus("LEAVE");
            daily.setLeaveType(leaveType(employeeId, date));
        } else if (approvedTrip(employeeId, date)) {
            daily.setStatus("TRIP");
        } else if (shift == null || (daily.getFirstIn() == null && daily.getLastOut() == null)) {
            daily.setStatus("ABSENT");
        } else {
            int late = lateMinutes(daily.getFirstIn(), date, shift);
            int early = earlyMinutes(daily.getLastOut(), date, shift);
            daily.setLateMinutes(late);
            daily.setEarlyMinutes(early);
            daily.setStatus(late > 0 ? "LATE" : early > 0 ? "EARLY" : "NORMAL");
        }
        AttDaily existing = dailyService.lambdaQuery()
                .eq(AttDaily::getEmployeeId, employeeId)
                .eq(AttDaily::getWorkDate, date)
                .one();
        if (existing == null) {
            dailyService.save(daily);
        } else {
            daily.setId(existing.getId());
            dailyService.updateById(daily);
        }
        return daily;
    }

    @Transactional
    public int recalc(Long deptId, LocalDate from, LocalDate to) {
        List<Long> employeeIds = employeeIds(deptId);
        int count = 0;
        for (Long employeeId : employeeIds) {
            for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
                calcDaily(employeeId, date);
                count++;
            }
        }
        return count;
    }

    @Scheduled(cron = "${oa.attendance.daily-cron:0 5 0 * * *}")
    public void scheduledDaily() {
        if (!scheduleEnabled) {
            return;
        }
        LocalDate yesterday = LocalDate.now().minusDays(1);
        recalc(null, yesterday, yesterday);
    }

    @Transactional
    public int generateMonthly(String yearMonth, Long deptId) {
        YearMonth month = YearMonth.parse(yearMonth);
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        List<Long> employeeIds = employeeIds(deptId);
        int count = 0;
        for (Long employeeId : employeeIds) {
            recalc(employeeId, from, to);
            Map<String, Object> values = monthlyValues(employeeId, yearMonth, from, to);
            jdbc.update("DELETE FROM att_monthly_summary WHERE employee_id = ? AND year_month = ? "
                            + "AND status <> 'LOCKED'", employeeId, yearMonth);
            if (monthlyService.lambdaQuery().eq(
                    com.oa.attendance.entity.AttMonthlySummary::getEmployeeId, employeeId)
                    .eq(com.oa.attendance.entity.AttMonthlySummary::getYearMonth, yearMonth)
                    .one() == null) {
                jdbc.update("INSERT INTO att_monthly_summary "
                                + "(employee_id, year_month, should_days, actual_days, late_count, "
                                + "late_minutes, early_count, absent_days, leave_days, "
                                + "overtime_hours, trip_days, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'DRAFT')",
                        employeeId, yearMonth, values.get("shouldDays"), values.get("actualDays"),
                        values.get("lateCount"), values.get("lateMinutes"), values.get("earlyCount"),
                        values.get("absentDays"), values.get("leaveDays"), values.get("overtimeHours"),
                        values.get("tripDays"));
            }
            count++;
        }
        return count;
    }

    public void confirmMonthly(String yearMonth) {
        jdbc.update("UPDATE att_monthly_summary SET status = 'CONFIRMED' "
                + "WHERE year_month = ? AND status = 'DRAFT'", yearMonth);
    }

    public void lockMonthly(String yearMonth) {
        jdbc.update("UPDATE att_monthly_summary SET status = 'LOCKED' "
                + "WHERE year_month = ? AND status IN ('DRAFT', 'CONFIRMED')", yearMonth);
    }

    public List<AttDaily> mineDaily(Long userId, String yearMonth) {
        return dailyService.lambdaQuery()
                .eq(AttDaily::getEmployeeId, employeeId(userId))
                .like(AttDaily::getWorkDate, yearMonth)
                .orderByAsc(AttDaily::getWorkDate)
                .list();
    }

    public AttLeaveBalance balance(Long userId, Integer year, String type) {
        return balanceService.lambdaQuery()
                .eq(AttLeaveBalance::getEmployeeId, employeeId(userId))
                .eq(AttLeaveBalance::getYear, year)
                .eq(AttLeaveBalance::getLeaveType, type)
                .one();
    }

    public Long employeeIdForUser(Long userId) {
        return employeeId(userId);
    }

    @Transactional
    public void completed(Long instanceId, String status) {
        Map<String, Object> instance = jdbc.queryForMap(
                "SELECT business_type, business_id FROM wf_instance WHERE id = ?", instanceId);
        String type = String.valueOf(instance.get("BUSINESS_TYPE"));
        Long businessId = Long.valueOf(String.valueOf(instance.get("BUSINESS_ID")));
        if ("LEAVE".equals(type)) {
            AttLeaveRequest request = leaveService.getById(businessId);
            request.setStatus(status);
            leaveService.updateById(request);
            if ("APPROVED".equals(status) && "ANNUAL".equals(request.getLeaveType())) {
                jdbc.update("UPDATE att_leave_balance SET used_days = used_days + ? "
                                + "WHERE employee_id = ? AND year = ? AND leave_type = ?",
                        request.getDays(), request.getEmployeeId(), request.getStartTime().getYear(),
                        request.getLeaveType());
            }
        } else if ("OVERTIME".equals(type)) {
            AttOvertimeRequest request = overtimeService.getById(businessId);
            request.setStatus(status);
            overtimeService.updateById(request);
        } else if ("PATCH_CLOCK".equals(type)) {
            AttPatchRequest request = patchService.getById(businessId);
            request.setStatus(status);
            patchService.updateById(request);
            if ("APPROVED".equals(status)) {
                AttClockRecord record = new AttClockRecord();
                record.setEmployeeId(request.getEmployeeId());
                record.setClockTime(request.getClockTime());
                record.setClockType(request.getClockType());
                record.setSource("PATCH");
                record.setRemark(request.getReason());
                clockService.save(record);
            }
        } else if ("BUSINESS_TRIP".equals(type)) {
            AttTripRequest request = tripService.getById(businessId);
            request.setStatus(status);
            tripService.updateById(request);
        }
    }

    public BigDecimal workingDays(LocalDate from, LocalDate to) {
        BigDecimal result = BigDecimal.ZERO;
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            if (isWorkday(date)) {
                result = result.add(BigDecimal.ONE);
            }
        }
        return result;
    }

    private Map<String, Object> monthlyValues(Long employeeId, String yearMonth,
                                              LocalDate from, LocalDate to) {
        List<AttDaily> records = dailyService.lambdaQuery()
                .eq(AttDaily::getEmployeeId, employeeId)
                .ge(AttDaily::getWorkDate, from)
                .le(AttDaily::getWorkDate, to)
                .list();
        int lateCount = 0;
        int lateMinutes = 0;
        int earlyCount = 0;
        BigDecimal actual = BigDecimal.ZERO;
        BigDecimal absent = BigDecimal.ZERO;
        BigDecimal trip = BigDecimal.ZERO;
        for (AttDaily item : records) {
            lateCount += item.getLateMinutes() == null || item.getLateMinutes() == 0 ? 0 : 1;
            lateMinutes += item.getLateMinutes() == null ? 0 : item.getLateMinutes();
            earlyCount += item.getEarlyMinutes() == null || item.getEarlyMinutes() == 0 ? 0 : 1;
            if ("NORMAL".equals(item.getStatus()) || "LATE".equals(item.getStatus())
                    || "EARLY".equals(item.getStatus()) || "PATCHED".equals(item.getStatus())) {
                actual = actual.add(BigDecimal.ONE);
            }
            if ("ABSENT".equals(item.getStatus())) {
                absent = absent.add(BigDecimal.ONE);
            }
            if ("TRIP".equals(item.getStatus())) {
                trip = trip.add(BigDecimal.ONE);
            }
        }
        Map<String, Object> values = new HashMap<>();
        values.put("shouldDays", workingDays(from, to));
        values.put("actualDays", actual);
        values.put("lateCount", lateCount);
        values.put("lateMinutes", lateMinutes);
        values.put("earlyCount", earlyCount);
        values.put("absentDays", absent);
        values.put("leaveDays", "{}");
        values.put("overtimeHours", BigDecimal.ZERO);
        values.put("tripDays", trip);
        return values;
    }

    private List<Long> employeeIds(Long deptId) {
        if (deptId == null) {
            return jdbc.query("SELECT id FROM hr_employee WHERE employment_status NOT IN ('LEFT', 'LEAVING')",
                    (result, rowNum) -> result.getLong(1));
        }
        return jdbc.query("SELECT id FROM hr_employee WHERE dept_id = ? "
                        + "AND employment_status NOT IN ('LEFT', 'LEAVING')",
                new Object[]{deptId}, (result, rowNum) -> result.getLong(1));
    }

    private AttShift findShift(Long employeeId, LocalDate date) {
        Long shiftId = jdbc.query("SELECT shift_id FROM att_schedule WHERE employee_id = ? "
                        + "AND work_date = ?", new Object[]{employeeId, date},
                result -> result.next() ? result.getLong(1) : null);
        if (shiftId != null) {
            return jdbc.query("SELECT * FROM att_shift WHERE id = ?", new Object[]{shiftId},
                    result -> result.next() ? shift(result) : null);
        }
        if (!isWorkday(date)) {
            return null;
        }
        return jdbc.query("SELECT * FROM att_shift WHERE is_default = 1",
                result -> result.next() ? shift(result) : null);
    }

    private AttShift shift(java.sql.ResultSet result) throws java.sql.SQLException {
        AttShift shift = new AttShift();
        shift.setId(result.getLong("id"));
        shift.setWorkStart(result.getTime("work_start").toLocalTime());
        shift.setWorkEnd(result.getTime("work_end").toLocalTime());
        shift.setLateGraceMinutes(result.getInt("late_grace_minutes"));
        shift.setEarlyGraceMinutes(result.getInt("early_grace_minutes"));
        return shift;
    }

    private String holidayStatus(LocalDate date) {
        String type = jdbc.query("SELECT type FROM att_holiday WHERE holiday_date = ?",
                new Object[]{date}, result -> result.next() ? result.getString(1) : null);
        if ("HOLIDAY".equals(type)) {
            return "HOLIDAY";
        }
        if ("WORKDAY".equals(type)) {
            return "WORKDAY";
        }
        return date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY ? "REST" : "WORKDAY";
    }

    private boolean isWorkday(LocalDate date) {
        return !"REST".equals(holidayStatus(date)) && !"HOLIDAY".equals(holidayStatus(date));
    }

    private boolean approvedLeave(Long employeeId, LocalDate date) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM att_leave_request "
                        + "WHERE employee_id = ? AND status = 'APPROVED' "
                        + "AND start_time < ? AND end_time >= ?",
                Integer.class, employeeId, date.plusDays(1).atStartOfDay(), date.atStartOfDay());
        return count != null && count > 0;
    }

    private String leaveType(Long employeeId, LocalDate date) {
        return jdbc.query("SELECT leave_type FROM att_leave_request "
                        + "WHERE employee_id = ? AND status = 'APPROVED' "
                        + "AND start_time < ? AND end_time >= ?",
                new Object[]{employeeId, date.plusDays(1).atStartOfDay(), date.atStartOfDay()},
                result -> result.next() ? result.getString(1) : null);
    }

    private boolean approvedTrip(Long employeeId, LocalDate date) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM att_trip_request "
                        + "WHERE employee_id = ? AND status = 'APPROVED' "
                        + "AND start_date <= ? AND end_date >= ?",
                Integer.class, employeeId, date, date);
        return count != null && count > 0;
    }

    private void ensureBalance(Long employeeId, String type, BigDecimal days) {
        AttLeaveBalance balance = balanceService.lambdaQuery()
                .eq(AttLeaveBalance::getEmployeeId, employeeId)
                .eq(AttLeaveBalance::getYear, LocalDate.now().getYear())
                .eq(AttLeaveBalance::getLeaveType, type)
                .one();
        if (balance == null || balance.getTotalDays().subtract(balance.getUsedDays())
                .compareTo(days) < 0) {
            throw new BizException("年假余额不足");
        }
    }

    private Long employeeId(Long userId) {
        Long employeeId = jdbc.query("SELECT employee_id FROM sys_user WHERE id = ?",
                new Object[]{userId}, result -> result.next() ? result.getLong(1) : null);
        if (employeeId == null) {
            throw new BizException("当前用户未关联员工");
        }
        return employeeId;
    }

    private LocalDateTime first(List<AttClockRecord> records, String type) {
        for (AttClockRecord record : records) {
            if (type.equals(record.getClockType())) {
                return record.getClockTime();
            }
        }
        return null;
    }

    private LocalDateTime last(List<AttClockRecord> records, String type) {
        LocalDateTime result = null;
        for (AttClockRecord record : records) {
            if (type.equals(record.getClockType())) {
                result = record.getClockTime();
            }
        }
        return result;
    }

    private int lateMinutes(LocalDateTime actual, LocalDate date, AttShift shift) {
        if (actual == null) {
            return 0;
        }
        LocalDateTime expected = date.atTime(shift.getWorkStart())
                .plusMinutes(shift.getLateGraceMinutes() == null ? 0 : shift.getLateGraceMinutes());
        return actual.isAfter(expected) ? (int) ChronoUnit.MINUTES.between(expected, actual) : 0;
    }

    private int earlyMinutes(LocalDateTime actual, LocalDate date, AttShift shift) {
        if (actual == null) {
            return 0;
        }
        LocalDateTime expected = date.atTime(shift.getWorkEnd())
                .minusMinutes(shift.getEarlyGraceMinutes() == null ? 0 : shift.getEarlyGraceMinutes());
        return actual.isBefore(expected) ? (int) ChronoUnit.MINUTES.between(actual, expected) : 0;
    }

    private int workMinutes(LocalDateTime from, LocalDateTime to) {
        return from == null || to == null ? 0 : Math.max(0, (int) ChronoUnit.MINUTES.between(from, to));
    }

    private static Long number(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? null : ((Number) value).longValue();
    }

    private static String defaultValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
