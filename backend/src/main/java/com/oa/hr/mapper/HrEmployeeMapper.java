package com.oa.hr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.hr.entity.HrEmployee;
import com.oa.hr.vo.EmployeeRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface HrEmployeeMapper extends BaseMapper<HrEmployee> {
    @Select("SELECT e.id, e.employee_no, e.name, e.gender, e.mobile, e.email, "
            + "e.dept_id, d.name AS dept_name, e.employment_status, e.hire_date "
            + "FROM hr_employee e LEFT JOIN org_dept d ON d.id = e.dept_id "
            + "WHERE (e.name LIKE CONCAT('%', #{keyword}, '%') "
            + "OR e.employee_no LIKE CONCAT('%', #{keyword}, '%')) "
            + "AND (#{status} IS NULL OR e.employment_status = #{status}) "
            + "ORDER BY e.id")
    Page<EmployeeRow> selectPageRows(Page<EmployeeRow> page,
                                     @Param("keyword") String keyword,
                                     @Param("status") String status);
}
