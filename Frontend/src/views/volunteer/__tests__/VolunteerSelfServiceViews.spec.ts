import { defineComponent, ref } from 'vue'
import { flushPromises, shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import VolunteerApplyRecordsView from '../VolunteerApplyRecordsView.vue'
import VolunteerCheckRecordsView from '../VolunteerCheckRecordsView.vue'
import VolunteerMyCommentsView from '../VolunteerMyCommentsView.vue'
import VolunteerMyFavoritesView from '../VolunteerMyFavoritesView.vue'
import VolunteerMyPostsView from '../VolunteerMyPostsView.vue'
import VolunteerProfileView from '../VolunteerProfileView.vue'

const pushMock = vi.fn()
const useTableMock = vi.fn()
const getMyProfileApi = vi.fn()
const updateMyProfileApi = vi.fn()
const updatePasswordApi = vi.fn()
const pageActivitiesApi = vi.fn()
const listForumCategoriesApi = vi.fn()
const runConfirmedAction = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: pushMock
  })
}))

vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    warning: vi.fn()
  }
}))

vi.mock('@/composables/useTable', () => ({
  useTable: (...args: unknown[]) => useTableMock(...args)
}))

vi.mock('@/composables/useSelectionIds', () => ({
  useSelectionIds: () => ({
    selectedIds: ref<number[]>([]),
    handleSelectionChange: vi.fn(),
    clearSelection: vi.fn()
  })
}))

vi.mock('@/utils/confirmed-action', () => ({
  runConfirmedAction: (...args: unknown[]) => runConfirmedAction(...args)
}))

vi.mock('@/api/user', () => ({
  getMyProfileApi: (...args: unknown[]) => getMyProfileApi(...args),
  updateMyProfileApi: (...args: unknown[]) => updateMyProfileApi(...args),
  updatePasswordApi: (...args: unknown[]) => updatePasswordApi(...args)
}))

vi.mock('@/api/activity', () => ({
  myApplicationsApi: vi.fn(),
  myCheckRecordsApi: vi.fn(),
  pageActivitiesApi: (...args: unknown[]) => pageActivitiesApi(...args),
  signInApi: vi.fn(),
  signOutApi: vi.fn()
}))

vi.mock('@/api/content', () => ({
  listForumCategoriesApi: (...args: unknown[]) => listForumCategoriesApi(...args),
  pageCommentsApi: vi.fn(),
  pageFavoritesApi: vi.fn(),
  pageForumPostsApi: vi.fn(),
  saveMyPostApi: vi.fn(),
  createFavoriteApi: vi.fn(),
  updateFavoriteApi: vi.fn(),
  removeFavoriteByIdApi: vi.fn(),
  batchDeleteFavoritesApi: vi.fn()
}))

const WorkspaceHeroStub = defineComponent({
  name: 'WorkspaceHero',
  props: {
    eyebrow: String,
    title: String,
    description: String
  },
  template: `
    <section class="workspace-hero-stub">
      <p>{{ eyebrow }}</p>
      <h1>{{ title }}</h1>
      <p>{{ description }}</p>
      <div class="hero-actions"><slot name="actions" /></div>
      <div class="hero-aside"><slot name="aside" /></div>
    </section>
  `
})

const VolunteerPageSectionStub = defineComponent({
  name: 'VolunteerPageSection',
  props: {
    eyebrow: String,
    title: String,
    description: String
  },
  template: `
    <section class="volunteer-page-section-stub">
      <p>{{ eyebrow }}</p>
      <h2>{{ title }}</h2>
      <p>{{ description }}</p>
      <div class="section-actions"><slot name="actions" /></div>
      <div class="section-body"><slot /></div>
      <div class="section-footer"><slot name="footer" /></div>
    </section>
  `
})

const StatePanelStub = defineComponent({
  name: 'StatePanel',
  props: {
    state: String,
    title: String,
    description: String
  },
  template: `
    <div class="state-panel-stub" :data-state="state">
      <strong>{{ title }}</strong>
      <span>{{ description }}</span>
    </div>
  `
})

const ElTableColumnStub = defineComponent({
  name: 'ElTableColumn',
  setup() {
    return {
      row: {}
    }
  },
  template: '<div class="table-column-stub"><slot :row="row" /></div>'
})

function createTableState<TRecord>(options?: { loading?: boolean; total?: number; records?: TRecord[] }) {
  return {
    loading: ref(options?.loading ?? false),
    total: ref(options?.total ?? 0),
    records: ref(options?.records ?? []),
    query: ref({ current: 1, size: 10 }).value,
    load: vi.fn().mockResolvedValue(undefined),
    reset: vi.fn().mockResolvedValue(undefined),
    handlePage: vi.fn(),
    handleSizeChange: vi.fn()
  }
}

function mountView(component: object) {
  return shallowMount(component, {
    global: {
      stubs: {
        WorkspaceHero: WorkspaceHeroStub,
        VolunteerPageSection: VolunteerPageSectionStub,
        StatePanel: StatePanelStub,
        'el-button': {
          emits: ['click'],
          template: '<button @click="$emit(\'click\')"><slot /></button>'
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
        'el-input-number': {
          template: '<input type="number" />'
        },
        'el-select': {
          template: '<select><slot /></select>'
        },
        'el-option': {
          template: '<option><slot /></option>'
        },
        'el-table': {
          template: '<div class="table-stub"><slot /></div>'
        },
        'el-table-column': ElTableColumnStub,
        'el-tag': {
          template: '<span class="tag-stub"><slot /></span>'
        },
        'el-pagination': {
          template: '<div class="pagination-stub" />'
        },
        'el-dialog': {
          template: '<div class="dialog-stub"><slot /><slot name="footer" /></div>'
        }
      }
    }
  })
}

describe('Volunteer self-service views', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    useTableMock.mockReset()
    getMyProfileApi.mockResolvedValue({
      username: 'volunteer',
      realName: '志愿者甲',
      email: 'volunteer@cvs.local',
      phone: '13900000000',
      gender: 'UNKNOWN',
      avatar: '',
      certified: 1
    })
    updateMyProfileApi.mockResolvedValue(undefined)
    updatePasswordApi.mockResolvedValue(undefined)
    pageActivitiesApi.mockResolvedValue({
      total: 1,
      records: [
        {
          id: 1,
          title: '社区清洁行动',
          address: '幸福社区广场',
          startTime: '2026-03-13 09:00:00',
          endTime: '2026-03-13 11:00:00'
        }
      ]
    })
    listForumCategoriesApi.mockResolvedValue([{ id: 1, name: '志愿心得' }])
    runConfirmedAction.mockResolvedValue(undefined)
  })

  it('renders profile hero and form actions', async () => {
    const wrapper = mountView(VolunteerProfileView)
    await flushPromises()

    expect(getMyProfileApi).toHaveBeenCalledTimes(1)
    expect(wrapper.text()).toContain('统一维护个人资料与账户安全')
    expect(wrapper.text()).toContain('查看报名记录')
    expect(wrapper.text()).toContain('保存资料')
    expect(wrapper.text()).toContain('更新密码')
  })

  it('shows apply records actions and pagination when data exists', async () => {
    useTableMock.mockReturnValue(
      createTableState({
        total: 12,
        records: [{ id: 1, activityTitle: '河道清洁', realName: '志愿者甲', status: 'APPROVED' }]
      })
    )

    const wrapper = mountView(VolunteerApplyRecordsView)
    await flushPromises()

    expect(wrapper.text()).toContain('我的报名记录')
    expect(wrapper.text()).toContain('刷新记录')
    expect(wrapper.find('.pagination-stub').exists()).toBe(true)
  })

  it('shows empty state for check records page', async () => {
    useTableMock.mockReturnValue(createTableState())

    const wrapper = mountView(VolunteerCheckRecordsView)
    await flushPromises()

    expect(wrapper.text()).toContain('暂无打卡记录')
    expect(wrapper.find('.pagination-stub').exists()).toBe(false)
  })

  it('renders favorites actions and pagination', async () => {
    useTableMock.mockReturnValue(
      createTableState({
        total: 8,
        records: [
          {
            id: 1,
            activityId: 1,
            activityTitle: '社区清洁行动快照',
            activityAddress: '幸福社区广场',
            activityStartTime: '2026-03-13 09:00:00',
            activityEndTime: '2026-03-13 11:00:00',
            note: '周末参加',
            tag: '周末',
            priority: 3,
            createTime: '2026-03-13 12:00:00'
          }
        ]
      })
    )

    const wrapper = mountView(VolunteerMyFavoritesView)
    await flushPromises()

    const setupState = (wrapper.vm as any).$
      .setupState as {
        favoriteTitleLabel: (row: Record<string, unknown>) => string
        favoriteAddressLabel: (row: Record<string, unknown>) => string
        favoriteScheduleLabel: (row: Record<string, unknown>) => string
      }
    const row = {
      activityId: 1,
      activityTitle: '社区清洁行动快照',
      activityAddress: '幸福社区广场',
      activityStartTime: '2026-03-13 09:00:00',
      activityEndTime: '2026-03-13 11:00:00'
    }

    expect(wrapper.text()).toContain('新增收藏')
    expect(wrapper.text()).toContain('批量删除')
    expect(wrapper.find('.pagination-stub').exists()).toBe(true)
    expect(setupState.favoriteTitleLabel(row)).toBe('社区清洁行动快照')
    expect(setupState.favoriteAddressLabel(row)).toBe('幸福社区广场')
    expect(setupState.favoriteScheduleLabel(row)).toContain('2026-03-13 09:00')
  })

  it('renders post actions and pagination', async () => {
    useTableMock.mockReturnValue(
      createTableState({
        total: 4,
        records: [{ id: 1, title: '暖心故事', categoryId: 1, status: 'APPROVED', content: '内容' }]
      })
    )

    const wrapper = mountView(VolunteerMyPostsView)
    await flushPromises()

    expect(wrapper.text()).toContain('统一管理个人发帖与审核状态')
    expect(wrapper.text()).toContain('发布帖子')
    expect(wrapper.find('.pagination-stub').exists()).toBe(true)
  })

  it('shows loading state for comments page while syncing data', async () => {
    useTableMock.mockReturnValue(
      createTableState({
        loading: true
      })
    )

    const wrapper = mountView(VolunteerMyCommentsView)
    await flushPromises()

    expect(wrapper.text()).toContain('正在同步评论记录')
    expect(wrapper.text()).toContain('刷新评论')
    expect(wrapper.find('.pagination-stub').exists()).toBe(false)
  })
})
