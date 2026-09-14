package com.oa.attendance.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.attendance.entity.AttSchedule;
import com.oa.attendance.mapper.AttScheduleMapper;
import org.springframework.stereotype.Service;

@Service
public class AttScheduleService extends ServiceImpl<AttScheduleMapper, AttSchedule> {
}
