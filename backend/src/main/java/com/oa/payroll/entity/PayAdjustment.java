package com.oa.payroll.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("pay_adjustment")
public class PayAdjustment extends BaseEntity {
    private Long periodId;
    private Long employeeId;
    private String itemCode;
    private BigDecimal amount;
    private String reason;
}
