package com.oa.collab.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.collab.entity.OaExpense;
import com.oa.collab.mapper.OaExpenseMapper;
import org.springframework.stereotype.Service;

@Service
public class OaExpenseService extends ServiceImpl<OaExpenseMapper, OaExpense> {
}
