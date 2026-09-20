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

        String approved = mockMvc.perform(post("/api/open/ir/actions")
                        .header("X-Api-Key", "oa-open-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"OA_APPROVE_TASK\",\"targetKey\":\"" + taskId
                                + "\",\"params\":{\"taskId\":\"" + taskId
                                + "\",\"comment\":\"IR 控制塔系统审批\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        JsonNode task = objectMapper.readTree(approved).get("data");
        String status = task.has("status") ? task.get("status").asText()
                : task.get("STATUS").asText();
        org.junit.jupiter.api.Assertions.assertEquals("APPROVED", status);
    }
}
