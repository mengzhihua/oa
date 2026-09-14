package com.oa.oauth.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.oauth.entity.OauthAuthorizationCode;
import com.oa.oauth.mapper.OauthAuthorizationCodeMapper;
import org.springframework.stereotype.Service;

@Service
public class OauthAuthorizationCodeService
        extends ServiceImpl<OauthAuthorizationCodeMapper, OauthAuthorizationCode> {
}
