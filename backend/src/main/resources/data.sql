MERGE INTO sys_role (code, name) KEY (code) VALUES
    ('ADMIN', '系统管理员'),
    ('HR', '人力资源'),
    ('FINANCE', '财务'),
    ('MANAGER', '部门主管'),
    ('EMPLOYEE', '普通员工');

MERGE INTO org_dept (code, name, parent_id, sort, status, path) KEY (code) VALUES
    ('HQ', '集团总部', NULL, 0, 1, '/HQ');
MERGE INTO org_dept (code, name, parent_id, sort, status, path)
    KEY (code)
    SELECT 'RD', '研发部', id, 1, 1, '/HQ/RD' FROM org_dept WHERE code = 'HQ';
MERGE INTO org_dept (code, name, parent_id, sort, status, path)
    KEY (code)
    SELECT 'HR', '人力资源部', id, 2, 1, '/HQ/HR' FROM org_dept WHERE code = 'HQ';
MERGE INTO org_dept (code, name, parent_id, sort, status, path)
    KEY (code)
    SELECT 'FIN', '财务部', id, 3, 1, '/HQ/FIN' FROM org_dept WHERE code = 'HQ';
MERGE INTO org_dept (code, name, parent_id, sort, status, path)
    KEY (code)
    SELECT 'MKT', '市场部', id, 4, 1, '/HQ/MKT' FROM org_dept WHERE code = 'HQ';
MERGE INTO org_dept (code, name, parent_id, sort, status, path)
    KEY (code)
    SELECT 'ADM', '行政部', id, 5, 1, '/HQ/ADM' FROM org_dept WHERE code = 'HQ';

MERGE INTO org_position (code, name, level, dept_id) KEY (code)
    SELECT 'DEV', '研发工程师', 1, id FROM org_dept WHERE code = 'RD';
MERGE INTO org_position (code, name, level, dept_id) KEY (code)
    SELECT 'DEV_MANAGER', '研发主管', 3, id FROM org_dept WHERE code = 'RD';
MERGE INTO org_position (code, name, level, dept_id) KEY (code)
    SELECT 'HR_SPECIALIST', '人事专员', 2, id FROM org_dept WHERE code = 'HR';
MERGE INTO org_position (code, name, level, dept_id) KEY (code)
    SELECT 'FIN_SPECIALIST', '财务专员', 2, id FROM org_dept WHERE code = 'FIN';
MERGE INTO org_position (code, name, level, dept_id) KEY (code)
    SELECT 'MARKETING', '市场专员', 1, id FROM org_dept WHERE code = 'MKT';
MERGE INTO org_position (code, name, level, dept_id) KEY (code)
    SELECT 'ADMIN', '行政专员', 1, id FROM org_dept WHERE code = 'ADM';

MERGE INTO org_job_grade (code, name, level) KEY (code) VALUES
    ('G1', '初级', 1),
    ('G2', '中级', 2),
    ('G3', '高级', 3);

MERGE INTO hr_employee (
    employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type
) KEY (employee_no)
SELECT 'E000001', '张三', '男', '13800000001', 'zhangsan@example.com', d.id, p.id, g.id,
       '2021-03-15', '2021-06-15', 'REGULAR', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'RD' AND p.code = 'DEV' AND g.code = 'G1';
MERGE INTO hr_employee (
    employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type
) KEY (employee_no)
SELECT 'E000002', '李主管', '男', '13800000002', 'manager@example.com', d.id, p.id, g.id,
       '2019-07-01', '2019-10-01', 'REGULAR', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'RD' AND p.code = 'DEV_MANAGER' AND g.code = 'G3';
MERGE INTO hr_employee (
    employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type
) KEY (employee_no)
SELECT 'E000003', '王人事', '女', '13800000003', 'hr@example.com', d.id, p.id, g.id,
       '2020-11-20', '2021-02-20', 'REGULAR', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'HR' AND p.code = 'HR_SPECIALIST' AND g.code = 'G2';
MERGE INTO hr_employee (
    employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type
) KEY (employee_no)
SELECT 'E000004', '赵财务', '女', '13800000004', 'finance@example.com', d.id, p.id, g.id,
       '2021-06-10', '2021-09-10', 'REGULAR', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'FIN' AND p.code = 'FIN_SPECIALIST' AND g.code = 'G2';
MERGE INTO hr_employee (employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type)
KEY (employee_no)
SELECT 'E000005', '陈市场', '男', '13800000005', 'chen@example.com', d.id, p.id, g.id,
       '2022-04-18', '2022-07-18', 'REGULAR', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'MKT' AND p.code = 'MARKETING' AND g.code = 'G1'
    AND NOT EXISTS (SELECT 1 FROM hr_employee WHERE employee_no = 'E000005');
MERGE INTO hr_employee (employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type)
KEY (employee_no)
SELECT 'E000006', '周行政', '女', '13800000006', 'zhou@example.com', d.id, p.id, g.id,
       '2023-09-05', '2023-12-05', 'REGULAR', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'ADM' AND p.code = 'ADMIN' AND g.code = 'G1'
    AND NOT EXISTS (SELECT 1 FROM hr_employee WHERE employee_no = 'E000006');
MERGE INTO hr_employee (employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type)
KEY (employee_no)
SELECT 'E000007', '刘研发', '男', '13800000007', 'liu@example.com', d.id, p.id, g.id,
       '2026-07-15', '2026-09-15', 'PROBATION', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'RD' AND p.code = 'DEV' AND g.code = 'G1'
    AND NOT EXISTS (SELECT 1 FROM hr_employee WHERE employee_no = 'E000007');
MERGE INTO hr_employee (employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type)
KEY (employee_no)
SELECT 'E000008', '孙研发', '女', '13800000008', 'sun@example.com', d.id, p.id, g.id,
       '2020-02-10', '2020-05-10', 'REGULAR', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'RD' AND p.code = 'DEV' AND g.code = 'G2'
    AND NOT EXISTS (SELECT 1 FROM hr_employee WHERE employee_no = 'E000008');
MERGE INTO hr_employee (employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type)
KEY (employee_no)
SELECT 'E000009', '吴财务', '男', '13800000009', 'wu@example.com', d.id, p.id, g.id,
       '2024-01-08', '2024-04-08', 'REGULAR', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'FIN' AND p.code = 'FIN_SPECIALIST' AND g.code = 'G1'
    AND NOT EXISTS (SELECT 1 FROM hr_employee WHERE employee_no = 'E000009');
MERGE INTO hr_employee (employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type)
KEY (employee_no)
SELECT 'E000010', '郑市场', '女', '13800000010', 'zheng@example.com', d.id, p.id, g.id,
       '2022-10-12', '2023-01-12', 'REGULAR', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'MKT' AND p.code = 'MARKETING' AND g.code = 'G2'
    AND NOT EXISTS (SELECT 1 FROM hr_employee WHERE employee_no = 'E000010');
MERGE INTO hr_employee (employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type)
KEY (employee_no)
SELECT 'E000011', '何行政', '男', '13800000011', 'he@example.com', d.id, p.id, g.id,
       '2025-03-03', '2025-06-03', 'REGULAR', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'ADM' AND p.code = 'ADMIN' AND g.code = 'G2'
    AND NOT EXISTS (SELECT 1 FROM hr_employee WHERE employee_no = 'E000011');
MERGE INTO hr_employee (employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type)
KEY (employee_no)
SELECT 'E000012', '高人事', '女', '13800000012', 'gao@example.com', d.id, p.id, g.id,
       '2019-12-16', '2020-03-16', 'REGULAR', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'HR' AND p.code = 'HR_SPECIALIST' AND g.code = 'G1'
    AND NOT EXISTS (SELECT 1 FROM hr_employee WHERE employee_no = 'E000012');

MERGE INTO oauth_client (
    client_id, client_secret_hash, client_name, redirect_uris, grant_types, scopes,
    access_token_ttl, refresh_token_ttl, status
) KEY (client_id) VALUES
    ('sap-client', 'placeholder', 'SAP系统', 'http://localhost:5175/sso/callback',
     'authorization_code,refresh_token,client_credentials,password',
     'openid profile email phone roles', 7200, 2592000, 1),
    ('srm-client', 'placeholder', 'SRM系统', 'http://localhost:5174/sso/callback',
     'authorization_code,refresh_token', 'openid profile email phone roles', 7200, 2592000, 1),
    ('crm-client', 'placeholder', 'CRM系统', 'http://localhost:5173/sso/callback',
     'authorization_code,refresh_token', 'openid profile email phone roles', 7200, 2592000, 1);

MERGE INTO wf_definition (code, name, form_schema_json, status, version) KEY (code) VALUES
    ('LEAVE', '请假申请', '{"fields":["days","reason"]}', 1, 1),
    ('OVERTIME', '加班申请', '{"fields":["hours","reason"]}', 1, 1),
    ('PATCH_CLOCK', '补卡申请', '{"fields":["date","reason"]}', 1, 1),
    ('BUSINESS_TRIP', '出差申请', '{"fields":["days","destination"]}', 1, 1),
    ('EXPENSE', '报销申请', '{"fields":["amount","reason"]}', 1, 1),
    ('GENERAL', '通用申请', '{"fields":["content"]}', 1, 1);

MERGE INTO sys_menu (code, name, path, parent_id, sort, icon, type, status) KEY (code) VALUES
    ('SYSTEM', '系统管理', '/system', NULL, 10, '设置', 'DIRECTORY', 1),
    ('SYSTEM_USER', '用户管理', '/system/users', NULL, 11, '用户', 'MENU', 1),
    ('SYSTEM_ROLE', '角色管理', '/system/roles', NULL, 12, '角色', 'MENU', 1),
    ('ORG', '组织架构', '/org', NULL, 20, '组织', 'DIRECTORY', 1),
    ('HR', '员工管理', '/hr', NULL, 30, '人员', 'DIRECTORY', 1),
    ('WORKFLOW', '审批中心', '/workflow', NULL, 40, '审批', 'DIRECTORY', 1),
    ('OAUTH', '单点登录', '/oauth', NULL, 50, '链接', 'MENU', 1);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r CROSS JOIN sys_menu m
WHERE r.code = 'ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu x WHERE x.role_id = r.id AND x.menu_id = m.id
  );

INSERT INTO wf_node (definition_id, seq, name, approver_type, multi_mode, condition_json)
SELECT d.id, 1, '部门主管', 'DEPT_LEADER', 'ANY', NULL
FROM wf_definition d
WHERE NOT EXISTS (
    SELECT 1 FROM wf_node n WHERE n.definition_id = d.id AND n.seq = 1
);
INSERT INTO wf_node (definition_id, seq, name, approver_type, multi_mode, condition_json)
SELECT d.id, 2, '人力资源', 'HR', 'ANY', '{"field":"days","op":">","value":3}'
FROM wf_definition d
WHERE d.code IN ('LEAVE', 'BUSINESS_TRIP')
  AND NOT EXISTS (
      SELECT 1 FROM wf_node n WHERE n.definition_id = d.id AND n.seq = 2
  );
INSERT INTO wf_node (definition_id, seq, name, approver_type, multi_mode, condition_json)
SELECT d.id, 2, '财务审核', 'FINANCE', 'ALL', NULL
FROM wf_definition d
WHERE d.code = 'EXPENSE'
  AND NOT EXISTS (
      SELECT 1 FROM wf_node n WHERE n.definition_id = d.id AND n.seq = 2
  );

MERGE INTO att_shift (
    code, name, work_start, work_end, rest_start, rest_end,
    late_grace_minutes, early_grace_minutes, is_default
) KEY (code) VALUES
    ('STANDARD', '标准班', '09:00:00', '18:00:00', '12:00:00', '13:00:00', 10, 10, 1),
    ('FLEXIBLE', '弹性班', '10:00:00', '19:00:00', '12:00:00', '13:00:00', 10, 10, 0);

MERGE INTO att_holiday (holiday_date, name, type) KEY (holiday_date) VALUES
    ('2026-01-01', '元旦', 'HOLIDAY'),
    ('2026-02-17', '春节', 'HOLIDAY'),
    ('2026-02-18', '春节', 'HOLIDAY'),
    ('2026-02-19', '春节', 'HOLIDAY'),
    ('2026-04-04', '清明节', 'HOLIDAY'),
    ('2026-05-01', '劳动节', 'HOLIDAY'),
    ('2026-06-19', '端午节', 'HOLIDAY'),
    ('2026-09-25', '中秋节', 'HOLIDAY'),
    ('2026-10-01', '国庆节', 'HOLIDAY'),
    ('2026-10-02', '国庆节', 'HOLIDAY'),
    ('2026-10-03', '国庆节', 'HOLIDAY'),
    ('2026-10-04', '国庆节', 'HOLIDAY'),
    ('2026-10-05', '国庆节', 'HOLIDAY'),
    ('2026-10-06', '国庆节', 'HOLIDAY'),
    ('2026-10-07', '国庆节', 'HOLIDAY'),
    ('2026-02-14', '春节调休', 'WORKDAY'),
    ('2026-02-28', '春节调休', 'WORKDAY'),
    ('2026-05-09', '劳动节调休', 'WORKDAY'),
    ('2026-09-27', '国庆调休', 'WORKDAY');

INSERT INTO att_leave_balance (
    employee_id, `year`, leave_type, total_days, used_days
)
SELECT e.id, 2026, 'ANNUAL',
       CASE WHEN e.id <= 2 THEN 15 WHEN e.id <= 4 THEN 10 ELSE 5 END,
       0
FROM hr_employee e
WHERE NOT EXISTS (
    SELECT 1
    FROM att_leave_balance b
    WHERE b.employee_id = e.id
      AND b.`year` = 2026
      AND b.leave_type = 'ANNUAL'
);

MERGE INTO att_clock_record (
    employee_id, clock_time, clock_type, source, device, remark
) KEY (employee_id, clock_time, clock_type) VALUES
    (1, '2026-09-11 09:05:00', 'IN', 'WEB', '演示设备', '演示迟到'),
    (1, '2026-09-11 18:02:00', 'OUT', 'WEB', '演示设备', NULL),
    (2, '2026-09-11 08:58:00', 'IN', 'WEB', '演示设备', NULL),
    (2, '2026-09-11 18:00:00', 'OUT', 'WEB', '演示设备', NULL),
    (3, '2026-09-12 09:00:00', 'IN', 'APP', '演示设备', NULL);

MERGE INTO pay_item (
    code, name, type, calc_type, formula, taxable, sort, is_system
) KEY (code) VALUES
    ('BASE', '基本工资', 'EARNING', 'FIXED', NULL, 1, 10, 1),
    ('POST', '岗位工资', 'EARNING', 'FIXED', NULL, 1, 20, 1),
    ('PERF', '绩效工资', 'EARNING', 'FIXED', NULL, 1, 30, 1),
    ('MEAL', '餐补', 'EARNING', 'FIXED', NULL, 1, 40, 1),
    ('TRAFFIC', '交通补贴', 'EARNING', 'FIXED', NULL, 1, 50, 1),
    ('OT_PAY', '加班费', 'EARNING', 'SYSTEM', NULL, 1, 60, 1),
    ('LATE_DED', '迟到扣款', 'DEDUCTION', 'SYSTEM', NULL, 0, 70, 1),
    ('ABSENT_DED', '缺勤扣款', 'DEDUCTION', 'SYSTEM', NULL, 0, 80, 1),
    ('LEAVE_DED', '请假扣款', 'DEDUCTION', 'SYSTEM', NULL, 0, 90, 1),
    ('SI_PERSONAL', '社保个人', 'DEDUCTION', 'SYSTEM', NULL, 0, 100, 1),
    ('HF_PERSONAL', '公积金个人', 'DEDUCTION', 'SYSTEM', NULL, 0, 110, 1),
    ('TAX', '个人所得税', 'DEDUCTION', 'SYSTEM', NULL, 0, 120, 1),
    ('SI_COMPANY', '社保单位', 'COMPANY_COST', 'SYSTEM', NULL, 0, 130, 1),
    ('HF_COMPANY', '公积金单位', 'COMPANY_COST', 'SYSTEM', NULL, 0, 140, 1);

MERGE INTO pay_insurance_rule (
    city, pension_p, pension_c, medical_p, medical_c, unemployment_p,
    unemployment_c, injury_c, maternity_c, hf_p, hf_c,
    si_base_min, si_base_max, hf_base_min, hf_base_max
) KEY (city) VALUES
    ('北京', 0.08, 0.16, 0.02, 0.09, 0.005, 0.005, 0.002, 0.008,
     0.12, 0.12, 5869, 31884, 2320, 31884);

MERGE INTO pay_tax_bracket (
    level_no, lower_bound, upper_bound, rate, quick_deduction
) KEY (level_no) VALUES
    (1, 0, 36000, 0.03, 0),
    (2, 36000.01, 144000, 0.10, 2520),
    (3, 144000.01, 300000, 0.20, 16920),
    (4, 300000.01, 420000, 0.25, 31920),
    (5, 420000.01, 660000, 0.30, 52920),
    (6, 660000.01, 960000, 0.35, 85920),
    (7, 960000.01, NULL, 0.45, 181920);

INSERT INTO pay_scheme (
    employee_id, effective_date, base_salary, post_salary, perf_salary,
    allowances_json, si_base, hf_base, status
)
SELECT e.id, '2026-01-01', 12000, 3000, 3000,
       '{"meal":500,"traffic":300}', 15000, 15000, 'ACTIVE'
FROM hr_employee e
WHERE NOT EXISTS (
    SELECT 1 FROM pay_scheme s WHERE s.employee_id = e.id
);

MERGE INTO pay_period (
    year_month, status, att_locked, calc_at, paid_at,
    total_gross, total_net, headcount
) KEY (year_month) VALUES
    ('2026-08', 'OPEN', 1, NULL, NULL, 0, 0, 0);

MERGE INTO oa_meeting_room (name, location, capacity, equipment, status) KEY (name) VALUES
    ('1号会议室', '总部一层', 10, '白板、投影', 'ACTIVE'),
    ('2号会议室', '总部二层', 20, '投影、音响', 'ACTIVE'),
    ('多媒体厅', '总部三层', 50, '大屏、音响、视频会议', 'ACTIVE');
