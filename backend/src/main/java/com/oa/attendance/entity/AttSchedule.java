package com.oa.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("att_schedule")
public class AttSchedule extends BaseEntity {
    private Long employeeId;
    private LocalDate workDate;
    private Long shiftId;
}
