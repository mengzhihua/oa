package com.oa.org.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class GradeRequest {
    @NotBlank(message = "职级编码不能为空")
    private String code;
    @NotBlank(message = "职级名称不能为空")
    private String name;
    private Integer level;
}
