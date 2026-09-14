package com.oa.oauth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.oauth.entity.OauthAuthorizationCode;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OauthAuthorizationCodeMapper extends BaseMapper<OauthAuthorizationCode> {
}
