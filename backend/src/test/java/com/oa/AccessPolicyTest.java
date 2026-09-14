package com.oa;

import com.oa.system.auth.AccessPolicy;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccessPolicyTest {
    private final AccessPolicy policy = new AccessPolicy();
    private final Set<String> employee = Collections.singleton("EMPLOYEE");
    private final Set<String> finance = Collections.singleton("FINANCE");
    private final Set<String> manager = Collections.singleton("MANAGER");
    private final Set<String> hr = Collections.singleton("HR");
    private final Set<String> admin = Collections.singleton("ADMIN");

    @Test
    public void 权限边界按路径和角色生效() {
        assertFalse(policy.allowed("GET", "/api/payroll/slips", employee));
        assertFalse(policy.allowed("GET", "/api/hr/employees", employee));
        assertTrue(policy.allowed("GET", "/api/payroll/slips/mine", employee));
        assertTrue(policy.allowed("GET", "/api/payroll/my/slips", employee));
        assertTrue(policy.allowed("GET", "/api/org/depts/tree", employee));
        assertTrue(policy.allowed("GET", "/api/hr/employees", manager));
        assertTrue(policy.allowed("GET", "/api/hr/contracts/export", manager));
        assertFalse(policy.allowed("POST", "/api/hr/employees", manager));
        assertTrue(policy.allowed("POST", "/api/collab/expenses/10/pay", finance));
        assertFalse(policy.allowed("POST", "/api/collab/expenses/10/pay", employee));
        assertTrue(policy.allowed("POST", "/api/payroll/periods/10/approve", finance));
        assertFalse(policy.allowed("POST", "/api/payroll/periods/10/approve", hr));
        assertTrue(policy.allowed("GET", "/api/system/users", hr));
        assertTrue(policy.allowed("POST", "/api/system/users", hr));
        assertFalse(policy.allowed("GET", "/api/system/oplogs", hr));
        assertTrue(policy.allowed("GET", "/api/oauth/clients", admin));
        assertFalse(policy.allowed("GET", "/api/oauth/clients", employee));
        assertTrue(policy.allowed("GET", "/api/attendance/clock/today", employee));
        assertTrue(policy.allowed("GET", "/api/workflow/instances", employee));
        assertTrue(policy.allowed("GET", "/api/dashboard", employee));
    }

    @Test
    public void 管理员拥有受保护资源权限() {
        assertTrue(policy.allowed("GET", "/api/payroll/slips", admin));
        assertTrue(policy.allowed("POST", "/api/hr/employees", admin));
        assertTrue(policy.allowed("DELETE", "/api/system/oplogs/1", admin));
    }
}
