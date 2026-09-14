package com.oa.collab;

import com.oa.collab.entity.OaExpense;
import com.oa.collab.service.OaExpenseService;
import com.oa.workflow.WorkflowCallback;
import org.springframework.stereotype.Component;

@Component
public class CollabWorkflowCallback implements WorkflowCallback {
    private final OaExpenseService expenseService;

    public CollabWorkflowCallback(OaExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @Override
    public String businessType() {
        return "EXPENSE";
    }

    @Override
    public void completed(Long instanceId, String status) {
        OaExpense expense = expenseService.lambdaQuery()
                .eq(OaExpense::getWfInstanceId, instanceId).one();
        if (expense != null) {
            expense.setStatus("APPROVED".equals(status) ? "APPROVED" : status);
            expenseService.updateById(expense);
        }
    }
}
