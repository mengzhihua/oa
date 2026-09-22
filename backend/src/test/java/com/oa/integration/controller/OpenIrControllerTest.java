package com.oa.integration.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class OpenIrControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void snapshotsIncludeSeededPendingTask() throws Exception {
        String snapshots = mockMvc.perform(get("/api/open/ir/snapshots")
                        .header("X-Api-Key", "oa-open-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.snapshots[?(@.dataType=='WF_INSTANCE' && @.sku=='IR-DEMO-WF')]").isArray())
                .andExpect(jsonPath("$.data.snapshots[?(@.dataType=='WF_TASK' && @.status=='PENDING')]").isArray())
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        boolean pending = false;
        boolean demoInstance = false;
        for (JsonNode row : objectMapper.readTree(snapshots).get("data").get("snapshots")) {
            if ("WF_INSTANCE".equals(row.path("dataType").asText())
                    && "IR-DEMO-WF".equals(row.path("sku").asText())) {
                demoInstance = true;
                org.junit.jupiter.api.Assertions.assertFalse(
                        row.path("title").asText().matches("待办 \\d+"),
                        "实例标题应带申请名");
            }
            if ("WF_TASK".equals(row.path("dataType").asText())
                    && "PENDING".equals(row.path("status").asText())) {
                pending = true;
                String title = row.path("title").asText();
                org.junit.jupiter.api.Assertions.assertFalse(
                        title.matches("待办 \\d+"),
                        "待办标题应带申请名而不是纯数字: " + title);
                org.junit.jupiter.api.Assertions.assertTrue(
                        title.contains(" · ") || title.length() > 6,
                        "待办标题应含节点或申请名: " + title);
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(demoInstance, "应保留演示审批实例");
        org.junit.jupiter.api.Assertions.assertTrue(pending, "启动后应有 IR 演示待办");
    }

    @Test
    public void startWorkflowReplayUsesIdempotencyKey() throws Exception {
        String body = "{\"type\":\"OA_START_WORKFLOW\",\"targetKey\":\"MAT-IDEM-1\","
                + "\"idempotencyKey\":\"OA-START-1\","
                + "\"params\":{\"definitionCode\":\"GENERAL\",\"title\":\"IR 幂等审批 MAT-IDEM-1\"}}";
        String first = mockMvc.perform(post("/api/open/ir/actions")
                        .header("X-Api-Key", "oa-open-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        String replay = mockMvc.perform(post("/api/open/ir/actions")
                        .header("X-Api-Key", "oa-open-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        JsonNode firstData = objectMapper.readTree(first).get("data");
        JsonNode replayData = objectMapper.readTree(replay).get("data");
        String firstNo = firstData.has("instance_no") ? firstData.get("instance_no").asText()
                : firstData.get("INSTANCE_NO").asText();
        String replayNo = replayData.has("instance_no") ? replayData.get("instance_no").asText()
                : replayData.get("INSTANCE_NO").asText();
        org.junit.jupiter.api.Assertions.assertEquals(firstNo, replayNo);
    }

    @Test
    public void openApiStartsWorkflowThenApprovesAssignedTask() throws Exception {
        mockMvc.perform(post("/api/open/ir/actions")
                        .header("X-Api-Key", "oa-open-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"OA_START_WORKFLOW\",\"targetKey\":\"MAT-1000\","
                                + "\"params\":{\"definitionCode\":\"GENERAL\",\"title\":\"IR 采购补货审批 MAT-1000\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        String snapshots = mockMvc.perform(get("/api/open/ir/snapshots")
                        .header("X-Api-Key", "oa-open-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        String taskId = null;
        for (JsonNode row : objectMapper.readTree(snapshots).get("data").get("snapshots")) {
            if ("WF_TASK".equals(row.path("dataType").asText())
                    && "PENDING".equals(row.path("status").asText())) {
                taskId = row.path("bizKey").asText();
                break;
            }
        }
        org.junit.jupiter.api.Assertions.assertNotNull(taskId, "应产生 OA 待办");

        String approve = "{\"type\":\"OA_APPROVE_TASK\",\"targetKey\":\"" + taskId
                + "\",\"idempotencyKey\":\"OA-APR-1\","
                + "\"params\":{\"taskId\":\"" + taskId
                + "\",\"comment\":\"IR 控制塔系统审批\"}}";
        mockMvc.perform(post("/api/open/ir/actions")
                        .header("X-Api-Key", "oa-open-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(approve))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        String approved = mockMvc.perform(post("/api/open/ir/actions")
                        .header("X-Api-Key", "oa-open-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(approve))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        JsonNode task = objectMapper.readTree(approved).get("data");
        String status = task.has("status") ? task.get("status").asText()
                : task.get("STATUS").asText();
        org.junit.jupiter.api.Assertions.assertEquals("APPROVED", status);
    }
}
