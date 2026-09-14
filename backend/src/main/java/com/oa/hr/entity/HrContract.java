package com.oa.hr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("hr_contract")
public class HrContract extends BaseEntity {
    private Long employeeId;
    private String contractNo;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate signDate;
    private String status;
}
