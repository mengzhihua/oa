package com.oa.workflow.callback;

public interface WorkflowCallback {
    String businessType();

    void completed(Long instanceId, String status);

    default boolean supports(String type) {
        return businessType().equals(type);
    }
}
