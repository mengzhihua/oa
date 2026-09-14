package com.oa.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.workflow.entity.WfInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.oa.workflow.vo.WorkflowInstanceView;

@Mapper
public interface WfInstanceMapper extends BaseMapper<WfInstance> {
    @Select("SELECT id, instance_no, definition_id, title, form_json, business_type, "
            + "business_id, applicant_id, status, current_node_seq, submitted_at, finished_at "
            + "FROM wf_instance WHERE applicant_id = #{applicantId} ORDER BY id DESC")
    Page<WorkflowInstanceView> selectApplicantPage(Page<WorkflowInstanceView> page,
                                                    @Param("applicantId") Long applicantId);
}
