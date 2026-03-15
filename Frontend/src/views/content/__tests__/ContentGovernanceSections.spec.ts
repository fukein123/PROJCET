import { flushPromises, shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import BannerManageSection from '../components/BannerManageSection.vue'
import CommentManageSection from '../components/CommentManageSection.vue'
import DynamicManageSection from '../components/DynamicManageSection.vue'
import ForumCategoryManageSection from '../components/ForumCategoryManageSection.vue'
import PostModerationSection from '../components/PostModerationSection.vue'

const { elMessageSuccess, elMessageWarning } = vi.hoisted(() => ({
  elMessageSuccess: vi.fn(),
  elMessageWarning: vi.fn()
}))

const pageActivitiesApi = vi.fn()
const pageDynamicsApi = vi.fn()
const pageForumPostsApi = vi.fn()
const pageCommentsApi = vi.fn()
const listManageBannersApi = vi.fn()
const listManageForumCategoriesApi = vi.fn()
const uploadImageApi = vi.fn()

vi.mock('element-plus', () => ({
  ElMessage: {
    success: elMessageSuccess,
    warning: elMessageWarning
  }
}))

vi.mock('@/api/common', () => ({
  uploadImageApi: (...args: unknown[]) => uploadImageApi(...args)
}))

vi.mock('@/api/activity', () => ({
  pageActivitiesApi: (...args: unknown[]) => pageActivitiesApi(...args)
}))

vi.mock('@/api/content', () => ({
  pageDynamicsApi: (...args: unknown[]) => pageDynamicsApi(...args),
  pageForumPostsApi: (...args: unknown[]) => pageForumPostsApi(...args),
  pageCommentsApi: (...args: unknown[]) => pageCommentsApi(...args),
  listManageBannersApi: (...args: unknown[]) => listManageBannersApi(...args),
  listManageForumCategoriesApi: (...args: unknown[]) => listManageForumCategoriesApi(...args),
  batchArchiveDynamicsApi: vi.fn(),
  batchRestoreDynamicsApi: vi.fn(),
  deleteDynamicApi: vi.fn(),
  saveDynamicApi: vi.fn(),
  updateDynamicApi: vi.fn(),
  batchDeleteBannersApi: vi.fn(),
  deleteBannerApi: vi.fn(),
  saveBannerApi: vi.fn(),
  updateBannerApi: vi.fn(),
  auditPostApi: vi.fn(),
  batchDeleteForumPostsApi: vi.fn(),
  deleteForumPostApi: vi.fn(),
  updateForumPostApi: vi.fn(),
  batchDeleteForumCategoriesApi: vi.fn(),
  deleteForumCategoryApi: vi.fn(),
  saveForumCategoryApi: vi.fn(),
  updateForumCategoryApi: vi.fn(),
  batchDeleteCommentsApi: vi.fn(),
  deleteCommentApi: vi.fn()
}))

function createGlobalStubs() {
  return {
    AdminContentSection: {
      template: '<div><slot name="actions" /><slot name="emptyActions" /><slot /><slot name="pagination" /></div>'
    },
    SearchForm: {
      template: '<div><slot /></div>'
    },
    RichTextEditor: {
      props: ['modelValue'],
      template: '<div />'
    },
    'el-form-item': {
      template: '<div><slot /></div>'
    },
    'el-input': {
      props: ['modelValue'],
      emits: ['update:modelValue'],
      template: '<input />'
    },
    'el-select': {
      props: ['modelValue'],
      emits: ['update:modelValue'],
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
    'el-button': {
      emits: ['click'],
      template: '<button @click="$emit(\'click\')"><slot /></button>'
    },
    'el-pagination': {
      template: '<div />'
    },
    'el-tooltip': {
      template: '<div><slot /></div>'
    },
    'el-tag': {
      template: '<span><slot /></span>'
    },
    'el-dialog': {
      template: '<div><slot /><slot name="footer" /></div>'
    },
    'el-form': {
      template: '<form><slot /></form>'
    },
    'el-switch': {
      template: '<input type="checkbox" />'
    },
    'el-upload': {
      template: '<div><slot /></div>'
    },
    'el-input-number': {
      template: '<input type="number" />'
    }
  }
}

function mountDynamicSection() {
  return shallowMount(DynamicManageSection, {
    global: {
      stubs: createGlobalStubs(),
      directives: {
        loading: {}
      }
    }
  })
}

function mountPostSection() {
  return shallowMount(PostModerationSection, {
    global: {
      stubs: createGlobalStubs(),
      directives: {
        loading: {}
      }
    }
  })
}

function mountCommentSection() {
  return shallowMount(CommentManageSection, {
    global: {
      stubs: createGlobalStubs(),
      directives: {
        loading: {}
      }
    }
  })
}

function mountBannerSection() {
  return shallowMount(BannerManageSection, {
    global: {
      stubs: createGlobalStubs(),
      directives: {
        loading: {}
      }
    }
  })
}

function mountForumCategorySection() {
  return shallowMount(ForumCategoryManageSection, {
    global: {
      stubs: createGlobalStubs(),
      directives: {
        loading: {}
      }
    }
  })
}

describe('content governance sections', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    pageActivitiesApi.mockResolvedValue({ total: 0, records: [] })
    pageDynamicsApi.mockResolvedValue({ total: 0, records: [] })
    pageForumPostsApi.mockResolvedValue({ total: 0, records: [] })
    pageCommentsApi.mockResolvedValue({ total: 0, records: [] })
    listManageBannersApi.mockResolvedValue([])
    listManageForumCategoriesApi.mockResolvedValue([])
    uploadImageApi.mockResolvedValue({ url: 'https://example.com/cover.jpg' })
  })

  it('loads dynamic governance list with keyword and type filters', async () => {
    const wrapper = mountDynamicSection()
    await flushPromises()

    const setupState = (wrapper.vm as any).$.setupState as {
      query: { keyword: string; type?: string }
      search: () => Promise<void>
      resetQuery: () => void
    }

    setupState.query.keyword = '社区'
    setupState.query.type = 'NEWS'
    await setupState.search()
    await flushPromises()

    expect(pageDynamicsApi).toHaveBeenLastCalledWith({
      current: 1,
      size: 10,
      type: 'NEWS',
      keyword: '社区',
      onlyPublished: false
    })

    setupState.resetQuery()
    await flushPromises()

    expect(pageDynamicsApi).toHaveBeenLastCalledWith({
      current: 1,
      size: 10,
      type: undefined,
      keyword: undefined,
      onlyPublished: false
    })
  })

  it('loads post moderation list with keyword and status filters', async () => {
    const wrapper = mountPostSection()
    await flushPromises()

    const setupState = (wrapper.vm as any).$.setupState as {
      query: { keyword: string; status?: string }
      search: () => Promise<void>
      resetQuery: () => void
    }

    setupState.query.keyword = '志愿服务'
    setupState.query.status = 'PENDING'
    await setupState.search()
    await flushPromises()

    expect(pageForumPostsApi).toHaveBeenLastCalledWith({
      current: 1,
      size: 10,
      keyword: '志愿服务',
      status: 'PENDING',
      onlyApproved: false
    })

    setupState.resetQuery()
    await flushPromises()

    expect(pageForumPostsApi).toHaveBeenLastCalledWith({
      current: 1,
      size: 10,
      keyword: undefined,
      status: undefined,
      onlyApproved: false
    })
  })

  it('loads comment governance list with target filters and data scope', async () => {
    const wrapper = mountCommentSection()
    await flushPromises()

    const setupState = (wrapper.vm as any).$.setupState as {
      query: { targetType?: string; targetIdText: string; includeTestData: boolean }
      search: () => Promise<void>
      resetQuery: () => void
    }

    setupState.query.targetType = 'POST'
    setupState.query.targetIdText = '42'
    setupState.query.includeTestData = false
    await setupState.search()
    await flushPromises()

    expect(pageCommentsApi).toHaveBeenLastCalledWith({
      current: 1,
      size: 10,
      targetType: 'POST',
      targetId: 42,
      includeTestData: false
    })

    setupState.resetQuery()
    await flushPromises()

    expect(pageCommentsApi).toHaveBeenLastCalledWith({
      current: 1,
      size: 10,
      targetType: undefined,
      targetId: undefined,
      includeTestData: true
    })
  })

  it('blocks comment target id search without target type', async () => {
    const wrapper = mountCommentSection()
    await flushPromises()

    const setupState = (wrapper.vm as any).$.setupState as {
      query: { targetType?: string; targetIdText: string; includeTestData: boolean }
      search: () => Promise<void>
    }

    const initialCallCount = pageCommentsApi.mock.calls.length
    setupState.query.targetIdText = '1'
    await setupState.search()
    await flushPromises()

    expect(pageCommentsApi).toHaveBeenCalledTimes(initialCallCount)
    expect(elMessageWarning).toHaveBeenCalledWith('按关联 ID 筛选评论时，请先选择所属模块')
  })

  it('filters banner governance records by keyword and status locally', async () => {
    pageActivitiesApi.mockResolvedValue({
      total: 2,
      records: [
        { id: 1, title: '春季活动' },
        { id: 2, title: '便民服务' }
      ]
    })
    listManageBannersApi.mockResolvedValue([
      { id: 11, title: '春季招募', activityId: 1, sort: 1, status: 1, imageUrl: '' },
      { id: 12, title: '停用轮播', activityId: 2, sort: 2, status: 0, imageUrl: '' }
    ])

    const wrapper = mountBannerSection()
    await flushPromises()

    const setupState = (wrapper.vm as any).$.setupState as {
      query: { keyword: string; status?: number }
      filteredRecords: Array<{ id: number }>
      resetQuery: () => void
    }

    setupState.query.keyword = '春季'
    setupState.query.status = 1
    await wrapper.vm.$nextTick()

    expect(setupState.filteredRecords.map((item) => item.id)).toEqual([11])

    setupState.resetQuery()
    await wrapper.vm.$nextTick()

    expect(setupState.filteredRecords.map((item) => item.id)).toEqual([11, 12])
  })

  it('filters forum category records by keyword and status locally', async () => {
    listManageForumCategoriesApi.mockResolvedValue([
      { id: 1, name: '志愿故事', sort: 1, status: 1 },
      { id: 2, name: '站务公告', sort: 2, status: 0 }
    ])

    const wrapper = mountForumCategorySection()
    await flushPromises()

    const setupState = (wrapper.vm as any).$.setupState as {
      query: { keyword: string; status?: number }
      filteredRecords: Array<{ id: number }>
      resetQuery: () => void
    }

    setupState.query.keyword = '志愿'
    setupState.query.status = 1
    await wrapper.vm.$nextTick()

    expect(setupState.filteredRecords.map((item) => item.id)).toEqual([1])

    setupState.resetQuery()
    await wrapper.vm.$nextTick()

    expect(setupState.filteredRecords.map((item) => item.id)).toEqual([1, 2])
  })
})
