package com.oa.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;

@Data
@TableName("att_shift")
public class AttShift extends BaseEntity {
    @NotBlank(message = "班次编码不能为空")
    private String code;
    @NotBlank(message = "班次名称不能为空")
    private String name;
    @NotNull(message = "上班时间不能为空")
    private LocalTime workStart;
    @NotNull(message = "下班时间不能为空")
    private LocalTime workEnd;
    private LocalTime restStart;
    private LocalTime restEnd;
    private Integer lateGraceMinutes;
    private Integer earlyGraceMinutes;
    private Integer isDefault;
}
