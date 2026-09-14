package com.oa.system.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    private String password;
    private String realName;
    private Long employeeId;
    private Integer status;
}
