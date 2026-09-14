package com.oa.collab.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("oa_expense")
public class OaExpense extends BaseEntity {
    private Long applicantId;
    private String title;
    private String itemsJson;
    private BigDecimal total;
    private String attachments;
    private String status;
    private Long wfInstanceId;
}
