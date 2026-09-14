import http from './request'

export const orgApi = {
  depts: () => http.get('/org/depts/tree'),
  createDept: (data) => http.post('/org/depts', data),
  updateDept: (id, data) => http.put(`/org/depts/${id}`, data),
  deleteDept: (id) => http.delete(`/org/depts/${id}`),
  positions: () => http.get('/org/positions'),
  createPosition: (data) => http.post('/org/positions', data),
  updatePosition: (id, data) => http.put(`/org/positions/${id}`, data),
  deletePosition: (id) => http.delete(`/org/positions/${id}`),
  grades: () => http.get('/org/grades'),
  createGrade: (data) => http.post('/org/grades', data),
  updateGrade: (id, data) => http.put(`/org/grades/${id}`, data),
  deleteGrade: (id) => http.delete(`/org/grades/${id}`),
}
