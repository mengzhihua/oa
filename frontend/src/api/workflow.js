import http from './request'

export const workflowApi = {
  definitions: () => http.get('/workflow/definitions'),
  definition: (id) => http.get(`/workflow/definitions/${id}`),
  saveDefinition: (data) =>
    data.id ? http.put(`/workflow/definitions/${data.id}`, data) : http.post('/workflow/definitions', data),
  instances: (params) => http.get('/workflow/instances', { params }),
  instance: (id) => http.get(`/workflow/instances/${id}`),
  todo: () => http.get('/workflow/tasks/todo'),
  done: () => http.get('/workflow/tasks/done'),
  approve: (id, data) => http.post(`/workflow/tasks/${id}/approve`, data),
  reject: (id, data) => http.post(`/workflow/tasks/${id}/reject`, data),
  transfer: (id, data) => http.post(`/workflow/tasks/${id}/transfer`, data),
  cancel: (id) => http.post(`/workflow/instances/${id}/cancel`),
  start: (data) => http.post('/workflow/instances/start', data),
}
