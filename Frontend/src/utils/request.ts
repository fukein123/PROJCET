import axios, { type AxiosInstance, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { clearAuthStorage, getToken } from './auth'
import router from '@/router'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  ((response: AxiosResponse<ApiResponse<unknown>>) => {
    const payload = response.data
    if (payload.code !== 0) {
      ElMessage.error(payload.message || '请求失败，请稍后重试')
      return Promise.reject(new Error(payload.message))
    }
    return payload.data as unknown
  }) as never,
  ((error: any) => {
    const status = error?.response?.status
    if (status === 401) {
      clearAuthStorage()
      ElMessage.error('登录状态已过期，请重新登录')
      router.push('/login')
    } else {
      const message = error?.response?.data?.message
      ElMessage.error(typeof message === 'string' && message.trim() ? message : '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }) as never
)

export default request
