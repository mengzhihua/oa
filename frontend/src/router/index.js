import { createRouter, createWebHistory } from 'vue-router'
import { auth } from '../auth'
import Login from '../views/Login.vue'
import OAuthAuthorize from '../views/OAuthAuthorize.vue'
import Layout from '../layout/Layout.vue'
import Dashboard from '../views/Dashboard.vue'
import ModuleView from '../views/ModuleView.vue'
import Depts from '../views/org/Depts.vue'
import Positions from '../views/org/Positions.vue'
import Grades from '../views/org/Grades.vue'
import Employees from '../views/hr/Employees.vue'
import EmployeeDetail from '../views/hr/EmployeeDetail.vue'
import Contracts from '../views/hr/Contracts.vue'
import Changes from '../views/hr/Changes.vue'
import MyAttendance from '../views/attendance/MyAttendance.vue'
import MyRequests from '../views/attendance/MyRequests.vue'
import LeaveBalance from '../views/attendance/LeaveBalance.vue'
import Shifts from '../views/attendance/Shifts.vue'
import Schedules from '../views/attendance/Schedules.vue'
import Holidays from '../views/attendance/Holidays.vue'
import DeptDaily from '../views/attendance/DeptDaily.vue'
import Monthly from '../views/attendance/Monthly.vue'
import Todo from '../views/workflow/Todo.vue'
import Done from '../views/workflow/Done.vue'
import MyInstances from '../views/workflow/MyInstances.vue'
import Start from '../views/workflow/Start.vue'
import Definitions from '../views/workflow/Definitions.vue'
import PayrollItems from '../views/payroll/Items.vue'
import PayrollSchemes from '../views/payroll/Schemes.vue'
import InsuranceRules from '../views/payroll/InsuranceRules.vue'
import TaxBrackets from '../views/payroll/TaxBrackets.vue'
import Periods from '../views/payroll/Periods.vue'
import Slips from '../views/payroll/Slips.vue'
import CostReport from '../views/payroll/CostReport.vue'
import MySlips from '../views/payroll/MySlips.vue'
import Notices from '../views/collab/Notices.vue'
import CollabSchedules from '../views/collab/Schedules.vue'
import Meetings from '../views/collab/Meetings.vue'
import Messages from '../views/collab/Messages.vue'
import Contacts from '../views/collab/Contacts.vue'
import Expenses from '../views/collab/Expenses.vue'
import Users from '../views/system/Users.vue'
import Roles from '../views/system/Roles.vue'
import Menus from '../views/system/Menus.vue'
import Dicts from '../views/system/Dicts.vue'
import OAuthClients from '../views/system/OAuthClients.vue'
import OpLogs from '../views/system/OpLogs.vue'

const routes = [
  { path: '/login', component: Login },
  { path: '/oauth/authorize', component: OAuthAuthorize },
  {
    path: '/',
    component: Layout,
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', component: Dashboard, meta: { title: '工作台' } },
      { path: 'org', redirect: '/org/depts' },
      { path: 'org/depts', component: Depts, meta: { title: '部门管理' } },
      { path: 'org/positions', component: Positions, meta: { title: '岗位管理' } },
      { path: 'org/grades', component: Grades, meta: { title: '职级管理' } },
      {
        path: 'hr/employees',
        component: Employees,
        meta: { title: '员工档案' },
      },
      { path: 'hr/employees/:id', component: EmployeeDetail, meta: { title: '员工详情' } },
      {
        path: 'hr/contracts',
        component: Contracts,
        meta: { title: '合同管理' },
      },
      { path: 'hr/changes', component: Changes, meta: { title: '人事异动' } },
      { path: 'attendance', redirect: '/attendance/mine' },
      { path: 'attendance/mine', component: MyAttendance, meta: { title: '我的考勤' } },
      { path: 'attendance/requests', component: MyRequests, meta: { title: '我的申请' } },
      { path: 'attendance/balance', component: LeaveBalance, meta: { title: '年假余额' } },
      { path: 'attendance/shifts', component: Shifts, meta: { title: '班次管理' } },
      { path: 'attendance/schedules', component: Schedules, meta: { title: '排班管理' } },
      { path: 'attendance/holidays', component: Holidays, meta: { title: '节假日' } },
      { path: 'attendance/dept-daily', component: DeptDaily, meta: { title: '部门日报' } },
      { path: 'attendance/monthly', component: Monthly, meta: { title: '月度汇总' } },
      { path: 'workflow', redirect: '/workflow/todo' },
      { path: 'workflow/todo', component: Todo, meta: { title: '待办审批' } },
      { path: 'workflow/done', component: Done, meta: { title: '已办审批' } },
      { path: 'workflow/mine', component: MyInstances, meta: { title: '我的申请' } },
      { path: 'workflow/start', component: Start, meta: { title: '发起申请' } },
      { path: 'workflow/definitions', component: Definitions, meta: { title: '流程定义' } },
      { path: 'payroll', redirect: '/payroll/periods' },
      { path: 'payroll/items', component: PayrollItems, meta: { title: '薪资项目' } },
      { path: 'payroll/schemes', component: PayrollSchemes, meta: { title: '薪资方案' } },
      { path: 'payroll/insurance-rules', component: InsuranceRules, meta: { title: '社保规则' } },
      { path: 'payroll/tax-brackets', component: TaxBrackets, meta: { title: '税率表' } },
      { path: 'payroll/periods', component: Periods, meta: { title: '工资期间' } },
      { path: 'payroll/slips', component: Slips, meta: { title: '工资单' } },
      { path: 'payroll/cost', component: CostReport, meta: { title: '人力成本' } },
      { path: 'payroll/mine', component: MySlips, meta: { title: '我的工资条' } },
      { path: 'collab', redirect: '/collab/notices' },
      { path: 'collab/notices', component: Notices, meta: { title: '公告管理' } },
      { path: 'collab/schedules', component: CollabSchedules, meta: { title: '我的日程' } },
      { path: 'collab/meetings', component: Meetings, meta: { title: '会议室' } },
      { path: 'collab/messages', component: Messages, meta: { title: '消息中心' } },
      { path: 'collab/contacts', component: Contacts, meta: { title: '通讯录' } },
      { path: 'collab/expenses', component: Expenses, meta: { title: '报销管理' } },
      { path: 'contacts', redirect: '/collab/contacts' },
      { path: 'system', redirect: '/system/users' },
      { path: 'system/users', component: Users, meta: { title: '用户管理' } },
      { path: 'system/roles', component: Roles, meta: { title: '角色管理' } },
      { path: 'system/menus', component: Menus, meta: { title: '菜单管理' } },
      { path: 'system/dicts', component: Dicts, meta: { title: '字典管理' } },
      { path: 'system/oauth-clients', component: OAuthClients, meta: { title: 'OAuth 客户端' } },
      { path: 'system/op-logs', component: OpLogs, meta: { title: '操作日志' } },
    ],
  },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to) => {
  if (to.path === '/login' || to.path === '/oauth/authorize') return true
  if (!auth.token) return { path: '/login', query: { redirect: to.fullPath } }
  return true
})

export default router
