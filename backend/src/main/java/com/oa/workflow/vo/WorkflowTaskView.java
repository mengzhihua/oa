package com.oa.workflow.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkflowTaskView {
    private Long id;
    private Long instanceId;
    private Integer nodeSeq;
    private Long approverUserId;
    private String status;
    private String comment;
    private LocalDateTime handledAt;
}
