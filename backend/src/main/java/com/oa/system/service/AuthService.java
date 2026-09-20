package com.oa.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oa.common.BizException;
import com.oa.system.auth.PasswordHasher;
import com.oa.system.auth.TokenService;
import com.oa.system.auth.UserAccessService;
import com.oa.system.dto.LoginRequest;
import com.oa.system.entity.SysMenu;
import com.oa.system.entity.SysRole;
import com.oa.system.entity.SysUser;
import com.oa.system.entity.SysUserRole;
import com.oa.system.mapper.SysMenuMapper;
import com.oa.system.mapper.SysRoleMapper;
import com.oa.system.mapper.SysRoleMenuMapper;
import com.oa.system.mapper.SysUserRoleMapper;
import com.oa.system.vo.LoginResponse;
import com.oa.system.vo.SysUserView;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final SysUserService userService;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;
    private final TokenService tokenService;
    private final UserAccessService userAccessService;

    public AuthService(SysUserService userService, SysRoleMapper roleMapper,
                       SysUserRoleMapper userRoleMapper, SysMenuMapper menuMapper,
                       SysRoleMenuMapper roleMenuMapper, TokenService tokenService,
                       UserAccessService userAccessService) {
        this.userService = userService;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.menuMapper = menuMapper;
        this.tokenService = tokenService;
        this.userAccessService = userAccessService;
    }

    public LoginResponse login(LoginRequest request) {
        SysUser user = userService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername())
                .eq(SysUser::getStatus, 1));
        if (user == null || !PasswordHasher.verify(request.getPassword(), user.getPasswordHash())) {
            throw new BizException("用户名或密码错误");
        }
        if (!userAccessService.isActive(user.getId())) {
            throw new BizException("账号已停用");
        }
        user.setLastLoginAt(LocalDateTime.now());
        userService.updateById(user);
        LoginResponse response = buildResponse(user);
        response.setToken(tokenService.issue(user.getId(), user.getUsername(), roleCodes(user.getId())));
        return response;
    }

    public LoginResponse current(Long userId) {
        SysUser user = userService.getById(userId);
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException("用户不存在或已停用");
        }
        return buildResponse(user);
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = userService.getById(userId);
        if (user == null || !PasswordHasher.verify(oldPassword, user.getPasswordHash())) {
            throw new BizException("原密码错误");
        }
        user.setPasswordHash(PasswordHasher.hash(newPassword));
        userService.updateById(user);
    }

    private LoginResponse buildResponse(SysUser user) {
        LoginResponse response = new LoginResponse();
        SysUserView view = new SysUserView();
        view.setId(user.getId());
        view.setUsername(user.getUsername());
        view.setRealName(user.getRealName());
        view.setEmployeeId(user.getEmployeeId());
        view.setStatus(user.getStatus());
        response.setUser(view);
        List<SysRole> roles = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, user.getId())).stream()
                .map(item -> roleMapper.selectById(item.getRoleId()))
                .collect(Collectors.toList());
        response.setRoles(roles);
        List<Long> roleIds = roles.stream().map(SysRole::getId).collect(Collectors.toList());
        List<Long> menuIds = roleIds.isEmpty() ? java.util.Collections.<Long>emptyList()
                : roleMenuMapper.selectList(new LambdaQueryWrapper<com.oa.system.entity.SysRoleMenu>()
                                .in(com.oa.system.entity.SysRoleMenu::getRoleId, roleIds))
                        .stream()
                        .map(com.oa.system.entity.SysRoleMenu::getMenuId)
                        .distinct()
                        .collect(Collectors.toList());
        boolean admin = roles.stream().anyMatch(role -> "ADMIN".equals(role.getCode()));
        response.setMenus(admin ? menuMapper.selectList(null) : menuIds.isEmpty()
                ? java.util.Collections.<SysMenu>emptyList()
                : menuMapper.selectBatchIds(menuIds));
        return response;
    }

    private Set<String> roleCodes(Long userId) {
        Set<String> result = new HashSet<>();
        for (SysUserRole relation : userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId))) {
            SysRole role = roleMapper.selectById(relation.getRoleId());
            if (role != null) {
                result.add(role.getCode());
            }
        }
        return result;
    }
}
