import http from './request'

export const authApi = {
  login: (data) => http.post('/auth/login', data),
  me: () => http.get('/auth/me'),
  password: (data) => http.put('/auth/password', data),
  logout: () => http.post('/auth/logout'),
}

export const dashboardApi = {
  summary: () => http.get('/dashboard'),
}

export const orgApi = {
  depts: () => http.get('/org/depts/tree'),
  positions: () => http.get('/org/positions'),
  grades: () => http.get('/org/grades'),
}

export const hrApi = {
  employees: (params) => http.get('/hr/employees', { params }),
  contracts: () => http.get('/hr/contracts'),
  expiringContracts: () => http.get('/hr/contracts/expiring'),
}

export const workflowApi = {
  definitions: () => http.get('/workflow/definitions'),
  mine: (params) => http.get('/workflow/instances', { params }),
  todo: () => http.get('/workflow/tasks/todo'),
  done: () => http.get('/workflow/tasks/done'),
  start: (data) => http.post('/workflow/instances/start', data),
  approve: (id, data) => http.post(`/workflow/tasks/${id}/approve`, data),
  reject: (id, data) => http.post(`/workflow/tasks/${id}/reject`, data),
}

export const attendanceApi = {
  today: () => http.get('/attendance/clock/today'),
  clock: (data) => http.post('/attendance/clock', data),
  mine: (params) => http.get('/attendance/daily/mine', { params }),
  leaves: () => http.get('/attendance/leaves/mine'),
  balance: () => http.get('/attendance/balance'),
  monthly: (params) => http.get('/attendance/monthly', { params }),
  submitLeave: (data) => http.post('/attendance/leaves', data),
  submitOvertime: (data) => http.post('/attendance/overtimes', data),
}

export const payrollApi = {
  periods: (params) => http.get('/payroll/periods', { params }),
  slips: (params) => http.get('/payroll/slips', { params }),
  mine: () => http.get('/payroll/slips/mine'),
  items: () => http.get('/payroll/items'),
  schemes: (params) => http.get('/payroll/schemes', { params }),
  insurance: () => http.get('/payroll/insurance-rules'),
  taxBrackets: () => http.get('/payroll/tax-brackets'),
  open: (yearMonth) => http.post('/payroll/periods/open', null, { params: { yearMonth } }),
  calculate: (id) => http.post(`/payroll/periods/${id}/calculate`),
  approve: (id) => http.post(`/payroll/periods/${id}/approve`),
  pay: (id) => http.post(`/payroll/periods/${id}/pay`),
}

export const collabApi = {
  notices: () => http.get('/notices/mine'),
  createNotice: (data) => http.post('/notices', data),
  publishNotice: (id) => http.post(`/notices/${id}/publish`),
  schedules: (params) => http.get('/schedules/mine', { params }),
  rooms: () => http.get('/meetings/rooms'),
  messages: () => http.get('/messages/mine'),
  unread: () => http.get('/messages/unread-count'),
  contacts: (params) => http.get('/contacts', { params }),
  expenses: () => http.get('/expenses/mine'),
  submitExpense: (data) => http.post('/expenses', data),
}
