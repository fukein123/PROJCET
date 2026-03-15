import request from '@/utils/request'

export type CertificationStatus = 'NOT_SUBMITTED' | 'PENDING' | 'APPROVED' | 'REJECTED'

export interface UserModel {
  id: number
  username: string
  email: string
  phone: string
  gender: string
  avatar: string
  role: 'ADMIN' | 'VOLUNTEER'
  status: number
  points?: number
  realName?: string
  certified: number
  certificationId?: number
  certificationStatus?: CertificationStatus
  certificationRejectReason?: string
  certificationIdCardNo?: string
  certificationIdCardFrontUrl?: string
  certificationIdCardBackUrl?: string
  certificationSubmitTime?: string
  certificationAuditTime?: string
  certificationAuditorId?: number
  createTime: string
}

export interface VolunteerCertificationModel {
  id?: number
  userId: number
  realName?: string
  idCardNo?: string
  idCardFrontUrl?: string
  idCardBackUrl?: string
  status: CertificationStatus
  rejectReason?: string
  submitTime?: string
  auditTime?: string
  auditorId?: number
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

export function getMyCertificationApi() {
  return request.get<never, VolunteerCertificationModel>('/api/users/me/certification')
}

export function submitMyCertificationApi(payload: {
  realName: string
  idCardNo: string
  idCardFrontUrl: string
  idCardBackUrl: string
}) {
  return request.post('/api/users/me/certification', payload)
}

export function pageUsersApi(params: {
  current: number
  size: number
  role?: string
  keyword?: string
  status?: number
  certified?: number
  certificationStatus?: CertificationStatus
}) {
  return request.get<never, PageResult<UserModel>>('/api/users/page', { params })
}

export function getUserDetailApi(id: number) {
  return request.get<never, UserModel>(`/api/users/${id}`)
}

export function adminUpdateUserApi(id: number, payload: Partial<UserModel>) {
  return request.put(`/api/users/${id}`, payload)
}

export function getUserCertificationDetailApi(id: number) {
  return request.get<never, VolunteerCertificationModel>(`/api/users/${id}/certification`)
}

export function auditUserCertificationApi(
  id: number,
  payload: { status: Extract<CertificationStatus, 'APPROVED' | 'REJECTED'>; rejectReason?: string }
) {
  return request.put(`/api/users/${id}/certification/audit`, payload)
}

export function createUserApi(payload: {
  username: string
  password: string
  realName: string
  email?: string
  phone: string
  gender?: string
  avatar?: string
  role: 'ADMIN' | 'VOLUNTEER'
  status?: number
}) {
  return request.post('/api/users', payload)
}

export function disableUserApi(id: number) {
  return request.put(`/api/users/${id}/disable`)
}

export function enableUserApi(id: number) {
  return request.put(`/api/users/${id}/enable`)
}

export function batchDisableUsersApi(ids: number[]) {
  return request.post('/api/users/batch-disable', { ids })
}

export function batchEnableUsersApi(ids: number[]) {
  return request.post('/api/users/batch-enable', { ids })
}

export function deleteUserApi(id: number) {
  return request.delete(`/api/users/${id}`)
}

export function batchDeleteUsersApi(ids: number[]) {
  return request.post('/api/users/batch-delete', { ids })
}
