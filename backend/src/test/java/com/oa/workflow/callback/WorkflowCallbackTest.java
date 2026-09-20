package com.oa.workflow.callback;

import com.oa.attendance.service.AttendanceService;
import com.oa.workflow.service.WorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class WorkflowCallbackTest {
    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    public void 请假审批回写状态和年假余额() {
        Long applicant = jdbc.queryForObject("SELECT id FROM sys_user WHERE username = 'zhangsan'",
                Long.class);
        Long employee = jdbc.queryForObject("SELECT employee_id FROM sys_user WHERE id = ?",
                Long.class, applicant);
        BigDecimal before = jdbc.queryForObject(
                "SELECT used_days FROM att_leave_balance WHERE employee_id = ? "
                        + "AND `year` = 2026 AND leave_type = 'ANNUAL'", BigDecimal.class, employee);
        jdbc.update("INSERT INTO att_leave_request "
                        + "(employee_id, leave_type, start_time, end_time, days, reason, status) "
                        + "VALUES (?, 'ANNUAL', '2026-09-14 09:00:00', "
                        + "'2026-09-14 18:00:00', 1, '测试', 'PENDING')", employee);
        Long leaveId = jdbc.queryForObject(
                "SELECT MAX(id) FROM att_leave_request WHERE employee_id = ?", Long.class, employee);
        Map<String, Object> form = new HashMap<>();
        form.put("days", 1);
        form.put("leaveType", "ANNUAL");
        Map<String, Object> instance = workflowService.start("LEAVE", applicant,
                "测试请假", form, "LEAVE", String.valueOf(leaveId));
        Long instanceId = ((Number) instance.get("ID")).longValue();
        Long taskId = jdbc.queryForObject("SELECT id FROM wf_task WHERE instance_id = ?",
                Long.class, instanceId);
        Long manager = jdbc.queryForObject("SELECT id FROM sys_user WHERE username = 'manager'",
                Long.class);
        workflowService.approve(taskId, manager, "同意");
        assertEquals("APPROVED", jdbc.queryForObject(
                "SELECT status FROM wf_instance WHERE id = ?", String.class, instanceId));
        assertEquals(before.add(BigDecimal.ONE), jdbc.queryForObject(
                "SELECT used_days FROM att_leave_balance WHERE employee_id = ? "
                        + "AND `year` = 2026 AND leave_type = 'ANNUAL'", BigDecimal.class, employee));
    }
}
