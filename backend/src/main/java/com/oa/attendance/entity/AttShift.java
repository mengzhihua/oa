package com.oa.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.time.LocalTime;

@Data
@TableName("att_shift")
public class AttShift extends BaseEntity {
    private String code;
    private String name;
    private LocalTime workStart;
    private LocalTime workEnd;
    private LocalTime restStart;
    private LocalTime restEnd;
    private Integer lateGraceMinutes;
    private Integer earlyGraceMinutes;
    private Integer isDefault;
}
