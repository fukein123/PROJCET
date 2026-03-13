import { defineComponent, nextTick } from 'vue'
import { flushPromises, shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import ContentManageView from '../ContentManageView.vue'

const emptyPageResult = {
  total: 0,
  records: []
}

const pageDynamicsApi = vi.fn()
const pageNoticesApi = vi.fn()
const pageForumPostsApi = vi.fn()
const pageCommentsApi = vi.fn()
const auditPostApi = vi.fn()
const batchArchiveDynamicsApi = vi.fn()
const batchRestoreDynamicsApi = vi.fn()
const batchArchiveNoticesApi = vi.fn()
const batchRestoreNoticesApi = vi.fn()
const saveDynamicApi = vi.fn()
const updateDynamicApi = vi.fn()
const deleteDynamicApi = vi.fn()
const batchDeleteDynamicsApi = vi.fn()
const saveNoticeApi = vi.fn()
const updateNoticeApi = vi.fn()
const deleteNoticeApi = vi.fn()
const batchDeleteNoticesApi = vi.fn()
const deleteForumPostApi = vi.fn()
const batchDeleteForumPostsApi = vi.fn()
const deleteCommentApi = vi.fn()
const batchDeleteCommentsApi = vi.fn()
const uploadImageApi = vi.fn()
const messageBoxConfirm = vi.fn()
const messageBoxPrompt = vi.fn()

vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    warning: vi.fn()
  },
  ElMessageBox: {
    confirm: (...args: unknown[]) => messageBoxConfirm(...args),
    prompt: (...args: unknown[]) => messageBoxPrompt(...args)
  }
}))

vi.mock('@/api/common', () => ({
  uploadImageApi: (...args: unknown[]) => uploadImageApi(...args)
}))

vi.mock('@/api/content', () => ({
  auditPostApi: (...args: unknown[]) => auditPostApi(...args),
  batchArchiveDynamicsApi: (...args: unknown[]) => batchArchiveDynamicsApi(...args),
  batchArchiveNoticesApi: (...args: unknown[]) => batchArchiveNoticesApi(...args),
  batchDeleteCommentsApi: (...args: unknown[]) => batchDeleteCommentsApi(...args),
  batchDeleteDynamicsApi: (...args: unknown[]) => batchDeleteDynamicsApi(...args),
  batchDeleteForumPostsApi: (...args: unknown[]) => batchDeleteForumPostsApi(...args),
  batchDeleteNoticesApi: (...args: unknown[]) => batchDeleteNoticesApi(...args),
  batchRestoreDynamicsApi: (...args: unknown[]) => batchRestoreDynamicsApi(...args),
  batchRestoreNoticesApi: (...args: unknown[]) => batchRestoreNoticesApi(...args),
  deleteCommentApi: (...args: unknown[]) => deleteCommentApi(...args),
  deleteDynamicApi: (...args: unknown[]) => deleteDynamicApi(...args),
  deleteForumPostApi: (...args: unknown[]) => deleteForumPostApi(...args),
  deleteNoticeApi: (...args: unknown[]) => deleteNoticeApi(...args),
  pageCommentsApi: (...args: unknown[]) => pageCommentsApi(...args),
  pageDynamicsApi: (...args: unknown[]) => pageDynamicsApi(...args),
  pageForumPostsApi: (...args: unknown[]) => pageForumPostsApi(...args),
  pageNoticesApi: (...args: unknown[]) => pageNoticesApi(...args),
  saveDynamicApi: (...args: unknown[]) => saveDynamicApi(...args),
  saveNoticeApi: (...args: unknown[]) => saveNoticeApi(...args),
  updateDynamicApi: (...args: unknown[]) => updateDynamicApi(...args),
  updateNoticeApi: (...args: unknown[]) => updateNoticeApi(...args)
}))

const ElTabsStub = defineComponent({
  name: 'ElTabs',
  props: {
    modelValue: {
      type: String,
      required: false
    }
  },
  emits: ['update:modelValue'],
  template: '<div><slot /></div>'
})

const ElTabPaneStub = defineComponent({
  name: 'ElTabPane',
  template: '<div><slot /></div>'
})

function mountView() {
  return shallowMount(ContentManageView, {
    global: {
      stubs: {
        AdminListScaffold: {
          template: '<div><slot name="filters" /><slot /><slot name="pagination" /></div>'
        },
        AdminContentSection: {
          template:
            '<div><slot name="actions" /><slot name="emptyActions" /><slot /><slot name="pagination" /></div>'
        },
        StatePanel: {
          template: '<div><slot name="actions" /></div>'
        },
        'el-tabs': ElTabsStub,
        'el-tab-pane': ElTabPaneStub,
        'el-table': {
          template: '<div><slot /></div>'
        },
        'el-table-column': {
          template: '<div />'
        },
        'el-tag': {
          template: '<span><slot /></span>'
        },
        'el-button': {
          emits: ['click'],
          template: '<button @click="$emit(\'click\')"><slot /></button>'
        },
        'el-dialog': {
          template: '<div><slot /><slot name="footer" /></div>'
        },
        'el-form': {
          template: '<form><slot /></form>'
        },
        'el-form-item': {
          template: '<div><slot /></div>'
        },
        'el-input': {
          template: '<input />'
        },
        'el-select': {
          template: '<select><slot /></select>'
        },
        'el-option': {
          template: '<option><slot /></option>'
        },
        'el-switch': {
          template: '<input type="checkbox" />'
        },
        'el-upload': {
          template: '<div><slot /></div>'
        },
        'el-pagination': {
          template: '<div />'
        }
      }
    }
  })
}

describe('ContentManageView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    messageBoxConfirm.mockResolvedValue(undefined)
    messageBoxPrompt.mockResolvedValue({ value: '测试原因' })
    pageDynamicsApi.mockResolvedValue(emptyPageResult)
    pageNoticesApi.mockResolvedValue(emptyPageResult)
    pageForumPostsApi.mockResolvedValue(emptyPageResult)
    pageCommentsApi.mockResolvedValue(emptyPageResult)
    auditPostApi.mockResolvedValue(undefined)
    batchArchiveDynamicsApi.mockResolvedValue(undefined)
    batchRestoreDynamicsApi.mockResolvedValue(undefined)
    batchArchiveNoticesApi.mockResolvedValue(undefined)
    batchRestoreNoticesApi.mockResolvedValue(undefined)
    saveDynamicApi.mockResolvedValue(undefined)
    updateDynamicApi.mockResolvedValue(undefined)
    deleteDynamicApi.mockResolvedValue(undefined)
    batchDeleteDynamicsApi.mockResolvedValue(undefined)
    saveNoticeApi.mockResolvedValue(undefined)
    updateNoticeApi.mockResolvedValue(undefined)
    deleteNoticeApi.mockResolvedValue(undefined)
    batchDeleteNoticesApi.mockResolvedValue(undefined)
    deleteForumPostApi.mockResolvedValue(undefined)
    batchDeleteForumPostsApi.mockResolvedValue(undefined)
    deleteCommentApi.mockResolvedValue(undefined)
    batchDeleteCommentsApi.mockResolvedValue(undefined)
    uploadImageApi.mockResolvedValue({ url: 'https://example.com/dynamic.jpg' })
  })

  it('loads only the current tab on mount', async () => {
    mountView()
    await flushPromises()

    expect(pageDynamicsApi).toHaveBeenCalledTimes(1)
    expect(pageDynamicsApi).toHaveBeenCalledWith({ current: 1, size: 10, onlyPublished: false })
    expect(pageNoticesApi).not.toHaveBeenCalled()
    expect(pageForumPostsApi).not.toHaveBeenCalled()
    expect(pageCommentsApi).not.toHaveBeenCalled()
  })

  it('refreshes only the active module after post audit', async () => {
    const wrapper = mountView()
    await flushPromises()

    const tabs = wrapper.findComponent(ElTabsStub)
    tabs.vm.$emit('update:modelValue', 'post')
    await nextTick()
    await flushPromises()

    const setupState = (wrapper.vm as any).$
      .setupState as {
        audit: (id: number, status: string) => Promise<void>
      }

    await setupState.audit(1, 'APPROVED')
    await flushPromises()

    expect(auditPostApi).toHaveBeenCalledWith(1, { status: 'APPROVED', reason: '' })
    expect(pageDynamicsApi).toHaveBeenCalledTimes(1)
    expect(pageNoticesApi).not.toHaveBeenCalled()
    expect(pageCommentsApi).not.toHaveBeenCalled()
    expect(pageForumPostsApi).toHaveBeenCalledTimes(2)
    expect(pageForumPostsApi).toHaveBeenNthCalledWith(1, { current: 1, size: 10, onlyApproved: false })
    expect(pageForumPostsApi).toHaveBeenNthCalledWith(2, { current: 1, size: 10, onlyApproved: false })
  })

  it('paginates only the current comment tab', async () => {
    const wrapper = mountView()
    await flushPromises()

    const tabs = wrapper.findComponent(ElTabsStub)
    tabs.vm.$emit('update:modelValue', 'comment')
    await nextTick()
    await flushPromises()

    const setupState = (wrapper.vm as any).$
      .setupState as {
        handleCommentPageChange: (page: number) => Promise<void>
      }

    await setupState.handleCommentPageChange(3)
    await flushPromises()

    expect(pageDynamicsApi).toHaveBeenCalledTimes(1)
    expect(pageNoticesApi).not.toHaveBeenCalled()
    expect(pageForumPostsApi).not.toHaveBeenCalled()
    expect(pageCommentsApi).toHaveBeenCalledTimes(2)
    expect(pageCommentsApi).toHaveBeenNthCalledWith(1, { current: 1, size: 10, includeTestData: true })
    expect(pageCommentsApi).toHaveBeenNthCalledWith(2, { current: 3, size: 10, includeTestData: true })
  })

  it('refreshes only the dynamic tab after saving a dynamic', async () => {
    const wrapper = mountView()
    await flushPromises()

    const setupState = (wrapper.vm as any).$
      .setupState as {
        dynamicForm: { title: string; content: string; type: string; status: number }
        submitDynamic: () => Promise<void>
      }

    setupState.dynamicForm.title = 'Morning Briefing'
    setupState.dynamicForm.content = 'Volunteer team is ready'
    setupState.dynamicForm.type = 'NEWS'
    setupState.dynamicForm.status = 1

    await setupState.submitDynamic()
    await flushPromises()

    expect(saveDynamicApi).toHaveBeenCalledTimes(1)
    expect(pageDynamicsApi).toHaveBeenCalledTimes(2)
    expect(pageDynamicsApi).toHaveBeenNthCalledWith(2, { current: 1, size: 10, onlyPublished: false })
    expect(pageNoticesApi).not.toHaveBeenCalled()
    expect(pageForumPostsApi).not.toHaveBeenCalled()
    expect(pageCommentsApi).not.toHaveBeenCalled()
  })

  it('blocks saving a dynamic when title is blank', async () => {
    const wrapper = mountView()
    await flushPromises()

    const setupState = (wrapper.vm as any).$
      .setupState as {
        dynamicForm: { title: string; content: string; type: string; status: number }
        submitDynamic: () => Promise<void>
      }

    setupState.dynamicForm.title = '   '
    setupState.dynamicForm.content = 'Volunteer team is ready'
    setupState.dynamicForm.type = 'NEWS'
    setupState.dynamicForm.status = 1

    await setupState.submitDynamic()
    await flushPromises()

    expect(saveDynamicApi).not.toHaveBeenCalled()
    expect(pageDynamicsApi).toHaveBeenCalledTimes(1)
  })

  it('refreshes only the dynamic tab after batch archiving dynamics', async () => {
    const wrapper = mountView()
    await flushPromises()

    const setupState = (wrapper.vm as any).$
      .setupState as {
        onDynamicSelection: (rows: Array<{ id: number }>) => void
        batchArchiveDynamics: () => Promise<void>
      }

    setupState.onDynamicSelection([{ id: 7 }, { id: 8 }])

    await setupState.batchArchiveDynamics()
    await flushPromises()

    expect(batchArchiveDynamicsApi).toHaveBeenCalledWith([7, 8])
    expect(pageDynamicsApi).toHaveBeenCalledTimes(2)
    expect(pageNoticesApi).not.toHaveBeenCalled()
    expect(pageForumPostsApi).not.toHaveBeenCalled()
    expect(pageCommentsApi).not.toHaveBeenCalled()
  })

  it('refreshes only the notice tab after batch deleting notices', async () => {
    const wrapper = mountView()
    await flushPromises()

    const tabs = wrapper.findComponent(ElTabsStub)
    tabs.vm.$emit('update:modelValue', 'notice')
    await nextTick()
    await flushPromises()

    const setupState = (wrapper.vm as any).$
      .setupState as {
        onNoticeSelection: (rows: Array<{ id: number }>) => void
        batchDeleteNotices: () => Promise<void>
      }

    setupState.onNoticeSelection([{ id: 4 }, { id: 5 }])

    await setupState.batchDeleteNotices()
    await flushPromises()

    expect(batchDeleteNoticesApi).toHaveBeenCalledWith([4, 5])
    expect(pageDynamicsApi).toHaveBeenCalledTimes(1)
    expect(pageNoticesApi).toHaveBeenCalledTimes(2)
    expect(pageNoticesApi).toHaveBeenNthCalledWith(1, { current: 1, size: 10, onlyPublished: false })
    expect(pageNoticesApi).toHaveBeenNthCalledWith(2, { current: 1, size: 10, onlyPublished: false })
    expect(pageForumPostsApi).not.toHaveBeenCalled()
    expect(pageCommentsApi).not.toHaveBeenCalled()
  })

  it('refreshes only the notice tab after restoring notices', async () => {
    const wrapper = mountView()
    await flushPromises()

    const tabs = wrapper.findComponent(ElTabsStub)
    tabs.vm.$emit('update:modelValue', 'notice')
    await nextTick()
    await flushPromises()

    const setupState = (wrapper.vm as any).$
      .setupState as {
        onNoticeSelection: (rows: Array<{ id: number }>) => void
        batchRestoreNotices: () => Promise<void>
      }

    setupState.onNoticeSelection([{ id: 12 }, { id: 13 }])

    await setupState.batchRestoreNotices()
    await flushPromises()

    expect(batchRestoreNoticesApi).toHaveBeenCalledWith([12, 13])
    expect(pageDynamicsApi).toHaveBeenCalledTimes(1)
    expect(pageNoticesApi).toHaveBeenCalledTimes(2)
    expect(pageForumPostsApi).not.toHaveBeenCalled()
    expect(pageCommentsApi).not.toHaveBeenCalled()
  })
})
