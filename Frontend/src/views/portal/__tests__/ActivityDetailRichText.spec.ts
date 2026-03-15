import { computed, defineComponent, ref } from 'vue'
import { flushPromises, shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const routeParams = ref({ id: '7' })
const routeFullPath = ref('/portal/activities/7')
const routeQuery = ref<Record<string, unknown>>({})
const pushMock = vi.fn()
const backMock = vi.fn()
const requireLoginMock = vi.fn()
const activityDetailApi = vi.fn()
const pageCommentsApi = vi.fn()

vi.mock('vue-router', () => ({
  useRoute: () => ({
    params: routeParams.value,
    fullPath: routeFullPath.value,
    query: routeQuery.value
  }),
  useRouter: () => ({
    push: pushMock,
    back: backMock
  })
}))

vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    warning: vi.fn(),
    info: vi.fn()
  }
}))

vi.mock('@/api/activity', () => ({
  activityDetailApi: (...args: unknown[]) => activityDetailApi(...args)
}))

vi.mock('@/api/content', () => ({
  pageCommentsApi: (...args: unknown[]) => pageCommentsApi(...args),
  addCommentApi: vi.fn(),
  createFavoriteApi: vi.fn()
}))

vi.mock('@/composables/usePortalNavigation', () => ({
  usePortalNavigation: () => ({
    requireLogin: (...args: Parameters<typeof requireLoginMock>) => requireLoginMock(...args)
  })
}))

vi.mock('@/stores/userStore', () => ({
  useUserStore: () => ({
    role: 'VOLUNTEER'
  })
}))

vi.mock('@/utils/activity-apply', () => ({
  submitActivityApplicationWithUndo: vi.fn()
}))

vi.mock('@/utils/confirmed-action', () => ({
  runConfirmedAction: vi.fn()
}))

vi.mock('../PortalNavBar.vue', () => ({
  default: {
    name: 'PortalNavBar',
    template: '<div class="portal-nav-stub" />'
  }
}))

const { default: PortalActivityDetailView } = await import('../PortalActivityDetailView.vue')
const { default: VolunteerActivityDetailView } = await import('../../volunteer/VolunteerActivityDetailView.vue')

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
      <p class="hero-description">{{ description }}</p>
      <div><slot name="actions" /></div>
      <div><slot name="aside" /></div>
    </section>
  `
})

const StatePanelStub = defineComponent({
  name: 'StatePanel',
  props: {
    title: String,
    description: String
  },
  template: '<section class="state-panel-stub"><strong>{{ title }}</strong><span>{{ description }}</span><slot name="actions" /></section>'
})

const RichTextRendererStub = defineComponent({
  name: 'RichTextRenderer',
  props: {
    value: {
      type: String,
      default: ''
    }
  },
  setup(props) {
    const text = computed(() => String(props.value || '').replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim())
    return {
      text
    }
  },
  template: '<div class="rich-text-renderer-stub">{{ text }}</div>'
})

function mountView(component: object) {
  return shallowMount(component, {
    global: {
      stubs: {
        WorkspaceHero: WorkspaceHeroStub,
        StatePanel: StatePanelStub,
        RichTextRenderer: RichTextRendererStub,
        'el-button': {
          emits: ['click'],
          template: '<button @click="$emit(\'click\')"><slot /></button>'
        },
        'el-tag': {
          template: '<span><slot /></span>'
        },
        'el-input': {
          template: '<textarea />'
        },
        'el-table': {
          template: '<div><slot /><slot name="empty" /></div>'
        },
        'el-table-column': {
          template: '<div><slot :row="{}" /></div>'
        },
        'el-pagination': {
          template: '<div class="pagination-stub" />'
        }
      }
    }
  })
}

describe('activity detail views rich text summaries', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    routeParams.value = { id: '7' }
    routeFullPath.value = '/portal/activities/7'
    routeQuery.value = {}
    requireLoginMock.mockImplementation(async (callback?: () => Promise<void>) => {
      await callback?.()
      return true
    })
    activityDetailApi.mockResolvedValue({
      id: 7,
      title: '河道清理行动',
      categoryId: 1,
      startTime: '2026-03-15 09:00:00',
      endTime: '2026-03-15 11:00:00',
      address: '幸福社区广场',
      status: 'PUBLISHED',
      targetCount: 20,
      volunteerQuota: 12,
      pointReward: 5,
      content: '<p>河道 <strong>清理</strong> 与分类回收</p>',
      description: '<p>详细说明</p>',
      coverImage: ''
    })
    pageCommentsApi.mockResolvedValue({
      total: 0,
      records: []
    })
  })

  it('renders portal activity summary as plain text instead of raw html', async () => {
    const wrapper = mountView(PortalActivityDetailView)
    await flushPromises()

    expect(activityDetailApi).toHaveBeenCalledWith(7)
    expect(wrapper.text()).toContain('河道 清理 与分类回收')
    expect(wrapper.text()).not.toContain('<strong>')
    expect(wrapper.text()).not.toContain('<p>河道')
  })

  it('renders volunteer activity summary as plain text instead of raw html', async () => {
    routeFullPath.value = '/volunteer/activities/7'

    const wrapper = mountView(VolunteerActivityDetailView)
    await flushPromises()

    expect(activityDetailApi).toHaveBeenCalledWith(7)
    expect(wrapper.text()).toContain('河道 清理 与分类回收')
    expect(wrapper.text()).not.toContain('<strong>')
    expect(wrapper.text()).not.toContain('<p>河道')
  })
})
