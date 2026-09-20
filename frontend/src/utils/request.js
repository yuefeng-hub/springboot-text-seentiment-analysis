import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 统一的 axios 实例：baseURL 为 /api（开发时由 Vite 代理到 8080）
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截：自动附带 token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截：统一错误提示；401 清登录态并跳转登录页
request.interceptors.response.use(
  response => response.data,
  error => {
    const res = error.response
    if (res && res.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
    } else {
      const msg = (res && res.data && res.data.message) || error.message || '请求失败'
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  }
)

export default request
