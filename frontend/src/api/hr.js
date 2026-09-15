import http from './request'

export const hrApi = {
  employees: (params) => http.get('/hr/employees', { params }),
  employee: (id) => http.get(`/hr/employees/${id}`),
  hire: (data) => http.post('/hr/employees', data),
  update: (id, data) => http.put(`/hr/employees/${id}`, data),
  regular: (id) => http.post(`/hr/employees/${id}/regular`),
  transfer: (id, data) => http.post(`/hr/employees/${id}/transfer`, data),
  leave: (id, data) => http.post(`/hr/employees/${id}/leave`, data),
  contracts: () => http.get('/hr/contracts'),
  employeeContracts: (id) => http.get(`/hr/contracts/employee/${id}`),
  createContract: (data) => http.post('/hr/contracts', data),
  updateContract: (id, data) => http.put(`/hr/contracts/${id}`, data),
  expiringContracts: () => http.get('/hr/contracts/expiring'),
  changes: (params) => http.get('/hr/changes', { params }),
  importEmployees: (file) => {
    const form = new FormData()
    form.append('file', file)
    return http.post('/hr/employees/import', form)
  },
  exportEmployees: () => http.get('/hr/employees/export', { responseType: 'blob' }),
}
