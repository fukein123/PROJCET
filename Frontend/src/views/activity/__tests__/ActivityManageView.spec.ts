import { flushPromises, shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import ActivityManageView from '../ActivityManageView.vue'

const pageActivitiesApi = vi.fn()
const listCategoriesApi = vi.fn()
const batchArchiveActivitiesApi = vi.fn()
const batchRestoreActivitiesApi = vi.fn()
const batchDeleteActivitiesApi = vi.fn()
const createActivityApi = vi.fn()
const updateActivityApi = vi.fn()
const deleteActivityApi = vi.fn()
const uploadImageApi = vi.fn()
const runConfirmedAction = vi.fn(async (options: Record<string, unknown>) => {
  if (typeof options.action === 'function') {
    await options.action()
  }
  if (typeof options.afterSuccess === 'function') {
    await options.afterSuccess()
  }
})

vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    warning: vi.fn()
  }
}))

vi.mock('@/api/common', () => ({
  uploadImageApi: (...args: unknown[]) => uploadImageApi(...args)
}))

vi.mock('@/api/activity', () => ({
  batchArchiveActivitiesApi: (...args: unknown[]) => batchArchiveActivitiesApi(...args),
  batchDeleteActivitiesApi: (...args: unknown[]) => batchDeleteActivitiesApi(...args),
  batchRestoreActivitiesApi: (...args: unknown[]) => batchRestoreActivitiesApi(...args),
  createActivityApi: (...args: unknown[]) => createActivityApi(...args),
  deleteActivityApi: (...args: unknown[]) => deleteActivityApi(...args),
  listCategoriesApi: (...args: unknown[]) => listCategoriesApi(...args),
  pageActivitiesApi: (...args: unknown[]) => pageActivitiesApi(...args),
  updateActivityApi: (...args: unknown[]) => updateActivityApi(...args)
}))

vi.mock('@/utils/confirmed-action', () => ({
  runConfirmedAction: (options: Record<string, unknown>) => runConfirmedAction(options)
}))

function mountView() {
  return shallowMount(ActivityManageView, {
    global: {
      stubs: {
        AdminListScaffold: {
          template: '<div><slot name="filters" /><slot /></div>'
        },
        AdminContentSection: {
          template:
            '<div><slot name="actions" /><slot name="emptyActions" /><slot /><slot name="pagination" /></div>'
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
        'el-pagination': {
          template: '<div />'
        },
        'el-dialog': {
          template: '<div><slot /><slot name="footer" /></div>'
        },
        'el-form': {
          template: '<form><slot /></form>'
        },
        'el-input-number': {
          template: '<input type="number" />'
        },
        'el-date-picker': {
          template: '<input />'
        },
        'el-upload': {
          template: '<div><slot /></div>'
        }
      }
    }
  })
}

describe('ActivityManageView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    pageActivitiesApi.mockResolvedValue({ total: 0, records: [] })
    listCategoriesApi.mockResolvedValue([{ id: 1, name: '社区关爱' }])
    batchArchiveActivitiesApi.mockResolvedValue(undefined)
    batchRestoreActivitiesApi.mockResolvedValue(undefined)
    batchDeleteActivitiesApi.mockResolvedValue(undefined)
    createActivityApi.mockResolvedValue(undefined)
    updateActivityApi.mockResolvedValue(undefined)
    deleteActivityApi.mockResolvedValue(undefined)
    uploadImageApi.mockResolvedValue({ url: 'https://example.com/cover.jpg' })
  })

  it('loads activity page with archived records included for admin governance', async () => {
    mountView()
    await flushPromises()

    expect(pageActivitiesApi).toHaveBeenCalledTimes(1)
    expect(pageActivitiesApi).toHaveBeenCalledWith({
      current: 1,
      size: 10,
      keyword: undefined,
      status: undefined,
      includeArchived: true
    })
  })

  it('archives selected activities and refreshes the current list', async () => {
    const wrapper = mountView()
    await flushPromises()

    const setupState = (wrapper.vm as any).$
      .setupState as {
        handleSelectionChange: (rows: Array<{ id: number }>) => void
        batchArchive: () => Promise<void>
      }

    setupState.handleSelectionChange([{ id: 3 }, { id: 4 }])
    await setupState.batchArchive()
    await flushPromises()

    expect(batchArchiveActivitiesApi).toHaveBeenCalledWith([3, 4])
    expect(pageActivitiesApi).toHaveBeenCalledTimes(2)
  })

  it('restores selected archived activities back to published and refreshes the list', async () => {
    const wrapper = mountView()
    await flushPromises()

    const setupState = (wrapper.vm as any).$
      .setupState as {
        handleSelectionChange: (rows: Array<{ id: number }>) => void
        batchRestore: () => Promise<void>
      }

    setupState.handleSelectionChange([{ id: 8 }, { id: 9 }])
    await setupState.batchRestore()
    await flushPromises()

    expect(batchRestoreActivitiesApi).toHaveBeenCalledWith([8, 9])
    expect(pageActivitiesApi).toHaveBeenCalledTimes(2)
  })
})
