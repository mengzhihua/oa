package com.oa.dashboard;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardView {
    private long pendingTasks;
    private long runningApplications;
    private long unreadMessages;
    private Object todayClock;
    private long monthLateCount;
    private BigDecimal monthLeaveDays;
    private List<?> latestNotices;
    private List<?> todaySchedules;
    private Long activeEmployees;
    private Long monthHires;
    private Long monthLeaves;
    private Long probationDue;
    private Long contractsDue;
    private BigDecimal attendanceRate;
    private Long abnormalClocks;
    private String payrollStatus;
    private BigDecimal payrollCost;
}
