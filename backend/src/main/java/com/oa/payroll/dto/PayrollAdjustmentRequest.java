package com.oa.payroll.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class PayrollAdjustmentRequest {
    @NotBlank
    private String itemCode;
    @NotNull
    private BigDecimal amount;
    private String reason;
}
