package com.oa.payroll.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pay_period")
public class PayPeriod extends BaseEntity {
    private String yearMonth;
    private String status;
    private Integer attLocked;
    private LocalDateTime calcAt;
    private Long approvedBy;
    private LocalDateTime paidAt;
    private BigDecimal totalGross;
    private BigDecimal totalNet;
    private Integer headcount;
}
