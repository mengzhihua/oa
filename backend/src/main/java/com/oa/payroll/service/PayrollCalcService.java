package com.oa.payroll.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.common.BizException;
import com.oa.collab.entity.OaMessage;
import com.oa.collab.service.OaMessageService;
import com.oa.payroll.entity.PayInsuranceRule;
import com.oa.payroll.entity.PayPeriod;
import com.oa.payroll.entity.PayScheme;
import com.oa.payroll.entity.PaySlip;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PayrollCalcService {
    private static final BigDecimal MONTHS = BigDecimal.valueOf(12);
    private static final BigDecimal WORK_DAYS = BigDecimal.valueOf(21.75);
    private final JdbcTemplate jdbc;
    private final PayPeriodService periodService;
    private final PaySchemeService schemeService;
    private final PaySlipService slipService;
    private final PayInsuranceRuleService insuranceService;
    private final PayTaxBracketService bracketService;
    private final PayAdjustmentService adjustmentService;
    private final ObjectMapper objectMapper;
    private final OaMessageService messageService;

    public PayrollCalcService(JdbcTemplate jdbc,
                              PayPeriodService periodService,
                              PaySchemeService schemeService,
                              PaySlipService slipService,
                              PayInsuranceRuleService insuranceService,
                              PayTaxBracketService bracketService,
                              PayAdjustmentService adjustmentService,
                              ObjectMapper objectMapper,
                              OaMessageService messageService) {
        this.jdbc = jdbc;
        this.periodService = periodService;
        this.schemeService = schemeService;
        this.slipService = slipService;
        this.insuranceService = insuranceService;
        this.bracketService = bracketService;
        this.adjustmentService = adjustmentService;
        this.objectMapper = objectMapper;
        this.messageService = messageService;
    }

    @Transactional
    public int calculate(Long periodId) {
        PayPeriod period = periodService.getById(periodId);
        if (period == null) {
            throw new BizException("工资期间不存在");
        }
        if (!Integer.valueOf(1).equals(period.getAttLocked())) {
            throw new BizException("月度考勤未锁定，请先锁定考勤");
        }
        if ("APPROVED".equals(period.getStatus()) || "PAID".equals(period.getStatus())
                || "CLOSED".equals(period.getStatus())) {
            throw new BizException("当前工资期间不允许重新计算");
        }
        String[] parts = period.getYearMonth().split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        PayInsuranceRule rule = insuranceService.lambdaQuery()
                .eq(PayInsuranceRule::getCity, "北京").one();
        if (rule == null) {
            throw new BizException("未配置北京社保公积金规则");
        }
        List<Long> employees = jdbc.query(
                "SELECT id FROM hr_employee WHERE employment_status NOT IN ('LEFT', 'LEAVING')",
                (result, rowNum) -> result.getLong(1));
        jdbc.update("DELETE FROM pay_slip WHERE period_id = ?", periodId);
        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;
        for (Long employeeId : employees) {
            PaySlip slip = calculateEmployee(period, employeeId, year, month, rule);
            slipService.save(slip);
            totalGross = totalGross.add(slip.getGross());
            totalNet = totalNet.add(slip.getNet());
        }
        period.setStatus("CALCULATED");
        period.setCalcAt(java.time.LocalDateTime.now());
        period.setTotalGross(totalGross);
        period.setTotalNet(totalNet);
        period.setHeadcount(employees.size());
        periodService.updateById(period);
        return employees.size();
    }

    public PaySlip calculateEmployee(PayPeriod period, Long employeeId, int year,
                                      int month, PayInsuranceRule rule) {
        PayScheme scheme = schemeService.lambdaQuery()
                .eq(PayScheme::getEmployeeId, employeeId)
                .le(PayScheme::getEffectiveDate, LocalDate.of(year, month, 1))
                .orderByDesc(PayScheme::getEffectiveDate)
                .last("LIMIT 1").one();
        if (scheme == null) {
            throw new BizException("员工未配置薪资方案：" + employeeId);
        }
        BigDecimal base = money(scheme.getBaseSalary());
        BigDecimal post = money(scheme.getPostSalary());
        BigDecimal perf = money(scheme.getPerfSalary());
        BigDecimal allowance = allowance(scheme.getAllowancesJson());
        BigDecimal adjustment = jdbc.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM pay_adjustment "
                        + "WHERE period_id = ? AND employee_id = ?",
                BigDecimal.class, period.getId(), employeeId);
        BigDecimal overtimeHours = jdbc.queryForObject(
                "SELECT COALESCE(SUM(hours), 0) FROM att_overtime_request "
                        + "WHERE employee_id = ? AND status = 'APPROVED' "
                        + "AND start_time >= ? AND start_time < ?",
                BigDecimal.class, employeeId, LocalDate.of(year, month, 1).atStartOfDay(),
                LocalDate.of(year, month, 1).plusMonths(1).atStartOfDay());
        BigDecimal hourly = base.add(post).divide(WORK_DAYS, 8, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(8), 8, RoundingMode.HALF_UP);
        BigDecimal overtime = money(overtimeHours).multiply(hourly)
                .multiply(BigDecimal.valueOf(1.5));
        Integer lateCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM att_daily WHERE employee_id = ? "
                        + "AND work_date >= ? AND work_date < ? AND late_minutes > 0",
                Integer.class, employeeId, LocalDate.of(year, month, 1),
                LocalDate.of(year, month, 1).plusMonths(1));
        BigDecimal absentDays = jdbc.queryForObject(
                "SELECT COALESCE(SUM(absent_days), 0) FROM att_monthly_summary "
                        + "WHERE employee_id = ? AND year_month = ?",
                BigDecimal.class, employeeId, period.getYearMonth());
        BigDecimal leaveDays = jdbc.queryForObject(
                "SELECT COALESCE(SUM(days), 0) FROM att_leave_request "
                        + "WHERE employee_id = ? AND status = 'APPROVED' "
                        + "AND start_time >= ? AND start_time < ? AND leave_type = 'PERSONAL'",
                BigDecimal.class, employeeId, LocalDate.of(year, month, 1).atStartOfDay(),
                LocalDate.of(year, month, 1).plusMonths(1).atStartOfDay());
        BigDecimal lateDed = BigDecimal.valueOf(lateCount == null ? 0 : lateCount * 20L);
        BigDecimal absentDed = money(absentDays).multiply(base.add(post))
                .divide(WORK_DAYS, 8, RoundingMode.HALF_UP);
        BigDecimal leaveDed = money(leaveDays).multiply(base.add(post))
                .divide(WORK_DAYS, 8, RoundingMode.HALF_UP);
        BigDecimal gross = base.add(post).add(perf).add(allowance).add(overtime).add(adjustment);
        BigDecimal siBase = clamp(money(scheme.getSiBase()), rule.getSiBaseMin(), rule.getSiBaseMax());
        BigDecimal hfBase = clamp(money(scheme.getHfBase()), rule.getHfBaseMin(), rule.getHfBaseMax());
        BigDecimal siPersonal = siBase.multiply(rule.getPensionP().add(rule.getMedicalP())
                        .add(rule.getUnemploymentP())).setScale(2, RoundingMode.HALF_UP);
        BigDecimal hfPersonal = hfBase.multiply(rule.getHfP()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal siCompany = siBase.multiply(rule.getPensionC().add(rule.getMedicalC())
                        .add(rule.getUnemploymentC()).add(rule.getInjuryC())
                        .add(rule.getMaternityC())).setScale(2, RoundingMode.HALF_UP);
        BigDecimal hfCompany = hfBase.multiply(rule.getHfC()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxable = gross.subtract(siPersonal).subtract(hfPersonal)
                .subtract(lateDed).subtract(absentDed).subtract(leaveDed).max(BigDecimal.ZERO);
        BigDecimal previousTaxable = jdbc.queryForObject(
                "SELECT COALESCE(SUM(taxable_income), 0) FROM pay_slip s JOIN pay_period p "
                        + "ON p.id = s.period_id WHERE s.employee_id = ? "
                        + "AND p.year_month LIKE ? AND p.status = 'PAID'",
                BigDecimal.class, employeeId, year + "-%");
        BigDecimal previousTax = jdbc.queryForObject(
                "SELECT COALESCE(SUM(tax), 0) FROM pay_slip s JOIN pay_period p "
                        + "ON p.id = s.period_id WHERE s.employee_id = ? "
                        + "AND p.year_month LIKE ? AND p.status = 'PAID'",
                BigDecimal.class, employeeId, year + "-%");
        BigDecimal cumulativeTaxable = money(previousTaxable).add(taxable)
                .subtract(BigDecimal.valueOf(5000L * month)).max(BigDecimal.ZERO);
        BigDecimal cumulativeTax = taxFor(cumulativeTaxable).subtract(money(previousTax))
                .max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        PaySlip slip = new PaySlip();
        slip.setPeriodId(period.getId());
        slip.setEmployeeId(employeeId);
        slip.setDeptId(jdbc.queryForObject("SELECT dept_id FROM hr_employee WHERE id = ?",
                Long.class, employeeId));
        slip.setItemsJson(items(base, post, perf, allowance, overtime, adjustment,
                lateDed, absentDed, leaveDed));
        slip.setGross(gross.setScale(2, RoundingMode.HALF_UP));
        slip.setTaxableIncome(taxable.setScale(2, RoundingMode.HALF_UP));
        slip.setCumulativeTaxable(cumulativeTaxable.setScale(2, RoundingMode.HALF_UP));
        slip.setCumulativeTax(taxFor(cumulativeTaxable).setScale(2, RoundingMode.HALF_UP));
        slip.setTax(cumulativeTax);
        slip.setSiPersonal(siPersonal);
        slip.setHfPersonal(hfPersonal);
        slip.setSiCompany(siCompany);
        slip.setHfCompany(hfCompany);
        slip.setNet(gross.subtract(lateDed).subtract(absentDed).subtract(leaveDed)
                .subtract(siPersonal).subtract(hfPersonal).subtract(cumulativeTax)
                .setScale(2, RoundingMode.HALF_UP));
        slip.setStatus("DRAFT");
        return slip;
    }

    public void approve(Long periodId, Long userId) {
        PayPeriod period = requiredPeriod(periodId);
        if (!"CALCULATED".equals(period.getStatus())) {
            throw new BizException("工资期间未计算");
        }
        period.setStatus("APPROVED");
        period.setApprovedBy(userId);
        periodService.updateById(period);
        jdbc.update("UPDATE pay_slip SET status = 'CONFIRMED' WHERE period_id = ?", periodId);
    }

    public void pay(Long periodId) {
        PayPeriod period = requiredPeriod(periodId);
        if (!"APPROVED".equals(period.getStatus())) {
            throw new BizException("工资期间未审核");
        }
        period.setStatus("PAID");
        period.setPaidAt(java.time.LocalDateTime.now());
        periodService.updateById(period);
        jdbc.update("UPDATE pay_slip SET status = 'PAID' WHERE period_id = ?", periodId);
        List<Long> employees = jdbc.query("SELECT employee_id FROM pay_slip WHERE period_id = ?",
                new Object[]{periodId}, (result, rowNum) -> result.getLong(1));
        for (Long employeeId : employees) {
            List<Long> userIds = jdbc.query("SELECT id FROM sys_user WHERE employee_id = ?",
                    new Object[]{employeeId},
                    (result, rowNum) -> result.getLong(1));
            for (Long userId : userIds) {
                OaMessage message = new OaMessage();
                message.setToUserId(userId);
                message.setType("SYSTEM");
                message.setTitle("工资单已发放");
                message.setContent("工资期间 " + period.getYearMonth() + " 的工资单已发放");
                message.setLink("/payroll/slips/mine");
                messageService.save(message);
            }
        }
    }

    public void close(Long periodId) {
        PayPeriod period = requiredPeriod(periodId);
        if (!"PAID".equals(period.getStatus())) {
            throw new BizException("工资期间未发放");
        }
        period.setStatus("CLOSED");
        periodService.updateById(period);
    }

    private PayPeriod requiredPeriod(Long id) {
        PayPeriod period = periodService.getById(id);
        if (period == null) {
            throw new BizException("工资期间不存在");
        }
        return period;
    }

    private BigDecimal taxFor(BigDecimal taxable) {
        Map<String, Object> row = jdbc.queryForMap(
                "SELECT rate, quick_deduction FROM pay_tax_bracket "
                        + "WHERE lower_bound <= ? AND (upper_bound IS NULL OR upper_bound >= ?)"
                        + " ORDER BY level_no DESC LIMIT 1", taxable, taxable);
        return taxable.multiply(new BigDecimal(String.valueOf(row.get("RATE"))))
                .subtract(new BigDecimal(String.valueOf(row.get("QUICK_DEDUCTION"))));
    }

    private BigDecimal allowance(String json) {
        if (json == null || json.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            Map<String, Object> values = objectMapper.readValue(json,
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                    });
            BigDecimal total = BigDecimal.ZERO;
            for (Object value : values.values()) {
                total = total.add(money(value));
            }
            return total;
        } catch (Exception exception) {
            throw new BizException("补贴配置格式错误");
        }
    }

    private String items(BigDecimal base, BigDecimal post, BigDecimal perf,
                         BigDecimal allowance, BigDecimal overtime, BigDecimal adjustment,
                         BigDecimal late, BigDecimal absent, BigDecimal leave) {
        Map<String, BigDecimal> values = new LinkedHashMap<>();
        values.put("BASE", base);
        values.put("POST", post);
        values.put("PERF", perf);
        values.put("ALLOWANCE", allowance);
        values.put("OT_PAY", overtime);
        values.put("ADJUSTMENT", adjustment);
        values.put("LATE_DED", late.negate());
        values.put("ABSENT_DED", absent.negate());
        values.put("LEAVE_DED", leave.negate());
        try {
            return objectMapper.writeValueAsString(values);
        } catch (Exception exception) {
            throw new BizException("工资明细生成失败");
        }
    }

    private BigDecimal clamp(BigDecimal value, BigDecimal min, BigDecimal max) {
        return value.max(money(min)).min(money(max));
    }

    private BigDecimal money(Object value) {
        return value == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(value));
    }
}
