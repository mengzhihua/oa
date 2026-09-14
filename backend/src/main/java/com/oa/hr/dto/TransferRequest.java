package com.oa.hr.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class TransferRequest {
    @NotNull(message = "目标部门不能为空")
    private Long deptId;
    private Long positionId;
    private String reason;
}
