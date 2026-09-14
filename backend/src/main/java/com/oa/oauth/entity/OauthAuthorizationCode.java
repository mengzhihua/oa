package com.oa.oauth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("oauth_authorization_code")
public class OauthAuthorizationCode extends BaseEntity {
    private String code;
    private String clientId;
    private Long userId;
    private String redirectUri;
    private String scope;
    private String codeChallenge;
    private String codeChallengeMethod;
    private LocalDateTime expiresAt;
    private Integer used;
}
