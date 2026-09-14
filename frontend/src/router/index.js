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
      { path: 'workflow', component: ModuleView, props: { module: 'workflow' }, meta: { title: '审批中心' } },
      { path: 'payroll', component: ModuleView, props: { module: 'payroll' }, meta: { title: '工资管理' } },
      { path: 'collab', component: ModuleView, props: { module: 'collab' }, meta: { title: '协同办公' } },
      { path: 'contacts', component: ModuleView, props: { module: 'contacts' }, meta: { title: '通讯录' } },
      { path: 'system', component: ModuleView, props: { module: 'system' }, meta: { title: '系统管理' } },
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
