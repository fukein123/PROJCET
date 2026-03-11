import axios from 'axios'
import { ElMessage } from 'element-plus'
import { clearRole, clearToken, getToken } from './auth'

export const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (resp) => {
    const payload = resp.data
    if (payload && typeof payload.code === 'number') {
      if (payload.code === 0) return payload.data
      if (payload.code === 401) {
        clearToken()
        clearRole()
      }
      ElMessage.error(payload.message || '请求失败')
      return Promise.reject(payload)
    }
    return payload
  },
  (err) => {
    ElMessage.error(err?.message || '网络错误')
    return Promise.reject(err)
  }
)

