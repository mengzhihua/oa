package com.oa.oauth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("oauth_token")
public class OauthToken extends BaseEntity {
    private String accessToken;
    private String refreshToken;
    private String clientId;
    private Long userId;
    private String scope;
    private LocalDateTime accessExpiresAt;
    private LocalDateTime refreshExpiresAt;
    private Integer revoked;
}
