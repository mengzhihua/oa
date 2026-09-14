import http from './request'

export const oauthApi = {
  clients: () => http.get('/oauth/clients'),
  saveClient: (data) => http.post('/oauth/clients', data),
}
