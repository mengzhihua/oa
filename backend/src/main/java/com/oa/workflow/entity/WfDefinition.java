package com.oa.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

@Data
@TableName("wf_definition")
public class WfDefinition extends BaseEntity {
    private String code;
    private String name;
    private String formSchemaJson;
    private Integer status;
    private Integer version;
}
