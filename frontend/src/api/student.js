import authApi from './auth'

export const getStudentAssignments = () => {
  return authApi.get('/student/assignments')
}

export const getStudentAssignmentDetail = (assignmentId) => {
  return authApi.get(`/student/assignments/${assignmentId}`)
}

export const submitStudentExam = (assignmentId, sessionId, answers) => {
  return authApi.post(`/student/assignments/${assignmentId}/submit`, { sessionId, answers })
}

export const getStudentResults = () => {
  return authApi.get('/student/results')
}

export const getStudentWrongQuestions = () => {
  return authApi.get('/student/wrong-questions')
}

export const getStudentScoreReport = (sessionId) => {
  return authApi.get(`/student/report/${sessionId}`)
}

// 学习资源
export const getStudentResources = (params) => {
  return authApi.get('/student/resources', { params })
}

export const getStudentResourceDetail = (id) => {
  return authApi.get(`/student/resources/${id}`)
}

export const downloadStudentResource = (id) => {
  return authApi.get(`/student/resources/${id}/download`, { responseType: 'blob' })
}

export const getSubjects = () => {
  return authApi.get('/system/subjects')
}
