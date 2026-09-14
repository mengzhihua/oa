package com.oa.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.common.BizException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class WorkflowService {
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;
    private final List<WorkflowCallback> callbacks;

    public WorkflowService(JdbcTemplate jdbc, ObjectMapper objectMapper,
                           List<WorkflowCallback> callbacks) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
        this.callbacks = callbacks;
    }

    @Transactional
    public Map<String, Object> start(String code, Long applicant, String title,
                                      Map<String, Object> form, String businessType,
                                      String businessId) {
        Map<String, Object> definition = one(
                "SELECT * FROM wf_definition WHERE code = ? AND status = 1", code);
        if (definition == null) {
            throw new BizException("流程定义不存在");
        }
        String instanceNo = "WF" + System.currentTimeMillis();
        try {
            String formJson = objectMapper.writeValueAsString(form);
            jdbc.update("INSERT INTO wf_instance "
                            + "(instance_no, definition_id, title, form_json, business_type, "
                            + "business_id, applicant_id, status, current_node_seq, submitted_at) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, 'RUNNING', 0, ?)",
                    instanceNo, definition.get("ID"), title, formJson, businessType, businessId,
                    applicant, LocalDateTime.now());
        } catch (Exception exception) {
            throw new BizException("表单格式错误");
        }
        Long instanceId = jdbc.queryForObject(
                "SELECT id FROM wf_instance WHERE instance_no = ?", Long.class, instanceNo);
        advance(instanceId, 1, form);
        return one("SELECT * FROM wf_instance WHERE id = ?", instanceId);
    }

    @Transactional
    public void approve(Long taskId, Long userId, String comment) {
        handle(taskId, userId, "APPROVED", comment);
    }

    @Transactional
    public void reject(Long taskId, Long userId, String comment) {
        handle(taskId, userId, "REJECTED", comment);
    }

    @Transactional
    public void transfer(Long taskId, Long userId, Long toUserId) {
        int updated = jdbc.update("UPDATE wf_task SET approver_user_id = ?, "
                        + "status = 'PENDING', comment = ? WHERE id = ? AND approver_user_id = ? "
                + "AND status = 'PENDING'", toUserId, null, taskId, userId);
        if (updated == 0) {
            throw new BizException("待办不存在或无权转交");
        }
    }

    @Transactional
    public void cancel(Long instanceId, Long applicantId) {
        int updated = jdbc.update("UPDATE wf_instance SET status = 'CANCELED', "
                        + "finished_at = ? WHERE id = ? AND applicant_id = ? "
                        + "AND status = 'RUNNING'", LocalDateTime.now(), instanceId, applicantId);
        if (updated == 0) {
            throw new BizException("只有申请人可以撤销进行中的流程");
        }
        jdbc.update("UPDATE wf_task SET status = 'SKIPPED' WHERE instance_id = ? "
                        + "AND status = 'PENDING'", instanceId);
    }

    @Transactional
    public void handle(Long taskId, Long userId, String action, String comment) {
        Map<String, Object> task = one(
                "SELECT * FROM wf_task WHERE id = ? AND approver_user_id = ? "
                        + "AND status = 'PENDING'", taskId, userId);
        if (task == null) {
            throw new BizException("待办不存在或无权处理");
        }
        jdbc.update("UPDATE wf_task SET status = ?, comment = ?, handled_at = ? WHERE id = ?",
                action, comment, LocalDateTime.now(), taskId);
        Long instanceId = number(task, "INSTANCE_ID");
        if ("REJECTED".equals(action)) {
            finish(instanceId, "REJECTED");
            return;
        }
        int nodeSeq = number(task, "NODE_SEQ").intValue();
        String mode = jdbc.queryForObject(
                "SELECT multi_mode FROM wf_node WHERE definition_id = "
                        + "(SELECT definition_id FROM wf_instance WHERE id = ?) AND seq = ?",
                String.class, instanceId, nodeSeq);
        if ("ANY".equalsIgnoreCase(mode)) {
            jdbc.update("UPDATE wf_task SET status = 'SKIPPED' WHERE instance_id = ? "
                    + "AND node_seq = ? AND status = 'PENDING'", instanceId, nodeSeq);
        } else if (pending(instanceId, nodeSeq) > 0) {
            return;
        }
        Map<String, Object> instance = one("SELECT form_json FROM wf_instance WHERE id = ?",
                instanceId);
        try {
            Map<String, Object> form = objectMapper.readValue(
                    string(instance, "FORM_JSON"), Map.class);
            advance(instanceId, nodeSeq + 1, form);
        } catch (Exception exception) {
            finish(instanceId, "APPROVED");
        }
    }

    private void advance(Long instanceId, int from, Map<String, Object> form) {
        Map<String, Object> instance = one("SELECT definition_id FROM wf_instance WHERE id = ?",
                instanceId);
        List<Map<String, Object>> nodes = jdbc.queryForList(
                "SELECT * FROM wf_node WHERE definition_id = ? AND seq >= ? ORDER BY seq",
                instance.get("DEFINITION_ID"), from);
        for (Map<String, Object> node : nodes) {
            if (!condition(value(node, "CONDITION_JSON"), form)) {
                continue;
            }
            List<Long> approvers = resolve(instanceId, string(node, "APPROVER_TYPE"),
                    value(node, "APPROVER_REF"));
            if (approvers.isEmpty()) {
                continue;
            }
            for (Long approver : approvers) {
                jdbc.update("INSERT INTO wf_task "
                                + "(instance_id, node_seq, approver_user_id, status) "
                                + "VALUES (?, ?, ?, 'PENDING')",
                        instanceId, value(node, "SEQ"), approver);
            }
            jdbc.update("UPDATE wf_instance SET current_node_seq = ? WHERE id = ?",
                    value(node, "SEQ"), instanceId);
            return;
        }
        finish(instanceId, "APPROVED");
    }

    private List<Long> resolve(Long instanceId, String type, Object reference) {
        Long applicant = number(one("SELECT applicant_id FROM wf_instance WHERE id = ?",
                instanceId), "APPLICANT_ID");
        if ("USER".equals(type)) {
            return reference == null ? Collections.<Long>emptyList()
                    : Collections.singletonList(Long.valueOf(String.valueOf(reference)));
        }
        if ("HR".equals(type) || "FINANCE".equals(type) || "ROLE".equals(type)) {
            String role = "ROLE".equals(type) ? String.valueOf(reference) : type;
            return jdbc.query("SELECT u.id FROM sys_user u JOIN sys_user_role ur "
                            + "ON ur.user_id = u.id JOIN sys_role r ON r.id = ur.role_id "
                            + "WHERE r.code = ? AND u.status = 1",
                    new Object[]{role}, (rs, rowNum) -> rs.getLong(1));
        }
        List<Long> result = new ArrayList<>();
        Map<String, Object> employee = one(
                "SELECT dept_id FROM hr_employee WHERE id = "
                        + "(SELECT employee_id FROM sys_user WHERE id = ?)", applicant);
        Long department = employee == null ? null : number(employee, "DEPT_ID");
        while (department != null) {
            Long leaderEmployee = jdbc.queryForObject(
                    "SELECT leader_employee_id FROM org_dept WHERE id = ?",
                    Long.class, department);
            if (leaderEmployee != null) {
                Long leaderUser = jdbc.queryForObject(
                        "SELECT id FROM sys_user WHERE employee_id = ? AND status = 1",
                        Long.class, leaderEmployee);
                if (leaderUser != null && !leaderUser.equals(applicant)) {
                    result.add(leaderUser);
                    return result;
                }
            }
            department = jdbc.queryForObject("SELECT parent_id FROM org_dept WHERE id = ?",
                    Long.class, department);
        }
        return result;
    }

    private boolean condition(String conditionJson, Map<String, Object> form) {
        if (conditionJson == null || conditionJson.trim().isEmpty()) {
            return true;
        }
        try {
            JsonNode condition = objectMapper.readTree(conditionJson);
            Object actual = form.get(condition.path("field").asText());
            if (actual == null) {
                return false;
            }
            int comparison = compare(actual, condition.path("value").asText());
            String operator = condition.path("op").asText();
            if (">".equals(operator)) {
                return comparison > 0;
            }
            if (">=".equals(operator)) {
                return comparison >= 0;
            }
            if ("<".equals(operator)) {
                return comparison < 0;
            }
            if ("<=".equals(operator)) {
                return comparison <= 0;
            }
            if ("==".equals(operator)) {
                return comparison == 0;
            }
            if ("!=".equals(operator)) {
                return comparison != 0;
            }
            return false;
        } catch (Exception exception) {
            throw new BizException("流程条件格式错误");
        }
    }

    private int compare(Object actual, String expected) {
        try {
            return Double.compare(Double.parseDouble(String.valueOf(actual)),
                    Double.parseDouble(expected));
        } catch (NumberFormatException exception) {
            return String.valueOf(actual).compareTo(expected);
        }
    }

    private int pending(Long instanceId, int nodeSeq) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM wf_task WHERE instance_id = ? "
                + "AND node_seq = ? AND status = 'PENDING'", Integer.class, instanceId, nodeSeq);
        return count == null ? 0 : count;
    }

    private void finish(Long instanceId, String status) {
        jdbc.update("UPDATE wf_instance SET status = ?, finished_at = ? WHERE id = ?",
                status, LocalDateTime.now(), instanceId);
        String businessType = jdbc.queryForObject(
                "SELECT business_type FROM wf_instance WHERE id = ?", String.class, instanceId);
        for (WorkflowCallback callback : callbacks) {
            if (callback.supports(businessType)) {
                callback.completed(instanceId, status);
            }
        }
    }

    private Map<String, Object> one(String sql, Object... args) {
        List<Map<String, Object>> rows = jdbc.queryForList(sql, args);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private static String string(Map<String, Object> row, String key) {
        Object value = row == null ? null : row.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private static String value(Map<String, Object> row, String key) {
        Object value = row == null ? null : row.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private static Long number(Map<String, Object> row, String key) {
        Object value = row == null ? null : row.get(key);
        return value == null ? null : ((Number) value).longValue();
    }
}
