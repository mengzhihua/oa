package com.oa.system.auth;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.*;
@Component
public class AccessPolicy {
    private final JdbcTemplate jdbc;
    public AccessPolicy(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public boolean allowed(Long id,String method,String path){if("GET".equalsIgnoreCase(method)||path.startsWith("/api/auth/")||path.startsWith("/api/oauth/"))return true; List<String> roles=jdbc.query("SELECT r.code FROM sys_role r JOIN sys_user_role ur ON ur.role_id=r.id WHERE ur.user_id=?",new Object[]{id},(rs,n)->rs.getString(1)); if(roles.contains("ADMIN"))return true; if(path.startsWith("/api/hr/")||path.startsWith("/api/org/")||path.startsWith("/api/system/"))return roles.contains("HR"); if(path.startsWith("/api/oauth/clients"))return roles.contains("ADMIN"); if(path.startsWith("/api/workflow/"))return roles.contains("MANAGER")||roles.contains("HR")||roles.contains("FINANCE")||roles.contains("EMPLOYEE"); return true;}
}
