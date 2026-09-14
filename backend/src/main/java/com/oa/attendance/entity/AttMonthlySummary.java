package com.oa.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("att_monthly_summary")
public class AttMonthlySummary extends BaseEntity {
    private Long employeeId;
    private String yearMonth;
    private BigDecimal shouldDays;
    private BigDecimal actualDays;
    private Integer lateCount;
    private Integer lateMinutes;
    private Integer earlyCount;
    private BigDecimal absentDays;
    private String leaveDays;
    private BigDecimal overtimeHours;
    private BigDecimal tripDays;
    private String status;
}
