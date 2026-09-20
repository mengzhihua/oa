package com.oa.system.service;

import com.oa.system.auth.PasswordHasher;
import com.oa.payroll.service.PayrollCalcService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class StartupService {
    private final JdbcTemplate jdbc;
    private final String adminPassword;
    private final PayrollCalcService payrollCalcService;
    private final boolean demoSeed;
    private final ResourceLoader resourceLoader;
    private final String demoDataLocation;

    public StartupService(JdbcTemplate jdbc,
                          @Value("${oa.auth.admin-password:admin123}") String adminPassword,
                          PayrollCalcService payrollCalcService,
                          @Value("${oa.demo.seed:true}") boolean demoSeed,
                          ResourceLoader resourceLoader,
                          @Value("${oa.demo.data-location:classpath:demo-data.sql}")
                          String demoDataLocation) {
        this.jdbc = jdbc;
        this.adminPassword = adminPassword;
        this.payrollCalcService = payrollCalcService;
        this.demoSeed = demoSeed;
        this.resourceLoader = resourceLoader;
        this.demoDataLocation = demoDataLocation;
    }

    @PostConstruct
    public void init() {
        ensureMenuColumns();
        ensureAdmin();
        if (!demoSeed) {
            return;
        }
        if (!demoDataExists()) {
            executeDemoData();
        }
        ensureUsers();
        ensureClientSecrets();
        ensureDepartments();
        ensureWorkflowNodes();
        ensureDemoPayroll();
        ensureDemoMeetingRooms();
    }

    private void ensureMenuColumns() {
        if (!columnExists("sys_menu", "type")) {
            jdbc.execute("ALTER TABLE sys_menu ADD COLUMN type VARCHAR(32) DEFAULT 'MENU'");
        }
        if (!columnExists("sys_menu", "status")) {
            jdbc.execute("ALTER TABLE sys_menu ADD COLUMN status INT DEFAULT 1");
        }
        jdbc.update("UPDATE sys_menu SET type = 'MENU' WHERE type IS NULL");
        jdbc.update("UPDATE sys_menu SET type = 'DIRECTORY' "
                + "WHERE code IN ('SYSTEM', 'ORG', 'HR', 'WORKFLOW') "
                + "AND (type IS NULL OR type = 'MENU')");
        jdbc.update("UPDATE sys_menu SET status = 1 WHERE status IS NULL");
    }

    private boolean columnExists(String table, String column) {
        return jdbc.execute((ConnectionCallback<Boolean>) connection -> {
            DatabaseMetaData metadata = connection.getMetaData();
            String catalog = connection.getCatalog();
            String schema = connection.getSchema();
            String[] tableNames = {table, table.toUpperCase(Locale.ROOT),
                    table.toLowerCase(Locale.ROOT)};
            String[] columnNames = {column, column.toUpperCase(Locale.ROOT),
                    column.toLowerCase(Locale.ROOT)};
            for (String tableName : tableNames) {
                for (String columnName : columnNames) {
                    try (ResultSet result = metadata.getColumns(
                            catalog, schema, tableName, columnName)) {
                        if (result.next()) {
                            return true;
                        }
                    }
                }
            }
            return false;
        });
    }

    private void ensureAdmin() {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE username = 'admin'", Integer.class);
        if (count == null || count == 0) {
            jdbc.update("INSERT INTO sys_user "
                            + "(username, password_hash, real_name, employee_id, status) "
                            + "VALUES (?, ?, ?, ?, 1)",
                    "admin", PasswordHasher.hash(adminPassword), "系统管理员", null);
        }
        Long userId = jdbc.queryForObject(
                "SELECT id FROM sys_user WHERE username = 'admin'", Long.class);
        Long roleId = jdbc.queryForObject(
                "SELECT id FROM sys_role WHERE code = 'ADMIN'", Long.class);
        Integer relationCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM sys_user_role WHERE user_id = ? AND role_id = ?",
                Integer.class, userId, roleId);
        if (relationCount == null || relationCount == 0) {
            jdbc.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (?, ?)",
                    userId, roleId);
        }
    }

    private boolean demoDataExists() {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM hr_employee WHERE employee_no = 'E000001'",
                Integer.class);
        return count != null && count > 0;
    }

    private void executeDemoData() {
        Resource resource = resourceLoader.getResource(demoDataLocation);
        jdbc.execute((ConnectionCallback<Void>) connection -> {
            ScriptUtils.executeSqlScript(connection, new EncodedResource(resource));
            return null;
        });
    }

    private void ensureUsers() {
        List<String[]> users = Arrays.asList(
                new String[]{"admin", "系统管理员", "", "ADMIN", adminPassword},
                new String[]{"hr", "王人事", "3", "HR", "hr123"},
                new String[]{"finance", "赵财务", "4", "FINANCE", "fin123"},
                new String[]{"manager", "李主管", "2", "MANAGER", "mgr123"},
                new String[]{"zhangsan", "张三", "1", "EMPLOYEE", "emp123"});
        for (String[] user : users) {
            Long employeeId = user[2].isEmpty() ? null : Long.valueOf(user[2]);
            Integer count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM sys_user WHERE username = ?", Integer.class, user[0]);
            if (count == null || count == 0) {
                jdbc.update("INSERT INTO sys_user "
                                + "(username, password_hash, real_name, employee_id, status) "
                                + "VALUES (?, ?, ?, ?, 1)",
                        user[0], PasswordHasher.hash(user[4]), user[1], employeeId);
            }
            Long userId = jdbc.queryForObject(
                    "SELECT id FROM sys_user WHERE username = ?", Long.class, user[0]);
            Long roleId = jdbc.queryForObject(
                    "SELECT id FROM sys_role WHERE code = ?", Long.class, user[3]);
            Integer relationCount = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM sys_user_role WHERE user_id = ? AND role_id = ?",
                    Integer.class, userId, roleId);
            if (relationCount == null || relationCount == 0) {
                jdbc.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (?, ?)",
                        userId, roleId);
            }
        }
    }

    private void ensureClientSecrets() {
        updateClientSecret("sap-client", "sap-client-secret");
        updateClientSecret("srm-client", "srm-client-secret");
        updateClientSecret("crm-client", "crm-client-secret");
    }

    private void updateClientSecret(String clientId, String secret) {
        jdbc.update("UPDATE oauth_client SET client_secret_hash = ? WHERE client_id = ?",
                PasswordHasher.hash(secret), clientId);
    }

    private void ensureDepartments() {
        jdbc.update("UPDATE org_dept SET parent_id = "
                        + "(SELECT id FROM org_dept WHERE code = 'HQ') "
                        + "WHERE code IN ('RD', 'HR', 'FIN', 'MKT', 'ADM') "
                        + "AND (parent_id IS NULL OR parent_id = 1)");
        jdbc.update("UPDATE hr_employee SET dept_id = "
                        + "(SELECT id FROM org_dept WHERE code = 'RD') "
                        + "WHERE employee_no IN ('E000001', 'E000002')");
        setLeader("RD", "E000002");
        setLeader("HR", "E000003");
        setLeader("FIN", "E000004");
    }

    private void setLeader(String departmentCode, String employeeNo) {
        jdbc.update("UPDATE org_dept SET leader_employee_id = "
                        + "(SELECT id FROM hr_employee WHERE employee_no = ?) "
                        + "WHERE code = ?", employeeNo, departmentCode);
    }

    private void ensureWorkflowNodes() {
        ensureNode("LEAVE", 2, "人力资源", "HR",
                "{\"field\":\"days\",\"op\":\">\",\"value\":3}");
        ensureNode("BUSINESS_TRIP", 2, "人力资源", "HR", null);
        ensureNode("EXPENSE", 2, "财务审核", "FINANCE", null);
    }

    private void ensureDemoPayroll() {
        List<Map<String, Object>> periods = jdbc.queryForList(
                "SELECT id FROM pay_period WHERE year_month = '2026-08'");
        Long periodId = periods.isEmpty() ? null : ((Number) column(periods.get(0), "ID")).longValue();
        if (periodId == null) {
            return;
        }
        List<Map<String, Object>> periodRows = jdbc.queryForList(
                "SELECT status FROM pay_period WHERE id = ?", periodId);
        String status = periodRows.isEmpty() ? null : String.valueOf(column(periodRows.get(0), "STATUS"));
        Integer slipCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM pay_slip WHERE period_id = ?", Integer.class, periodId);
        if (!"OPEN".equals(status) || (slipCount != null && slipCount > 0)) {
            return;
        }
        jdbc.update("INSERT INTO att_monthly_summary "
                        + "(employee_id, year_month, should_days, actual_days, late_count, "
                        + "late_minutes, early_count, absent_days, leave_days, overtime_hours, "
                        + "trip_days, status) "
                        + "SELECT e.id, '2026-08', 21, 21, 0, 0, 0, 0, '{}', 0, 0, 'DRAFT' "
                        + "FROM hr_employee e "
                        + "WHERE e.employment_status NOT IN ('LEFT', 'LEAVING') "
                        + "AND NOT EXISTS (SELECT 1 FROM att_monthly_summary m "
                        + "WHERE m.employee_id = e.id AND m.year_month = '2026-08')");
        jdbc.update("UPDATE att_monthly_summary SET status = 'LOCKED' "
                + "WHERE year_month = '2026-08' AND status <> 'LOCKED'");
        Long financeUserId = jdbc.queryForObject(
                "SELECT id FROM sys_user WHERE username = 'finance'", Long.class);
        payrollCalcService.calculate(periodId);
        payrollCalcService.approve(periodId, financeUserId);
        payrollCalcService.pay(periodId);
    }

    private void ensureDemoMeetingRooms() {
        ensureMeetingRoom("1号会议室", "总部一层", 10, "白板、投影");
        ensureMeetingRoom("2号会议室", "总部二层", 20, "投影、音响");
        ensureMeetingRoom("多媒体厅", "总部三层", 50, "大屏、音响、视频会议");
    }

    private void ensureMeetingRoom(String name, String location, int capacity,
                                   String equipment) {
        jdbc.update("INSERT INTO oa_meeting_room "
                        + "(name, location, capacity, equipment, status) "
                        + "SELECT ?, ?, ?, ?, 'ACTIVE' WHERE NOT EXISTS "
                        + "(SELECT 1 FROM oa_meeting_room WHERE name = ?)",
                name, location, capacity, equipment, name);
    }

    private void ensureNode(String code, int seq, String name, String type,
                            String condition) {
        jdbc.update("INSERT INTO wf_node "
                        + "(definition_id, seq, name, approver_type, multi_mode, condition_json) "
                        + "SELECT id, ?, ?, ?, 'ANY', ? FROM wf_definition "
                        + "WHERE code = ? AND NOT EXISTS "
                        + "(SELECT 1 FROM wf_node existing WHERE existing.definition_id = "
                        + "wf_definition.id AND existing.seq = ?)",
                seq, name, type, condition, code, seq);
    }

    private Object column(Map<String, Object> row, String name) {
        Object value = row.get(name);
        return value == null ? row.get(name.toLowerCase()) : value;
    }
}
