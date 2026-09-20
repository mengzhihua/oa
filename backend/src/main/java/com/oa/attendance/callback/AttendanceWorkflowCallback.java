package com.oa.attendance.callback;

import com.oa.attendance.service.AttendanceService;
import com.oa.workflow.callback.WorkflowCallback;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class AttendanceWorkflowCallback implements WorkflowCallback {
    private final AttendanceService attendanceService;

    public AttendanceWorkflowCallback(@Lazy AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @Override
    public String businessType() {
        return "ATTENDANCE";
    }

    @Override
    public boolean supports(String type) {
        return "LEAVE".equals(type) || "OVERTIME".equals(type)
                || "PATCH_CLOCK".equals(type) || "BUSINESS_TRIP".equals(type);
    }

    @Override
    public void completed(Long instanceId, String status) {
        attendanceService.completed(instanceId, status);
    }
}
