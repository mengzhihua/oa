package com.oa.workflow.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
public class WorkflowServiceTest {
    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    public void 条件节点跳过与满足时进入下一节点() {
        Long applicant = user("zhangsan");
        Map<String, Object> shortLeave = new HashMap<>();
        shortLeave.put("days", 1);
        Map<String, Object> skipped = workflowService.start("LEAVE", applicant,
                "一天请假", shortLeave, "LEAVE", "T-" + System.nanoTime());
        assertEquals(1, countTasks(id(skipped)));

        Map<String, Object> longLeave = new HashMap<>();
        longLeave.put("days", 5);
        Map<String, Object> required = workflowService.start("LEAVE", applicant,
                "五天请假", longLeave, "LEAVE", "T-" + System.nanoTime());
        Long instanceId = id(required);
        Long taskId = jdbc.queryForObject(
                "SELECT id FROM wf_task WHERE instance_id = ? AND status = 'PENDING'",
                Long.class, instanceId);
        workflowService.approve(taskId, user("manager"), "同意");
        assertEquals(2, countTasks(instanceId));
    }

    @Test
    public void 申请人是部门主管时上溯父部门() {
        Long manager = user("manager");
        Long hrEmployee = jdbc.queryForObject(
                "SELECT employee_id FROM sys_user WHERE username = 'hr'", Long.class);
        jdbc.update("UPDATE org_dept SET leader_employee_id = ? WHERE code = 'HQ'",
                hrEmployee);
        Map<String, Object> form = new HashMap<>();
        form.put("content", "主管申请");
        Map<String, Object> instance = workflowService.start("GENERAL", manager,
                "主管申请", form, "GENERAL", "T-" + System.nanoTime());
        Long approver = jdbc.queryForObject(
                "SELECT approver_user_id FROM wf_task WHERE instance_id = ?",
                Long.class, id(instance));
        Long hqLeader = jdbc.queryForObject(
                "SELECT u.id FROM sys_user u JOIN hr_employee e ON e.id = u.employee_id "
                        + "JOIN org_dept d ON d.leader_employee_id = e.id WHERE d.code = 'HQ'",
                Long.class);
        if (hqLeader != null) {
            assertEquals(hqLeader, approver);
        }
    }

    @Test
    public void 驳回结束且撤销只允许申请人() {
        Long applicant = user("zhangsan");
        Map<String, Object> form = new HashMap<>();
        form.put("content", "驳回测试");
        Map<String, Object> rejected = workflowService.start("GENERAL", applicant,
                "驳回测试", form, "GENERAL", "T-" + System.nanoTime());
        Long rejectedId = id(rejected);
        Long rejectedTask = jdbc.queryForObject(
                "SELECT id FROM wf_task WHERE instance_id = ?", Long.class, rejectedId);
        workflowService.reject(rejectedTask, user("manager"), "退回");
        assertEquals("REJECTED", status(rejectedId));

        Map<String, Object> canceled = workflowService.start("GENERAL", applicant,
                "撤销测试", form, "GENERAL", "T-" + System.nanoTime());
        Long canceledId = id(canceled);
        assertThrows(RuntimeException.class,
                () -> workflowService.cancel(canceledId, user("manager")));
        workflowService.cancel(canceledId, applicant);
        assertEquals("CANCELED", status(canceledId));
    }

    @Test
    public void ALL模式需要全部审批人() {
        Long adminRole = jdbc.queryForObject(
                "SELECT id FROM sys_role WHERE code = 'ADMIN'", Long.class);
        Long manager = user("manager");
        jdbc.update("MERGE INTO sys_user_role (user_id, role_id) KEY(user_id, role_id) "
                + "VALUES (?, ?)", manager, adminRole);
        Long definition = jdbc.queryForObject(
                "SELECT id FROM wf_definition WHERE code = 'GENERAL'", Long.class);
        jdbc.update("UPDATE wf_node SET approver_type = 'ROLE', approver_ref = 'ADMIN', "
                + "multi_mode = 'ALL' WHERE definition_id = ? AND seq = 1", definition);

        Map<String, Object> form = new HashMap<>();
        form.put("content", "多人审批");
        Map<String, Object> instance = workflowService.start("GENERAL", user("zhangsan"),
                "多人审批", form, "GENERAL", "T-" + System.nanoTime());
        Long instanceId = id(instance);
        List<Long> tasks = jdbc.query("SELECT id FROM wf_task WHERE instance_id = ?",
                new Object[]{instanceId}, (result, row) -> result.getLong(1));
        assertEquals(2, tasks.size());
        workflowService.approve(tasks.get(0), user("admin"), "同意");
        assertEquals("RUNNING", status(instanceId));
        workflowService.approve(tasks.get(1), user("manager"), "同意");
        assertEquals("APPROVED", status(instanceId));
    }

    private Long user(String username) {
        return jdbc.queryForObject("SELECT id FROM sys_user WHERE username = ?",
                Long.class, username);
    }

    private Long id(Map<String, Object> row) {
        return ((Number) row.get("ID")).longValue();
    }

    private int countTasks(Long instanceId) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM wf_task WHERE instance_id = ?",
                Integer.class, instanceId);
    }

    private String status(Long instanceId) {
        return jdbc.queryForObject("SELECT status FROM wf_instance WHERE id = ?",
                String.class, instanceId);
    }
}
