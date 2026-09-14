package com.oa.oauth.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.oauth.entity.OauthToken;
import com.oa.oauth.mapper.OauthTokenMapper;
import org.springframework.stereotype.Service;

@Service
public class OauthTokenService extends ServiceImpl<OauthTokenMapper, OauthToken> {
}
