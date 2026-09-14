package com.oa.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

@Data
@TableName("sys_role_menu")
public class SysRoleMenu extends BaseEntity {
    private Long roleId;
    private Long menuId;
}
