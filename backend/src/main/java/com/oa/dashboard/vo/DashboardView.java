package com.oa.dashboard.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
    private Map<String, Long> departmentCounts;
    private String payrollStatus;
    private BigDecimal payrollCost;
}
