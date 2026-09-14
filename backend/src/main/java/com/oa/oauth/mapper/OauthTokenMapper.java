package com.oa.oauth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.oauth.entity.OauthToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OauthTokenMapper extends BaseMapper<OauthToken> {
}
