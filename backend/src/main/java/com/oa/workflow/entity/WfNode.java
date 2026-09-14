package com.oa.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

@Data
@TableName("wf_node")
public class WfNode extends BaseEntity {
    private Long definitionId;
    private Integer seq;
    private String name;
    private String approverType;
    private String approverRef;
    private String multiMode;
    private String conditionJson;
}
