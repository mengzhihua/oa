package com.oa.attendance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.attendance.dto.ClockRequest;
import com.oa.attendance.dto.LeaveRequest;
import com.oa.attendance.dto.OvertimeRequest;
import com.oa.attendance.dto.PatchRequest;
import com.oa.attendance.dto.ScheduleBatchRequest;
import com.oa.attendance.dto.TripRequest;
import com.oa.attendance.entity.AttDaily;
import com.oa.attendance.entity.AttLeaveBalance;
import com.oa.attendance.entity.AttLeaveRequest;
import com.oa.attendance.entity.AttMonthlySummary;
import com.oa.attendance.entity.AttOvertimeRequest;
import com.oa.attendance.entity.AttPatchRequest;
import com.oa.attendance.entity.AttTripRequest;
import com.oa.attendance.service.AttDailyService;
import com.oa.attendance.service.AttLeaveBalanceService;
import com.oa.attendance.service.AttLeaveRequestService;
import com.oa.attendance.service.AttMonthlySummaryService;
import com.oa.attendance.service.AttOvertimeRequestService;
import com.oa.attendance.service.AttPatchRequestService;
import com.oa.attendance.service.AttTripRequestService;
import com.oa.attendance.service.AttendanceService;
import com.oa.attendance.vo.ClockTodayView;
import com.oa.common.PageResult;
import com.oa.common.R;
import com.oa.system.auth.CurrentUser;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final AttDailyService dailyService;
    private final AttLeaveRequestService leaveService;
    private final AttOvertimeRequestService overtimeService;
    private final AttPatchRequestService patchService;
    private final AttTripRequestService tripService;
    private final AttLeaveBalanceService balanceService;
    private final AttMonthlySummaryService monthlyService;

    public AttendanceController(AttendanceService attendanceService,
                                AttDailyService dailyService,
                                AttLeaveRequestService leaveService,
                                AttOvertimeRequestService overtimeService,
                                AttPatchRequestService patchService,
                                AttTripRequestService tripService,
                                AttLeaveBalanceService balanceService,
                                AttMonthlySummaryService monthlyService) {
        this.attendanceService = attendanceService;
        this.dailyService = dailyService;
        this.leaveService = leaveService;
        this.overtimeService = overtimeService;
        this.patchService = patchService;
        this.tripService = tripService;
        this.balanceService = balanceService;
        this.monthlyService = monthlyService;
    }

    @PostMapping("/clock")
    public R<?> clock(@Valid @RequestBody ClockRequest request, HttpServletRequest servletRequest) {
        return R.ok(attendanceService.clock(CurrentUser.id(), request, servletRequest));
    }

    @GetMapping("/clock/today")
    public R<ClockTodayView> today() {
        return R.ok(attendanceService.today(CurrentUser.id()));
    }

    @PostMapping("/leaves")
    public R<AttLeaveRequest> leave(@Valid @RequestBody LeaveRequest request) {
        return R.ok(attendanceService.submitLeave(CurrentUser.id(), request));
    }

    @GetMapping("/leaves/mine")
    public R<List<AttLeaveRequest>> myLeaves() {
        return R.ok(leaveService.lambdaQuery()
                .eq(AttLeaveRequest::getEmployeeId, employeeId())
                .orderByDesc(AttLeaveRequest::getId)
                .list());
    }

    @GetMapping("/overtimes/mine")
    public R<List<AttOvertimeRequest>> myOvertimes() {
        return R.ok(overtimeService.lambdaQuery()
                .eq(AttOvertimeRequest::getEmployeeId, employeeId())
                .orderByDesc(AttOvertimeRequest::getId)
                .list());
    }

    @GetMapping("/patches/mine")
    public R<List<AttPatchRequest>> myPatches() {
        return R.ok(patchService.lambdaQuery()
                .eq(AttPatchRequest::getEmployeeId, employeeId())
                .orderByDesc(AttPatchRequest::getId)
                .list());
    }

    @GetMapping("/trips/mine")
    public R<List<AttTripRequest>> myTrips() {
        return R.ok(tripService.lambdaQuery()
                .eq(AttTripRequest::getEmployeeId, employeeId())
                .orderByDesc(AttTripRequest::getId)
                .list());
    }

    @PostMapping("/overtimes")
    public R<AttOvertimeRequest> overtime(@Valid @RequestBody OvertimeRequest request) {
        return R.ok(attendanceService.submitOvertime(CurrentUser.id(), request));
    }

    @PostMapping("/patches")
    public R<AttPatchRequest> patch(@Valid @RequestBody PatchRequest request) {
        return R.ok(attendanceService.submitPatch(CurrentUser.id(), request));
    }

    @PostMapping("/trips")
    public R<AttTripRequest> trip(@Valid @RequestBody TripRequest request) {
        return R.ok(attendanceService.submitTrip(CurrentUser.id(), request));
    }

    @PostMapping("/requests/{type}/{id}/cancel")
    public R<Void> cancel(@PathVariable String type, @PathVariable Long id) {
        attendanceService.cancel(CurrentUser.id(), type, id);
        return R.ok();
    }

    @GetMapping("/daily/mine")
    public R<List<AttDaily>> mineDaily(@RequestParam String yearMonth) {
        return R.ok(attendanceService.mineDaily(CurrentUser.id(), yearMonth));
    }

    @PostMapping("/daily/recalc")
    public R<Integer> recalc(@RequestParam(required = false) Long deptId,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                             LocalDate from,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                             LocalDate to) {
        return R.ok(attendanceService.recalc(deptId, from, to));
    }

    @PostMapping("/schedules/batch")
    public R<Integer> batchSchedule(@Valid @RequestBody ScheduleBatchRequest request) {
        return R.ok(attendanceService.batchSchedule(request));
    }

    @GetMapping("/schedules")
    public R<List<com.oa.attendance.entity.AttSchedule>> schedules(
            @RequestParam(required = false) Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return R.ok(attendanceService.schedules(employeeId, from, to));
    }

    @GetMapping("/daily/department")
    public R<List<AttDaily>> departmentDaily(
            @RequestParam(required = false) Long deptId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return R.ok(attendanceService.departmentDaily(deptId, date));
    }

    @GetMapping("/daily/abnormal")
    public R<List<AttDaily>> abnormalities(@RequestParam(required = false) Long deptId,
                                           @RequestParam(required = false)
                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                           LocalDate from,
                                           @RequestParam(required = false)
                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                           LocalDate to) {
        return R.ok(attendanceService.abnormalities(deptId, from, to));
    }

    @GetMapping("/daily")
    public R<PageResult<AttDaily>> daily(@RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "20") long size,
                                         @RequestParam(required = false) Long employeeId,
                                         @RequestParam(required = false) String status) {
        Page<AttDaily> result = dailyService.page(new Page<>(page, size),
                new LambdaQueryWrapper<AttDaily>()
                        .eq(employeeId != null, AttDaily::getEmployeeId, employeeId)
                        .eq(status != null, AttDaily::getStatus, status)
                        .orderByDesc(AttDaily::getWorkDate));
        return R.ok(new PageResult<>(result.getTotal(), page, size, result.getRecords()));
    }

    @GetMapping("/balances/mine")
    public R<AttLeaveBalance> balance(@RequestParam(defaultValue = "ANNUAL") String type) {
        return R.ok(attendanceService.balance(CurrentUser.id(), LocalDate.now().getYear(), type));
    }

    @PostMapping("/monthly/generate")
    public R<Integer> generate(@RequestParam String yearMonth,
                               @RequestParam(required = false) Long deptId) {
        return R.ok(attendanceService.generateMonthly(yearMonth, deptId));
    }

    @PostMapping("/monthly/{yearMonth}/confirm")
    public R<Void> confirm(@PathVariable String yearMonth) {
        attendanceService.confirmMonthly(yearMonth);
        return R.ok();
    }

    @PostMapping("/monthly/{yearMonth}/lock")
    public R<Void> lock(@PathVariable String yearMonth) {
        attendanceService.lockMonthly(yearMonth);
        return R.ok();
    }

    @GetMapping("/monthly")
    public R<PageResult<AttMonthlySummary>> monthly(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) Long employeeId) {
        Page<AttMonthlySummary> result = monthlyService.page(new Page<>(page, size),
                new LambdaQueryWrapper<AttMonthlySummary>()
                        .eq(yearMonth != null, AttMonthlySummary::getYearMonth, yearMonth)
                        .eq(employeeId != null, AttMonthlySummary::getEmployeeId, employeeId)
                        .orderByDesc(AttMonthlySummary::getYearMonth));
        return R.ok(new PageResult<>(result.getTotal(), page, size, result.getRecords()));
    }

    @GetMapping("/monthly/{yearMonth}/export")
    public void exportMonthly(@PathVariable String yearMonth,
                               HttpServletResponse response) throws Exception {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=attendance-" + yearMonth + ".csv");
        response.getWriter().write(attendanceService.monthlyCsv(yearMonth));
    }

    private Long employeeId() {
        return attendanceService.employeeIdForUser(CurrentUser.id());
    }
}
