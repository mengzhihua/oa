package com.oa.collab.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("oa_schedule")
public class OaSchedule extends BaseEntity {
    private Long userId;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private String participantsJson;
    private Integer remindMinutes;
}
