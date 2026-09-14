package com.oa.org.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PositionRequest {
    @NotBlank(message = "岗位编码不能为空")
    private String code;
    @NotBlank(message = "岗位名称不能为空")
    private String name;
    private Integer level;
    private Long deptId;
}
