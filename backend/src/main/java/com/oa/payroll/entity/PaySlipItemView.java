package com.oa.payroll.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaySlipItemView {
    private String code;
    private String name;
    private BigDecimal amount;
}
