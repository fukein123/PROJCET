import { applyActivityApi, undoActivityApplicationApi, type ApplicationModel } from '@/api/activity'
import { runConfirmedAction } from '@/utils/confirmed-action'
import { DEFAULT_UNDO_WINDOW_SECONDS, showTimedUndoNotification } from '@/utils/timed-undo'

interface SubmitActivityApplicationOptions {
  activityId: number
  refresh?: () => Promise<void> | void
}

export function createActivityApplyPrompt() {
  return {
    message: '请输入报名理由',
    inputPlaceholder: '请简要说明参与原因、可服务时间等',
    inputType: 'textarea' as const,
    inputValidator: (value: string) => {
      const trimmed = value.trim()
      if (!trimmed) {
        return '请输入报名理由'
      }
      if (trimmed.length > 500) {
        return '报名理由不能超过 500 字'
      }
      return true
    }
  }
}

export function normalizeApplyReason(value?: string) {
  return (value || '').trim()
}

export async function submitActivityApplicationWithUndo({ activityId, refresh }: SubmitActivityApplicationOptions) {
  const application = await runConfirmedAction<ApplicationModel>({
    message: '请输入报名理由',
    title: '报名活动',
    type: 'info',
    confirmButtonText: '提交报名',
    prompt: createActivityApplyPrompt(),
    action: (reason) =>
      applyActivityApi(activityId, {
        applyReason: normalizeApplyReason(reason)
      })
  })

  await refresh?.()

  showTimedUndoNotification({
    title: '报名申请已提交',
    message: `管理员审核前，你可以在 ${DEFAULT_UNDO_WINDOW_SECONDS} 秒内撤销本次报名，避免误操作。`,
    undoLabel: '撤销报名',
    undoSuccessMessage: '报名申请已撤销',
    onUndo: async () => {
      await undoActivityApplicationApi(application.id)
      await refresh?.()
    }
  })

  return application
}
