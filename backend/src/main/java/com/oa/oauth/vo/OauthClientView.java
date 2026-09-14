package com.oa.oauth.vo;

import lombok.Data;

@Data
public class OauthClientView {
    private Long id;
    private String clientId;
    private String clientName;
    private String redirectUris;
    private String grantTypes;
    private String scopes;
    private Integer accessTokenTtl;
    private Integer refreshTokenTtl;
    private Integer status;
}
