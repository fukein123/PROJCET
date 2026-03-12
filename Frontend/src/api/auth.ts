import request from '@/utils/request'

export interface LoginPayload {
  username: string
  password: string
  role?: 'ADMIN' | 'VOLUNTEER'
}

export interface LoginResult {
  token: string
  userId: number
  username: string
  role: 'ADMIN' | 'VOLUNTEER'
}

export interface RegisterPayload {
  username: string
  realName: string
  password: string
  confirmPassword: string
  email: string
  phone: string
  gender: string
  avatar?: string
}

export function loginApi(payload: LoginPayload) {
  return request.post<never, LoginResult>('/api/auth/login', payload)
}

export function registerApi(payload: RegisterPayload) {
  return request.post('/api/auth/register', payload)
}
