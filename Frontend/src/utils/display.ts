import dayjs from 'dayjs'

export const DEFAULT_ACTIVITY_COVER =
  'https://images.unsplash.com/photo-1509062522246-3755977927d7?auto=format&fit=crop&w=700&q=80'

const activityStatusTextMap: Record<string, string> = {
  PUBLISHED: '报名中',
  ONGOING: '进行中',
  ENDED: '已结束'
}

const activityStatusTagMap: Record<string, 'success' | 'warning' | 'info'> = {
  PUBLISHED: 'success',
  ONGOING: 'warning',
  ENDED: 'info'
}

const applicationStatusTextMap: Record<string, string> = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已拒绝'
}

const applicationStatusTagMap: Record<string, 'warning' | 'success' | 'danger' | 'info'> = {
  PENDING: 'warning',
  APPROVED: 'success',
  REJECTED: 'danger'
}

const checkRecordStatusTextMap: Record<string, string> = {
  SIGNED_IN: '已签到',
  FINISHED: '已完成'
}

const checkRecordStatusTagMap: Record<string, 'warning' | 'success' | 'info'> = {
  SIGNED_IN: 'warning',
  FINISHED: 'success'
}

export function formatDateTime(value?: string) {
  if (!value) {
    return '-'
  }
  return dayjs(value).format('YYYY-MM-DD HH:mm')
}

export function getActivityStatusLabel(status?: string) {
  return activityStatusTextMap[status || ''] || status || '未知'
}

export function getActivityStatusTag(status?: string) {
  return activityStatusTagMap[status || '']
}

export function getApplicationStatusLabel(status?: string) {
  return applicationStatusTextMap[status || ''] || status || '未知'
}

export function getApplicationStatusTag(status?: string) {
  return applicationStatusTagMap[status || ''] || 'info'
}

export function getCheckRecordStatusLabel(status?: string) {
  return checkRecordStatusTextMap[status || ''] || status || '未知'
}

export function getCheckRecordStatusTag(status?: string) {
  return checkRecordStatusTagMap[status || ''] || 'info'
}

export function toServiceHours(minutes?: number) {
  const value = minutes || 0
  if (!value) {
    return '0 分钟'
  }

  const hours = Math.floor(value / 60)
  const remainMinutes = value % 60
  if (!hours) {
    return `${remainMinutes} 分钟`
  }

  return `${hours} 小时 ${remainMinutes} 分钟`
}
