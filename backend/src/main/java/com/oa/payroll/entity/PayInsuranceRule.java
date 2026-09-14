package com.oa.payroll.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("pay_insurance_rule")
public class PayInsuranceRule extends BaseEntity {
    private String city;
    private BigDecimal pensionP;
    private BigDecimal pensionC;
    private BigDecimal medicalP;
    private BigDecimal medicalC;
    private BigDecimal unemploymentP;
    private BigDecimal unemploymentC;
    private BigDecimal injuryC;
    private BigDecimal maternityC;
    private BigDecimal hfP;
    private BigDecimal hfC;
    private BigDecimal siBaseMin;
    private BigDecimal siBaseMax;
    private BigDecimal hfBaseMin;
    private BigDecimal hfBaseMax;
}
