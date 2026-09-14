package com.oa.collab.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("oa_message")
public class OaMessage extends BaseEntity {
    private Long toUserId;
    private String type;
    private String title;
    private String content;
    private String link;
    private LocalDateTime readAt;
}
