package com.oa.attendance.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.attendance.entity.AttTripRequest;
import com.oa.attendance.mapper.AttTripRequestMapper;
import org.springframework.stereotype.Service;

@Service
public class AttTripRequestService extends ServiceImpl<AttTripRequestMapper, AttTripRequest> {
}
