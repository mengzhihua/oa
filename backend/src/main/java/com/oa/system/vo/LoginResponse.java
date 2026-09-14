package com.oa.system.vo;

import com.oa.system.entity.SysMenu;
import com.oa.system.entity.SysRole;
import lombok.Data;

import java.util.List;

@Data
public class LoginResponse {
    private String token;
    private SysUserView user;
    private List<SysRole> roles;
    private List<SysMenu> menus;
}
