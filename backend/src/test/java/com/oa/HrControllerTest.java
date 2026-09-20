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

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class HrControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    public void 离职接口使用请求日期并记录离职状态() throws Exception {
        String employeeNo = "TEST-HR-LEAVE-" + System.nanoTime();
        jdbc.update("INSERT INTO hr_employee "
                        + "(employee_no, name, hire_date, employment_status, employee_type) "
                        + "VALUES (?, ?, ?, 'REGULAR', 'FULLTIME')",
                employeeNo, "离职接口测试员工", java.time.LocalDate.of(2024, 1, 1));
        Long employeeId = jdbc.queryForObject(
                "SELECT id FROM hr_employee WHERE employee_no = ?", Long.class, employeeNo);
        String token = loginToken();
        try {
            mockMvc.perform(post("/api/hr/employees/" + employeeId + "/leave")
                            .header("Authorization", "Bearer " + token)
                            .contentType(APPLICATION_JSON)
                            .content("{\"leaveDate\":\"2026-09-15\",\"reason\":\"合同到期\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));

            String[] employee = jdbc.queryForObject(
                    "SELECT employment_status, leave_date FROM hr_employee WHERE id = ?",
                    (result, rowNum) -> new String[]{
                            result.getString(1), result.getDate(2).toLocalDate().toString()
                    }, employeeId);
            org.junit.jupiter.api.Assertions.assertEquals("LEFT", employee[0]);
            org.junit.jupiter.api.Assertions.assertEquals("2026-09-15", employee[1]);
            org.junit.jupiter.api.Assertions.assertEquals(1, jdbc.queryForObject(
                    "SELECT COUNT(*) FROM hr_employee_change "
                            + "WHERE employee_id = ? AND change_type = 'LEAVE' "
                            + "AND effective_date = '2026-09-15'",
                    Integer.class, employeeId));
        } finally {
            jdbc.update("DELETE FROM hr_employee_change WHERE employee_id = ?", employeeId);
            jdbc.update("DELETE FROM sys_user WHERE employee_id = ?", employeeId);
            jdbc.update("DELETE FROM hr_employee WHERE id = ?", employeeId);
        }
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
