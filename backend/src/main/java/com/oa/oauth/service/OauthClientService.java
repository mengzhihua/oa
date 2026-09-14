package com.oa.oauth.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.oauth.entity.OauthClient;
import com.oa.oauth.mapper.OauthClientMapper;
import org.springframework.stereotype.Service;

@Service
public class OauthClientService extends ServiceImpl<OauthClientMapper, OauthClient> {
}
