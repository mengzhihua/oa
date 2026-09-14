package com.oa.org.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

@Data
@TableName("org_dept")
public class OrgDept extends BaseEntity {
    private Long parentId;
    private String name;
    private String code;
    private Long leaderEmployeeId;
    private Integer sort;
    private Integer status;
    private String path;
}
