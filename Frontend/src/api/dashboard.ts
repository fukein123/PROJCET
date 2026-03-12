import request from '@/utils/request'

export interface DashboardPayload {
  activityCount: number
  postCount: number
  commentCount: number
  volunteerCount: number
  weeklyApplicationTrend: Array<{ day: string; value: number }>
  activityTypeBar: Array<{ name: string; value: number }>
  postTypePie: Array<{ name: string; value: number }>
}

export function adminDashboardApi() {
  return request.get<never, DashboardPayload>('/api/dashboard/admin')
}

export interface VolunteerWeeklyRankingItem {
  rankNo: number
  userId: number
  username: string
  realName: string
  completedCount: number
  signInCount: number
  signOutCount: number
  serviceMinutes: number
  serviceHours: string
}

export interface VolunteerWeeklyRankingPayload {
  weekStart: string
  weekEnd: string
  generatedTime?: string
  total: number
  records: VolunteerWeeklyRankingItem[]
}

export function weeklyVolunteerRankingApi(params?: { weekStart?: string; size?: number }) {
  return request.get<never, VolunteerWeeklyRankingPayload>('/api/dashboard/weekly-ranking', { params })
}

export function rebuildWeeklyVolunteerRankingApi(params?: { weekStart?: string }) {
  return request.post('/api/dashboard/weekly-ranking/rebuild', null, { params })
}
