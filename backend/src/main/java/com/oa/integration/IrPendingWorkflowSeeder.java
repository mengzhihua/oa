package com.oa.integration;

import com.oa.workflow.service.WorkflowService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/** 启动后补一条 PENDING 待办，供 IR OA_WF_PENDING 在 HTTP 模式下能扫到。 */
@Component
@Order
public class IrPendingWorkflowSeeder implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final WorkflowService workflowService;

    public IrPendingWorkflowSeeder(JdbcTemplate jdbc, WorkflowService workflowService) {
        this.jdbc = jdbc;
        this.workflowService = workflowService;
    }

    @Override
    public void run(ApplicationArguments args) {
        Integer pending = jdbc.queryForObject(
                "SELECT COUNT(*) FROM wf_task WHERE status = 'PENDING'", Integer.class);
        if (pending != null && pending > 0) {
            return;
        }
        Integer existing = jdbc.queryForObject(
                "SELECT COUNT(*) FROM wf_instance WHERE business_id = 'IR-DEMO-WF'", Integer.class);
        if (existing != null && existing > 0) {
            return;
        }
        List<Map<String, Object>> users = jdbc.queryForList(
                "SELECT id FROM sys_user WHERE username = 'zhangsan' AND status = 1");
        if (users.isEmpty()) {
            users = jdbc.queryForList(
                    "SELECT id FROM sys_user WHERE status = 1 AND employee_id IS NOT NULL ORDER BY id");
        }
        if (users.isEmpty()) {
            return;
        }
        Object id = users.get(0).containsKey("id") ? users.get(0).get("id") : users.get(0).get("ID");
        try {
            workflowService.start("GENERAL", ((Number) id).longValue(),
                    "IR 控制塔演示待办",
                    Collections.singletonMap("content", "IR-DEMO-WF"),
                    "IR", "IR-DEMO-WF");
            reassignDemoTaskToAdmin();
        } catch (RuntimeException ignored) {
            // 演示种子失败不影响启动
        }
    }

    /** 默认管理员登录后应能在工作台看到演示待办。 */
    private void reassignDemoTaskToAdmin() {
        List<Map<String, Object>> admins = jdbc.queryForList(
                "SELECT id FROM sys_user WHERE username = 'admin' AND status = 1");
        if (admins.isEmpty()) {
            return;
        }
        Object adminId = admins.get(0).containsKey("id") ? admins.get(0).get("id") : admins.get(0).get("ID");
        jdbc.update("UPDATE wf_task SET approver_user_id = ? WHERE status = 'PENDING' "
                        + "AND instance_id IN (SELECT id FROM wf_instance WHERE business_id = 'IR-DEMO-WF')",
                ((Number) adminId).longValue());
    }
}
