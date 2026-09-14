package com.oa.org.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.org.entity.OrgDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrgDeptMapper extends BaseMapper<OrgDept> {
    @Select("SELECT COUNT(*) FROM hr_employee WHERE dept_id = #{deptId} "
            + "AND employment_status NOT IN ('LEFT', 'LEAVING')")
    Long countEmployees(Long deptId);
}
