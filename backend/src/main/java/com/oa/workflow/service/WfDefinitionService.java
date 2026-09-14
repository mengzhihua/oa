package com.oa.workflow.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.workflow.entity.WfDefinition;
import com.oa.workflow.mapper.WfDefinitionMapper;
import org.springframework.stereotype.Service;

@Service
public class WfDefinitionService extends ServiceImpl<WfDefinitionMapper, WfDefinition> {
}
