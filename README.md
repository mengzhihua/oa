# 集团 OA 协同办公平台

集团 OA 是面向集团企业的组织、人事、考勤、审批、工资和协同办公平台，采用
Spring Boot + MyBatis-Plus 后端与 Vue 3 + Element Plus 前端，覆盖从员工入职、
打卡和请假，到审批、工资核算、公告沟通和工作台分析的完整闭环。系统提供 H2
开箱即用模式，也支持 MySQL 部署和集团系统 OAuth2.0 统一认证接入。

## 能力定位与竞品映射

| OA 能力 | 用友 / NC、U8 | 金蝶 / 云星空、s-HR | 泛微 / e-cology | 本系统 |
| --- | --- | --- | --- | --- |
| 组织人事 | 组织、员工、岗位、合同 | 人员档案、薪资方案 | 通讯录、组织权限 | 部门树、员工档案、合同、异动、薪资方案 |
| 考勤 | 排班、打卡、假期 | 出勤核算、假勤余额 | 移动考勤、流程联动 | 班次、排班、节假日、日结/月结、申请审批 |
| 审批 | 多级审批、权限 | 费用与人事审批 | 流程引擎、条件分支 | 节点、条件、多人模式、转办/撤回/回调 |
| 薪资 | 薪资核算、社保 | s-HR 工资条、个税 | 薪酬流程协同 | 加班、扣款、社保、公积金、累计预扣个税 |
| 协同 | 公告、日程、会议 | 消息、费用 | 门户、知识与协作 | 公告、日程、会议室、消息、通讯录、报销 |
| 集成 | ERP 集成 | 集团应用集成 | SSO/OAuth | OAuth2 授权码 + PKCE、userinfo、令牌管理 |

## 技术栈

- 后端：Java 8、Spring Boot 2.7.18、Spring MVC、Spring Scheduling、
  MyBatis-Plus、H2、MySQL、JUnit 5。
- 前端：Vue 3、Vite、Element Plus、vue-router、axios、Prettier。
- 认证：OA Bearer Token、OAuth2.0 Authorization Code + PKCE、Refresh Token
  Rotation。
- 数据：`schema.sql` 为 H2/MySQL 兼容表结构，`data.sql` 用于 H2，`data-mysql.sql`
  用于 MySQL。

## 总体架构

```text
┌────────────────────────────── 集团应用 ──────────────────────────────┐
│ SAP / SRM / CRM / 其他系统 ── OAuth2 authorize/token/userinfo/... ──┐ │
└─────────────────────────────────────────────────────────────────────┘ │
                                      │                                  │
                         ┌────────────▼────────────┐                     │
                         │ Vue 3 OA Web (5176)     │                     │
                         │ Layout / Router / Views │                     │
                         └────────────┬────────────┘                     │
                                      │ /api proxy                       │
                         ┌────────────▼────────────┐                     │
                         │ Spring Boot API (8086)  │                     │
                         │ Controller → Service    │                     │
                         │ DTO/VO → MyBatis-Plus   │                     │
                         └──────┬───────────┬──────┘                     │
                                │           │                             │
                         ┌──────▼───┐ ┌────▼────────┐                    │
                         │ H2/MySQL │ │ Workflow    │                    │
                         │ 业务数据 │ │ Callback    │                    │
                         └──────────┘ └─────────────┘                    │
```

## 功能范围

| 模块 | 功能范围 |
| --- | --- |
| 工作台 | 待办、申请中、未读消息、今日打卡、公告、日程；HR/ADMIN 人事统计、部门人数与异常；FINANCE 工资期间与人力成本 |
| 组织人事 | 部门树、部门主管、岗位、职级、员工入职/编辑/转正/调岗/离职、合同、异动、CSV 导入导出 |
| 考勤管理 | 班次、排班、节假日、打卡、日结、月度汇总、异常日报、请假/加班/补卡/出差、年假余额、CSV 导出 |
| 审批中心 | 待办、已办、我的申请、通用申请、流程定义、节点条件、同意/驳回/转办/撤回、审批时间线 |
| 工资管理 | 薪资项目、薪资方案、调薪、社保规则、税率、工资期间、工资单、手工调整、成本报表、员工工资条 |
| 协同办公 | 公告发布/撤回/已读、日程、会议室冲突校验、消息中心、通讯录、报销与财务付款 |
| 系统管理 | 用户、角色与菜单授权、菜单、字典、OAuth 客户端、操作日志、密码管理 |

## 角色与演示账号

| 角色 | 账号 | 主要权限 |
| --- | --- | --- |
| ADMIN | `admin / admin123` | 全部模块和系统配置 |
| HR | `hr / hr123` | 组织人事、考勤、审批和人事看板 |
| FINANCE | `finance / fin123` | 工资、社保税率、报销付款和财务审批 |
| MANAGER | `manager / mgr123` | 部门员工、部门考勤和审批 |
| EMPLOYEE | `zhangsan / emp123` | 自助打卡、申请、消息、日程和工资条 |

前端菜单和写按钮依据 `/api/auth/me` 返回的角色过滤；后端通过
`AccessPolicy` 和当前用户上下文再次校验权限，不能仅依赖前端隐藏按钮。

## OAuth2.0 统一认证

### 端点

| 方法 | 端点 | 说明 |
| --- | --- | --- |
| GET | `/api/oauth/authorize` | 授权码申请，支持 `state` 和 PKCE |
| POST | `/api/oauth/token` | authorization_code、refresh_token 换取令牌 |
| GET | `/api/oauth/userinfo` | 按 scope 返回用户信息 |
| POST | `/api/oauth/introspect` | 检查 access/refresh token 是否有效 |
| POST | `/api/oauth/revoke` | 撤销 access 或 refresh token |

### 授权码 + PKCE 时序

```text
集团系统生成 code_verifier
    │
    ├─ code_challenge = BASE64URL(SHA256(code_verifier))
    │
    ├─ 浏览器 GET /api/oauth/authorize
    │       client_id + redirect_uri + scope + state + code_challenge
    │
    ├─ OA 登录并同意授权
    │
    ├─ 302 redirect_uri?code=...&state=...
    │
    └─ POST /api/oauth/token
       grant_type=authorization_code + code + redirect_uri + code_verifier
       → access_token + refresh_token
```

演示 client：

| client_id | secret 约定 | 默认回调 |
| --- | --- | --- |
| `sap-client` | `sap-client-secret` | `http://localhost:5175/sso/callback` |
| `srm-client` | `srm-client-secret` | `http://localhost:5174/sso/callback` |
| `crm-client` | `crm-client-secret` | `http://localhost:5173/sso/callback` |

secret 仅用于演示，生产环境应通过客户端管理页面生成并安全保存，服务端只保存
哈希。其他集团系统接入示例：

```bash
curl -G 'http://localhost:8086/api/oauth/authorize' \
  --data-urlencode 'client_id=srm-client' \
  --data-urlencode 'redirect_uri=http://localhost:5174/sso/callback' \
  --data-urlencode 'response_type=code' \
  --data-urlencode 'scope=openid profile email roles' \
  --data-urlencode 'state=demo-state' \
  --data-urlencode 'code_challenge=BASE64URL_SHA256_CODE_VERIFIER' \
  --data-urlencode 'code_challenge_method=S256'

curl -X POST 'http://localhost:8086/api/oauth/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'grant_type=authorization_code' \
  --data-urlencode 'client_id=srm-client' \
  --data-urlencode 'client_secret=srm-client-secret' \
  --data-urlencode 'redirect_uri=http://localhost:5174/sso/callback' \
  --data-urlencode 'code=AUTHORIZATION_CODE' \
  --data-urlencode 'code_verifier=ORIGINAL_CODE_VERIFIER'

curl 'http://localhost:8086/api/oauth/userinfo' \
  -H 'Authorization: Bearer ACCESS_TOKEN'
curl -X POST 'http://localhost:8086/api/oauth/introspect' \
  -d 'token=ACCESS_TOKEN'
curl -X POST 'http://localhost:8086/api/oauth/revoke' \
  -d 'token=ACCESS_TOKEN'
```

## 审批流引擎

- 节点类型：`DEPT_LEADER`、`HR`、`FINANCE`、`MANAGER`、`USER` 等。
- 条件节点使用 JSON：`{"field":"days","op":">","value":3}`，支持 `>`、`>=`、
  `<`、`<=`、`==`、`!=`。
- 多人模式支持 `ANY`（任一人通过）和 `ALL`（全部通过）。
- 请假、加班、补卡、出差、报销和通用申请通过 `WorkflowService.start` 启动。
- `WorkflowCallback` 在流程结束后按业务类型回写状态；例如通过请假扣减年假、
  补卡写入 PATCH 打卡、加班完成业务确认。
- 支持申请人撤回、管理员/审批人转办和流程取消；审批任务创建与结束会写入消息。

## 考勤与工资规则

### 考勤

标准班次为 09:00–18:00，午休 12:00–13:00，迟到/早退宽限 10 分钟；弹性班次
为 10:00–19:00。排班优先使用员工班次，无排班时使用默认班次，周末默认休息。
法定节假日与调休上班日记录在 `att_holiday`。请假按工作日计算，排除周末和
法定假日但包含调休工作日；支持补卡、出差、加班审批。日结后生成月度汇总，
确认并锁定后才允许工资计算。

### 工资

- 应发：基本工资 + 岗位工资 + 绩效 + 补贴 + 加班费 + 手工调整。
- 加班倍率：工作日 `1.5`、周末 `2.0`、法定节假日 `3.0`。
- 时薪：`(基本工资 + 岗位工资) / 21.75 / 8`。
- 迟到按次扣款；缺勤按日薪扣除；事假按日薪全扣，病假按日薪的 `40%` 扣除。
- 北京社保、公积金按个人/公司比例和基数上下限计算。
- 个人所得税使用累计预扣法：`累计应纳税所得额 = 累计收入 - 累计免税
  5000×月份 - 累计专项扣除 - 累计其他扣除`，按 7 级税率表计算累计税额，
  当月税额为累计应纳税额减去本年度已预缴税额。
- 月度考勤未达到 `LOCKED` 状态时，工资期间禁止计算；工资审核后不可重算。

## 目录结构

```text
backend/
  src/main/java/com/oa/
    system/       登录、权限、OAuth、启动初始化
    org/ hr/      组织与人事
    attendance/   考勤、申请、日结/月结
    workflow/     流程定义、实例、任务和回调
    payroll/      薪资、工资期间、工资条、报表
    collab/       公告、日程、会议、消息、报销
    dashboard/    工作台聚合视图
  src/main/resources/
    schema.sql data.sql data-mysql.sql application*.yml
frontend/
  src/api/        按模块拆分的接口
  src/layout/     Layout
  src/router/     路由和菜单分组
  src/views/      多页面业务视图
  src/components/ 公共业务组件
scripts/smoke.sh  登录、审批、考勤、工资、协同链路冒烟
```

## 启动方式

环境要求：JDK 8+、Maven 3.6+、Node 18+。

```bash
# 默认 H2 文件库，后端 8086
cd backend
mvn spring-boot:run

# 前端 5176
cd ../frontend
npm install
npm run dev
```

默认数据库文件位于 `backend/data/`。如需重新初始化演示数据：

```bash
rm -rf backend/data
```

MySQL profile：

```bash
DB_HOST=127.0.0.1 DB_PORT=3306 DB_NAME=oa \
DB_USER=root DB_PASSWORD=secret \
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

仓库提供 `docker-compose.yml`，可用于启动 MySQL 等本地依赖；生产部署请替换
默认密码和 OAuth secret。

端口约定：后端 `8086`，前端 `5176`，前端 `/api` 代理到后端。

## 测试与 smoke

```bash
cd backend
mvn -q test

cd ../frontend
npm run build
npx prettier --check "src/**/*.{vue,js}"

cd ..
./scripts/smoke.sh
```

测试覆盖考勤迟到/早退/缺勤/请假/节假日/补卡、工资金额与累计个税、考勤锁定
约束和审批回调。smoke 覆盖登录、打卡、请假审批、月度锁定、工资发放、工资条、
公告消息、会议室冲突和工作台。

## 主要数据表

```text
sys_user sys_role sys_menu sys_user_role sys_role_menu sys_dict
oauth_client oauth_authorization_code oauth_token
org_dept org_position org_job_grade
hr_employee hr_contract hr_employee_change
att_shift att_schedule att_holiday att_clock_record att_daily
att_leave_request att_overtime_request att_patch_request att_trip_request
att_leave_balance att_monthly_summary
wf_definition wf_node wf_instance wf_task
pay_item pay_scheme pay_insurance_rule pay_tax_bracket pay_period pay_slip
pay_adjustment pay_salary_change
oa_notice oa_notice_read oa_schedule oa_meeting_room oa_meeting_booking
oa_message oa_expense oa_operation_log
```

## FAQ

**Q：分页仍显示英文？**
A：入口 `main.js` 已注册 Element Plus `zhCn`，并且页面统一使用分页组件；
重新启动 Vite 后清理浏览器缓存即可。

**Q：工资期间提示“月度考勤未锁定”？**
A：先在考勤月度汇总页执行生成、确认、锁定，再回到工资期间计算。

**Q：如何获得演示工资条？**
A：首次启动会为 `2026-08` OPEN 期间补齐 LOCKED 月度汇总，并幂等执行计算、
审核和发放；已有工资单或已发放期间不会重复发送消息。

**Q：如何接入 MySQL？**
A：设置数据库环境变量并使用 `mysql` profile；该 profile 使用
`data-mysql.sql`，避免 H2 的 `MERGE INTO` 语法。

**Q：生产环境需要修改什么？**
A：至少修改管理员密码、数据库密码、Token 签名密钥、OAuth client secret、
CORS 来源，并关闭不必要的 H2 控制台和演示初始化配置。
