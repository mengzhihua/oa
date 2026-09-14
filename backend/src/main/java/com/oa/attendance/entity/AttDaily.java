package com.oa.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("att_daily")
public class AttDaily extends BaseEntity {
    private Long employeeId;
    private LocalDate workDate;
    private Long shiftId;
    private LocalDateTime firstIn;
    private LocalDateTime lastOut;
    private String status;
    private Integer lateMinutes;
    private Integer earlyMinutes;
    private Integer workMinutes;
    private Integer overtimeMinutes;
    private String leaveType;
    private String remark;
}
