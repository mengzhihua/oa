package com.oa.collab.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.collab.entity.OaMessage;
import com.oa.collab.mapper.OaMessageMapper;
import org.springframework.stereotype.Service;

@Service
public class OaMessageService extends ServiceImpl<OaMessageMapper, OaMessage> {
}
