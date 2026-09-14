package com.oa.system.service;

import com.oa.system.auth.PasswordHasher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.*;

@Component
public class StartupService {
    private final JdbcTemplate jdbc;
    private final String adminPassword;
    public StartupService(JdbcTemplate jdbc,@Value("${oa.auth.admin-password:admin123}") String p){this.jdbc=jdbc;adminPassword=p;}
    @PostConstruct
    public void init() {
        String[][] users={{"admin","系统管理员","","ADMIN"},{"hr","王人事","3","HR"},{"finance","赵财务","4","FINANCE"},{"manager","李主管","2","MANAGER"},{"zhangsan","张三","1","EMPLOYEE"}};
        for(String[] u:users){String password="admin".equals(u[0])?adminPassword:("hr".equals(u[0])?"hr123":("finance".equals(u[0])?"fin123":("manager".equals(u[0])?"mgr123":"emp123"))); Integer n=jdbc.queryForObject("SELECT COUNT(*) FROM sys_user WHERE username=?",Integer.class,u[0]); if(n==0) jdbc.update("INSERT INTO sys_user(username,password_hash,real_name,employee_id,status) VALUES(?,?,?,?,1)",u[0],PasswordHasher.hash(password),u[1],u[2].isEmpty()?null:Long.valueOf(u[2])); else jdbc.update("UPDATE sys_user SET password_hash=?,real_name=?,employee_id=?,status=1 WHERE username=?",PasswordHasher.hash(password),u[1],u[2].isEmpty()?null:Long.valueOf(u[2]),u[0]); Long uid=jdbc.queryForObject("SELECT id FROM sys_user WHERE username=?",Long.class,u[0]); Long rid=jdbc.queryForObject("SELECT id FROM sys_role WHERE code=?",Long.class,u[3]); if(jdbc.queryForObject("SELECT COUNT(*) FROM sys_user_role WHERE user_id=? AND role_id=?",Integer.class,uid,rid)==0)jdbc.update("INSERT INTO sys_user_role(user_id,role_id) VALUES(?,?)",uid,rid); }
        for(String[] c:new String[][]{{"sap-client","sap-client-secret"},{"srm-client","srm-client-secret"},{"crm-client","crm-client-secret"}}) jdbc.update("UPDATE oauth_client SET client_secret_hash=? WHERE client_id=?",PasswordHasher.hash(c[1]),c[0]);
        jdbc.update("UPDATE org_dept SET parent_id=(SELECT id FROM org_dept WHERE code='HQ') WHERE code IN ('RD','HR','FIN','MKT','ADM') AND (parent_id IS NULL OR parent_id=1)");
        jdbc.update("UPDATE hr_employee SET dept_id=(SELECT id FROM org_dept WHERE code='RD') WHERE employee_no IN ('E000001','E000002')");
        jdbc.update("UPDATE org_dept SET leader_employee_id=(SELECT id FROM hr_employee WHERE employee_no='E000002') WHERE code='RD'");
        jdbc.update("UPDATE org_dept SET leader_employee_id=(SELECT id FROM hr_employee WHERE employee_no='E000003') WHERE code='HR'");
        jdbc.update("UPDATE org_dept SET leader_employee_id=(SELECT id FROM hr_employee WHERE employee_no='E000004') WHERE code='FIN'");
        ensureNode("LEAVE", 2, "人力资源", "HR", "{\"field\":\"days\",\"op\":\">\",\"value\":3}");
        ensureNode("BUSINESS_TRIP", 2, "人力资源", "HR", null);
        ensureNode("EXPENSE", 2, "财务审核", "FINANCE", null);
    }
    private void ensureNode(String code, int seq, String name, String type, String condition) {
        jdbc.update("INSERT INTO wf_node(definition_id,seq,name,approver_type,multi_mode,condition_json) SELECT id,?,?,?,'ANY',? FROM wf_definition WHERE code=? AND NOT EXISTS (SELECT 1 FROM wf_node WHERE definition_id=wf_definition.id AND seq=?)", seq, name, type, condition, code, seq);
    }
}
