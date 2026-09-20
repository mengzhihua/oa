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
