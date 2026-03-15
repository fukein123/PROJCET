import dayjs from 'dayjs'

export const DEFAULT_ACTIVITY_COVER =
  'https://images.unsplash.com/photo-1509062522246-3755977927d7?auto=format&fit=crop&w=700&q=80'

const activityStatusTextMap: Record<string, string> = {
  PUBLISHED: '报名中',
  ONGOING: '进行中',
  ENDED: '已结束',
  ARCHIVED: '已归档'
}

const activityStatusTagMap: Record<string, 'success' | 'warning' | 'info'> = {
  PUBLISHED: 'success',
  ONGOING: 'warning',
  ENDED: 'info',
  ARCHIVED: 'info'
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

const forumPostStatusTextMap: Record<string, string> = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
  ARCHIVED: '已归档'
}

const forumPostStatusTagMap: Record<string, 'warning' | 'success' | 'danger' | 'info'> = {
  PENDING: 'warning',
  APPROVED: 'success',
  REJECTED: 'danger',
  ARCHIVED: 'info'
}

const commentTargetTextMap: Record<string, string> = {
  POST: '论坛帖子',
  ACTIVITY: '活动评论',
  DYNAMIC: '信息动态',
  NOTICE: '公告评论'
}

const orderStatusTextMap: Record<string, string> = {
  CREATED: '待处理',
  SHIPPED: '待签收',
  RECEIVED: '已签收',
  CANCELLED: '已取消'
}

const orderStatusTagMap: Record<string, 'warning' | 'success' | 'info' | 'danger'> = {
  CREATED: 'warning',
  SHIPPED: 'info',
  RECEIVED: 'success',
  CANCELLED: 'danger'
}

const certificationStatusTextMap: Record<string, string> = {
  NOT_SUBMITTED: '待提交',
  PENDING: '待审核',
  APPROVED: '已认证',
  REJECTED: '已驳回'
}

const certificationStatusTagMap: Record<string, 'info' | 'warning' | 'success' | 'danger'> = {
  NOT_SUBMITTED: 'info',
  PENDING: 'warning',
  APPROVED: 'success',
  REJECTED: 'danger'
}

const adminActionTextMap: Record<string, string> = {
  AUDIT_APPLICATION: '审核报名申请',
  AUDIT_POST: '审核论坛帖子',
  AUDIT_CERTIFICATION: '审核认证信息',
  UPDATE_ORDER_STATUS: '修改订单状态',
  ARCHIVE_ACTIVITY_BATCH: '归档活动',
  ARCHIVE_DYNAMIC_BATCH: '归档信息动态',
  ARCHIVE_NOTICE_BATCH: '归档系统公告',
  DISABLE_MALL_PRODUCT: '停用商城商品',
  DISABLE_USER: '停用用户',
  DELETE_USER: '删除志愿者',
  DELETE_POST: '删除论坛帖子'
}

const adminTargetTextMap: Record<string, string> = {
  ACTIVITY: '志愿活动',
  APPLICATION: '活动报名申请',
  POST: '论坛帖子',
  CERTIFICATION: '认证审核',
  ORDER: '兑换订单',
  DYNAMIC: '信息动态',
  NOTICE: '系统公告',
  MALL_PRODUCT: '积分商城',
  USER: '用户信息'
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

export function getForumPostStatusLabel(status?: string) {
  return forumPostStatusTextMap[status || ''] || status || '未知'
}

export function getForumPostStatusTag(status?: string) {
  return forumPostStatusTagMap[status || ''] || 'info'
}

export function getCommentTargetLabel(targetType?: string) {
  return commentTargetTextMap[targetType || ''] || targetType || '未知'
}

export function getOrderStatusLabel(status?: string) {
  return orderStatusTextMap[status || ''] || status || '未知'
}

export function getOrderStatusTag(status?: string) {
  return orderStatusTagMap[status || ''] || 'info'
}

export function getCertificationStatusLabel(status?: string) {
  return certificationStatusTextMap[status || ''] || status || '未知'
}

export function getCertificationStatusTag(status?: string) {
  return certificationStatusTagMap[status || ''] || 'info'
}

export function getAdminActionLabel(actionType?: string) {
  return adminActionTextMap[actionType || ''] || actionType || '未知动作'
}

export function getAdminTargetLabel(targetType?: string) {
  return adminTargetTextMap[targetType || ''] || targetType || '未知对象'
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
