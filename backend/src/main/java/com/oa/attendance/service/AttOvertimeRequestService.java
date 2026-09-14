package com.oa.attendance.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.attendance.entity.AttOvertimeRequest;
import com.oa.attendance.mapper.AttOvertimeRequestMapper;
import org.springframework.stereotype.Service;

@Service
public class AttOvertimeRequestService extends ServiceImpl<AttOvertimeRequestMapper, AttOvertimeRequest> {
}
