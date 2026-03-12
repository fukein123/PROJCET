import request from '@/utils/request'

export interface UserModel {
  id: number
  username: string
  email: string
  phone: string
  gender: string
  avatar: string
  role: 'ADMIN' | 'VOLUNTEER'
  status: number
  realName?: string
  certified: number
  createTime: string
}

export interface PageResult<T> {
  total: number
  current: number
  pageSize: number
  records: T[]
}

export function getMyProfileApi() {
  return request.get<never, UserModel>('/api/users/me')
}

export function updateMyProfileApi(payload: Partial<UserModel>) {
  return request.put('/api/users/me', payload)
}

export function updatePasswordApi(payload: { oldPassword: string; newPassword: string }) {
  return request.put('/api/users/password', payload)
}

export function pageUsersApi(params: { current: number; size: number; role?: string; keyword?: string }) {
  return request.get<never, PageResult<UserModel>>('/api/users/page', { params })
}

export function adminUpdateUserApi(id: number, payload: Partial<UserModel>) {
  return request.put(`/api/users/${id}`, payload)
}

export function createVolunteerUserApi(payload: {
  username: string
  password: string
  realName: string
  email?: string
  phone: string
  gender?: string
  avatar?: string
  status?: number
  certified?: number
}) {
  return request.post('/api/users', payload)
}

export function deleteVolunteerUserApi(id: number) {
  return request.delete(`/api/users/${id}`)
}

export function batchDeleteVolunteerUsersApi(ids: number[]) {
  return request.post('/api/users/batch-delete', { ids })
}
