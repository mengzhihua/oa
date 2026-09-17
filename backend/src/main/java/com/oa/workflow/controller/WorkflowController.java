package com.oa.workflow.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oa.common.PageResult;
import com.oa.common.R;
import com.oa.common.BizException;
import com.oa.system.auth.CurrentUser;
import com.oa.workflow.dto.StartWorkflowRequest;
import com.oa.workflow.dto.TaskActionRequest;
import com.oa.workflow.entity.WfDefinition;
import com.oa.workflow.entity.WfTask;
import com.oa.workflow.service.WfDefinitionService;
import com.oa.workflow.service.WfTaskService;
import com.oa.workflow.vo.WorkflowInstanceView;
import com.oa.workflow.vo.WorkflowTaskView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.oa.workflow.service.WorkflowService;

@Validated
@RestController
@RequestMapping("/api/workflow")
public class WorkflowController {
    private final JdbcTemplate jdbc;
    private final WorkflowService workflowService;
    private final WfDefinitionService definitionService;
    private final WfTaskService taskService;
    private final com.oa.workflow.service.WfInstanceService instanceService;

    public WorkflowController(JdbcTemplate jdbc, WorkflowService workflowService,
                              WfDefinitionService definitionService,
                              WfTaskService taskService,
                              com.oa.workflow.service.WfInstanceService instanceService) {
        this.jdbc = jdbc;
        this.workflowService = workflowService;
        this.definitionService = definitionService;
        this.taskService = taskService;
        this.instanceService = instanceService;
    }

    @GetMapping("/definitions")
    public R<List<WfDefinition>> definitions() {
        return R.ok(definitionService.list(new LambdaQueryWrapper<WfDefinition>()
                .eq(WfDefinition::getStatus, 1)
                .orderByAsc(WfDefinition::getId)));
    }

    @GetMapping("/instances")
    public R<PageResult<WorkflowInstanceView>> instances(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<WorkflowInstanceView> result =
                instanceService.pageByApplicant(page, size, CurrentUser.id());
        return R.ok(new PageResult<>(result.getTotal(), page, size, result.getRecords()));
    }

    @GetMapping("/instances/{id}")
    public R<WorkflowInstanceView> detail(@PathVariable Long id) {
        Map<String, Object> row = jdbc.queryForMap("SELECT * FROM wf_instance WHERE id = ?", id);
        if (!CurrentUser.get().getRoles().contains("ADMIN")
                && !CurrentUser.get().getRoles().contains("HR")
                && !visibleToCurrentUser(id, row)) {
            throw new com.oa.common.BizException(403, "无权查看该流程");
        }
        WorkflowInstanceView view = toInstance(row);
        view.setTasks(taskService.list(new LambdaQueryWrapper<WfTask>()
                        .eq(WfTask::getInstanceId, id)
                        .orderByAsc(WfTask::getNodeSeq)
                        .orderByAsc(WfTask::getId))
                .stream().map(this::toTask).collect(Collectors.toList()));
        return R.ok(view);
    }

    private boolean visibleToCurrentUser(Long instanceId, Map<String, Object> instance) {
        Long applicantId = number(instance, "APPLICANT_ID");
        if (CurrentUser.id().equals(applicantId)) {
            return true;
        }
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM wf_task "
                        + "WHERE instance_id = ? AND approver_user_id = ?",
                Integer.class, instanceId, CurrentUser.id());
        return count != null && count > 0;
    }

    @GetMapping("/tasks/todo")
    public R<List<WorkflowTaskView>> todo() {
        return R.ok(taskService.list(new LambdaQueryWrapper<WfTask>()
                        .eq(WfTask::getApproverUserId, CurrentUser.id())
                        .eq(WfTask::getStatus, "PENDING")
                        .orderByAsc(WfTask::getId))
                .stream().map(this::toTask).collect(Collectors.toList()));
    }

    @GetMapping("/tasks/done")
    public R<List<WorkflowTaskView>> done() {
        return R.ok(taskService.list(new LambdaQueryWrapper<WfTask>()
                        .eq(WfTask::getApproverUserId, CurrentUser.id())
                        .ne(WfTask::getStatus, "PENDING")
                        .orderByDesc(WfTask::getId))
                .stream().map(this::toTask).collect(Collectors.toList()));
    }

    @PostMapping("/tasks/{id}/approve")
    public R<Void> approve(@PathVariable Long id,
                           @RequestBody(required = false) TaskActionRequest request) {
        workflowService.approve(id, CurrentUser.id(), request == null ? null : request.getComment());
        return R.ok();
    }

    @PostMapping("/tasks/{id}/reject")
    public R<Void> reject(@PathVariable Long id,
                          @RequestBody(required = false) TaskActionRequest request) {
        workflowService.reject(id, CurrentUser.id(), request == null ? null : request.getComment());
        return R.ok();
    }

    @PostMapping("/tasks/{id}/transfer")
    public R<Void> transfer(@PathVariable Long id,
                            @Valid @RequestBody TaskActionRequest request) {
        workflowService.transfer(id, CurrentUser.id(), request.getToUserId());
        return R.ok();
    }

    @PostMapping("/instances/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id) {
        workflowService.cancel(id, CurrentUser.id());
        return R.ok();
    }

    @PostMapping("/instances/start")
    public R<WorkflowInstanceView> start(@Valid @RequestBody StartWorkflowRequest request) {
        if (!"GENERAL".equals(request.getDefinitionCode())
                || (request.getBusinessType() != null
                && !request.getBusinessType().trim().isEmpty()
                && !"GENERAL".equals(request.getBusinessType()))
                || (request.getBusinessId() != null
                && !request.getBusinessId().trim().isEmpty())) {
            throw new BizException("通用流程仅允许 GENERAL，业务编号必须为空");
        }
        Map<String, Object> instance = workflowService.start(
                request.getDefinitionCode(),
                CurrentUser.id(),
                request.getTitle(),
                request.getForm(),
                "GENERAL",
                null);
        return R.ok(toInstance(instance));
    }

    private WorkflowInstanceView toInstance(Map<String, Object> row) {
        WorkflowInstanceView view = new WorkflowInstanceView();
        view.setId(number(row, "ID"));
        view.setInstanceNo(text(row, "INSTANCE_NO"));
        view.setDefinitionId(number(row, "DEFINITION_ID"));
        view.setTitle(text(row, "TITLE"));
        view.setFormJson(text(row, "FORM_JSON"));
        view.setBusinessType(text(row, "BUSINESS_TYPE"));
        view.setBusinessId(text(row, "BUSINESS_ID"));
        view.setApplicantId(number(row, "APPLICANT_ID"));
        view.setStatus(text(row, "STATUS"));
        view.setCurrentNodeSeq(integer(row, "CURRENT_NODE_SEQ"));
        view.setSubmittedAt(dateTime(row, "SUBMITTED_AT"));
        view.setFinishedAt(dateTime(row, "FINISHED_AT"));
        return view;
    }

    private WorkflowTaskView toTask(WfTask task) {
        WorkflowTaskView view = new WorkflowTaskView();
        view.setId(task.getId());
        view.setInstanceId(task.getInstanceId());
        view.setNodeSeq(task.getNodeSeq());
        view.setApproverUserId(task.getApproverUserId());
        if (task.getApproverUserId() != null) {
            view.setApproverName(jdbc.queryForObject(
                    "SELECT real_name FROM sys_user WHERE id = ?", String.class,
                    task.getApproverUserId()));
        }
        view.setStatus(task.getStatus());
        view.setComment(task.getComment());
        view.setHandledAt(task.getHandledAt());
        return view;
    }

    private static String text(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private static Long number(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? null : ((Number) value).longValue();
    }

    private static Integer integer(Map<String, Object> row, String key) {
        Long value = number(row, key);
        return value == null ? null : value.intValue();
    }

    private static LocalDateTime dateTime(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime();
        }
        return value instanceof LocalDateTime ? (LocalDateTime) value : null;
    }
}
