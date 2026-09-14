package com.oa.collab.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("oa_notice")
public class OaNotice extends BaseEntity {
    private String title;
    private String content;
    private String type;
    private Long deptId;
    private Long publisher;
    private LocalDateTime publishedAt;
    private Integer pinned;
    private String status;
    private Integer readCount;
}
