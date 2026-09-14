import http from './request'

export const dashboardApi = {
  summary: () => http.get('/dashboard'),
}
