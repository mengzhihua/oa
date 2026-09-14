package com.oa.collab.vo;

import lombok.Data;

@Data
public class ContactView {
    private Long employeeId;
    private String employeeNo;
    private String name;
    private Long deptId;
    private String deptName;
    private String position;
    private String mobile;
    private String email;
}
