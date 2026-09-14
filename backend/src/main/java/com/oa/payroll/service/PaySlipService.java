package com.oa.payroll.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.payroll.entity.PaySlip;
import com.oa.payroll.mapper.PaySlipMapper;
import org.springframework.stereotype.Service;

@Service
public class PaySlipService extends ServiceImpl<PaySlipMapper, PaySlip> {
}
