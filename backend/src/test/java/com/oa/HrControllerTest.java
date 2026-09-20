package com.oa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.hr.service.HrScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Autowired
    private HrScheduler hrScheduler;

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

    @Test
    public void 离职后恢复在职会恢复账号() throws Exception {
        String employeeNo = "TEST-HR-RESTORE-" + System.nanoTime();
        jdbc.update("INSERT INTO hr_employee "
                        + "(employee_no, name, hire_date, employment_status, employee_type) "
                        + "VALUES (?, ?, ?, 'REGULAR', 'FULLTIME')",
                employeeNo, "恢复在职测试员工", java.time.LocalDate.of(2024, 1, 1));
        Long employeeId = jdbc.queryForObject(
                "SELECT id FROM hr_employee WHERE employee_no = ?", Long.class, employeeNo);
        jdbc.update("INSERT INTO sys_user "
                        + "(username, password_hash, real_name, employee_id, status) "
                        + "VALUES (?, 'test', ?, ?, 1)",
                employeeNo, "恢复在职测试员工", employeeId);
        String token = loginToken();
        try {
            String today = java.time.LocalDate.now().toString();
            mockMvc.perform(post("/api/hr/employees/" + employeeId + "/leave")
                            .header("Authorization", "Bearer " + token)
                            .contentType(APPLICATION_JSON)
                            .content("{\"leaveDate\":\"" + today + "\"}"))
                    .andExpect(status().isOk());
            org.junit.jupiter.api.Assertions.assertEquals("LEFT",
                    jdbc.queryForObject("SELECT employment_status FROM hr_employee WHERE id = ?",
                            String.class, employeeId));
            org.junit.jupiter.api.Assertions.assertEquals(0,
                    jdbc.queryForObject("SELECT status FROM sys_user WHERE employee_id = ?",
                            Integer.class, employeeId));

            mockMvc.perform(put("/api/hr/employees/" + employeeId)
                            .header("Authorization", "Bearer " + token)
                            .contentType(APPLICATION_JSON)
                            .content("{\"name\":\"恢复在职测试员工\",\"employmentStatus\":\"REGULAR\"}"))
                    .andExpect(status().isOk());
            org.junit.jupiter.api.Assertions.assertNull(jdbc.queryForObject(
                    "SELECT leave_date FROM hr_employee WHERE id = ?", java.sql.Date.class, employeeId));
            org.junit.jupiter.api.Assertions.assertEquals(1,
                    jdbc.queryForObject("SELECT status FROM sys_user WHERE employee_id = ?",
                            Integer.class, employeeId));
        } finally {
            jdbc.update("DELETE FROM sys_user WHERE employee_id = ?", employeeId);
            jdbc.update("DELETE FROM hr_employee WHERE id = ?", employeeId);
        }
    }

    @Test
    public void 到期离职员工由调度任务失效账号() {
        String employeeNo = "TEST-HR-EXPIRE-" + System.nanoTime();
        java.time.LocalDate yesterday = java.time.LocalDate.now().minusDays(1);
        jdbc.update("INSERT INTO hr_employee "
                        + "(employee_no, name, hire_date, employment_status, employee_type, leave_date) "
                        + "VALUES (?, ?, ?, 'LEAVING', 'FULLTIME', ?)",
                employeeNo, "到期离职测试员工", java.time.LocalDate.of(2024, 1, 1), yesterday);
        Long employeeId = jdbc.queryForObject(
                "SELECT id FROM hr_employee WHERE employee_no = ?", Long.class, employeeNo);
        jdbc.update("INSERT INTO sys_user "
                        + "(username, password_hash, real_name, employee_id, status) "
                        + "VALUES (?, 'test', ?, ?, 1)",
                employeeNo, "到期离职测试员工", employeeId);
        try {
            hrScheduler.expireLeaving();
            org.junit.jupiter.api.Assertions.assertEquals("LEFT",
                    jdbc.queryForObject("SELECT employment_status FROM hr_employee WHERE id = ?",
                            String.class, employeeId));
            org.junit.jupiter.api.Assertions.assertEquals(0,
                    jdbc.queryForObject("SELECT status FROM sys_user WHERE employee_id = ?",
                            Integer.class, employeeId));
        } finally {
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
