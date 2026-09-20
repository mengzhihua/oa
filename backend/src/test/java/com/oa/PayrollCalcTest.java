package com.oa;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.common.BizException;
import com.oa.attendance.service.AttendanceService;
import com.oa.payroll.entity.PayPeriod;
import com.oa.payroll.entity.PayScheme;
import com.oa.payroll.entity.PaySlip;
import com.oa.payroll.service.PayPeriodService;
import com.oa.payroll.service.PaySchemeService;
import com.oa.payroll.service.PaySlipService;
import com.oa.payroll.service.PayrollCalcService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class PayrollCalcTest {
    private Long testEmployeeId;
    @Autowired
    private PayrollCalcService payrollCalcService;

    @Autowired
    private PayPeriodService periodService;

    @Autowired
    private PaySchemeService schemeService;

    @Autowired
    private PaySlipService slipService;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AttendanceService attendanceService;

    @Test
    public void 完整计算应断言工资金额和累计预扣税() throws Exception {
        Long employeeId = prepareEmployee();
        PayPeriod january = newPeriod("2025-01");
        jdbc.update("INSERT INTO att_overtime_request "
                        + "(employee_id, start_time, end_time, hours, type, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                employeeId, LocalDateTime.of(2025, 1, 11, 10, 0),
                LocalDateTime.of(2025, 1, 11, 14, 0), new BigDecimal("4"),
                "WEEKEND", "APPROVED");
        jdbc.update("INSERT INTO att_daily "
                        + "(employee_id, work_date, status, late_minutes, early_minutes, "
                        + "work_minutes, overtime_minutes) VALUES (?, ?, ?, ?, ?, ?, ?)",
                employeeId, LocalDate.of(2025, 1, 6), "LATE", 5, 0, 480, 0);
        jdbc.update("INSERT INTO att_daily "
                        + "(employee_id, work_date, status, late_minutes, early_minutes, "
                        + "work_minutes, overtime_minutes) VALUES (?, ?, ?, ?, ?, ?, ?)",
                employeeId, LocalDate.of(2025, 1, 7), "LATE", 8, 0, 480, 0);

        payrollCalcService.calculate(january.getId());
        PaySlip firstSlip = slipService.lambdaQuery()
                .eq(PaySlip::getPeriodId, january.getId())
                .eq(PaySlip::getEmployeeId, employeeId)
                .one();

        // 时薪=20000/21.75/8=114.94252874；周末加班费=4×114.94252874×2=919.54。
        // 应发=20000+919.54=20919.54；迟到扣款=2×20=40.00。
        // 个人社保=20000×(8%+2%+0.5%)=2100.00；个人公积金=20000×12%=2400.00。
        // 应纳税所得额=20919.54-2100-2400-40=16379.54；
        // 累计应纳税所得额=16379.54-5000=11379.54；累计税=11379.54×3%=341.39。
        // 实发=20919.54-40-2100-2400-341.39=16038.15。
        assertEquals(new BigDecimal("20919.54"), firstSlip.getGross());
        assertEquals(new BigDecimal("919.54"), item(firstSlip, "OT_PAY"));
        assertEquals(new BigDecimal("-40.00"), item(firstSlip, "LATE_DED"));
        assertEquals(new BigDecimal("2100.00"), firstSlip.getSiPersonal());
        assertEquals(new BigDecimal("2400.00"), firstSlip.getHfPersonal());
        assertEquals(new BigDecimal("5300.00"), firstSlip.getSiCompany());
        assertEquals(new BigDecimal("2400.00"), firstSlip.getHfCompany());
        assertEquals(new BigDecimal("16379.54"), firstSlip.getTaxableIncome());
        assertEquals(new BigDecimal("11379.54"), firstSlip.getCumulativeTaxable());
        assertEquals(new BigDecimal("341.39"), firstSlip.getTax());
        assertEquals(new BigDecimal("16038.15"), firstSlip.getNet());
        assertEquals(new BigDecimal("-2100.00"), item(firstSlip, "SI_PERSONAL"));
        assertEquals(new BigDecimal("-2400.00"), item(firstSlip, "HF_PERSONAL"));
        assertEquals(new BigDecimal("-341.39"), item(firstSlip, "TAX"));

        january.setStatus("PAID");
        periodService.updateById(january);
        PayPeriod february = newPeriod("2025-02");
        payrollCalcService.calculate(february.getId());
        PaySlip secondSlip = slipService.lambdaQuery()
                .eq(PaySlip::getPeriodId, february.getId())
                .eq(PaySlip::getEmployeeId, employeeId)
                .one();

        // 二月累计应纳税所得额=16379.54+15500-10000=21879.54；
        // 累计税=21879.54×3%=656.39；当月税=656.39-341.39=315.00；
        // 实发=20000-4500-315=15185.00。
        assertEquals(new BigDecimal("20000.00"), secondSlip.getGross());
        assertEquals(new BigDecimal("15500.00"), secondSlip.getTaxableIncome());
        assertEquals(new BigDecimal("21879.54"), secondSlip.getCumulativeTaxable());
        assertEquals(new BigDecimal("315.00"), secondSlip.getTax());
        assertEquals(new BigDecimal("15185.00"), secondSlip.getNet());
    }

    @Test
    public void 考勤未锁定时拒绝计算() {
        PayPeriod period = newPeriod("2099-01");
        period.setAttLocked(0);
        periodService.updateById(period);
        assertThrows(BizException.class, () -> payrollCalcService.calculate(period.getId()));
    }

    @Test
    public void 当月离职员工可以生成月结并算薪() {
        List<Long> existing = jdbc.query(
                "SELECT id FROM hr_employee WHERE employee_no = 'TEST-LEFT-001'",
                (result, rowNum) -> result.getLong(1));
        Long employeeId = existing.isEmpty() ? null : existing.get(0);
        if (employeeId == null) {
            jdbc.update("INSERT INTO hr_employee "
                            + "(employee_no, name, dept_id, hire_date, regular_date, leave_date, "
                            + "employment_status, employee_type) VALUES "
                            + "('TEST-LEFT-001', '离职测试员工', 2, '2024-01-01', "
                            + "'2024-04-01', '2025-03-15', 'LEFT', 'FULLTIME')");
            employeeId = jdbc.queryForObject(
                    "SELECT id FROM hr_employee WHERE employee_no = 'TEST-LEFT-001'",
                    Long.class);
        }
        jdbc.update("UPDATE hr_employee SET employment_status = 'REGULAR', leave_date = NULL "
                        + "WHERE id = ?", employeeId);
        jdbc.update("DELETE FROM pay_scheme WHERE employee_id = ?", employeeId);
        PayScheme scheme = new PayScheme();
        scheme.setEmployeeId(employeeId);
        scheme.setEffectiveDate(LocalDate.of(2024, 1, 1));
        scheme.setBaseSalary(new BigDecimal("10000.00"));
        scheme.setPostSalary(BigDecimal.ZERO);
        scheme.setPerfSalary(BigDecimal.ZERO);
        scheme.setAllowancesJson("{}");
        scheme.setSiBase(new BigDecimal("10000.00"));
        scheme.setHfBase(new BigDecimal("10000.00"));
        scheme.setStatus("ACTIVE");
        schemeService.save(scheme);
        jdbc.update("DELETE FROM pay_period WHERE year_month = '2025-03'");
        jdbc.update("DELETE FROM att_monthly_summary WHERE year_month = '2025-03'");
        jdbc.update("DELETE FROM pay_slip WHERE employee_id = ?", employeeId);
        PayPeriod period = new PayPeriod();
        period.setYearMonth("2025-03");
        period.setStatus("OPEN");
        period.setAttLocked(0);
        period.setTotalGross(BigDecimal.ZERO);
        period.setTotalNet(BigDecimal.ZERO);
        periodService.save(period);
        attendanceService.generateMonthly("2025-03", null);
        int generatedAfterLeaveDate = jdbc.queryForObject(
                "SELECT COUNT(*) FROM att_daily WHERE employee_id = ? "
                        + "AND work_date > '2025-03-15' AND work_date <= '2025-03-31'",
                Integer.class, employeeId);
        assertTrue(generatedAfterLeaveDate > 0);
        jdbc.update("UPDATE hr_employee SET employment_status = 'LEFT', leave_date = '2025-03-15' "
                        + "WHERE id = ?", employeeId);
        attendanceService.generateMonthly("2025-03", null);
        assertEquals(0, jdbc.queryForObject(
                "SELECT COUNT(*) FROM att_daily WHERE employee_id = ? "
                        + "AND work_date > '2025-03-15' AND work_date <= '2025-03-31'",
                Integer.class, employeeId));
        attendanceService.lockMonthly("2025-03");
        payrollCalcService.calculate(period.getId());
        assertEquals(1, slipService.lambdaQuery()
                .eq(PaySlip::getPeriodId, period.getId())
                .eq(PaySlip::getEmployeeId, employeeId).count());
    }

    @Test
    public void 旧离职日的在职员工不截断当月考勤() {
        List<Long> existing = jdbc.query(
                "SELECT id FROM hr_employee WHERE employee_no = 'TEST-LEFT-EARLY-001'",
                (result, rowNum) -> result.getLong(1));
        Long employeeId = existing.isEmpty() ? null : existing.get(0);
        if (employeeId == null) {
            jdbc.update("INSERT INTO hr_employee "
                            + "(employee_no, name, dept_id, hire_date, regular_date, "
                            + "employment_status, employee_type) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    "TEST-LEFT-EARLY-001", "历史离职日测试员工", 2L,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 4, 1),
                    "REGULAR", "FULLTIME");
            employeeId = jdbc.queryForObject(
                    "SELECT id FROM hr_employee WHERE employee_no = 'TEST-LEFT-EARLY-001'",
                    Long.class);
        }
        jdbc.update("UPDATE hr_employee SET employment_status = 'REGULAR', "
                        + "leave_date = '2097-02-15' WHERE id = ?", employeeId);
        jdbc.update("DELETE FROM pay_scheme WHERE employee_id = ?", employeeId);
        PayScheme scheme = new PayScheme();
        scheme.setEmployeeId(employeeId);
        scheme.setEffectiveDate(LocalDate.of(2024, 1, 1));
        scheme.setBaseSalary(new BigDecimal("10000.00"));
        scheme.setPostSalary(BigDecimal.ZERO);
        scheme.setPerfSalary(BigDecimal.ZERO);
        scheme.setAllowancesJson("{}");
        scheme.setSiBase(new BigDecimal("10000.00"));
        scheme.setHfBase(new BigDecimal("10000.00"));
        scheme.setStatus("ACTIVE");
        schemeService.save(scheme);
        jdbc.update("DELETE FROM att_monthly_summary WHERE employee_id = ? "
                        + "AND year_month = '2097-03'", employeeId);
        jdbc.update("DELETE FROM att_daily WHERE employee_id = ? "
                        + "AND work_date >= '2097-03-01' AND work_date < '2097-04-01'",
                employeeId);

        attendanceService.generateMonthly("2097-03", null);

        assertTrue(jdbc.queryForObject(
                "SELECT COUNT(*) FROM att_daily WHERE employee_id = ? "
                        + "AND work_date > '2097-03-15' AND work_date <= '2097-03-31'",
                Integer.class, employeeId) > 0);
        assertTrue(jdbc.queryForObject(
                "SELECT absent_days FROM att_monthly_summary "
                        + "WHERE employee_id = ? AND year_month = '2097-03'",
                BigDecimal.class, employeeId).compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    public void 工资条接口补充未配置工资项目的明细() throws Exception {
        Long employeeId = jdbc.queryForObject(
                "SELECT employee_id FROM sys_user WHERE username = 'zhangsan'", Long.class);
        jdbc.update("DELETE FROM pay_slip WHERE period_id IN "
                        + "(SELECT id FROM pay_period WHERE year_month = '2098-11')");
        jdbc.update("DELETE FROM pay_period WHERE year_month = '2098-11'");
        jdbc.update("INSERT INTO pay_period "
                        + "(year_month, status, att_locked, total_gross, total_net, headcount) "
                        + "VALUES ('2098-11', 'PAID', 1, 0, 0, 1)");
        Long periodId = jdbc.queryForObject(
                "SELECT id FROM pay_period WHERE year_month = '2098-11'", Long.class);
        jdbc.update("INSERT INTO pay_slip "
                        + "(period_id, employee_id, items_json, gross, taxable_income, cumulative_taxable, "
                        + "cumulative_tax, tax, si_personal, hf_personal, si_company, hf_company, net, status) "
                        + "VALUES (?, ?, ?, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'PAID')",
                periodId, employeeId,
                "{\"BASE\":1000,\"ALLOWANCE\":200,\"ADJUSTMENT\":-10,\"CUSTOM\":5}");
        String token = loginToken();
        mockMvc.perform(get("/api/payroll/slips/mine")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].yearMonth").value("2098-11"))
                .andExpect(jsonPath("$.data[0].itemDetails[0].code").value("BASE"))
                .andExpect(jsonPath("$.data[0].itemDetails[1].name").value("补贴合计"))
                .andExpect(jsonPath("$.data[0].itemDetails[2].name").value("手工调整"))
                .andExpect(jsonPath("$.data[0].itemDetails[3].name").value("CUSTOM"));
    }

    private String loginToken() throws Exception {
        org.springframework.test.web.servlet.MvcResult result =
                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/api/auth/login")
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"zhangsan\",\"password\":\"emp123\"}"))
                        .andExpect(status().isOk())
                        .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data").get("token").asText();
    }

    private Long prepareEmployee() {
        List<Long> employeeIds = jdbc.query(
                "SELECT id FROM hr_employee WHERE employee_no = 'TEST-PAYROLL-001'",
                (result, rowNum) -> result.getLong(1));
        Long employeeId = employeeIds.isEmpty() ? null : employeeIds.get(0);
        if (employeeId == null) {
            jdbc.update("INSERT INTO hr_employee "
                            + "(employee_no, name, dept_id, hire_date, regular_date, "
                            + "employment_status, employee_type) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                    "TEST-PAYROLL-001", "工资测试员工", 2L,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 4, 1),
                    "REGULAR", "FULLTIME");
            employeeId = jdbc.queryForObject(
                    "SELECT id FROM hr_employee WHERE employee_no = 'TEST-PAYROLL-001'", Long.class);
        }
        jdbc.update("DELETE FROM pay_scheme WHERE employee_id = ?", employeeId);
        PayScheme scheme = new PayScheme();
        scheme.setEmployeeId(employeeId);
        scheme.setEffectiveDate(LocalDate.of(2024, 1, 1));
        scheme.setBaseSalary(new BigDecimal("20000.00"));
        scheme.setPostSalary(BigDecimal.ZERO);
        scheme.setPerfSalary(BigDecimal.ZERO);
        scheme.setAllowancesJson("{}");
        scheme.setSiBase(new BigDecimal("20000.00"));
        scheme.setHfBase(new BigDecimal("20000.00"));
        scheme.setStatus("ACTIVE");
        schemeService.save(scheme);
        jdbc.update("DELETE FROM att_overtime_request WHERE employee_id = ?", employeeId);
        jdbc.update("DELETE FROM att_daily WHERE employee_id = ?", employeeId);
        jdbc.update("DELETE FROM pay_slip WHERE employee_id = ?", employeeId);
        testEmployeeId = employeeId;
        return employeeId;
    }

    private PayPeriod newPeriod(String yearMonth) {
        jdbc.update("DELETE FROM pay_period WHERE year_month = ?", yearMonth);
        PayPeriod period = new PayPeriod();
        period.setYearMonth(yearMonth);
        period.setStatus("OPEN");
        period.setAttLocked(1);
        period.setTotalGross(BigDecimal.ZERO);
        period.setTotalNet(BigDecimal.ZERO);
        periodService.save(period);
        jdbc.update("DELETE FROM att_monthly_summary WHERE year_month = ?", yearMonth);
        jdbc.update("INSERT INTO att_monthly_summary "
                        + "(employee_id, year_month, should_days, actual_days, leave_days, status) "
                        + "SELECT e.id, ?, 21, 21, '{}', 'LOCKED' FROM hr_employee e "
                        + "WHERE EXISTS (SELECT 1 FROM pay_scheme s "
                        + "WHERE s.employee_id = e.id AND s.effective_date <= ?)",
                yearMonth, LocalDate.parse(yearMonth + "-01"));
        return period;
    }

    private BigDecimal item(PaySlip slip, String code) throws Exception {
        Map<String, BigDecimal> items = objectMapper.readValue(
                slip.getItemsJson(), new TypeReference<Map<String, BigDecimal>>() {
                });
        return items.get(code);
    }
}
