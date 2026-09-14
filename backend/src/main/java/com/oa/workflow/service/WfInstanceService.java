package com.oa.workflow.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.workflow.entity.WfInstance;
import com.oa.workflow.mapper.WfInstanceMapper;
import com.oa.workflow.vo.WorkflowInstanceView;
import org.springframework.stereotype.Service;

@Service
public class WfInstanceService extends ServiceImpl<WfInstanceMapper, WfInstance> {
    public Page<WorkflowInstanceView> pageByApplicant(long page, long size, Long applicantId) {
        return baseMapper.selectApplicantPage(new Page<>(page, size), applicantId);
    }
}
