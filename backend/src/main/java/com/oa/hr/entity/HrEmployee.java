package com.oa.hr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("hr_employee")
public class HrEmployee extends BaseEntity {
    private String employeeNo;
    private String name;
    private String gender;
    private String idCard;
    private LocalDate birthday;
    private String mobile;
    private String email;
    private Long deptId;
    private Long positionId;
    private Long gradeId;
    private LocalDate hireDate;
    private LocalDate regularDate;
    private String employmentStatus;
    private String employeeType;
    private String education;
    private String address;
    private String emergencyContact;
    private String bankName;
    private String bankAccount;
    private BigDecimal socialSecurityBase;
    private BigDecimal housingFundBase;
    private LocalDate leaveDate;
    private String remark;
}
