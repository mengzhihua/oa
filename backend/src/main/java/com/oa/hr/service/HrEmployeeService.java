package com.oa.hr.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.hr.entity.HrEmployee;
import com.oa.hr.mapper.HrEmployeeMapper;
import com.oa.hr.vo.EmployeeRow;
import org.springframework.stereotype.Service;

@Service
public class HrEmployeeService extends ServiceImpl<HrEmployeeMapper, HrEmployee> {
    public Page<EmployeeRow> pageRows(long page, long size, String keyword, String status) {
        return baseMapper.selectPageRows(new Page<>(page, size), keyword == null ? "" : keyword,
                status);
    }
}
