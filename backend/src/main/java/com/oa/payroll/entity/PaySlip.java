package com.oa.payroll.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("pay_slip")
public class PaySlip extends BaseEntity {
    private Long periodId;
    private Long employeeId;
    private Long deptId;
    private String itemsJson;
    private BigDecimal gross;
    private BigDecimal taxableIncome;
    private BigDecimal cumulativeTaxable;
    private BigDecimal cumulativeTax;
    private BigDecimal tax;
    private BigDecimal siPersonal;
    private BigDecimal hfPersonal;
    private BigDecimal siCompany;
    private BigDecimal hfCompany;
    private BigDecimal net;
    private String status;
    private String remark;
    private LocalDateTime viewedAt;
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String yearMonth;
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private List<PaySlipItemView> itemDetails;
}
