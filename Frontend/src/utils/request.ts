import axios, { AxiosError, type AxiosInstance, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { clearAuthStorage, getToken } from './auth'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

interface ApiFailurePayload {
  code?: number
  message?: string
  data?: unknown
}

const API_ERROR_CODE = {
  REQUEST_INVALID: 40000,
  UNAUTHORIZED: 40100,
  FORBIDDEN: 40300,
  RESOURCE_NOT_FOUND: 40400,
  BUSINESS_CONFLICT: 40900,
  VALIDATION_FAILED: 42200,
  INTERNAL_ERROR: 50000
} as const

const TEST_DATA_MARKER_STORAGE_KEY = 'cvs_test_data_marker'
const TEST_DATA_MARKER_HEADER = 'X-Test-Data-Marker'

class ApiRequestError extends Error {
  code?: number
  status?: number

  constructor(message: string, options?: { code?: number; status?: number }) {
    super(message)
    this.name = 'ApiRequestError'
    this.code = options?.code
    this.status = options?.status
  }
}

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  config.url = normalizeRequestUrl(config.baseURL, config.url)
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  const testDataMarker = resolveTestDataMarker()
  if (testDataMarker) {
    config.headers[TEST_DATA_MARKER_HEADER] = testDataMarker
  }
  return config
})

request.interceptors.response.use(
  ((response: AxiosResponse<ApiResponse<unknown>>) => {
    const payload = response.data
    if (payload.code !== 0) {
      return rejectApiFailure(payload, response.status)
    }
    return payload.data as unknown
  }) as never,
  ((error: AxiosError<ApiFailurePayload>) => {
    const status = error.response?.status
    const payload = error.response?.data
    return rejectApiFailure(payload, status, error)
  }) as never
)

function rejectApiFailure(payload?: ApiFailurePayload, status?: number, error?: AxiosError<ApiFailurePayload>) {
  const code = typeof payload?.code === 'number' ? payload.code : undefined
  const rawMessage = typeof payload?.message === 'string' ? payload.message.trim() : ''
  const message = resolveErrorMessage(code, status, rawMessage, error)

  if (shouldClearAuth(code, status)) {
    clearAuthStorage()
    redirectToLogin()
  }

  ElMessage.error(message)
  return Promise.reject(new ApiRequestError(message, { code, status }))
}

function normalizeRequestUrl(baseURL?: string, requestUrl?: string) {
  if (!requestUrl) {
    return requestUrl
  }

  const normalizedBase = (baseURL || '').replace(/\/+$/, '')
  if (!normalizedBase.endsWith('/api')) {
    return requestUrl
  }

  if (requestUrl.startsWith('/api/')) {
    return requestUrl.slice(4)
  }

  if (requestUrl === '/api') {
    return '/'
  }

  return requestUrl
}

function resolveErrorMessage(
  code?: number,
  status?: number,
  rawMessage?: string,
  error?: AxiosError<ApiFailurePayload>
) {
  if (matchesError(code, status, API_ERROR_CODE.UNAUTHORIZED)) {
    return rawMessage || '登录状态已失效，请重新登录后继续操作'
  }
  if (matchesError(code, status, API_ERROR_CODE.FORBIDDEN)) {
    return rawMessage || '当前账号无权执行此操作'
  }
  if (matchesError(code, status, API_ERROR_CODE.VALIDATION_FAILED)) {
    return rawMessage || '提交内容校验未通过，请检查后重试'
  }
  if (matchesError(code, status, API_ERROR_CODE.REQUEST_INVALID)) {
    return rawMessage || '请求参数不正确，请检查后重试'
  }
  if (matchesError(code, status, API_ERROR_CODE.RESOURCE_NOT_FOUND)) {
    return rawMessage || '请求的资源不存在或已被删除'
  }
  if (matchesError(code, status, API_ERROR_CODE.BUSINESS_CONFLICT)) {
    return rawMessage || '当前操作与业务状态冲突，请刷新后重试'
  }
  if (matchesError(code, status, API_ERROR_CODE.INTERNAL_ERROR)) {
    return rawMessage || '系统繁忙，请稍后重试'
  }
  if (!error?.response) {
    return '网络异常，请检查后端服务是否已启动'
  }
  return rawMessage || '请求失败，请稍后重试'
}

function matchesError(code: number | undefined, status: number | undefined, errorCode: number) {
  if (code === errorCode) {
    return true
  }

  return getStatusByErrorCode(errorCode) === status
}

function getStatusByErrorCode(errorCode: number) {
  switch (errorCode) {
    case API_ERROR_CODE.REQUEST_INVALID:
      return 400
    case API_ERROR_CODE.UNAUTHORIZED:
      return 401
    case API_ERROR_CODE.FORBIDDEN:
      return 403
    case API_ERROR_CODE.RESOURCE_NOT_FOUND:
      return 404
    case API_ERROR_CODE.BUSINESS_CONFLICT:
      return 409
    case API_ERROR_CODE.VALIDATION_FAILED:
      return 422
    case API_ERROR_CODE.INTERNAL_ERROR:
      return 500
    default:
      return undefined
  }
}

function shouldClearAuth(code?: number, status?: number) {
  return matchesError(code, status, API_ERROR_CODE.UNAUTHORIZED)
}

function redirectToLogin() {
  const currentPath = router.currentRoute.value.path
  if (currentPath === '/login' || currentPath === '/register') {
    return
  }

  const redirect = router.currentRoute.value.fullPath || '/'
  void router.push(`/login?redirect=${encodeURIComponent(redirect)}`)
}

function resolveTestDataMarker() {
  if (typeof window === 'undefined') {
    return ''
  }

  try {
    return window.localStorage.getItem(TEST_DATA_MARKER_STORAGE_KEY)?.trim() || ''
  } catch {
    return ''
  }
}

export default request
