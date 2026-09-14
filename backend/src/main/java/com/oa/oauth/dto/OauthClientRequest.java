package com.oa.oauth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class OauthClientRequest {
    private Long id;

    @NotBlank
    private String clientId;

    private String clientSecret;

    @NotBlank
    private String clientName;

    @NotBlank
    private String redirectUris;

    private String grantTypes;

    private String scopes;

    private Integer accessTokenTtl;

    private Integer refreshTokenTtl;

    private Integer status;
}
