import axios from 'axios'

const API_BASE_URL = '/api'

const authApi = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000
})

// 请求拦截器
authApi.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器
authApi.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export const login = (username, password) => {
  return authApi.post('/auth/login', { username, password })
}

export const register = (userData) => {
  return authApi.post('/auth/register', userData)
}

export const getUserInfo = () => {
  return authApi.get('/auth/userinfo')
}

export default authApi
