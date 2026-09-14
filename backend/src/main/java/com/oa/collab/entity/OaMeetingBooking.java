package com.oa.collab.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("oa_meeting_booking")
public class OaMeetingBooking extends BaseEntity {
    private Long roomId;
    private Long organizer;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String attendees;
    private String status;
}
