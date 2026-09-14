package com.oa;

import com.oa.common.BizException;
import com.oa.payroll.entity.PayPeriod;
import com.oa.payroll.entity.PayScheme;
import com.oa.payroll.service.PayPeriodService;
import com.oa.payroll.service.PaySchemeService;
import com.oa.payroll.service.PayrollCalcService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
public class PayrollCalcTest {
    @Autowired
    private PayrollCalcService payrollCalcService;

    @Autowired
    private PayPeriodService periodService;

    @Autowired
    private PaySchemeService schemeService;

    @Test
    public void 考勤未锁定时拒绝计算() {
        PayPeriod period = new PayPeriod();
        period.setYearMonth("2099-01");
        period.setStatus("OPEN");
        period.setAttLocked(0);
        period.setTotalGross(BigDecimal.ZERO);
        period.setTotalNet(BigDecimal.ZERO);
        periodService.save(period);
        assertThrows(BizException.class, () -> payrollCalcService.calculate(period.getId()));
    }

    @Test
    public void 基本工资方案金额可持久化() {
        PayScheme scheme = schemeService.lambdaQuery()
                .eq(PayScheme::getEmployeeId, 1L).one();
        if (scheme == null) {
            scheme = new PayScheme();
            scheme.setEmployeeId(1L);
            scheme.setEffectiveDate(java.time.LocalDate.of(2026, 1, 1));
            scheme.setBaseSalary(new BigDecimal("12000.00"));
            scheme.setPostSalary(new BigDecimal("3000.00"));
            scheme.setPerfSalary(new BigDecimal("3000.00"));
            scheme.setSiBase(new BigDecimal("15000.00"));
            scheme.setHfBase(new BigDecimal("15000.00"));
            scheme.setStatus("ACTIVE");
            schemeService.save(scheme);
        }
        assertEquals(new BigDecimal("12000.00"), scheme.getBaseSalary());
        // 月薪算式：基本工资 + 岗位工资 + 绩效工资 + 补贴；具体扣款由工资计算服务按考勤和规则计算。
    }
}
