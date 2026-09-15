package com.oa.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

@Data
@TableName("sys_menu")
public class SysMenu extends BaseEntity {
    private String code;
    private String name;
    private String path;
    private Long parentId;
    private Integer sort;
    private String icon;
    private String type;
    private Integer status;
}
