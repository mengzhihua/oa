package com.oa.workflow.dto;

import lombok.Data;

@Data
public class TaskActionRequest {
    private String comment;
    private Long toUserId;
}
