package com.oa.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wf_instance")
public class WfInstance extends BaseEntity {
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
}
