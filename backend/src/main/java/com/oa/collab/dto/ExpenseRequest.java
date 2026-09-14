package com.oa.collab.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ExpenseRequest {
    @NotBlank
    private String title;
    private String itemsJson;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal total;
    private String attachments;
}
