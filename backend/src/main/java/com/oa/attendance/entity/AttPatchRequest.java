package com.oa.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("att_patch_request")
public class AttPatchRequest extends BaseEntity {
    private Long employeeId;
    private LocalDate workDate;
    private String clockType;
    private LocalDateTime clockTime;
    private String reason;
    private String status;
    private Long wfInstanceId;
}
