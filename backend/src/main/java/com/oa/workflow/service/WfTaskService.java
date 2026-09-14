package com.oa.workflow.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.workflow.entity.WfTask;
import com.oa.workflow.mapper.WfTaskMapper;
import org.springframework.stereotype.Service;

@Service
public class WfTaskService extends ServiceImpl<WfTaskMapper, WfTask> {
}
