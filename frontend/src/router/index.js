import { createRouter, createWebHistory } from 'vue-router'
import { auth } from '../auth'
import Login from '../views/Login.vue'
import OAuthAuthorize from '../views/OAuthAuthorize.vue'
import Layout from '../layout/Layout.vue'
import Dashboard from '../views/Dashboard.vue'
import ModuleView from '../views/ModuleView.vue'

const routes = [
  { path: '/login', component: Login },
  { path: '/oauth/authorize', component: OAuthAuthorize },
  {
    path: '/',
    component: Layout,
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', component: Dashboard, meta: { title: '工作台' } },
      { path: 'org', component: ModuleView, props: { module: 'org' }, meta: { title: '组织架构' } },
      {
        path: 'hr/employees',
        component: ModuleView,
        props: { module: 'employees' },
        meta: { title: '员工档案' },
      },
      {
        path: 'hr/contracts',
        component: ModuleView,
        props: { module: 'contracts' },
        meta: { title: '合同管理' },
      },
      {
        path: 'attendance',
        component: ModuleView,
        props: { module: 'attendance' },
        meta: { title: '我的考勤' },
      },
      {
        path: 'attendance/manage',
        component: ModuleView,
        props: { module: 'attendanceManage' },
        meta: { title: '考勤管理' },
      },
      { path: 'workflow', component: ModuleView, props: { module: 'workflow' }, meta: { title: '审批中心' } },
      { path: 'payroll', component: ModuleView, props: { module: 'payroll' }, meta: { title: '工资管理' } },
      { path: 'collab', component: ModuleView, props: { module: 'collab' }, meta: { title: '协同办公' } },
      { path: 'contacts', component: ModuleView, props: { module: 'contacts' }, meta: { title: '通讯录' } },
      { path: 'system', component: ModuleView, props: { module: 'system' }, meta: { title: '系统管理' } },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  if (to.path === '/login' || to.path === '/oauth/authorize') {
    return true
  }
  if (!auth.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
