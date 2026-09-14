package com.oa.attendance.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.attendance.entity.AttLeaveRequest;
import com.oa.attendance.mapper.AttLeaveRequestMapper;
import org.springframework.stereotype.Service;

@Service
public class AttLeaveRequestService extends ServiceImpl<AttLeaveRequestMapper, AttLeaveRequest> {
}
