package com.oa.oauth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.oauth.entity.OauthClient;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OauthClientMapper extends BaseMapper<OauthClient> {
}
