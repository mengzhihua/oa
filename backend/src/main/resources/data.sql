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
       CURRENT_DATE, CURRENT_DATE, 'REGULAR', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'RD' AND p.code = 'DEV' AND g.code = 'G1';
MERGE INTO hr_employee (
    employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type
) KEY (employee_no)
SELECT 'E000002', '李主管', '男', '13800000002', 'manager@example.com', d.id, p.id, g.id,
       CURRENT_DATE, CURRENT_DATE, 'REGULAR', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'RD' AND p.code = 'DEV_MANAGER' AND g.code = 'G3';
MERGE INTO hr_employee (
    employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type
) KEY (employee_no)
SELECT 'E000003', '王人事', '女', '13800000003', 'hr@example.com', d.id, p.id, g.id,
       CURRENT_DATE, CURRENT_DATE, 'REGULAR', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'HR' AND p.code = 'HR_SPECIALIST' AND g.code = 'G2';
MERGE INTO hr_employee (
    employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type
) KEY (employee_no)
SELECT 'E000004', '赵财务', '女', '13800000004', 'finance@example.com', d.id, p.id, g.id,
       CURRENT_DATE, CURRENT_DATE, 'REGULAR', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'FIN' AND p.code = 'FIN_SPECIALIST' AND g.code = 'G2';
MERGE INTO hr_employee (employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type)
KEY (employee_no)
SELECT 'E000005', '陈市场', '男', '13800000005', 'chen@example.com', d.id, p.id, g.id,
       CURRENT_DATE, CURRENT_DATE, 'REGULAR', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'MKT' AND p.code = 'MARKETING' AND g.code = 'G1'
    AND NOT EXISTS (SELECT 1 FROM hr_employee WHERE employee_no = 'E000005');
MERGE INTO hr_employee (employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type)
KEY (employee_no)
SELECT 'E000006', '周行政', '女', '13800000006', 'zhou@example.com', d.id, p.id, g.id,
       CURRENT_DATE, CURRENT_DATE, 'REGULAR', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'ADM' AND p.code = 'ADMIN' AND g.code = 'G1'
    AND NOT EXISTS (SELECT 1 FROM hr_employee WHERE employee_no = 'E000006');
MERGE INTO hr_employee (employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type)
KEY (employee_no)
SELECT 'E000007', '刘研发', '男', '13800000007', 'liu@example.com', d.id, p.id, g.id,
       CURRENT_DATE, CURRENT_DATE, 'PROBATION', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'RD' AND p.code = 'DEV' AND g.code = 'G1'
    AND NOT EXISTS (SELECT 1 FROM hr_employee WHERE employee_no = 'E000007');
MERGE INTO hr_employee (employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type)
KEY (employee_no)
SELECT 'E000008', '孙研发', '女', '13800000008', 'sun@example.com', d.id, p.id, g.id,
       CURRENT_DATE, CURRENT_DATE, 'REGULAR', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'RD' AND p.code = 'DEV' AND g.code = 'G2'
    AND NOT EXISTS (SELECT 1 FROM hr_employee WHERE employee_no = 'E000008');
MERGE INTO hr_employee (employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type)
KEY (employee_no)
SELECT 'E000009', '吴财务', '男', '13800000009', 'wu@example.com', d.id, p.id, g.id,
       CURRENT_DATE, CURRENT_DATE, 'REGULAR', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'FIN' AND p.code = 'FIN_SPECIALIST' AND g.code = 'G1'
    AND NOT EXISTS (SELECT 1 FROM hr_employee WHERE employee_no = 'E000009');
MERGE INTO hr_employee (employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type)
KEY (employee_no)
SELECT 'E000010', '郑市场', '女', '13800000010', 'zheng@example.com', d.id, p.id, g.id,
       CURRENT_DATE, CURRENT_DATE, 'REGULAR', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'MKT' AND p.code = 'MARKETING' AND g.code = 'G2'
    AND NOT EXISTS (SELECT 1 FROM hr_employee WHERE employee_no = 'E000010');
MERGE INTO hr_employee (employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type)
KEY (employee_no)
SELECT 'E000011', '何行政', '男', '13800000011', 'he@example.com', d.id, p.id, g.id,
       CURRENT_DATE, CURRENT_DATE, 'REGULAR', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'ADM' AND p.code = 'ADMIN' AND g.code = 'G2'
    AND NOT EXISTS (SELECT 1 FROM hr_employee WHERE employee_no = 'E000011');
MERGE INTO hr_employee (employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type)
KEY (employee_no)
SELECT 'E000012', '高人事', '女', '13800000012', 'gao@example.com', d.id, p.id, g.id,
       CURRENT_DATE, CURRENT_DATE, 'REGULAR', 'FULLTIME'
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

MERGE INTO sys_menu (code, name, path, parent_id, sort, icon) KEY (code) VALUES
    ('SYSTEM', '系统管理', '/system', NULL, 10, '设置'),
    ('SYSTEM_USER', '用户管理', '/system/users', NULL, 11, '用户'),
    ('SYSTEM_ROLE', '角色管理', '/system/roles', NULL, 12, '角色'),
    ('ORG', '组织架构', '/org', NULL, 20, '组织'),
    ('HR', '员工管理', '/hr', NULL, 30, '人员'),
    ('WORKFLOW', '审批中心', '/workflow', NULL, 40, '审批'),
    ('OAUTH', '单点登录', '/oauth', NULL, 50, '链接');

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

MERGE INTO att_leave_balance (
    employee_id, year, leave_type, total_days, used_days
) KEY (employee_id, year, leave_type) VALUES
    (1, 15, 'ANNUAL', 15, 0),
    (2, 15, 'ANNUAL', 15, 0),
    (3, 10, 'ANNUAL', 10, 0),
    (4, 10, 'ANNUAL', 10, 0),
    (5, 5, 'ANNUAL', 5, 0),
    (6, 5, 'ANNUAL', 5, 0),
    (7, 5, 'ANNUAL', 5, 0),
    (8, 5, 'ANNUAL', 5, 0),
    (9, 5, 'ANNUAL', 5, 0),
    (10, 5, 'ANNUAL', 5, 0),
    (11, 5, 'ANNUAL', 5, 0),
    (12, 5, 'ANNUAL', 5, 0);

MERGE INTO att_clock_record (
    employee_id, clock_time, clock_type, source, device, remark
) KEY (employee_id, clock_time, clock_type) VALUES
    (1, '2026-09-11 09:05:00', 'IN', 'WEB', '演示设备', '演示迟到'),
    (1, '2026-09-11 18:02:00', 'OUT', 'WEB', '演示设备', NULL),
    (2, '2026-09-11 08:58:00', 'IN', 'WEB', '演示设备', NULL),
    (2, '2026-09-11 18:00:00', 'OUT', 'WEB', '演示设备', NULL),
    (3, '2026-09-12 09:00:00', 'IN', 'APP', '演示设备', NULL);
