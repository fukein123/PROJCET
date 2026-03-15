import { flushPromises, shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import AdminDashboardView from '../AdminDashboardView.vue'

const adminDashboardApi = vi.fn()
const push = vi.fn()
const elMessageWarning = vi.fn()

vi.mock('element-plus', () => ({
  ElMessage: {
    warning: (...args: unknown[]) => elMessageWarning(...args)
  }
}))

vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-router')>()

  return {
    ...actual,
    useRouter: () => ({
      push
    })
  }
})

vi.mock('@/api/dashboard', () => ({
  adminDashboardApi: () => adminDashboardApi()
}))

function mountView() {
  return shallowMount(AdminDashboardView, {
    global: {
      stubs: {
        'el-card': {
          template: '<section><slot name="header" /><slot /></section>'
        },
        'el-button': {
          emits: ['click'],
          template: '<button @click="$emit(\'click\')"><slot /></button>'
        }
      }
    }
  })
}

describe('AdminDashboardView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    adminDashboardApi.mockResolvedValue({
      activityCount: 12,
      postCount: 34,
      commentCount: 56,
      volunteerCount: 78,
      orderCount: 9,
      pendingApplicationCount: 3,
      pendingPostCount: 4,
      weeklyApplicationTrend: [{ day: '2026-03-15', value: 3 }],
      activityTypeBar: [{ name: '社区服务', value: 12 }],
      postTypePie: [{ name: '志愿心得', value: 20 }],
      recentOperationLogs: [{ id: 1, operatorUsername: 'admin', actionType: 'AUDIT_POST' }]
    })
  })

  it('renders dashboard as a summary workspace without the old governance wall copy', async () => {
    const wrapper = mountView()
    await flushPromises()

    expect(adminDashboardApi).toHaveBeenCalledTimes(1)
    expect(wrapper.text()).toContain('系统首页')
    expect(wrapper.text()).toContain('独立页面分流')
    expect(wrapper.text()).not.toContain('治理入口')
    expect(wrapper.text()).toContain('12')
    expect(wrapper.text()).toContain('78')
  })

  it('navigates to dedicated pages from handoff buttons', async () => {
    const wrapper = mountView()
    await flushPromises()

    const buttons = wrapper.findAll('button')
    const analyticsButton = buttons.find((item) => item.text() === '查看分析')

    expect(analyticsButton).toBeDefined()

    await analyticsButton!.trigger('click')

    expect(push).toHaveBeenCalledWith({ path: '/admin/analytics' })
  })
})
