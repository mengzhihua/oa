package com.oa.payroll.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("pay_tax_bracket")
public class PayTaxBracket extends BaseEntity {
    private Integer levelNo;
    private BigDecimal lowerBound;
    private BigDecimal upperBound;
    private BigDecimal rate;
    private BigDecimal quickDeduction;
}
