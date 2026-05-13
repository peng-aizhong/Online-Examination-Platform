import request from './auth'   // 复用你已有的 axios 实例（已带 token 拦截器）

export const getUserList = (params) => {
    return request.get('/users', { params })
}

export const getUserById = (userId) => {
    return request.get(`/users/${userId}`)
}

export const createUser = (data) => {
    return request.post('/users', data)
}

export const updateUser = (userId, data) => {
    return request.put(`/users/${userId}`, data)
}

export const resetUserPassword = (userId, newPassword) => {
    return request.post(`/users/${userId}/reset-password`, null, { params: { newPassword } })
}

export const deleteUser = (userId) => {
    return request.delete(`/users/${userId}`)
}