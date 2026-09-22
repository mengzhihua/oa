package com.oa.integration.controller;

import com.oa.common.BizException;
import com.oa.common.R;
import com.oa.workflow.service.WorkflowService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/** IR 控制塔：审批实例 / 待办快照，以及发起采购审批。 */
@RestController
@RequestMapping("/api/open/ir")
public class OpenIrController {
    private final JdbcTemplate jdbc;
    private final WorkflowService workflowService;
    private final String apiKey;
    private final ConcurrentHashMap<String, Object> actionCache = new ConcurrentHashMap<String, Object>();

    public OpenIrController(
            JdbcTemplate jdbc,
            WorkflowService workflowService,
            @Value("${oa.open.api-key:oa-open-key}") String apiKey) {
        this.jdbc = jdbc;
        this.workflowService = workflowService;
        this.apiKey = apiKey;
    }

    @GetMapping("/snapshots")
    public R<Map<String, Object>> snapshots(
            @RequestHeader(value = "X-Api-Key", required = false) String key) {
        checkKey(key);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> instance : jdbc.queryForList(
                "SELECT * FROM wf_instance ORDER BY id DESC")) {
            rows.add(row("WF_INSTANCE",
                    str(cell(instance, "instance_no", "INSTANCE_NO")),
                    str(cell(instance, "status", "STATUS")),
                    str(cell(instance, "business_id", "BUSINESS_ID")),
                    BigDecimal.ONE, null, str(cell(instance, "business_type", "BUSINESS_TYPE")),
                    str(cell(instance, "title", "TITLE"))));
        }
        for (Map<String, Object> task : jdbc.queryForList(
                "SELECT t.id AS id, t.status AS status, t.instance_id AS instance_id, "
                        + "i.title AS instance_title, i.instance_no AS instance_no, "
                        + "i.business_id AS business_id, n.name AS node_name "
                        + "FROM wf_task t "
                        + "LEFT JOIN wf_instance i ON i.id = t.instance_id "
                        + "LEFT JOIN wf_node n ON n.definition_id = i.definition_id "
                        + "AND n.seq = t.node_seq "
                        + "WHERE t.status = 'PENDING' ORDER BY t.id DESC")) {
            Object taskId = cell(task, "id", "ID");
            String instanceTitle = str(cell(task, "instance_title", "INSTANCE_TITLE"));
            String nodeName = str(cell(task, "node_name", "NODE_NAME"));
            rows.add(row("WF_TASK",
                    String.valueOf(taskId),
                    str(cell(task, "status", "STATUS")),
                    first(str(cell(task, "business_id", "BUSINESS_ID")),
                            str(cell(task, "instance_no", "INSTANCE_NO"))),
                    BigDecimal.ONE, null,
                    str(cell(task, "instance_no", "INSTANCE_NO")),
                    taskTitle(instanceTitle, nodeName, taskId)));
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("system", "OA");
        data.put("snapshots", rows);
        return R.ok(data);
    }

    @PostMapping("/actions")
    public R<Object> actions(
            @RequestHeader(value = "X-Api-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        checkKey(key);
        String type = String.valueOf(body.getOrDefault("type", ""));
        String targetKey = String.valueOf(body.getOrDefault("targetKey", ""));
        @SuppressWarnings("unchecked")
        Map<String, Object> params = body.get("params") instanceof Map
                ? (Map<String, Object>) body.get("params") : new LinkedHashMap<String, Object>();
        return R.ok(executeOnce(cacheKey(type, targetKey, body.get("idempotencyKey")), () -> {
            if ("OA_START_WORKFLOW".equals(type)) {
                String code = first(str(params.get("definitionCode")), str(params.get("code")), "GENERAL");
                String title = first(str(params.get("title")), "IR 控制塔审批 " + targetKey);
                Long applicant = applicantId();
                Map<String, Object> form = params.get("form") instanceof Map
                        ? (Map<String, Object>) params.get("form")
                        : Collections.singletonMap("content", targetKey);
                return workflowService.start(
                        code, applicant, title, form,
                        first(str(params.get("businessType")), "IR"),
                        first(str(params.get("businessId")), targetKey));
            }
            if ("OA_APPROVE_TASK".equals(type) || "OA_COMPLETE_TASK".equals(type)) {
                Long taskId = taskId(first(str(params.get("taskId")), targetKey));
                Map<String, Object> task = one("SELECT * FROM wf_task WHERE id = ?", taskId);
                if (task == null) {
                    throw new BizException("待办不存在: " + taskId);
                }
                Object approver = cell(task, "approver_user_id", "APPROVER_USER_ID");
                if (!(approver instanceof Number)) {
                    throw new BizException("待办缺少审批人: " + taskId);
                }
                workflowService.approve(taskId, ((Number) approver).longValue(),
                        first(str(params.get("comment")), "IR 控制塔系统审批"));
                return one("SELECT * FROM wf_task WHERE id = ?", taskId);
            }
            throw new BizException("不支持的 IR 指令: " + type);
        }));
    }

    private Object executeOnce(String cacheKey, Supplier<Object> work) {
        if (cacheKey == null) {
            return work.get();
        }
        Object cached = actionCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        synchronized (actionCache) {
            cached = actionCache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
            Object created = work.get();
            actionCache.put(cacheKey, created);
            return created;
        }
    }

    private static String cacheKey(String type, String targetKey, Object idempotencyKey) {
        String key = str(idempotencyKey);
        if (key == null || key.trim().isEmpty() || "null".equals(key)) {
            return null;
        }
        return type + "|" + (targetKey == null ? "" : targetKey) + "|" + key.trim();
    }

    private static String taskTitle(String instanceTitle, String nodeName, Object taskId) {
        String title = first(instanceTitle);
        if (title == null) {
            return "待办 " + taskId;
        }
        String node = first(nodeName);
        return node == null ? title : title + " · " + node;
    }

    private Long taskId(String value) {
        if (value == null || value.trim().isEmpty() || "null".equals(value)) {
            throw new BizException("缺少待办 ID");
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            throw new BizException("待办 ID 非法: " + value);
        }
    }

    private Map<String, Object> one(String sql, Object... args) {
        List<Map<String, Object>> rows = jdbc.queryForList(sql, args);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private Long applicantId() {
        List<Map<String, Object>> users = jdbc.queryForList(
                "SELECT id FROM sys_user WHERE username = 'zhangsan' AND status = 1");
        if (users.isEmpty()) {
            users = jdbc.queryForList(
                    "SELECT id FROM sys_user WHERE status = 1 AND employee_id IS NOT NULL ORDER BY id");
        }
        if (users.isEmpty()) {
            users = jdbc.queryForList("SELECT id FROM sys_user WHERE status = 1 ORDER BY id");
        }
        if (users.isEmpty()) {
            throw new BizException("OA 没有可用申请人");
        }
        Object id = cell(users.get(0), "id", "ID");
        return ((Number) id).longValue();
    }

    private void checkKey(String key) {
        if (apiKey == null || apiKey.trim().isEmpty() || !apiKey.equals(key)) {
            throw new BizException("无效的 API Key");
        }
    }

    private static Map<String, Object> row(
            String dataType, String bizKey, String status, String sku,
            BigDecimal qty, BigDecimal amount, String plantCode, String title) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("dataType", dataType);
        row.put("bizKey", bizKey);
        row.put("status", status);
        row.put("sku", sku);
        row.put("qty", qty);
        row.put("amount", amount);
        row.put("plantCode", plantCode);
        row.put("title", title);
        return row;
    }

    private static Object cell(Map<String, Object> row, String... keys) {
        if (row == null) {
            return null;
        }
        for (String key : keys) {
            if (row.containsKey(key)) {
                return row.get(key);
            }
        }
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            for (String key : keys) {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private static String first(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty() && !"null".equals(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private static String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
