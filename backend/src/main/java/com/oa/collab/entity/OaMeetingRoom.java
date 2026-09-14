package com.oa.collab.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

@Data
@TableName("oa_meeting_room")
public class OaMeetingRoom extends BaseEntity {
    private String name;
    private String location;
    private Integer capacity;
    private String equipment;
    private String status;
}
