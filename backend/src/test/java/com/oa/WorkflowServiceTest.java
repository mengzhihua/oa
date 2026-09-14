package com.oa;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import com.oa.workflow.WorkflowService;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class WorkflowServiceTest {
    @Autowired private WorkflowService workflowService;
    @Autowired private JdbcTemplate jdbc;
    @Test public void 条件节点与部门主管节点可解析() {
        Map<String,Object> form = new HashMap<String,Object>();
        form.put("days", 1);
        Map<String,Object> first = workflowService.start("LEAVE", 5L, "一天请假", form, "LEAVE", "T1");
        assertEquals("RUNNING", first.get("STATUS"));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM wf_task WHERE instance_id=?", Integer.class, first.get("ID")).intValue());
        form.put("days", 5);
        Map<String,Object> second = workflowService.start("LEAVE", 5L, "五天请假", form, "LEAVE", "T2");
        Long taskId = jdbc.queryForObject("SELECT id FROM wf_task WHERE instance_id=? AND status='PENDING'", Long.class, second.get("ID"));
        workflowService.handle(taskId, 4L, "APPROVED", "同意");
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM wf_task WHERE instance_id=?", Integer.class, second.get("ID")).intValue());
    }
}
