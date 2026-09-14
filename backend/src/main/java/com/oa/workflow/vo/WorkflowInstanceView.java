package com.oa.workflow.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkflowInstanceView {
    private Long id;
    private String instanceNo;
    private Long definitionId;
    private String title;
    private String formJson;
    private String businessType;
    private String businessId;
    private Long applicantId;
    private String status;
    private Integer currentNodeSeq;
    private LocalDateTime submittedAt;
    private LocalDateTime finishedAt;
    private List<WorkflowTaskView> tasks;
}
