package com.oa.workflow.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.workflow.entity.WfInstance;
import com.oa.workflow.mapper.WfInstanceMapper;
import org.springframework.stereotype.Service;

@Service
public class WfInstanceService extends ServiceImpl<WfInstanceMapper, WfInstance> {
}
