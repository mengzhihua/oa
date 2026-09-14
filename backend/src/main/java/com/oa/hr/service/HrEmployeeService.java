package com.oa.hr.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.hr.entity.HrEmployee;
import com.oa.hr.mapper.HrEmployeeMapper;
import org.springframework.stereotype.Service;

@Service
public class HrEmployeeService extends ServiceImpl<HrEmployeeMapper, HrEmployee> {
}
