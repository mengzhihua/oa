package com.oa.system.vo;

import lombok.Data;

@Data
public class SysUserView {
    private Long id;
    private String username;
    private String realName;
    private Long employeeId;
    private Integer status;
}
