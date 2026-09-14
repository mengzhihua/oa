package com.oa.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.common.PageResult;
import com.oa.common.R;
import com.oa.system.auth.PasswordHasher;
import com.oa.system.dto.MenuAssignRequest;
import com.oa.system.dto.RoleAssignRequest;
import com.oa.system.dto.RoleRequest;
import com.oa.system.dto.UserRequest;
import com.oa.system.entity.SysDict;
import com.oa.system.entity.SysMenu;
import com.oa.system.entity.SysOpLog;
import com.oa.system.entity.SysRole;
import com.oa.system.entity.SysRoleMenu;
import com.oa.system.entity.SysUser;
import com.oa.system.entity.SysUserRole;
import com.oa.system.service.SysDictService;
import com.oa.system.service.SysMenuService;
import com.oa.system.service.SysOpLogService;
import com.oa.system.service.SysRoleMenuService;
import com.oa.system.service.SysRoleService;
import com.oa.system.service.SysUserRoleService;
import com.oa.system.service.SysUserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/api/system")
public class SystemController {
    private final SysUserService userService;
    private final SysRoleService roleService;
    private final SysMenuService menuService;
    private final SysDictService dictService;
    private final SysOpLogService opLogService;
    private final SysUserRoleService userRoleService;
    private final SysRoleMenuService roleMenuService;

    public SystemController(SysUserService userService, SysRoleService roleService,
                             SysMenuService menuService, SysDictService dictService,
                             SysUserRoleService userRoleService,
                             SysRoleMenuService roleMenuService,
                             SysOpLogService opLogService) {
        this.userService = userService;
        this.roleService = roleService;
        this.menuService = menuService;
        this.dictService = dictService;
        this.userRoleService = userRoleService;
        this.roleMenuService = roleMenuService;
        this.opLogService = opLogService;
    }

    @GetMapping("/users")
    public R<PageResult<SysUser>> users(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword) {
        Page<SysUser> result = userService.page(new Page<>(page, size),
                new LambdaQueryWrapper<SysUser>()
                        .like(keyword != null, SysUser::getUsername, keyword)
                        .or(keyword != null)
                        .like(keyword != null, SysUser::getRealName, keyword)
                        .orderByDesc(SysUser::getId));
        return R.ok(new PageResult<>(result.getTotal(), page, size, result.getRecords()));
    }

    @PostMapping("/users")
    public R<SysUser> createUser(@Valid @RequestBody UserRequest request) {
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPasswordHash(PasswordHasher.hash(request.getPassword() == null
                ? "123456" : request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmployeeId(request.getEmployeeId());
        user.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        userService.save(user);
        return R.ok(user);
    }

    @PutMapping("/users/{id}")
    public R<SysUser> updateUser(@PathVariable Long id,
                                 @Valid @RequestBody UserRequest request) {
        SysUser user = userService.getById(id);
        user.setRealName(request.getRealName());
        user.setEmployeeId(request.getEmployeeId());
        user.setStatus(request.getStatus());
        userService.updateById(user);
        return R.ok(user);
    }

    @DeleteMapping("/users/{id}")
    public R<Void> disableUser(@PathVariable Long id) {
        SysUser user = userService.getById(id);
        user.setStatus(0);
        userService.updateById(user);
        return R.ok();
    }

    @PostMapping("/users/{id}/reset-password")
    public R<Void> resetPassword(@PathVariable Long id,
                                 @RequestBody(required = false) UserRequest request) {
        SysUser user = userService.getById(id);
        String password = request == null || request.getPassword() == null
                ? "123456" : request.getPassword();
        user.setPasswordHash(PasswordHasher.hash(password));
        userService.updateById(user);
        return R.ok();
    }

    @PostMapping("/users/{id}/roles")
    public R<Void> assignRoles(@PathVariable Long id,
                               @Valid @RequestBody RoleAssignRequest request) {
        userRoleService.remove(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, id));
        List<SysUserRole> relations = request.getRoleIds().stream().map(roleId -> {
            SysUserRole relation = new SysUserRole();
            relation.setUserId(id);
            relation.setRoleId(roleId);
            return relation;
        }).collect(Collectors.toList());
        userRoleService.saveBatch(relations);
        return R.ok();
    }

    @GetMapping("/roles")
    public R<List<SysRole>> roles() {
        return R.ok(roleService.list(new LambdaQueryWrapper<SysRole>()
                .orderByAsc(SysRole::getId)));
    }

    @PostMapping("/roles")
    public R<SysRole> createRole(@Valid @RequestBody RoleRequest request) {
        SysRole role = new SysRole();
        role.setCode(request.getCode());
        role.setName(request.getName());
        roleService.save(role);
        return R.ok(role);
    }

    @PutMapping("/roles/{id}")
    public R<SysRole> updateRole(@PathVariable Long id,
                                 @Valid @RequestBody RoleRequest request) {
        SysRole role = roleService.getById(id);
        role.setCode(request.getCode());
        role.setName(request.getName());
        roleService.updateById(role);
        return R.ok(role);
    }

    @DeleteMapping("/roles/{id}")
    public R<Void> deleteRole(@PathVariable Long id) {
        roleService.removeById(id);
        return R.ok();
    }

    @PostMapping("/roles/{id}/menus")
    public R<Void> assignMenus(@PathVariable Long id,
                               @Valid @RequestBody MenuAssignRequest request) {
        roleMenuService.remove(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, id));
        List<SysRoleMenu> relations = request.getMenuIds().stream().map(menuId -> {
            SysRoleMenu relation = new SysRoleMenu();
            relation.setRoleId(id);
            relation.setMenuId(menuId);
            return relation;
        }).collect(Collectors.toList());
        roleMenuService.saveBatch(relations);
        return R.ok();
    }

    @GetMapping("/menus")
    public R<List<SysMenu>> menus() {
        return R.ok(menuService.list(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getParentId).orderByAsc(SysMenu::getSort)));
    }

    @GetMapping("/dicts")
    public R<List<SysDict>> dicts(@RequestParam(required = false) String type) {
        return R.ok(dictService.list(new LambdaQueryWrapper<SysDict>()
                .eq(type != null, SysDict::getType, type)
                .orderByAsc(SysDict::getSort)));
    }

    @GetMapping("/oplogs")
    public R<PageResult<SysOpLog>> opLogs(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        Page<SysOpLog> result = opLogService.page(new Page<>(page, size),
                new LambdaQueryWrapper<SysOpLog>().orderByDesc(SysOpLog::getId));
        return R.ok(new PageResult<>(result.getTotal(), page, size, result.getRecords()));
    }
}
