package com.oa.org.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

@Data
@TableName("org_job_grade")
public class OrgJobGrade extends BaseEntity {
    private String code;
    private String name;
    private Integer level;
}
