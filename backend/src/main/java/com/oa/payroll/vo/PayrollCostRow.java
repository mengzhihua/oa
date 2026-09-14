package com.oa.payroll.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayrollCostRow {
    private String yearMonth;
    private Long deptId;
    private BigDecimal gross;
    private BigDecimal companyInsurance;
    private BigDecimal totalCost;
}
