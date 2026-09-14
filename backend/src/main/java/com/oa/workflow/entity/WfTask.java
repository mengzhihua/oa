package com.oa.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wf_task")
public class WfTask extends BaseEntity {
    private Long instanceId;
    private Integer nodeSeq;
    private Long approverUserId;
    private String status;
    private String comment;
    private LocalDateTime handledAt;
}
