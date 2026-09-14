import http from './request'

export const payrollApi = {
  items: () => http.get('/payroll/items'),
  schemes: (params) => http.get('/payroll/schemes', { params }),
  adjustScheme: (id, data) => http.post(`/payroll/schemes/${id}/adjust`, data),
  insuranceRules: () => http.get('/payroll/insurance-rules'),
  taxBrackets: () => http.get('/payroll/tax-brackets'),
  periods: (params) => http.get('/payroll/periods', { params }),
  open: (yearMonth) => http.post('/payroll/periods/open', null, { params: { yearMonth } }),
  calculate: (id) => http.post(`/payroll/periods/${id}/calculate`),
  approve: (id) => http.post(`/payroll/periods/${id}/approve`),
  pay: (id) => http.post(`/payroll/periods/${id}/pay`),
  close: (id) => http.post(`/payroll/periods/${id}/close`),
  slips: (params) => http.get('/payroll/slips', { params }),
  mine: () => http.get('/payroll/slips/mine'),
  adjustSlip: (id, data) => http.put(`/payroll/slips/${id}/adjust`, data),
  exportPeriod: (id) => http.get(`/payroll/periods/${id}/export`, { responseType: 'blob' }),
  cost: (year) => http.get('/payroll/reports/cost', { params: { year } }),
}
