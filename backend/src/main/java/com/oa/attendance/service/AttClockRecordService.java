package com.oa.attendance.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.attendance.entity.AttClockRecord;
import com.oa.attendance.mapper.AttClockRecordMapper;
import org.springframework.stereotype.Service;

@Service
public class AttClockRecordService extends ServiceImpl<AttClockRecordMapper, AttClockRecord> {
}
