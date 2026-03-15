import { defineComponent, h, ref } from 'vue'
import { ElButton, ElMessage, ElNotification } from 'element-plus'

interface TimedUndoOptions {
  title: string
  message: string
  undoLabel?: string
  durationMs?: number
  undoSuccessMessage?: string
  onUndo: () => Promise<void> | void
}

export const DEFAULT_UNDO_WINDOW_MS = 30_000
export const DEFAULT_UNDO_WINDOW_SECONDS = DEFAULT_UNDO_WINDOW_MS / 1000

export function showTimedUndoNotification({
  title,
  message,
  undoLabel = '撤销',
  durationMs = DEFAULT_UNDO_WINDOW_MS,
  undoSuccessMessage = '操作已撤销',
  onUndo
}: TimedUndoOptions) {
  const remainingSeconds = ref(Math.max(1, Math.ceil(durationMs / 1000)))
  const undoing = ref(false)
  let timer: number | undefined
  let closed = false

  const clearTimer = () => {
    if (timer) {
      window.clearInterval(timer)
      timer = undefined
    }
  }

  const notification = ElNotification({
    title,
    duration: durationMs,
    position: 'bottom-right',
    customClass: 'timed-undo-notification',
    message: h(
      defineComponent({
        name: 'TimedUndoNotificationBody',
        setup() {
          const handleUndo = async () => {
            if (undoing.value || closed) {
              return
            }

            undoing.value = true
            try {
              await onUndo()
              ElMessage.success(undoSuccessMessage)
              notification.close()
            } catch {
              undoing.value = false
            }
          }

          return () =>
            h('div', { style: 'display:grid;gap:10px;min-width:280px;' }, [
              h('p', { style: 'margin:0;color:#52635d;line-height:1.6;' }, message),
              h('div', { style: 'display:flex;align-items:center;justify-content:space-between;gap:12px;' }, [
                h('span', { style: 'color:#1f7a54;font-size:12px;font-weight:600;' }, `${remainingSeconds.value} 秒内可撤销`),
                h(
                  ElButton,
                  {
                    type: 'primary',
                    plain: true,
                    size: 'small',
                    loading: undoing.value,
                    onClick: handleUndo
                  },
                  () => undoLabel
                )
              ])
            ])
        }
      })
    ),
    onClose: () => {
      closed = true
      clearTimer()
    }
  })

  timer = window.setInterval(() => {
    remainingSeconds.value = Math.max(0, remainingSeconds.value - 1)
    if (remainingSeconds.value <= 0) {
      clearTimer()
    }
  }, 1000)
}
