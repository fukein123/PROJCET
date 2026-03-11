import { request } from '@/utils/request'

export type LoginType = 'ADMIN' | 'VOLUNTEER'

export interface LoginReq {
  username: string
  password: string
  loginType: LoginType
}

export interface UserMe {
  id: number
  username: string
  email?: string
  phone?: string
  avatarUrl?: string
  gender?: number
  role: string
}

export interface LoginResp {
  token: string
  user: UserMe
}

export function apiLogin(data: LoginReq) {
  return request.post<any, LoginResp>('/auth/login', data)
}

export function apiRegister(data: {
  username: string
  password: string
  email?: string
  phone?: string
  avatarUrl?: string
  gender?: number
}) {
  return request.post<any, UserMe>('/auth/register', data)
}

export function apiMe() {
  return request.get<any, UserMe>('/auth/me')
}

