import http from './request'

export const authApi = {
  login: (data) => http.post('/auth/login', data),
  me: () => http.get('/auth/me'),
  password: (data) => http.put('/auth/password', data),
  logout: () => http.post('/auth/logout'),
}
