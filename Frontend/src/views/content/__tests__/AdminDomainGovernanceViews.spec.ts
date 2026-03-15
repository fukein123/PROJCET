import { defineComponent, nextTick, reactive } from 'vue'
import { shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import AdminContentGovernanceView from '../AdminContentGovernanceView.vue'
import AdminForumGovernanceView from '../AdminForumGovernanceView.vue'
import AdminMallGovernanceView from '../AdminMallGovernanceView.vue'

const route = reactive({
  path: '/admin/content',
  query: {} as Record<string, unknown>
})

const replace = vi.fn((target: { path?: string; query?: Record<string, unknown> }) => {
  route.path = target.path || route.path
  route.query = { ...(target.query || {}) }
})

vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-router')>()

  return {
    ...actual,
    useRoute: () => route,
    useRouter: () => ({
      replace
    })
  }
})

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

function mountContentView() {
  return shallowMount(AdminContentGovernanceView, {
    global: {
      stubs: {
        AdminListScaffold: { template: '<div><slot /></div>' },
        DynamicManageSection: { template: '<div data-test="dynamic-section" />' },
        NoticeManageSection: { template: '<div data-test="notice-section" />' },
        BannerManageSection: { template: '<div data-test="banner-section" />' },
        'el-tabs': ElTabsStub,
        'el-tab-pane': ElTabPaneStub
      }
    }
  })
}

function mountForumView() {
  return shallowMount(AdminForumGovernanceView, {
    global: {
      stubs: {
        AdminListScaffold: { template: '<div><slot /></div>' },
        ForumCategoryManageSection: { template: '<div data-test="forum-category-section" />' },
        PostModerationSection: { template: '<div data-test="post-section" />' },
        CommentManageSection: { template: '<div data-test="comment-section" />' },
        'el-tabs': ElTabsStub,
        'el-tab-pane': ElTabPaneStub
      }
    }
  })
}

function mountMallView() {
  return shallowMount(AdminMallGovernanceView, {
    global: {
      stubs: {
        AdminListScaffold: { template: '<div><slot /></div>' },
        MallProductManageSection: { template: '<div data-test="mall-product-section" />' },
        ExchangeOrderManageSection: { template: '<div data-test="exchange-order-section" />' },
        'el-tabs': ElTabsStub,
        'el-tab-pane': ElTabPaneStub
      }
    }
  })
}

describe('split admin governance views', () => {
  beforeEach(() => {
    route.path = '/admin/content'
    route.query = {}
    replace.mockClear()
  })

  it('hydrates content governance tabs from route query', () => {
    route.query = { tab: 'notice' }
    const wrapper = mountContentView()

    expect(wrapper.find('[data-test="notice-section"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="dynamic-section"]').exists()).toBe(true)
  })

  it('syncs forum governance tabs back to route query', async () => {
    route.path = '/admin/forum'
    const wrapper = mountForumView()

    wrapper.findComponent(ElTabsStub).vm.$emit('update:modelValue', 'post')
    await nextTick()

    expect(replace).toHaveBeenCalledWith({
      path: '/admin/forum',
      query: { tab: 'post' }
    })
  })

  it('falls back to default mall governance tab for invalid query', () => {
    route.path = '/admin/mall'
    route.query = { tab: 'invalid' }
    const wrapper = mountMallView()

    expect(wrapper.find('[data-test="mall-product-section"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="exchange-order-section"]').exists()).toBe(false)
  })
})
