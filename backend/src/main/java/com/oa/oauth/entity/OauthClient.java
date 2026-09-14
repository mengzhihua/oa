package com.oa.oauth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

@Data
@TableName("oauth_client")
public class OauthClient extends BaseEntity {
    private String clientId;
    private String clientSecretHash;
    private String clientName;
    private String redirectUris;
    private String grantTypes;
    private String scopes;
    private Integer accessTokenTtl;
    private Integer refreshTokenTtl;
    private Integer status;
}
