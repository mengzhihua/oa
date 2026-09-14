package com.oa.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

@Data
@TableName("sys_role")
public class SysRole extends BaseEntity {
    private String code;
    private String name;
}
