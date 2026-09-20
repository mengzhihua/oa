package com.oa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AttendanceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    public void 班次接口支持增改查删且拒绝删除已排班班次() throws Exception {
        String token = loginToken();
        String code = "TEST-CONTROLLER-" + System.nanoTime();
        String body = "{\"code\":\"" + code + "\",\"name\":\"控制器测试班\","
                + "\"workStart\":\"09:00:00\",\"workEnd\":\"18:00:00\","
                + "\"lateGraceMinutes\":5,\"earlyGraceMinutes\":5,\"isDefault\":0}";
        String response = mockMvc.perform(post("/api/attendance/shifts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        Long shiftId = objectMapper.readTree(response).get("data").get("id").asLong();
        Long scheduleId = null;
        try {
            mockMvc.perform(put("/api/attendance/shifts/" + shiftId)
                            .header("Authorization", "Bearer " + token)
                            .contentType(APPLICATION_JSON)
                            .content(body.replace("控制器测试班", "控制器测试班-更新")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("控制器测试班-更新"));

            mockMvc.perform(get("/api/attendance/shifts")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());

            LocalDate date = LocalDate.of(2099, 12, 31);
            jdbc.update("DELETE FROM att_schedule WHERE employee_id = 1 AND work_date = ?", date);
            jdbc.update("INSERT INTO att_schedule (employee_id, work_date, shift_id) "
                            + "VALUES (1, ?, ?)", date, shiftId);
            scheduleId = jdbc.queryForObject(
                    "SELECT id FROM att_schedule WHERE employee_id = 1 AND work_date = ?",
                    Long.class, date);
            mockMvc.perform(delete("/api/attendance/shifts/" + shiftId)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.msg").value("班次已被排班引用"));
        } finally {
            if (scheduleId != null) {
                jdbc.update("DELETE FROM att_schedule WHERE id = ?", scheduleId);
            }
            mockMvc.perform(delete("/api/attendance/shifts/" + shiftId)
                            .header("Authorization", "Bearer " + token));
        }
    }

    @Test
    public void 排班查询缺少日期时默认当月范围() throws Exception {
        mockMvc.perform(get("/api/attendance/schedules")
                        .header("Authorization", "Bearer " + loginToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    private String loginToken() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode data = objectMapper.readTree(response).get("data");
        return data.get("token").asText();
    }
}
