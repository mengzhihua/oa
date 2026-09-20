package com.oa.hr.controller;

import com.oa.common.PageResult;
import com.oa.common.R;
import com.oa.hr.dto.EmployeeRequest;
import com.oa.hr.dto.ContractRequest;
import com.oa.hr.dto.LeaveRequest;
import com.oa.hr.dto.TransferRequest;
import com.oa.hr.entity.HrEmployee;
import com.oa.hr.service.HrEmployeeService;
import com.oa.hr.service.HrContractService;
import com.oa.hr.service.HrEmployeeChangeService;
import com.oa.hr.entity.HrEmployeeChange;
import com.oa.hr.entity.HrContract;
import com.oa.hr.vo.EmployeeRow;
import com.oa.hr.vo.ImportResult;
import com.oa.hr.vo.ImportRow;
import com.oa.system.auth.CurrentUser;
import com.oa.system.auth.PasswordHasher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/hr")
public class HrController {
    private final JdbcTemplate jdbc;
    private final HrEmployeeService employeeService;
    private final HrContractService contractService;
    private final HrEmployeeChangeService changeService;

    public HrController(JdbcTemplate jdbc, HrEmployeeService employeeService,
                        HrContractService contractService,
                        HrEmployeeChangeService changeService) {
        this.jdbc = jdbc;
        this.employeeService = employeeService;
        this.contractService = contractService;
        this.changeService = changeService;
    }

    @GetMapping("/employees")
    public R<PageResult<EmployeeRow>> employees(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<EmployeeRow> result =
                employeeService.pageRows(page, size, keyword, status);
        return R.ok(new PageResult<>(result.getTotal(), page, size, result.getRecords()));
    }

    @GetMapping("/employees/{id}")
    public R<HrEmployee> employee(@PathVariable Long id) {
        return R.ok(employeeService.getById(id));
    }

    @PostMapping("/employees")
    public R<HrEmployee> hire(@Valid @RequestBody EmployeeRequest request) {
        HrEmployee employee = toEntity(request);
        employee.setEmployeeNo(nextEmployeeNo());
        employee.setHireDate(LocalDate.now());
        employee.setEmploymentStatus(defaultValue(request.getEmploymentStatus(), "PROBATION"));
        employee.setEmployeeType(defaultValue(request.getEmployeeType(), "FULLTIME"));
        employeeService.save(employee);
        createUser(employee);
        return R.ok(employee);
    }

    @PutMapping("/employees/{id}")
    public R<HrEmployee> update(@PathVariable Long id,
                                @Valid @RequestBody EmployeeRequest request) {
        HrEmployee employee = toEntity(request);
        employee.setId(id);
        employeeService.updateById(employee);
        if (request.getEmploymentStatus() != null
                && !"LEFT".equals(request.getEmploymentStatus())
                && !"LEAVING".equals(request.getEmploymentStatus())) {
            jdbc.update("UPDATE hr_employee SET leave_date = NULL WHERE id = ?", id);
        }
        return R.ok(employeeService.getById(id));
    }

    @PostMapping("/employees/{id}/regular")
    public R<Void> regular(@PathVariable Long id) {
        jdbc.update("UPDATE hr_employee SET employment_status = 'REGULAR', "
                + "regular_date = ?, leave_date = NULL WHERE id = ?", LocalDate.now(), id);
        return R.ok();
    }

    @PostMapping("/employees/{id}/transfer")
    public R<Void> transfer(@PathVariable Long id,
                            @Valid @RequestBody TransferRequest request) {
        HrEmployee before = employeeService.getById(id);
        jdbc.update("UPDATE hr_employee SET dept_id = ?, position_id = ? WHERE id = ?",
                request.getDeptId(), request.getPositionId(), id);
        jdbc.update("INSERT INTO hr_employee_change "
                        + "(employee_id, change_type, before_json, after_json, effective_date, "
                        + "reason, operator) VALUES (?, 'TRANSFER', ?, ?, ?, ?, ?)",
                id, before == null ? null : before.toString(),
                request.toString(), LocalDate.now(), request.getReason(), CurrentUser.id());
        return R.ok();
    }

    @PostMapping("/employees/{id}/leave")
    public R<Void> leave(@PathVariable Long id,
                         @RequestBody(required = false) LeaveRequest request) {
        LocalDate leaveDate = request == null || request.getLeaveDate() == null
                ? LocalDate.now() : request.getLeaveDate();
        String status = leaveDate.isAfter(LocalDate.now()) ? "LEAVING" : "LEFT";
        HrEmployee before = employeeService.getById(id);
        jdbc.update("UPDATE hr_employee SET employment_status = ?, leave_date = ? "
                        + "WHERE id = ?", status, leaveDate, id);
        if ("LEFT".equals(status)) {
            jdbc.update("UPDATE sys_user SET status = 0 WHERE employee_id = ?", id);
        } else {
            jdbc.update("UPDATE sys_user SET status = 1 WHERE employee_id = ?", id);
        }
        jdbc.update("INSERT INTO hr_employee_change "
                        + "(employee_id, change_type, before_json, after_json, effective_date, "
                        + "reason, operator) VALUES (?, 'LEAVE', ?, ?, ?, ?, ?)",
                id, before == null ? null : before.toString(),
                request == null ? null : request.toString(), leaveDate,
                request == null ? null : request.getReason(), CurrentUser.id());
        return R.ok();
    }

    @GetMapping("/contracts")
    public R<List<HrContract>> contracts() {
        return R.ok(contractService.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HrContract>()
                .orderByAsc(HrContract::getEndDate)));
    }

    @GetMapping("/contracts/employee/{employeeId}")
    public R<List<HrContract>> employeeContracts(@PathVariable Long employeeId) {
        return R.ok(contractService.lambdaQuery()
                .eq(HrContract::getEmployeeId, employeeId)
                .orderByAsc(HrContract::getEndDate)
                .list());
    }

    @PostMapping("/contracts")
    public R<HrContract> createContract(@Valid @RequestBody ContractRequest request) {
        HrContract contract = toContract(request);
        contract.setStatus(defaultValue(request.getStatus(), "ACTIVE"));
        contractService.save(contract);
        return R.ok(contract);
    }

    @PutMapping("/contracts/{id}")
    public R<HrContract> updateContract(@PathVariable Long id,
                                        @Valid @RequestBody ContractRequest request) {
        HrContract contract = toContract(request);
        contract.setId(id);
        contractService.updateById(contract);
        return R.ok(contractService.getById(id));
    }

    @GetMapping("/contracts/expiring")
    public R<List<HrContract>> expiring() {
        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(30);
        return R.ok(contractService.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HrContract>()
                .between(HrContract::getEndDate, today, end)
                .orderByAsc(HrContract::getEndDate)));
    }

    @GetMapping("/changes")
    public R<PageResult<HrEmployeeChange>> changes(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) Long employeeId) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<HrEmployeeChange> result =
                changeService.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size),
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HrEmployeeChange>()
                                .eq(employeeId != null, HrEmployeeChange::getEmployeeId, employeeId)
                                .orderByDesc(HrEmployeeChange::getId));
        return R.ok(new PageResult<>(result.getTotal(), page, size, result.getRecords()));
    }

    @PostMapping("/employees/import")
    public R<ImportResult> importEmployees(@RequestParam MultipartFile file) throws IOException {
        ImportResult result = new ImportResult();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int rowNumber = 0;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (header) {
                    header = false;
                    continue;
                }
                if (line.trim().isEmpty()) {
                    continue;
                }
                List<String> values = parseCsv(line.replace("\uFEFF", ""));
                if (values.size() < 2 || values.get(0).trim().isEmpty()
                        || values.get(1).trim().isEmpty()) {
                    result.getRows().add(new ImportRow(rowNumber, false, null,
                            "工号和姓名不能为空"));
                    result.setFailureCount(result.getFailureCount() + 1);
                    continue;
                }
                try {
                    jdbc.update("MERGE INTO hr_employee "
                                    + "(employee_no, name, mobile, email, employment_status, "
                                    + "employee_type, hire_date) KEY(employee_no) "
                                    + "VALUES (?, ?, ?, ?, 'PROBATION', 'FULLTIME', ?)",
                            values.get(0).trim(), values.get(1).trim(),
                            values.size() > 2 ? values.get(2).trim() : null,
                            values.size() > 3 ? values.get(3).trim() : null, LocalDate.now());
                    result.getRows().add(new ImportRow(rowNumber, true, values.get(0).trim(),
                            "导入成功"));
                    result.setSuccessCount(result.getSuccessCount() + 1);
                } catch (Exception exception) {
                    result.getRows().add(new ImportRow(rowNumber, false, values.get(0).trim(),
                            "导入失败：" + exception.getMessage()));
                    result.setFailureCount(result.getFailureCount() + 1);
                }
            }
        }
        return R.ok(result);
    }

    @GetMapping("/employees/export")
    public void export(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=employees.csv");
        response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                response.getOutputStream(), StandardCharsets.UTF_8))) {
            writer.println("工号,姓名,手机号,邮箱,状态");
            jdbc.query("SELECT employee_no, name, mobile, email, employment_status "
                            + "FROM hr_employee ORDER BY id",
                    (RowCallbackHandler) result -> writer.printf("%s,%s,%s,%s,%s%n",
                            result.getString("employee_no"), result.getString("name"),
                            result.getString("mobile"), result.getString("email"),
                            result.getString("employment_status")));
        }
    }

    private HrEmployee toEntity(EmployeeRequest request) {
        HrEmployee employee = new HrEmployee();
        employee.setName(request.getName());
        employee.setGender(request.getGender());
        employee.setIdCard(request.getIdCard());
        employee.setMobile(request.getMobile());
        employee.setEmail(request.getEmail());
        employee.setDeptId(request.getDeptId());
        employee.setPositionId(request.getPositionId());
        employee.setGradeId(request.getGradeId());
        employee.setEmploymentStatus(request.getEmploymentStatus());
        employee.setEmployeeType(request.getEmployeeType());
        employee.setEducation(request.getEducation());
        employee.setAddress(request.getAddress());
        employee.setEmergencyContact(request.getEmergencyContact());
        employee.setRemark(request.getRemark());
        return employee;
    }

    private HrContract toContract(ContractRequest request) {
        HrContract contract = new HrContract();
        contract.setEmployeeId(request.getEmployeeId());
        contract.setContractNo(request.getContractNo());
        contract.setType(request.getType());
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setSignDate(request.getSignDate());
        contract.setStatus(request.getStatus());
        return contract;
    }

    private void createUser(HrEmployee employee) {
        String username = employee.getEmployeeNo();
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM sys_user WHERE username = ?",
                Integer.class, username);
        if (count != null && count > 0) {
            return;
        }
        jdbc.update("INSERT INTO sys_user (username, password_hash, real_name, employee_id, "
                        + "status) VALUES (?, ?, ?, ?, 1)",
                username, PasswordHasher.hash(username.substring(Math.max(0, username.length() - 6))),
                employee.getName(), employee.getId());
        Long userId = jdbc.queryForObject("SELECT id FROM sys_user WHERE username = ?",
                Long.class, username);
        Long roleId = jdbc.queryForObject("SELECT id FROM sys_role WHERE code = 'EMPLOYEE'",
                Long.class);
        jdbc.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (?, ?)",
                userId, roleId);
        Integer leader = jdbc.queryForObject(
                "SELECT COUNT(*) FROM org_dept WHERE leader_employee_id = ?",
                Integer.class, employee.getId());
        if (leader != null && leader > 0) {
            Long managerRoleId = jdbc.queryForObject(
                    "SELECT id FROM sys_role WHERE code = 'MANAGER'", Long.class);
            jdbc.update("INSERT INTO sys_user_role (user_id, role_id) "
                            + "SELECT ?, ? WHERE NOT EXISTS "
                            + "(SELECT 1 FROM sys_user_role WHERE user_id = ? AND role_id = ?)",
                    userId, managerRoleId, userId, managerRoleId);
        }
    }

    private String nextEmployeeNo() {
        Integer next = jdbc.queryForObject(
                "SELECT COALESCE(MAX(CAST(SUBSTRING(employee_no, 2) AS INT)), 0) + 1 "
                        + "FROM hr_employee", Integer.class);
        return String.format("E%06d", next == null ? 1 : next);
    }

    private static String defaultValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private static List<String> parseCsv(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);
            if (current == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (current == ',' && !quoted) {
                result.add(field.toString());
                field.setLength(0);
            } else {
                field.append(current);
            }
        }
        result.add(field.toString());
        return result;
    }
}
