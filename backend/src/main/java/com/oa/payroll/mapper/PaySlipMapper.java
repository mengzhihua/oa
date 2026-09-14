package com.oa.payroll.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.payroll.entity.PaySlip;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaySlipMapper extends BaseMapper<PaySlip> {
}
