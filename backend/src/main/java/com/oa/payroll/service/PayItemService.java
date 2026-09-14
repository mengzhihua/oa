package com.oa.payroll.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.payroll.entity.PayItem;
import com.oa.payroll.mapper.PayItemMapper;
import org.springframework.stereotype.Service;

@Service
public class PayItemService extends ServiceImpl<PayItemMapper, PayItem> {
}
