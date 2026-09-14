package com.oa.payroll.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

@Data
@TableName("pay_item")
public class PayItem extends BaseEntity {
    private String code;
    private String name;
    private String type;
    private String calcType;
    private String formula;
    private Integer taxable;
    private Integer sort;
    private Integer isSystem;
}
