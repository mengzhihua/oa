package com.oa.workflow.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Map;

@Data
public class StartWorkflowRequest {
    @NotBlank(message = "流程编码不能为空")
    private String definitionCode;
    @NotBlank(message = "标题不能为空")
    private String title;
    private Map<String, Object> form;
    private String businessType;
    private String businessId;
}
