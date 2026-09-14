import http from './request'

export const attendanceApi = {
  today: () => http.get('/attendance/clock/today'),
  clock: (data) => http.post('/attendance/clock', data),
  dailyMine: (params) => http.get('/attendance/daily/mine', { params }),
  leaves: () => http.get('/attendance/leaves/mine'),
  overtimes: () => http.get('/attendance/overtimes/mine'),
  patches: () => http.get('/attendance/patches/mine'),
  trips: () => http.get('/attendance/trips/mine'),
  balance: (params) => http.get('/attendance/balance', { params }),
  cancel: (type, id) => http.post(`/attendance/requests/${type}/${id}/cancel`),
  shifts: () => http.get('/attendance/shifts'),
  saveShift: (data) =>
    data.id ? http.put(`/attendance/shifts/${data.id}`, data) : http.post('/attendance/shifts', data),
  deleteShift: (id) => http.delete(`/attendance/shifts/${id}`),
  schedules: (params) => http.get('/attendance/schedules', { params }),
  batchSchedule: (data) => http.post('/attendance/schedules/batch', data),
  holidays: (params) => http.get('/attendance/holidays', { params }),
  saveHoliday: (data) =>
    data.id ? http.put(`/attendance/holidays/${data.id}`, data) : http.post('/attendance/holidays', data),
  deleteHoliday: (id) => http.delete(`/attendance/holidays/${id}`),
  recalc: (params) => http.post('/attendance/daily/recalc', null, { params }),
  deptDaily: (params) => http.get('/attendance/daily/dept', { params }),
  monthly: (params) => http.get('/attendance/monthly', { params }),
  generateMonthly: (params) => http.post('/attendance/monthly/generate', null, { params }),
  confirmMonthly: (yearMonth) => http.post(`/attendance/monthly/${yearMonth}/confirm`),
  lockMonthly: (yearMonth) => http.post(`/attendance/monthly/${yearMonth}/lock`),
  exportMonthly: (yearMonth) => http.get(`/attendance/monthly/${yearMonth}/export`, { responseType: 'blob' }),
  submitLeave: (data) => http.post('/attendance/leaves', data),
  submitOvertime: (data) => http.post('/attendance/overtimes', data),
  submitPatch: (data) => http.post('/attendance/patches', data),
  submitTrip: (data) => http.post('/attendance/trips', data),
}
