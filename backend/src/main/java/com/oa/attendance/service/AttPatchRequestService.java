package com.oa.attendance.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.attendance.entity.AttPatchRequest;
import com.oa.attendance.mapper.AttPatchRequestMapper;
import org.springframework.stereotype.Service;

@Service
public class AttPatchRequestService extends ServiceImpl<AttPatchRequestMapper, AttPatchRequest> {
}
