import { reactive } from 'vue'
import { shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import ContentManageView from '../ContentManageView.vue'

const route = reactive({
  path: '/admin/content-manage',
  query: {} as Record<string, unknown>
})

vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-router')>()

  return {
    ...actual,
    useRoute: () => route
  }
})

function mountView() {
  return shallowMount(ContentManageView, {
    global: {
      stubs: {
        DynamicManageSection: {
          template: '<div data-test="dynamic-section" />'
        },
        NoticeManageSection: {
          template: '<div data-test="notice-section" />'
        },
        BannerManageSection: {
          template: '<div data-test="banner-section" />'
        }
      }
    }
  })
}

describe('ContentManageView', () => {
  beforeEach(() => {
    route.path = '/admin/content-manage'
    route.query = {}
  })

  it('renders only the default section on mount', () => {
    const wrapper = mountView()

    expect(wrapper.find('[data-test="dynamic-section"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="notice-section"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="banner-section"]').exists()).toBe(false)
  })

  it('hydrates the current section from route query', () => {
    route.query = { tab: 'banner' }

    const wrapper = mountView()

    expect(wrapper.find('[data-test="banner-section"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="dynamic-section"]').exists()).toBe(false)
  })

  it('falls back to dynamic section when query is invalid', () => {
    route.query = { tab: 'unknown' }

    const wrapper = mountView()

    expect(wrapper.find('[data-test="dynamic-section"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="banner-section"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="notice-section"]').exists()).toBe(false)
  })
})
