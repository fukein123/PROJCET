import request from '@/utils/request'
import type { PageResult } from './user'

export interface AdminOperationLogItem {
  id: number
  operatorId?: number
  operatorUsername?: string
  actionType: string
  targetType: string
  targetId?: number
  targetName?: string
  result: string
  detail?: string
  createTime?: string
}

export function pageAdminOperationLogsApi(params: {
  current: number
  size: number
  actionType?: string
  targetType?: string
  result?: string
  operatorKeyword?: string
  targetKeyword?: string
}) {
  return request.get<never, PageResult<AdminOperationLogItem>>('/api/audit/operations/page', { params })
}
