package com.oa.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("att_leave_balance")
public class AttLeaveBalance extends BaseEntity {
    private Long employeeId;
    @TableField("`year`")
    private Integer year;
    private String leaveType;
    private BigDecimal totalDays;
    private BigDecimal usedDays;
}
