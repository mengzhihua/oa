package com.oa;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.common.BizException;
import com.oa.payroll.entity.PayPeriod;
import com.oa.payroll.entity.PayScheme;
import com.oa.payroll.entity.PaySlip;
import com.oa.payroll.service.PayPeriodService;
import com.oa.payroll.service.PaySchemeService;
import com.oa.payroll.service.PaySlipService;
import com.oa.payroll.service.PayrollCalcService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
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
