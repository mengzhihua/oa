package com.oa.integration;

import com.oa.common.BizException;
import com.oa.common.R;
import com.oa.workflow.WorkflowService;
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

/** IR 控制塔：审批实例 / 待办快照，以及发起采购审批。 */
@RestController
@RequestMapping("/api/open/ir")
public class OpenIrController {
    private final JdbcTemplate jdbc;
    private final WorkflowService workflowService;
    private final String apiKey;

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
                "SELECT * FROM wf_task WHERE status = 'PENDING' ORDER BY id DESC")) {
            rows.add(row("WF_TASK",
                    String.valueOf(cell(task, "id", "ID")),
                    str(cell(task, "status", "STATUS")),
                    String.valueOf(cell(task, "instance_id", "INSTANCE_ID")),
                    BigDecimal.ONE, null, null,
                    "待办 " + cell(task, "id", "ID")));
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
        if ("OA_START_WORKFLOW".equals(type)) {
            String code = first(str(params.get("definitionCode")), str(params.get("code")), "GENERAL");
            String title = first(str(params.get("title")), "IR 控制塔审批 " + targetKey);
            Long applicant = applicantId();
            Map<String, Object> form = params.get("form") instanceof Map
                    ? (Map<String, Object>) params.get("form")
                    : Collections.singletonMap("content", targetKey);
            return R.ok(workflowService.start(
                    code, applicant, title, form,
                    first(str(params.get("businessType")), "IR"),
                    first(str(params.get("businessId")), targetKey)));
        }
        throw new BizException("不支持的 IR 指令: " + type);
    }

    private Long applicantId() {
        List<Map<String, Object>> users = jdbc.queryForList(
                "SELECT id FROM sys_user WHERE status = 1 ORDER BY id");
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
