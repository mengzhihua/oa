package com.oa.hr.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeRow {
    private Long id;
    private String employeeNo;
    private String name;
    private String gender;
    private String mobile;
    private String email;
    private Long deptId;
    private String deptName;
    private String employmentStatus;
    private LocalDate hireDate;
}
