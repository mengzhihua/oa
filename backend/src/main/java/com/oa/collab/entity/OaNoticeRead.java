package com.oa.collab.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("oa_notice_read")
public class OaNoticeRead extends BaseEntity {
    private Long noticeId;
    private Long userId;
    private LocalDateTime readAt;
}
