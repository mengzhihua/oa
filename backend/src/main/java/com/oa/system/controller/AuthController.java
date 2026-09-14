package com.oa.system.controller;
import com.oa.common.*;
import com.oa.system.auth.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JdbcTemplate jdbc; private final TokenService tokens;
    public AuthController(JdbcTemplate j,TokenService t){jdbc=j;tokens=t;}
    public static class Login { @javax.validation.constraints.NotBlank public String username; @javax.validation.constraints.NotBlank public String password; }
    @PostMapping("/login") public R<Map<String,Object>> login(@Valid @RequestBody Login in){
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT * FROM sys_user WHERE username=? AND status=1",in.username); if(rows.isEmpty()||!PasswordHasher.verify(in.password,String.valueOf(rows.get(0).get("PASSWORD_HASH"))))throw new BizException("用户名或密码错误");
        Map<String,Object> u=rows.get(0); Long id=((Number)u.get("ID")).longValue(); jdbc.update("UPDATE sys_user SET last_login_at=CURRENT_TIMESTAMP WHERE id=?",id); Map<String,Object> out=new HashMap<String,Object>();out.put("token",tokens.issue(id,in.username));out.put("user",me(id));return R.ok(out);
    }
    @GetMapping("/me") public R<Map<String,Object>> me(){return R.ok(me(CurrentUser.id()));}
    @PutMapping("/password") public R<Void> password(@RequestBody Map<String,String> in){Map<String,Object> u=jdbc.queryForMap("SELECT password_hash FROM sys_user WHERE id=?",CurrentUser.id());if(!PasswordHasher.verify(in.get("oldPassword"),String.valueOf(u.get("PASSWORD_HASH"))))throw new BizException("原密码错误");jdbc.update("UPDATE sys_user SET password_hash=? WHERE id=?",PasswordHasher.hash(in.get("newPassword")),CurrentUser.id());return R.ok();}
    @PostMapping("/logout") public R<Void> logout(){return R.ok();}
    private Map<String,Object> me(Long id){Map<String,Object> out=jdbc.queryForMap("SELECT id,username,real_name,employee_id,status FROM sys_user WHERE id=?",id);out.put("roles",jdbc.queryForList("SELECT r.code,r.name FROM sys_role r JOIN sys_user_role ur ON ur.role_id=r.id WHERE ur.user_id=?",id));out.put("menus",jdbc.queryForList("SELECT DISTINCT m.* FROM sys_menu m JOIN sys_role_menu rm ON rm.menu_id=m.id JOIN sys_user_role ur ON ur.role_id=rm.role_id WHERE ur.user_id=? ORDER BY m.sort",id));return out;}
}
