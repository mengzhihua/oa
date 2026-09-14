package com.oa.org;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oa.common.BizException;
import com.oa.common.R;
import com.oa.org.dto.DeptRequest;
import com.oa.org.dto.GradeRequest;
import com.oa.org.dto.PositionRequest;
import com.oa.org.entity.OrgDept;
import com.oa.org.entity.OrgJobGrade;
import com.oa.org.entity.OrgPosition;
import com.oa.org.service.OrgDeptService;
import com.oa.org.service.OrgJobGradeService;
import com.oa.org.service.OrgPositionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/org")
public class OrgController {
    private final OrgDeptService deptService;
    private final OrgPositionService positionService;
    private final OrgJobGradeService gradeService;

    public OrgController(OrgDeptService deptService, OrgPositionService positionService,
                         OrgJobGradeService gradeService) {
        this.deptService = deptService;
        this.positionService = positionService;
        this.gradeService = gradeService;
    }

    @GetMapping("/depts/tree")
    public R<List<OrgDept>> deptTree() {
        return R.ok(deptService.list(new LambdaQueryWrapper<OrgDept>()
                .eq(OrgDept::getStatus, 1)
                .orderByAsc(OrgDept::getParentId)
                .orderByAsc(OrgDept::getSort)));
    }

    @GetMapping("/depts")
    public R<List<OrgDept>> depts() {
        return deptTree();
    }

    @PostMapping("/depts")
    public R<OrgDept> createDept(@Valid @RequestBody DeptRequest request) {
        OrgDept dept = toDept(request);
        deptService.save(dept);
        return R.ok(dept);
    }

    @PutMapping("/depts/{id}")
    public R<OrgDept> updateDept(@PathVariable Long id,
                                 @Valid @RequestBody DeptRequest request) {
        OrgDept dept = toDept(request);
        dept.setId(id);
        deptService.updateById(dept);
        return R.ok(deptService.getById(id));
    }

    @DeleteMapping("/depts/{id}")
    public R<Void> deleteDept(@PathVariable Long id) {
        long children = deptService.count(new LambdaQueryWrapper<OrgDept>()
                .eq(OrgDept::getParentId, id));
        Long employeeCount = deptService.getBaseMapper().countEmployees(id);
        if (children > 0 || employeeCount > 0) {
            throw new BizException("部门存在子部门或在职员工，不能删除");
        }
        deptService.removeById(id);
        return R.ok();
    }

    @GetMapping("/positions")
    public R<List<OrgPosition>> positions() {
        return R.ok(positionService.list(new LambdaQueryWrapper<OrgPosition>()
                .orderByAsc(OrgPosition::getLevel)));
    }

    @PostMapping("/positions")
    public R<OrgPosition> createPosition(@Valid @RequestBody PositionRequest request) {
        OrgPosition position = toPosition(request);
        positionService.save(position);
        return R.ok(position);
    }

    @PutMapping("/positions/{id}")
    public R<OrgPosition> updatePosition(@PathVariable Long id,
                                         @Valid @RequestBody PositionRequest request) {
        OrgPosition position = toPosition(request);
        position.setId(id);
        positionService.updateById(position);
        return R.ok(positionService.getById(id));
    }

    @DeleteMapping("/positions/{id}")
    public R<Void> deletePosition(@PathVariable Long id) {
        positionService.removeById(id);
        return R.ok();
    }

    @GetMapping("/grades")
    public R<List<OrgJobGrade>> grades() {
        return R.ok(gradeService.list(new LambdaQueryWrapper<OrgJobGrade>()
                .orderByAsc(OrgJobGrade::getLevel)));
    }

    @PostMapping("/grades")
    public R<OrgJobGrade> createGrade(@Valid @RequestBody GradeRequest request) {
        OrgJobGrade grade = toGrade(request);
        gradeService.save(grade);
        return R.ok(grade);
    }

    @PutMapping("/grades/{id}")
    public R<OrgJobGrade> updateGrade(@PathVariable Long id,
                                      @Valid @RequestBody GradeRequest request) {
        OrgJobGrade grade = toGrade(request);
        grade.setId(id);
        gradeService.updateById(grade);
        return R.ok(gradeService.getById(id));
    }

    @DeleteMapping("/grades/{id}")
    public R<Void> deleteGrade(@PathVariable Long id) {
        gradeService.removeById(id);
        return R.ok();
    }

    private OrgDept toDept(DeptRequest request) {
        OrgDept dept = new OrgDept();
        dept.setParentId(request.getParentId());
        dept.setName(request.getName());
        dept.setCode(request.getCode());
        dept.setLeaderEmployeeId(request.getLeaderEmployeeId());
        dept.setSort(request.getSort() == null ? 0 : request.getSort());
        dept.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        dept.setPath(request.getPath());
        return dept;
    }

    private OrgPosition toPosition(PositionRequest request) {
        OrgPosition position = new OrgPosition();
        position.setCode(request.getCode());
        position.setName(request.getName());
        position.setLevel(request.getLevel());
        position.setDeptId(request.getDeptId());
        return position;
    }

    private OrgJobGrade toGrade(GradeRequest request) {
        OrgJobGrade grade = new OrgJobGrade();
        grade.setCode(request.getCode());
        grade.setName(request.getName());
        grade.setLevel(request.getLevel());
        return grade;
    }
}
