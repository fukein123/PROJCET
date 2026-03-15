import { defineComponent, nextTick } from 'vue'
import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const notificationCloseMock = vi.fn()
const notificationMock = vi.fn(() => ({
  close: notificationCloseMock
}))
const successMock = vi.fn()

vi.mock('element-plus', () => ({
  ElButton: defineComponent({
    name: 'ElButton',
    emits: ['click'],
    props: {
      loading: Boolean,
      type: String,
      plain: Boolean,
      size: String
    },
    template: '<button :data-loading="loading" @click="$emit(\'click\')"><slot /></button>'
  }),
  ElMessage: {
    success: (...args: unknown[]) => successMock(...args)
  },
  ElNotification: (...args: unknown[]) => notificationMock(...args)
}))

const { DEFAULT_UNDO_WINDOW_MS, DEFAULT_UNDO_WINDOW_SECONDS, showTimedUndoNotification } = await import('../timed-undo')

describe('timed undo notification', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.useFakeTimers()
  })

  it('uses the extended 30-second undo window by default', () => {
    showTimedUndoNotification({
      title: '帖子已提交',
      message: '可在窗口期内撤销',
      onUndo: vi.fn()
    })

    expect(DEFAULT_UNDO_WINDOW_MS).toBe(30_000)
    expect(DEFAULT_UNDO_WINDOW_SECONDS).toBe(30)
    expect(notificationMock).toHaveBeenCalledTimes(1)
    expect(notificationMock.mock.calls[0]?.[0]).toMatchObject({
      title: '帖子已提交',
      duration: 30_000,
      position: 'bottom-right',
      customClass: 'timed-undo-notification'
    })
  })

  it('updates countdown and executes undo callback from the notification body', async () => {
    const onUndo = vi.fn().mockResolvedValue(undefined)

    showTimedUndoNotification({
      title: '报名成功',
      message: '你可以在限定时间内撤销本次操作',
      onUndo
    })

    const options = notificationMock.mock.calls[0]?.[0] as { message: unknown }
    const Host = defineComponent({
      render: () => options.message as never
    })

    const wrapper = mount(Host)
    expect(wrapper.text()).toContain('30')

    vi.advanceTimersByTime(1_000)
    await nextTick()
    expect(wrapper.text()).toContain('29')

    await wrapper.get('button').trigger('click')
    await flushPromises()

    expect(onUndo).toHaveBeenCalledTimes(1)
    expect(successMock).toHaveBeenCalledWith('操作已撤销')
    expect(notificationCloseMock).toHaveBeenCalledTimes(1)
  })
})
