package com.oa.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("att_clock_record")
public class AttClockRecord extends BaseEntity {
    private Long employeeId;
    private LocalDateTime clockTime;
    private String clockType;
    private String source;
    private String device;
    private String ip;
    private String remark;
}
