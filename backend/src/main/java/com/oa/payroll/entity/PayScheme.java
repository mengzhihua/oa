package com.oa.payroll.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("pay_scheme")
public class PayScheme extends BaseEntity {
    private Long employeeId;
    private LocalDate effectiveDate;
    private BigDecimal baseSalary;
    private BigDecimal postSalary;
    private BigDecimal perfSalary;
    private String allowancesJson;
    private BigDecimal siBase;
    private BigDecimal hfBase;
    private String status;
}
