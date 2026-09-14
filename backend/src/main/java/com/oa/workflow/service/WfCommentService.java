package com.oa.workflow.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.workflow.entity.WfComment;
import com.oa.workflow.mapper.WfCommentMapper;
import org.springframework.stereotype.Service;

@Service
public class WfCommentService extends ServiceImpl<WfCommentMapper, WfComment> {
}
