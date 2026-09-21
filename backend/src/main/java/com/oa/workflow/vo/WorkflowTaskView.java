package com.oa.workflow.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkflowTaskView {
    private Long id;
    private Long instanceId;
    private String instanceNo;
    private String instanceTitle;
    private String businessId;
    private String businessType;
    private Integer nodeSeq;
    private String nodeName;
    private Long approverUserId;
    private String approverName;
    private String status;
    private String comment;
    private LocalDateTime handledAt;
}
