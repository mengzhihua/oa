package com.oa.payroll;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.common.PageResult;
import com.oa.common.R;
import com.oa.payroll.dto.PayrollAdjustmentRequest;
import com.oa.payroll.dto.SalaryAdjustRequest;
import com.oa.payroll.entity.PayAdjustment;
import com.oa.payroll.entity.PayInsuranceRule;
import com.oa.payroll.entity.PayItem;
import com.oa.payroll.entity.PayPeriod;
import com.oa.payroll.entity.PayScheme;
import com.oa.payroll.entity.PaySlip;
import com.oa.payroll.entity.PayTaxBracket;
import com.oa.payroll.service.PayAdjustmentService;
import com.oa.payroll.service.PayInsuranceRuleService;
import com.oa.payroll.service.PayItemService;
import com.oa.payroll.service.PayPeriodService;
import com.oa.payroll.service.PaySalaryChangeService;
import com.oa.payroll.service.PaySchemeService;
import com.oa.payroll.service.PaySlipService;
import com.oa.payroll.service.PayTaxBracketService;
import com.oa.payroll.service.PayrollCalcService;
import com.oa.payroll.entity.PaySalaryChange;
import com.oa.system.auth.CurrentUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletResponse;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import com.oa.payroll.vo.PayrollCostRow;

@Validated
@RestController
@RequestMapping("/api/payroll")
public class PayrollController {
    private final PayItemService itemService;
    private final PaySchemeService schemeService;
    private final PayInsuranceRuleService insuranceService;
    private final PayTaxBracketService bracketService;
    private final PayPeriodService periodService;
    private final PaySlipService slipService;
    private final PayAdjustmentService adjustmentService;
    private final PaySalaryChangeService salaryChangeService;
    private final PayrollCalcService calcService;
    private final JdbcTemplate jdbc;

    public PayrollController(PayItemService itemService,
                             PaySchemeService schemeService,
                             PayInsuranceRuleService insuranceService,
                             PayTaxBracketService bracketService,
                             PayPeriodService periodService,
                             PaySlipService slipService,
                             PayAdjustmentService adjustmentService,
                             PaySalaryChangeService salaryChangeService,
                             PayrollCalcService calcService,
                             JdbcTemplate jdbc) {
        this.itemService = itemService;
        this.schemeService = schemeService;
        this.insuranceService = insuranceService;
        this.bracketService = bracketService;
        this.periodService = periodService;
        this.slipService = slipService;
        this.adjustmentService = adjustmentService;
        this.salaryChangeService = salaryChangeService;
        this.calcService = calcService;
        this.jdbc = jdbc;
    }

    @GetMapping("/items")
    public R<List<PayItem>> items() {
        return R.ok(itemService.list());
    }

    @GetMapping("/schemes")
    public R<PageResult<PayScheme>> schemes(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) Long employeeId) {
        Page<PayScheme> result = schemeService.page(new Page<>(page, size),
                new LambdaQueryWrapper<PayScheme>()
                        .eq(employeeId != null, PayScheme::getEmployeeId, employeeId)
                        .orderByDesc(PayScheme::getEffectiveDate));
        return R.ok(new PageResult<>(result.getTotal(), page, size, result.getRecords()));
    }

    @PostMapping("/schemes/{employeeId}/adjust")
    public R<PayScheme> adjustScheme(@PathVariable Long employeeId,
                                      @Valid @RequestBody SalaryAdjustRequest request) {
        PayScheme scheme = new PayScheme();
        scheme.setEmployeeId(employeeId);
        scheme.setEffectiveDate(request.getEffectiveDate());
        scheme.setBaseSalary(request.getBaseSalary());
        scheme.setPostSalary(request.getPostSalary());
        scheme.setPerfSalary(request.getPerfSalary());
        scheme.setAllowancesJson(request.getAllowancesJson());
        scheme.setSiBase(request.getSiBase());
        scheme.setHfBase(request.getHfBase());
        scheme.setStatus("ACTIVE");
        schemeService.save(scheme);
        PaySalaryChange change = new PaySalaryChange();
        change.setEmployeeId(employeeId);
        change.setEffectiveDate(request.getEffectiveDate());
        change.setAfterJson(request.getAllowancesJson());
        change.setReason(request.getReason());
        change.setOperator(CurrentUser.id());
        salaryChangeService.save(change);
        return R.ok(scheme);
    }

    @GetMapping("/insurance-rules")
    public R<List<PayInsuranceRule>> insuranceRules() {
        return R.ok(insuranceService.list());
    }

    @GetMapping("/tax-brackets")
    public R<List<PayTaxBracket>> taxBrackets() {
        return R.ok(bracketService.list());
    }

    @GetMapping("/periods")
    public R<PageResult<PayPeriod>> periods(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        Page<PayPeriod> result = periodService.page(new Page<>(page, size),
                new LambdaQueryWrapper<PayPeriod>().orderByDesc(PayPeriod::getYearMonth));
        return R.ok(new PageResult<>(result.getTotal(), page, size, result.getRecords()));
    }

    @PostMapping("/periods/open")
    public R<PayPeriod> open(@RequestParam String yearMonth) {
        PayPeriod period = new PayPeriod();
        period.setYearMonth(yearMonth);
        period.setStatus("OPEN");
        period.setAttLocked(1);
        period.setTotalGross(java.math.BigDecimal.ZERO);
        period.setTotalNet(java.math.BigDecimal.ZERO);
        period.setHeadcount(0);
        periodService.save(period);
        return R.ok(period);
    }

    @PostMapping("/periods/{id}/calculate")
    public R<Integer> calculate(@PathVariable Long id) {
        return R.ok(calcService.calculate(id));
    }

    @PostMapping("/periods/{id}/approve")
    public R<Void> approve(@PathVariable Long id) {
        calcService.approve(id, CurrentUser.id());
        return R.ok();
    }

    @PostMapping("/periods/{id}/pay")
    public R<Void> pay(@PathVariable Long id) {
        calcService.pay(id);
        return R.ok();
    }

    @PostMapping("/periods/{id}/close")
    public R<Void> close(@PathVariable Long id) {
        calcService.close(id);
        return R.ok();
    }

    @GetMapping("/slips")
    public R<PageResult<PaySlip>> slips(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) Long periodId,
            @RequestParam(required = false) Long deptId) {
        Page<PaySlip> result = slipService.page(new Page<>(page, size),
                new LambdaQueryWrapper<PaySlip>()
                        .eq(periodId != null, PaySlip::getPeriodId, periodId)
                        .eq(deptId != null, PaySlip::getDeptId, deptId)
                        .orderByDesc(PaySlip::getId));
        return R.ok(new PageResult<>(result.getTotal(), page, size, result.getRecords()));
    }

    @PutMapping("/slips/{id}/adjust")
    public R<PaySlip> adjust(@PathVariable Long id,
                             @Valid @RequestBody PayrollAdjustmentRequest request) {
        PaySlip slip = slipService.getById(id);
        PayAdjustment adjustment = new PayAdjustment();
        adjustment.setPeriodId(slip.getPeriodId());
        adjustment.setEmployeeId(slip.getEmployeeId());
        adjustment.setItemCode(request.getItemCode());
        adjustment.setAmount(request.getAmount());
        adjustment.setReason(request.getReason());
        adjustmentService.save(adjustment);
        calcService.calculate(slip.getPeriodId());
        return R.ok(slipService.getById(id));
    }

    @GetMapping("/slips/mine")
    public R<List<PaySlip>> mine() {
        List<PaySlip> slips = slipService.lambdaQuery()
                .eq(PaySlip::getEmployeeId, employeeId())
                .in(PaySlip::getStatus, "PAID", "CONFIRMED")
                .orderByDesc(PaySlip::getId)
                .list();
        for (PaySlip slip : slips) {
            slip.setViewedAt(LocalDateTime.now());
            slipService.updateById(slip);
        }
        return R.ok(slips);
    }

    @GetMapping("/periods/{id}/export")
    public void export(@PathVariable Long id, HttpServletResponse response) throws Exception {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=payroll-" + id + ".csv");
        StringBuilder csv = new StringBuilder("\uFEFF员工ID,部门ID,应发,应税收入,个税,"
                + "个人社保,个人公积金,实发,状态\n");
        for (PaySlip slip : slipService.lambdaQuery().eq(PaySlip::getPeriodId, id)
                .orderByAsc(PaySlip::getEmployeeId).list()) {
            csv.append(slip.getEmployeeId()).append(',').append(slip.getDeptId()).append(',')
                    .append(slip.getGross()).append(',').append(slip.getTaxableIncome()).append(',')
                    .append(slip.getTax()).append(',').append(slip.getSiPersonal()).append(',')
                    .append(slip.getHfPersonal()).append(',').append(slip.getNet()).append(',')
                    .append(slip.getStatus()).append('\n');
        }
        response.getWriter().write(csv.toString());
    }

    @GetMapping("/reports/cost")
    public R<List<PayrollCostRow>> cost(@RequestParam int year) {
        return R.ok(jdbc.query("SELECT p.year_month, s.dept_id, SUM(s.gross) gross, "
                        + "SUM(s.si_company) + SUM(s.hf_company) company_insurance, "
                        + "SUM(s.gross) + SUM(s.si_company) + SUM(s.hf_company) total_cost "
                        + "FROM pay_slip s JOIN pay_period p ON p.id = s.period_id "
                        + "WHERE p.year_month LIKE ? GROUP BY p.year_month, s.dept_id "
                        + "ORDER BY p.year_month, s.dept_id",
                new Object[]{year + "-%"}, (result, rowNum) -> {
                    PayrollCostRow row = new PayrollCostRow();
                    row.setYearMonth(result.getString("year_month"));
                    row.setDeptId(result.getObject("dept_id", Long.class));
                    row.setGross(result.getBigDecimal("gross"));
                    row.setCompanyInsurance(result.getBigDecimal("company_insurance"));
                    row.setTotalCost(result.getBigDecimal("total_cost"));
                    return row;
                }));
    }

    private Long employeeId() {
        return jdbc.query("SELECT employee_id FROM sys_user WHERE id = ?",
                new Object[]{CurrentUser.id()},
                result -> result.next() ? result.getLong(1) : null);
    }
}
