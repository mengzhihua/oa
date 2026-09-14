package com.oa.hr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.hr.entity.HrEmployee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HrEmployeeMapper extends BaseMapper<HrEmployee> {
}
