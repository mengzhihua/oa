package com.oa.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("att_overtime_request")
public class AttOvertimeRequest extends BaseEntity {
    private Long employeeId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal hours;
    private String type;
    private String reason;
    private String status;
    private Long wfInstanceId;
}
