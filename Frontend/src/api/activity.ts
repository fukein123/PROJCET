import request from '@/utils/request'
import type { PageResult } from './user'

export interface ActivityCategory {
  id: number
  name: string
  description: string
  sort: number
  status: number
}

export interface ActivityModel {
  id: number
  title: string
  categoryId: number
  startTime: string
  endTime: string
  address: string
  status: string
  targetCount: number
  volunteerQuota: number
  content: string
  description: string
  coverImage?: string
  createTime?: string
}

export interface ApplicationModel {
  id: number
  activityId: number
  activityTitle?: string
  userId: number
  username?: string
  realName?: string
  status: string
  rejectReason?: string
  applyTime: string
  auditTime?: string
}

export interface CheckRecordModel {
  id: number
  activityId: number
  activityTitle?: string
  activityAddress?: string
  activityStartTime?: string
  activityEndTime?: string
  signInTime?: string
  signOutTime?: string
  signInDistance?: number
  signOutDistance?: number
  serviceMinutes?: number
  status: string
}

export function listCategoriesApi() {
  return request.get<never, ActivityCategory[]>('/api/activity/categories')
}

export function createCategoryApi(payload: Partial<ActivityCategory>) {
  return request.post('/api/activity/categories', payload)
}

export function updateCategoryApi(id: number, payload: Partial<ActivityCategory>) {
  return request.put(`/api/activity/categories/${id}`, payload)
}

export function deleteCategoryApi(id: number) {
  return request.delete(`/api/activity/categories/${id}`)
}

export function batchDeleteCategoriesApi(ids: number[]) {
  return request.post('/api/activity/categories/batch-delete', { ids })
}

export function pageActivitiesApi(params: {
  current: number
  size: number
  keyword?: string
  categoryId?: number
  status?: string
}) {
  return request.get<never, PageResult<ActivityModel>>('/api/activity/page', { params })
}

export function activityDetailApi(id: number) {
  return request.get<never, ActivityModel>(`/api/activity/${id}`)
}

export function createActivityApi(payload: Partial<ActivityModel>) {
  return request.post('/api/activity', payload)
}

export function updateActivityApi(id: number, payload: Partial<ActivityModel>) {
  return request.put(`/api/activity/${id}`, payload)
}

export function deleteActivityApi(id: number) {
  return request.delete(`/api/activity/${id}`)
}

export function batchDeleteActivitiesApi(ids: number[]) {
  return request.post('/api/activity/batch-delete', { ids })
}

export function applyActivityApi(activityId: number) {
  return request.post(`/api/activity/${activityId}/apply`)
}

export function pageApplicationsApi(params: { current: number; size: number; activityId?: number; status?: string }) {
  return request.get<never, PageResult<ApplicationModel>>('/api/activity/applications/page', { params })
}

export function myApplicationsApi(params: { current: number; size: number; activityId?: number; status?: string }) {
  return request.get<never, PageResult<ApplicationModel>>('/api/activity/applications/my', { params })
}

export function auditApplicationApi(id: number, payload: { status: string; rejectReason?: string }) {
  return request.put(`/api/activity/applications/${id}/audit`, payload)
}

export function signInApi(payload: { applicationId: number }) {
  return request.post('/api/activity/sign/in', payload)
}

export function signOutApi(payload: { applicationId: number }) {
  return request.post('/api/activity/sign/out', payload)
}

export function myCheckRecordsApi(params: { current: number; size: number }) {
  return request.get<never, PageResult<CheckRecordModel>>('/api/activity/sign/my-records', { params })
}
