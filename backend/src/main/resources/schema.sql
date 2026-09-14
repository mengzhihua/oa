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
    icon VARCHAR(64)
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
