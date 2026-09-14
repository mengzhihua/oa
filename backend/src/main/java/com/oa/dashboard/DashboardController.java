package com.oa.dashboard;

import com.oa.attendance.service.AttendanceService;
import com.oa.collab.service.CollabService;
import com.oa.common.R;
import com.oa.system.auth.CurrentUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final JdbcTemplate jdbc;
    private final AttendanceService attendanceService;
    private final CollabService collabService;

    public DashboardController(JdbcTemplate jdbc,
                               AttendanceService attendanceService,
                               CollabService collabService) {
        this.jdbc = jdbc;
        this.attendanceService = attendanceService;
        this.collabService = collabService;
    }

    @GetMapping
    public R<DashboardView> dashboard() {
        DashboardView view = new DashboardView();
        Long userId = CurrentUser.id();
        view.setPendingTasks(count("SELECT COUNT(*) FROM wf_task WHERE approver_user_id = ? "
                + "AND status = 'PENDING'", userId));
        view.setRunningApplications(count("SELECT COUNT(*) FROM wf_instance WHERE applicant_id = ? "
                + "AND status = 'RUNNING'", userId));
        view.setUnreadMessages(collabService.unreadCount(userId));
        view.setTodayClock(attendanceService.today(userId));
        view.setMonthLateCount(count("SELECT COUNT(*) FROM att_daily d JOIN sys_user u "
                + "ON u.employee_id = d.employee_id WHERE u.id = ? "
                + "AND d.work_date >= ?", userId, YearMonth.now().atDay(1)));
        view.setMonthLeaveDays(jdbc.queryForObject(
                "SELECT COALESCE(SUM(days), 0) FROM att_leave_request WHERE employee_id = "
                        + "(SELECT employee_id FROM sys_user WHERE id = ?) "
                        + "AND status = 'APPROVED' AND start_time >= ?",
                BigDecimal.class, userId, YearMonth.now().atDay(1).atStartOfDay()));
        view.setLatestNotices(collabService.mineNotices(userId).stream().limit(5)
                .collect(java.util.stream.Collectors.toList()));
        view.setTodaySchedules(Collections.emptyList());
        if (CurrentUser.roles().contains("HR") || CurrentUser.roles().contains("ADMIN")) {
            fillHr(view);
        }
        if (CurrentUser.roles().contains("FINANCE")) {
            fillFinance(view);
        }
        return R.ok(view);
    }

    private void fillHr(DashboardView view) {
        view.setActiveEmployees(jdbc.queryForObject(
                "SELECT COUNT(*) FROM hr_employee WHERE employment_status IN ('PROBATION', 'REGULAR')",
                Long.class));
        view.setMonthHires(count("SELECT COUNT(*) FROM hr_employee WHERE hire_date >= ?",
                YearMonth.now().atDay(1)));
        view.setMonthLeaves(count("SELECT COUNT(*) FROM hr_employee WHERE leave_date >= ?",
                YearMonth.now().atDay(1)));
        view.setAbnormalClocks(count("SELECT COUNT(*) FROM att_daily WHERE work_date = ? "
                + "AND status IN ('LATE', 'EARLY', 'ABSENT')", LocalDate.now()));
        Long should = jdbc.queryForObject("SELECT COUNT(*) FROM att_daily WHERE work_date = ?",
                Long.class, LocalDate.now());
        Long actual = jdbc.queryForObject("SELECT COUNT(*) FROM att_daily WHERE work_date = ? "
                + "AND status IN ('NORMAL', 'LATE', 'EARLY', 'PATCHED')",
                Long.class, LocalDate.now());
        view.setAttendanceRate(should == null || should == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(actual == null ? 0 : actual)
                .divide(BigDecimal.valueOf(should), 4, java.math.RoundingMode.HALF_UP));
    }

    private void fillFinance(DashboardView view) {
        view.setPayrollStatus(jdbc.queryForObject(
                "SELECT status FROM pay_period ORDER BY year_month DESC LIMIT 1", String.class));
        view.setPayrollCost(jdbc.queryForObject(
                "SELECT COALESCE(total_gross, 0) + COALESCE("
                        + "(SELECT SUM(si_company + hf_company) FROM pay_slip "
                        + "WHERE period_id = p.id), 0) FROM pay_period p "
                        + "ORDER BY year_month DESC LIMIT 1", BigDecimal.class));
    }

    private long count(String sql, Object... args) {
        Long value = jdbc.queryForObject(sql, Long.class, args);
        return value == null ? 0 : value;
    }
}
