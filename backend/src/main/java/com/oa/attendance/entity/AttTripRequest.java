package com.oa.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("att_trip_request")
public class AttTripRequest extends BaseEntity {
    private Long employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String destination;
    private BigDecimal days;
    private String reason;
    private String status;
    private Long wfInstanceId;
}
