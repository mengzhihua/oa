import {
  Calendar,
  Connection,
  Finished,
  Odometer,
  OfficeBuilding,
  Setting,
  Wallet,
} from '@element-plus/icons-vue'

export const groups = [
  {
    name: '工作台',
    icon: Odometer,
    items: [{ title: '工作台', path: '/dashboard', roles: [] }],
  },
  {
    name: '组织人事',
    icon: OfficeBuilding,
    items: [
      { title: '部门管理', path: '/org/depts', roles: ['ADMIN', 'HR', 'MANAGER'] },
      { title: '岗位管理', path: '/org/positions', roles: ['ADMIN', 'HR'] },
      { title: '职级管理', path: '/org/grades', roles: ['ADMIN', 'HR'] },
      { title: '员工档案', path: '/hr/employees', roles: ['ADMIN', 'HR', 'MANAGER'] },
      { title: '合同管理', path: '/hr/contracts', roles: ['ADMIN', 'HR'] },
      { title: '人事异动', path: '/hr/changes', roles: ['ADMIN', 'HR'] },
    ],
  },
  {
    name: '考勤管理',
    icon: Calendar,
    items: [
      { title: '我的考勤', path: '/attendance/mine', roles: [] },
      { title: '我的申请', path: '/attendance/requests', roles: [] },
      { title: '年假余额', path: '/attendance/balance', roles: [] },
      { title: '班次管理', path: '/attendance/shifts', roles: ['ADMIN', 'HR'] },
      { title: '排班管理', path: '/attendance/schedules', roles: ['ADMIN', 'HR', 'MANAGER'] },
      { title: '节假日', path: '/attendance/holidays', roles: ['ADMIN', 'HR'] },
      { title: '部门日报', path: '/attendance/dept-daily', roles: ['ADMIN', 'HR', 'MANAGER'] },
      { title: '月度汇总', path: '/attendance/monthly', roles: ['ADMIN', 'HR', 'MANAGER'] },
    ],
  },
  {
    name: '审批中心',
    icon: Finished,
    items: [
      { title: '待办审批', path: '/workflow/todo', roles: ['ADMIN', 'HR', 'FINANCE', 'MANAGER'] },
      { title: '已办审批', path: '/workflow/done', roles: ['ADMIN', 'HR', 'FINANCE', 'MANAGER'] },
      { title: '我的申请', path: '/workflow/mine', roles: [] },
      { title: '发起申请', path: '/workflow/start', roles: [] },
      { title: '流程定义', path: '/workflow/definitions', roles: ['ADMIN', 'HR'] },
    ],
  },
  {
    name: '工资管理',
    icon: Wallet,
    items: [
      { title: '薪资项目', path: '/payroll/items', roles: ['ADMIN', 'HR', 'FINANCE'] },
      { title: '薪资方案', path: '/payroll/schemes', roles: ['ADMIN', 'HR', 'FINANCE'] },
      { title: '社保规则', path: '/payroll/insurance-rules', roles: ['ADMIN', 'HR', 'FINANCE'] },
      { title: '税率表', path: '/payroll/tax-brackets', roles: ['ADMIN', 'HR', 'FINANCE'] },
      { title: '工资期间', path: '/payroll/periods', roles: ['ADMIN', 'FINANCE'] },
      { title: '工资单', path: '/payroll/slips', roles: ['ADMIN', 'HR', 'FINANCE'] },
      { title: '人力成本', path: '/payroll/cost', roles: ['ADMIN', 'HR', 'FINANCE'] },
      { title: '我的工资条', path: '/payroll/mine', roles: [] },
    ],
  },
  {
    name: '协同办公',
    icon: Connection,
    items: [
      { title: '公告管理', path: '/collab/notices', roles: ['ADMIN', 'HR', 'MANAGER'] },
      { title: '我的日程', path: '/collab/schedules', roles: [] },
      { title: '会议室', path: '/collab/meetings', roles: [] },
      { title: '消息中心', path: '/collab/messages', roles: [] },
      { title: '通讯录', path: '/collab/contacts', roles: [] },
      { title: '报销管理', path: '/collab/expenses', roles: ['ADMIN', 'FINANCE', 'EMPLOYEE'] },
    ],
  },
  {
    name: '系统管理',
    icon: Setting,
    items: [
      { title: '用户管理', path: '/system/users', roles: ['ADMIN'] },
      { title: '角色管理', path: '/system/roles', roles: ['ADMIN'] },
      { title: '菜单管理', path: '/system/menus', roles: ['ADMIN'] },
      { title: '字典管理', path: '/system/dicts', roles: ['ADMIN'] },
      { title: 'OAuth 客户端', path: '/system/oauth-clients', roles: ['ADMIN'] },
      { title: '操作日志', path: '/system/op-logs', roles: ['ADMIN'] },
    ],
  },
]

export function menuItems() {
  return groups.flatMap((group) => group.items)
}
