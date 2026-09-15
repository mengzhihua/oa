CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    username VARCHAR(64) UNIQUE NOT NULL,
    password_hash VARCHAR(512) NOT NULL,
    real_name VARCHAR(128),
    employee_id BIGINT,
    status INT DEFAULT 1,
    last_login_at TIMESTAMP
);
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    code VARCHAR(32) UNIQUE NOT NULL,
    name VARCHAR(128) NOT NULL
);
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE (user_id, role_id)
);
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    code VARCHAR(64) UNIQUE NOT NULL,
    name VARCHAR(128),
    path VARCHAR(255),
    parent_id BIGINT,
    sort INT DEFAULT 0,
    icon VARCHAR(64),
    type VARCHAR(32) DEFAULT 'MENU',
    status INT DEFAULT 1
);
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    UNIQUE (role_id, menu_id)
);
CREATE TABLE IF NOT EXISTS sys_op_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    username VARCHAR(64),
    method VARCHAR(16),
    path VARCHAR(255),
    request_body TEXT,
    response_body TEXT
);
CREATE TABLE IF NOT EXISTS sys_dict (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    type VARCHAR(64),
    code VARCHAR(64),
    label VARCHAR(128),
    sort INT DEFAULT 0
);
CREATE TABLE IF NOT EXISTS org_dept (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    parent_id BIGINT,
    name VARCHAR(128),
    code VARCHAR(64) UNIQUE,
    leader_employee_id BIGINT,
    sort INT DEFAULT 0,
    status INT DEFAULT 1,
    path VARCHAR(512)
);
CREATE TABLE IF NOT EXISTS org_position (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    code VARCHAR(64) UNIQUE,
    name VARCHAR(128),
    level INT,
    dept_id BIGINT
);
CREATE TABLE IF NOT EXISTS org_job_grade (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    code VARCHAR(64) UNIQUE,
    name VARCHAR(128),
    level INT
);
CREATE TABLE IF NOT EXISTS hr_employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    employee_no VARCHAR(32) UNIQUE,
    name VARCHAR(128),
    gender VARCHAR(16),
    id_card VARCHAR(64),
    birthday DATE,
    mobile VARCHAR(32),
    email VARCHAR(128),
    dept_id BIGINT,
    position_id BIGINT,
    grade_id BIGINT,
    hire_date DATE,
    regular_date DATE,
    employment_status VARCHAR(32),
    employee_type VARCHAR(32),
    education VARCHAR(64),
    address VARCHAR(255),
    emergency_contact VARCHAR(128),
    bank_name VARCHAR(128),
    bank_account VARCHAR(128),
    social_security_base DECIMAL(12, 2),
    housing_fund_base DECIMAL(12, 2),
    leave_date DATE,
    remark VARCHAR(512)
);
CREATE TABLE IF NOT EXISTS hr_contract (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    employee_id BIGINT,
    contract_no VARCHAR(64),
    type VARCHAR(32),
    start_date DATE,
    end_date DATE,
    sign_date DATE,
    status VARCHAR(32)
);
CREATE TABLE IF NOT EXISTS hr_employee_change (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    employee_id BIGINT,
    change_type VARCHAR(32),
    before_json TEXT,
    after_json TEXT,
    effective_date DATE,
    reason VARCHAR(512),
    operator BIGINT
);
CREATE TABLE IF NOT EXISTS oauth_client (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    client_id VARCHAR(64) UNIQUE,
    client_secret_hash VARCHAR(512),
    client_name VARCHAR(128),
    redirect_uris VARCHAR(1024),
    grant_types VARCHAR(255),
    scopes VARCHAR(255),
    access_token_ttl INT DEFAULT 7200,
    refresh_token_ttl INT DEFAULT 2592000,
    status INT DEFAULT 1
);
CREATE TABLE IF NOT EXISTS oauth_authorization_code (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    code VARCHAR(128) UNIQUE,
    client_id VARCHAR(64),
    user_id BIGINT,
    redirect_uri VARCHAR(512),
    scope VARCHAR(255),
    code_challenge VARCHAR(255),
    code_challenge_method VARCHAR(32),
    expires_at TIMESTAMP,
    used INT DEFAULT 0
);
CREATE TABLE IF NOT EXISTS oauth_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    access_token VARCHAR(512) UNIQUE,
    refresh_token VARCHAR(512) UNIQUE,
    client_id VARCHAR(64),
    user_id BIGINT,
    scope VARCHAR(255),
    access_expires_at TIMESTAMP,
    refresh_expires_at TIMESTAMP,
    revoked INT DEFAULT 0
);
CREATE TABLE IF NOT EXISTS wf_definition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    code VARCHAR(64) UNIQUE,
    name VARCHAR(128),
    form_schema_json TEXT,
    status INT DEFAULT 1,
    version INT DEFAULT 1
);
CREATE TABLE IF NOT EXISTS wf_node (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    definition_id BIGINT,
    seq INT,
    name VARCHAR(128),
    approver_type VARCHAR(32),
    approver_ref VARCHAR(128),
    multi_mode VARCHAR(16),
    condition_json TEXT
);
CREATE TABLE IF NOT EXISTS wf_instance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    instance_no VARCHAR(64) UNIQUE,
    definition_id BIGINT,
    title VARCHAR(255),
    form_json TEXT,
    business_type VARCHAR(64),
    business_id VARCHAR(64),
    applicant_id BIGINT,
    status VARCHAR(32),
    current_node_seq INT,
    submitted_at TIMESTAMP,
    finished_at TIMESTAMP
);
CREATE TABLE IF NOT EXISTS wf_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    instance_id BIGINT,
    node_seq INT,
    approver_user_id BIGINT,
    status VARCHAR(32),
    comment VARCHAR(1000),
    handled_at TIMESTAMP
);
CREATE TABLE IF NOT EXISTS wf_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    instance_id BIGINT,
    user_id BIGINT,
    action VARCHAR(32),
    comment VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS att_shift (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    code VARCHAR(64) UNIQUE NOT NULL,
    name VARCHAR(128) NOT NULL,
    work_start TIME NOT NULL,
    work_end TIME NOT NULL,
    rest_start TIME,
    rest_end TIME,
    late_grace_minutes INT DEFAULT 0,
    early_grace_minutes INT DEFAULT 0,
    is_default INT DEFAULT 0
);
CREATE TABLE IF NOT EXISTS att_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    employee_id BIGINT NOT NULL,
    work_date DATE NOT NULL,
    shift_id BIGINT,
    UNIQUE (employee_id, work_date)
);
CREATE TABLE IF NOT EXISTS att_holiday (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    holiday_date DATE UNIQUE NOT NULL,
    name VARCHAR(128) NOT NULL,
    type VARCHAR(16) NOT NULL
);
CREATE TABLE IF NOT EXISTS att_clock_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    employee_id BIGINT NOT NULL,
    clock_time TIMESTAMP NOT NULL,
    clock_type VARCHAR(8) NOT NULL,
    source VARCHAR(16) NOT NULL,
    device VARCHAR(128),
    ip VARCHAR(64),
    remark VARCHAR(512)
);
CREATE TABLE IF NOT EXISTS att_daily (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    employee_id BIGINT NOT NULL,
    work_date DATE NOT NULL,
    shift_id BIGINT,
    first_in TIMESTAMP,
    last_out TIMESTAMP,
    status VARCHAR(16) NOT NULL,
    late_minutes INT DEFAULT 0,
    early_minutes INT DEFAULT 0,
    work_minutes INT DEFAULT 0,
    overtime_minutes INT DEFAULT 0,
    leave_type VARCHAR(32),
    remark VARCHAR(512),
    UNIQUE (employee_id, work_date)
);
CREATE TABLE IF NOT EXISTS att_leave_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    employee_id BIGINT NOT NULL,
    leave_type VARCHAR(32) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    days DECIMAL(8, 2) NOT NULL,
    reason VARCHAR(1000),
    status VARCHAR(16) NOT NULL,
    wf_instance_id BIGINT
);
CREATE TABLE IF NOT EXISTS att_overtime_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    employee_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    hours DECIMAL(8, 2) NOT NULL,
    type VARCHAR(16) NOT NULL,
    reason VARCHAR(1000),
    status VARCHAR(16) NOT NULL,
    wf_instance_id BIGINT
);
CREATE TABLE IF NOT EXISTS att_patch_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    employee_id BIGINT NOT NULL,
    work_date DATE NOT NULL,
    clock_type VARCHAR(8) NOT NULL,
    clock_time TIMESTAMP NOT NULL,
    reason VARCHAR(1000),
    status VARCHAR(16) NOT NULL,
    wf_instance_id BIGINT
);
CREATE TABLE IF NOT EXISTS att_trip_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    employee_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    destination VARCHAR(255),
    days DECIMAL(8, 2) NOT NULL,
    reason VARCHAR(1000),
    status VARCHAR(16) NOT NULL,
    wf_instance_id BIGINT
);
CREATE TABLE IF NOT EXISTS att_leave_balance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    employee_id BIGINT NOT NULL,
    `year` INT NOT NULL,
    leave_type VARCHAR(32) NOT NULL,
    total_days DECIMAL(8, 2) NOT NULL,
    used_days DECIMAL(8, 2) DEFAULT 0,
    UNIQUE (employee_id, `year`, leave_type)
);
CREATE TABLE IF NOT EXISTS att_monthly_summary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    employee_id BIGINT NOT NULL,
    year_month VARCHAR(7) NOT NULL,
    should_days DECIMAL(8, 2) DEFAULT 0,
    actual_days DECIMAL(8, 2) DEFAULT 0,
    late_count INT DEFAULT 0,
    late_minutes INT DEFAULT 0,
    early_count INT DEFAULT 0,
    absent_days DECIMAL(8, 2) DEFAULT 0,
    leave_days TEXT,
    overtime_hours DECIMAL(8, 2) DEFAULT 0,
    trip_days DECIMAL(8, 2) DEFAULT 0,
    status VARCHAR(16) NOT NULL,
    UNIQUE (employee_id, year_month)
);

CREATE TABLE IF NOT EXISTS pay_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    code VARCHAR(64) UNIQUE NOT NULL,
    name VARCHAR(128) NOT NULL,
    type VARCHAR(32) NOT NULL,
    calc_type VARCHAR(32) NOT NULL,
    formula VARCHAR(1000),
    taxable INT DEFAULT 1,
    sort INT DEFAULT 0,
    is_system INT DEFAULT 0
);
CREATE TABLE IF NOT EXISTS pay_scheme (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    employee_id BIGINT NOT NULL,
    effective_date DATE NOT NULL,
    base_salary DECIMAL(14, 2) DEFAULT 0,
    post_salary DECIMAL(14, 2) DEFAULT 0,
    perf_salary DECIMAL(14, 2) DEFAULT 0,
    allowances_json TEXT,
    si_base DECIMAL(14, 2) DEFAULT 0,
    hf_base DECIMAL(14, 2) DEFAULT 0,
    status VARCHAR(16) NOT NULL
);
CREATE TABLE IF NOT EXISTS pay_insurance_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    city VARCHAR(64) UNIQUE NOT NULL,
    pension_p DECIMAL(8, 5),
    pension_c DECIMAL(8, 5),
    medical_p DECIMAL(8, 5),
    medical_c DECIMAL(8, 5),
    unemployment_p DECIMAL(8, 5),
    unemployment_c DECIMAL(8, 5),
    injury_c DECIMAL(8, 5),
    maternity_c DECIMAL(8, 5),
    hf_p DECIMAL(8, 5),
    hf_c DECIMAL(8, 5),
    si_base_min DECIMAL(14, 2),
    si_base_max DECIMAL(14, 2),
    hf_base_min DECIMAL(14, 2),
    hf_base_max DECIMAL(14, 2)
);
CREATE TABLE IF NOT EXISTS pay_tax_bracket (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    level_no INT UNIQUE NOT NULL,
    lower_bound DECIMAL(14, 2) NOT NULL,
    upper_bound DECIMAL(14, 2),
    rate DECIMAL(8, 5) NOT NULL,
    quick_deduction DECIMAL(14, 2) NOT NULL
);
CREATE TABLE IF NOT EXISTS pay_period (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    year_month VARCHAR(7) UNIQUE NOT NULL,
    status VARCHAR(16) NOT NULL,
    att_locked INT DEFAULT 0,
    calc_at TIMESTAMP,
    approved_by BIGINT,
    paid_at TIMESTAMP,
    total_gross DECIMAL(16, 2) DEFAULT 0,
    total_net DECIMAL(16, 2) DEFAULT 0,
    headcount INT DEFAULT 0
);
CREATE TABLE IF NOT EXISTS pay_slip (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    period_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    dept_id BIGINT,
    items_json TEXT,
    gross DECIMAL(16, 2) DEFAULT 0,
    taxable_income DECIMAL(16, 2) DEFAULT 0,
    cumulative_taxable DECIMAL(16, 2) DEFAULT 0,
    cumulative_tax DECIMAL(16, 2) DEFAULT 0,
    tax DECIMAL(16, 2) DEFAULT 0,
    si_personal DECIMAL(16, 2) DEFAULT 0,
    hf_personal DECIMAL(16, 2) DEFAULT 0,
    si_company DECIMAL(16, 2) DEFAULT 0,
    hf_company DECIMAL(16, 2) DEFAULT 0,
    net DECIMAL(16, 2) DEFAULT 0,
    status VARCHAR(16) NOT NULL,
    remark VARCHAR(512),
    viewed_at TIMESTAMP,
    UNIQUE (period_id, employee_id)
);
CREATE TABLE IF NOT EXISTS pay_adjustment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    period_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    item_code VARCHAR(64) NOT NULL,
    amount DECIMAL(14, 2) NOT NULL,
    reason VARCHAR(512)
);
CREATE TABLE IF NOT EXISTS pay_salary_change (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    employee_id BIGINT NOT NULL,
    effective_date DATE NOT NULL,
    before_json TEXT,
    after_json TEXT,
    reason VARCHAR(512),
    operator BIGINT
);

CREATE TABLE IF NOT EXISTS oa_notice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    title VARCHAR(256) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(32),
    dept_id BIGINT,
    publisher BIGINT,
    published_at TIMESTAMP,
    pinned INT DEFAULT 0,
    status VARCHAR(16) NOT NULL,
    read_count INT DEFAULT 0
);
CREATE TABLE IF NOT EXISTS oa_notice_read (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    notice_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    read_at TIMESTAMP,
    UNIQUE (notice_id, user_id)
);
CREATE TABLE IF NOT EXISTS oa_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    user_id BIGINT NOT NULL,
    title VARCHAR(256) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    location VARCHAR(256),
    participants_json TEXT,
    remind_minutes INT DEFAULT 10
);
CREATE TABLE IF NOT EXISTS oa_meeting_room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    name VARCHAR(128) NOT NULL,
    location VARCHAR(256),
    capacity INT DEFAULT 1,
    equipment VARCHAR(512),
    status VARCHAR(16) NOT NULL
);
CREATE TABLE IF NOT EXISTS oa_meeting_booking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    room_id BIGINT NOT NULL,
    organizer BIGINT NOT NULL,
    title VARCHAR(256) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    attendees TEXT,
    status VARCHAR(16) NOT NULL
);
CREATE TABLE IF NOT EXISTS oa_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    to_user_id BIGINT NOT NULL,
    type VARCHAR(32) NOT NULL,
    title VARCHAR(256) NOT NULL,
    content TEXT,
    link VARCHAR(512),
    read_at TIMESTAMP
);
CREATE TABLE IF NOT EXISTS oa_expense (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    applicant_id BIGINT NOT NULL,
    title VARCHAR(256) NOT NULL,
    items_json TEXT,
    total DECIMAL(14, 2) NOT NULL,
    attachments VARCHAR(2000),
    status VARCHAR(16) NOT NULL,
    wf_instance_id BIGINT
);
