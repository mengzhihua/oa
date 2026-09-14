package com.oa.org;
import com.oa.common.*;import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestController @RequestMapping("/api/org")
public class OrgController {
 private final JdbcTemplate jdbc;public OrgController(JdbcTemplate j){jdbc=j;}
 @GetMapping("/depts/tree")public R<List<Map<String,Object>>> tree(){return R.ok(jdbc.queryForList("SELECT * FROM org_dept WHERE status=1 ORDER BY parent_id,sort,id"));}
 @GetMapping("/depts")public R<List<Map<String,Object>>> depts(){return tree();}
 @RequestMapping(value="/depts",method={RequestMethod.POST,RequestMethod.PUT})public R<Void> dept(@RequestBody Map<String,Object>b){if(b.get("id")==null)jdbc.update("INSERT INTO org_dept(parent_id,name,code,sort,status,path) VALUES(?,?,?,?,1,?)",b.get("parentId"),b.get("name"),b.get("code"),b.getOrDefault("sort",0),b.get("path"));else jdbc.update("UPDATE org_dept SET parent_id=?,name=?,code=?,sort=?,status=? WHERE id=?",b.get("parentId"),b.get("name"),b.get("code"),b.get("sort"),b.getOrDefault("status",1),b.get("id"));return R.ok();}
 @DeleteMapping("/depts/{id}")public R<Void> deptDel(@PathVariable Long id){if(jdbc.queryForObject("SELECT COUNT(*) FROM org_dept WHERE parent_id=?",Integer.class,id)>0)throw new BizException("部门存在子部门，不能删除");if(jdbc.queryForObject("SELECT COUNT(*) FROM hr_employee WHERE dept_id=? AND employment_status NOT IN ('LEFT','LEAVING')",Integer.class,id)>0)throw new BizException("部门存在在职员工，不能删除");jdbc.update("DELETE FROM org_dept WHERE id=?",id);return R.ok();}
 @GetMapping("/positions")public R<List<Map<String,Object>>> positions(){return R.ok(jdbc.queryForList("SELECT * FROM org_position ORDER BY id"));}
 @RequestMapping(value="/positions",method={RequestMethod.POST,RequestMethod.PUT})public R<Void> position(@RequestBody Map<String,Object>b){if(b.get("id")==null)jdbc.update("INSERT INTO org_position(code,name,level,dept_id) VALUES(?,?,?,?)",b.get("code"),b.get("name"),b.get("level"),b.get("deptId"));else jdbc.update("UPDATE org_position SET code=?,name=?,level=?,dept_id=? WHERE id=?",b.get("code"),b.get("name"),b.get("level"),b.get("deptId"),b.get("id"));return R.ok();}
 @GetMapping("/grades")public R<List<Map<String,Object>>> grades(){return R.ok(jdbc.queryForList("SELECT * FROM org_job_grade ORDER BY level"));}
 @RequestMapping(value="/grades",method={RequestMethod.POST,RequestMethod.PUT})public R<Void> grade(@RequestBody Map<String,Object>b){if(b.get("id")==null)jdbc.update("INSERT INTO org_job_grade(code,name,level) VALUES(?,?,?)",b.get("code"),b.get("name"),b.get("level"));else jdbc.update("UPDATE org_job_grade SET code=?,name=?,level=? WHERE id=?",b.get("code"),b.get("name"),b.get("level"),b.get("id"));return R.ok();}
}
