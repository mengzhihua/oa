import http from './request'

export const collabApi = {
  notices: () => http.get('/notices/mine'),
  createNotice: (data) => http.post('/notices', data),
  updateNotice: (id, data) => http.put(`/notices/${id}`, data),
  publishNotice: (id) => http.post(`/notices/${id}/publish`),
  revokeNotice: (id) => http.post(`/notices/${id}/revoke`),
  readNotice: (id) => http.post(`/notices/${id}/read`),
  schedules: (params) => http.get('/schedules/mine', { params }),
  createSchedule: (data) => http.post('/schedules', data),
  rooms: () => http.get('/meetings/rooms'),
  createRoom: (data) => http.post('/meetings/rooms', data),
  bookings: (params) => http.get('/meetings/bookings', { params }),
  createBooking: (data) => http.post('/meetings/bookings', data),
  cancelBooking: (id) => http.post(`/meetings/bookings/${id}/cancel`),
  messages: () => http.get('/messages/mine'),
  unread: () => http.get('/messages/unread-count'),
  readMessage: (id) => http.post(`/messages/${id}/read`),
  contacts: (params) => http.get('/contacts', { params }),
  expenses: (params) => http.get('/expenses/mine', { params }),
  createExpense: (data) => http.post('/expenses', data),
  payExpense: (id) => http.post(`/expenses/${id}/pay`),
}
