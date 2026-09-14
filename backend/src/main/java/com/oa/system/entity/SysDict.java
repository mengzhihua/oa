package com.oa.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

@Data
@TableName("sys_dict")
public class SysDict extends BaseEntity {
    private String type;
    private String code;
    private String label;
    private Integer sort;
}
