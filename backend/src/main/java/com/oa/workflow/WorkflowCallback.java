package com.oa.workflow;

public interface WorkflowCallback {
    String businessType();

    void completed(Long instanceId, String status);
}
