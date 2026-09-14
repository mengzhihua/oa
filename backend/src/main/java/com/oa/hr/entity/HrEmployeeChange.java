package com.oa.hr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("hr_employee_change")
public class HrEmployeeChange extends BaseEntity {
    private Long employeeId;
    private String changeType;
    private String beforeJson;
    private String afterJson;
    private LocalDate effectiveDate;
    private String reason;
    private Long operator;
}
