INSERT IGNORE INTO hr_employee (
    employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type
)
SELECT 'E000001', '张三', '男', '13800000001', 'zhangsan@example.com', d.id, p.id, g.id,
       '2021-03-15', '2021-06-15', 'REGULAR', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'RD' AND p.code = 'DEV' AND g.code = 'G1';

INSERT IGNORE INTO hr_employee (
    employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type
)
SELECT 'E000002', '李主管', '男', '13800000002', 'manager@example.com', d.id, p.id, g.id,
       '2019-07-01', '2019-10-01', 'REGULAR', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'RD' AND p.code = 'DEV_MANAGER' AND g.code = 'G3';

INSERT IGNORE INTO hr_employee (
    employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type
)
SELECT 'E000003', '王人事', '女', '13800000003', 'hr@example.com', d.id, p.id, g.id,
       '2020-11-20', '2021-02-20', 'REGULAR', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'HR' AND p.code = 'HR_SPECIALIST' AND g.code = 'G2';

INSERT IGNORE INTO hr_employee (
    employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type
)
SELECT 'E000004', '赵财务', '女', '13800000004', 'finance@example.com', d.id, p.id, g.id,
       '2021-06-10', '2021-09-10', 'REGULAR', 'FULLTIME'
FROM org_dept d, org_position p, org_job_grade g
WHERE d.code = 'FIN' AND p.code = 'FIN_SPECIALIST' AND g.code = 'G2';

INSERT IGNORE INTO hr_employee (
    employee_no, name, gender, mobile, email, dept_id, position_id, grade_id,
    hire_date, regular_date, employment_status, employee_type
)
SELECT employee_no, name, gender, mobile, email, d.id, p.id, g.id,
       CASE employee_no WHEN 'E000005' THEN '2022-04-18' WHEN 'E000006' THEN '2023-09-05'
                        WHEN 'E000007' THEN '2026-07-15' WHEN 'E000008' THEN '2020-02-10'
                        WHEN 'E000009' THEN '2024-01-08' WHEN 'E000010' THEN '2022-10-12'
                        WHEN 'E000011' THEN '2025-03-03' WHEN 'E000012' THEN '2019-12-16' END,
       CASE employee_no WHEN 'E000005' THEN '2022-07-18' WHEN 'E000006' THEN '2023-12-05'
                        WHEN 'E000007' THEN '2026-09-15' WHEN 'E000008' THEN '2020-05-10'
                        WHEN 'E000009' THEN '2024-04-08' WHEN 'E000010' THEN '2023-01-12'
                        WHEN 'E000011' THEN '2025-06-03' WHEN 'E000012' THEN '2020-03-16' END,
       employment_status, employee_type
FROM (
    SELECT 'E000005' employee_no, '陈市场' name, '男' gender, '13800000005' mobile,
           'chen@example.com' email, 'MKT' dept_code, 'MARKETING' position_code,
           'G1' grade_code, 'REGULAR' employment_status, 'FULLTIME' employee_type
    UNION ALL SELECT 'E000006', '周行政', '女', '13800000006', 'zhou@example.com',
           'ADM', 'ADMIN', 'G1', 'REGULAR', 'FULLTIME'
    UNION ALL SELECT 'E000007', '刘研发', '男', '13800000007', 'liu@example.com',
           'RD', 'DEV', 'G1', 'PROBATION', 'FULLTIME'
    UNION ALL SELECT 'E000008', '孙研发', '女', '13800000008', 'sun@example.com',
           'RD', 'DEV', 'G2', 'REGULAR', 'FULLTIME'
    UNION ALL SELECT 'E000009', '吴财务', '男', '13800000009', 'wu@example.com',
           'FIN', 'FIN_SPECIALIST', 'G1', 'REGULAR', 'FULLTIME'
    UNION ALL SELECT 'E000010', '郑市场', '女', '13800000010', 'zheng@example.com',
           'MKT', 'MARKETING', 'G2', 'REGULAR', 'FULLTIME'
    UNION ALL SELECT 'E000011', '何行政', '男', '13800000011', 'he@example.com',
           'ADM', 'ADMIN', 'G2', 'REGULAR', 'FULLTIME'
    UNION ALL SELECT 'E000012', '高人事', '女', '13800000012', 'gao@example.com',
           'HR', 'HR_SPECIALIST', 'G1', 'REGULAR', 'FULLTIME'
) seed
JOIN org_dept d ON d.code = seed.dept_code
JOIN org_position p ON p.code = seed.position_code
JOIN org_job_grade g ON g.code = seed.grade_code;

INSERT IGNORE INTO oauth_client (
    client_id, client_secret_hash, client_name, redirect_uris, grant_types, scopes,
    access_token_ttl, refresh_token_ttl, status
) VALUES
    ('sap-client', 'placeholder', 'SAP系统', 'http://localhost:5175/sso/callback',
     'authorization_code,refresh_token,client_credentials,password',
     'openid profile email phone roles', 7200, 2592000, 1),
    ('srm-client', 'placeholder', 'SRM系统', 'http://localhost:5174/sso/callback',
     'authorization_code,refresh_token', 'openid profile email phone roles', 7200, 2592000, 1),
    ('crm-client', 'placeholder', 'CRM系统', 'http://localhost:5173/sso/callback',
     'authorization_code,refresh_token', 'openid profile email phone roles', 7200, 2592000, 1);

INSERT IGNORE INTO att_leave_balance (employee_id, `year`, leave_type, total_days, used_days)
SELECT e.id, 2026, 'ANNUAL',
       CASE WHEN e.id <= 2 THEN 15 WHEN e.id <= 4 THEN 10 ELSE 5 END, 0
FROM hr_employee e;

INSERT IGNORE INTO att_clock_record (
    employee_id, clock_time, clock_type, source, device, remark
) VALUES
    (1, '2026-09-11 09:05:00', 'IN', 'WEB', '演示设备', '演示迟到'),
    (1, '2026-09-11 18:02:00', 'OUT', 'WEB', '演示设备', NULL),
    (2, '2026-09-11 08:58:00', 'IN', 'WEB', '演示设备', NULL),
    (2, '2026-09-11 18:00:00', 'OUT', 'WEB', '演示设备', NULL),
    (3, '2026-09-12 09:00:00', 'IN', 'APP', '演示设备', NULL);

INSERT IGNORE INTO pay_scheme (
    employee_id, effective_date, base_salary, post_salary, perf_salary,
    allowances_json, si_base, hf_base, status
)
SELECT e.id, '2026-01-01', 12000, 3000, 3000,
       '{"meal":500,"traffic":300}', 15000, 15000, 'ACTIVE'
FROM hr_employee e
WHERE NOT EXISTS (SELECT 1 FROM pay_scheme s WHERE s.employee_id = e.id);

INSERT IGNORE INTO pay_period (
    year_month, status, att_locked, calc_at, paid_at,
    total_gross, total_net, headcount
) VALUES ('2026-08', 'OPEN', 1, NULL, NULL, 0, 0, 0);

INSERT IGNORE INTO oa_meeting_room (name, location, capacity, equipment, status) VALUES
    ('1号会议室', '总部一层', 10, '白板、投影', 'ACTIVE'),
    ('2号会议室', '总部二层', 20, '投影、音响', 'ACTIVE'),
    ('多媒体厅', '总部三层', 50, '大屏、音响、视频会议', 'ACTIVE');
