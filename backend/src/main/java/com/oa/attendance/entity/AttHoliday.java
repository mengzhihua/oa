package com.oa.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("att_holiday")
public class AttHoliday extends BaseEntity {
    private LocalDate holidayDate;
    private String name;
    private String type;
}
