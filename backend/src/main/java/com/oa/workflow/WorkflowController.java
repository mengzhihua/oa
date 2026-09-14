package com.oa.workflow;
import com.oa.common.*;import com.oa.system.auth.CurrentUser;import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestController @RequestMapping("/api/workflow")
public class WorkflowController {
 private final JdbcTemplate jdbc;private final WorkflowService service;public WorkflowController(JdbcTemplate j,WorkflowService s){jdbc=j;service=s;}
 @GetMapping("/definitions")public R<List<Map<String,Object>>> defs(){return R.ok(jdbc.queryForList("SELECT * FROM wf_definition WHERE status=1 ORDER BY id"));}
 @GetMapping("/instances")public R<PageResult<Map<String,Object>>> instances(@RequestParam(defaultValue="1")long page,@RequestParam(defaultValue="20")long size){List<Map<String,Object>>rs=jdbc.queryForList("SELECT * FROM wf_instance WHERE applicant_id=? ORDER BY id DESC",CurrentUser.id());return R.ok(new PageResult<Map<String,Object>>(rs.size(),page,size,rs));}
 @GetMapping("/instances/{id}")public R<Map<String,Object>> detail(@PathVariable Long id){Map<String,Object>i=jdbc.queryForMap("SELECT * FROM wf_instance WHERE id=?",id);i.put("tasks",jdbc.queryForList("SELECT * FROM wf_task WHERE instance_id=? ORDER BY node_seq,id",id));return R.ok(i);}
 @GetMapping("/tasks/todo")public R<List<Map<String,Object>>> todo(){return R.ok(jdbc.queryForList("SELECT * FROM wf_task WHERE approver_user_id=? AND status='PENDING' ORDER BY id",CurrentUser.id()));}
 @GetMapping("/tasks/done")public R<List<Map<String,Object>>> done(){return R.ok(jdbc.queryForList("SELECT * FROM wf_task WHERE approver_user_id=? AND status<>'PENDING' ORDER BY id DESC",CurrentUser.id()));}
 @PostMapping("/tasks/{id}/approve")public R<Void> approve(@PathVariable Long id,@RequestBody(required=false)Map<String,String>b){service.handle(id,CurrentUser.id(),"APPROVED",b==null?null:b.get("comment"));return R.ok();}
 @PostMapping("/tasks/{id}/reject")public R<Void> reject(@PathVariable Long id,@RequestBody(required=false)Map<String,String>b){service.handle(id,CurrentUser.id(),"REJECTED",b==null?null:b.get("comment"));return R.ok();}
 @PostMapping("/tasks/{id}/transfer")public R<Void> transfer(@PathVariable Long id,@RequestBody Map<String,Object>b){jdbc.update("UPDATE wf_task SET approver_user_id=?,status='TRANSFERRED' WHERE id=? AND approver_user_id=?",b.get("toUserId"),id,CurrentUser.id());return R.ok();}
 @PostMapping("/instances/{id}/cancel")public R<Void> cancel(@PathVariable Long id){jdbc.update("UPDATE wf_instance SET status='CANCELED' WHERE id=? AND applicant_id=? AND status='RUNNING'",id,CurrentUser.id());return R.ok();}
 @PostMapping("/instances/start")public R<Map<String,Object>> start(@RequestBody Map<String,Object>b){return R.ok(service.start(String.valueOf(b.getOrDefault("businessType","GENERAL")),CurrentUser.id(),String.valueOf(b.get("title")),b.get("form")==null?Collections.<String,Object>emptyMap():(Map<String,Object>)b.get("form"),"GENERAL",null));}
}
