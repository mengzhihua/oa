import http from './request'

export const systemApi = {
  users: (params) => http.get('/system/users', { params }),
  createUser: (data) => http.post('/system/users', data),
  updateUser: (id, data) => http.put(`/system/users/${id}`, data),
  deleteUser: (id) => http.delete(`/system/users/${id}`),
  resetPassword: (id, data) => http.put(`/system/users/${id}/password`, data),
  roles: () => http.get('/system/roles'),
  saveRole: (data) =>
    data.id ? http.put(`/system/roles/${data.id}`, data) : http.post('/system/roles', data),
  deleteRole: (id) => http.delete(`/system/roles/${id}`),
  menus: () => http.get('/system/menus'),
  dicts: (params) => http.get('/system/dicts', { params }),
  oauthClients: () => http.get('/oauth/clients'),
  saveOauthClient: (data) => http.post('/oauth/clients', data),
  logs: (params) => http.get('/system/op-logs', { params }),
}
