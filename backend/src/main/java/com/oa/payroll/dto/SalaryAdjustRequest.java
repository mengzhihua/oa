package com.oa.payroll.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SalaryAdjustRequest {
    @NotNull
    private LocalDate effectiveDate;
    @NotNull
    private BigDecimal baseSalary;
    @NotNull
    private BigDecimal postSalary;
    @NotNull
    private BigDecimal perfSalary;
    private String allowancesJson;
    private BigDecimal siBase;
    private BigDecimal hfBase;
    private String reason;
}
