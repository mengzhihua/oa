package com.oa.payroll.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("pay_salary_change")
public class PaySalaryChange extends BaseEntity {
    private Long employeeId;
    private LocalDate effectiveDate;
    private String beforeJson;
    private String afterJson;
    private String reason;
    private Long operator;
}
