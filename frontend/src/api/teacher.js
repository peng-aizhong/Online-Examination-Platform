import authApi from './auth'

export const getTeacherStatistics = () => {
  return authApi.get('/teacher/statistics')
}

export const getTeacherGradingResults = (assignmentId) => {
  return authApi.get(`/teacher/grading/${assignmentId}`)
}

// 题库管理（已修改为调用我们新增的v2接口）
export const getQuestions = (params) => {
  return authApi.get('/teacher/questions/v2', { params })
}

export const getQuestion = (id) => {
  // 适配我们修改后的详情接口路径：/{id}/detail
  return authApi.get(`/teacher/questions/v2/${id}/detail`)
}

export const createQuestion = (data) => {
  return authApi.post('/teacher/questions/v2', data)
}

export const updateQuestion = (id, data) => {
  // 注意：我们的后端暂未实现更新接口，如需使用请补充后端代码
  return authApi.put(`/teacher/questions/v2/${id}`, data)
}

export const deleteQuestion = (id) => {
  // 注意：我们的后端暂未实现删除接口，如需使用请补充后端代码
  return authApi.delete(`/teacher/questions/v2/${id}`)
}

// 试卷管理（保持不变）
export const getPapers = () => {
  return authApi.get('/teacher/papers')
}

export const getPaper = (id) => {
  return authApi.get(`/teacher/papers/${id}`)
}

export const createPaper = (data) => {
  return authApi.post('/teacher/papers', data)
}

export const updatePaper = (id, data) => {
  return authApi.put(`/teacher/papers/${id}`, data)
}

export const deletePaper = (id) => {
  return authApi.delete(`/teacher/papers/${id}`)
}

export const addPaperQuestion = (paperId, questionId, score) => {
  return authApi.post(`/teacher/papers/${paperId}/questions`, null, {
    params: { questionId, score }
  })
}

export const removePaperQuestion = (paperId, questionId) => {
  return authApi.delete(`/teacher/papers/${paperId}/questions/${questionId}`)
}

// 学习资源管理（保持不变）
export const getTeacherResources = (params) => {
  return authApi.get('/teacher/resources', { params })
}

export const uploadResource = (formData) => {
  return authApi.post('/teacher/resources/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const createLinkResource = (data) => {
  return authApi.post('/teacher/resources/link', data)
}

export const updateResource = (id, data) => {
  return authApi.put(`/teacher/resources/${id}`, data)
}

export const deleteResource = (id) => {
  return authApi.delete(`/teacher/resources/${id}`)
}

// 考试分配管理（保持不变）
export const createAssignment = (data) => {
  return authApi.post('/teacher/assignments', data)
}

export const getTeacherAssignments = () => {
  return authApi.get('/teacher/assignments')
}

export const getStudentList = () => {
  return authApi.get('/teacher/students')
}

// 主观题评分（保持不变）
export const gradeSubjective = (sessionId, data) => {
  return authApi.post(`/teacher/grading/${sessionId}/grade`, data)
}

// 获取科目列表（保持不变）
export const getSubjects = () => {
  return authApi.get('/system/subjects')
}