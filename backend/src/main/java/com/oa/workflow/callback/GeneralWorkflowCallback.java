package com.oa.workflow.callback;

import org.springframework.stereotype.Component;

@Component
public class GeneralWorkflowCallback implements WorkflowCallback {
    @Override
    public String businessType() {
        return "GENERAL";
    }

    @Override
    public void completed(Long instanceId, String status) {
    }
}
