package com.oa.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

@Data
@TableName("wf_comment")
public class WfComment extends BaseEntity {
    private Long instanceId;
    private Long userId;
    private String action;
    private String comment;
}
