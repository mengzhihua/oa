package com.oa.hr.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class EmployeeRequest {
    @NotBlank(message = "员工姓名不能为空")
    private String name;
    private String gender;
    private String idCard;
    private String mobile;
    private String email;
    private Long deptId;
    private Long positionId;
    private Long gradeId;
    private String employmentStatus;
    private String employeeType;
    private String education;
    private String address;
    private String emergencyContact;
    private String remark;
}
