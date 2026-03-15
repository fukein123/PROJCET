import { defineComponent, nextTick, reactive } from 'vue'
import { shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import UserManageView from '../UserManageView.vue'

const route = reactive({
  path: '/admin/users',
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

function mountView() {
  return shallowMount(UserManageView, {
    global: {
      stubs: {
        AdminListScaffold: {
          template: '<div><slot /></div>'
        },
        UserManageSection: {
          props: ['role'],
          template: '<div :data-test="`section-${role}`" />'
        },
        'el-tabs': ElTabsStub,
        'el-tab-pane': ElTabPaneStub
      }
    }
  })
}

describe('UserManageView', () => {
  beforeEach(() => {
    route.path = '/admin/users'
    route.query = {}
    replace.mockClear()
  })

  it('renders only volunteer section on mount', () => {
    const wrapper = mountView()

    expect(wrapper.find('[data-test="section-VOLUNTEER"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="section-ADMIN"]').exists()).toBe(false)
  })

  it('hydrates the admin tab from route query', () => {
    route.query = { tab: 'admin' }

    const wrapper = mountView()

    expect(wrapper.find('[data-test="section-ADMIN"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="section-VOLUNTEER"]').exists()).toBe(true)
  })

  it('syncs query after switching tabs and keeps lazy mounted sections', async () => {
    const wrapper = mountView()

    wrapper.findComponent(ElTabsStub).vm.$emit('update:modelValue', 'admin')
    await nextTick()

    expect(replace).toHaveBeenCalledWith({
      path: '/admin/users',
      query: { tab: 'admin' }
    })
    expect(wrapper.find('[data-test="section-VOLUNTEER"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="section-ADMIN"]').exists()).toBe(true)
  })
})
