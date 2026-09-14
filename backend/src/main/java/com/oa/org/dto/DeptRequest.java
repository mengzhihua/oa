package com.oa.org.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class DeptRequest {
    private Long parentId;

    @NotBlank(message = "部门名称不能为空")
    private String name;

    @NotBlank(message = "部门编码不能为空")
    private String code;
    private Long leaderEmployeeId;
    private Integer sort;
    private Integer status;
    private String path;
}
