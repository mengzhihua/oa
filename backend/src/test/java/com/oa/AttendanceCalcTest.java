package com.oa;

import com.oa.attendance.service.AttendanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class AttendanceCalcTest {
    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    public void 计算工作日排除节假日并包含调休() {
        // 2026-09-25 中秋节排除，2026-09-27 调休上班，2026-09-28 周一上班。
        assertEquals(new BigDecimal("2"),
                attendanceService.workingDays(LocalDate.of(2026, 9, 25),
                        LocalDate.of(2026, 9, 28)));
    }

    @Test
    public void 迟到记录计算为迟到() {
        jdbc.update("INSERT INTO att_shift "
                        + "(code, name, work_start, work_end, late_grace_minutes, "
                        + "early_grace_minutes, is_default) "
                        + "VALUES ('TEST_STANDARD', '测试标准班', '09:00:00', '18:00:00', 0, 0, 0)");
        Long shiftId = jdbc.queryForObject(
                "SELECT id FROM att_shift WHERE code = 'TEST_STANDARD'", Long.class);
        jdbc.update("INSERT INTO att_schedule (employee_id, work_date, shift_id) "
                        + "VALUES (1, '2026-09-11', ?)", shiftId);
        jdbc.update("INSERT INTO att_clock_record "
                        + "(employee_id, clock_time, clock_type, source, device) "
                        + "VALUES (1, '2026-09-11 09:15:00', 'IN', 'WEB', '测试设备')");
        assertEquals("LATE",
                attendanceService.calcDaily(1L, LocalDate.of(2026, 9, 11)).getStatus());
    }
}
