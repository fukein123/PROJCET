import { beforeEach, describe, expect, it, vi } from 'vitest'
import { PORTAL_PATHS } from '@/constants/portal-routes'
import { privateRoutes } from '../routes'
import { resolveRouteAccess } from '../app-router'

const getToken = vi.fn(() => '')
const getRole = vi.fn(() => '')
const clearAuthStorage = vi.fn()

vi.mock('@/utils/auth', () => ({
  getToken: () => getToken(),
  getRole: () => getRole(),
  clearAuthStorage: () => clearAuthStorage()
}))

describe('router guard', () => {
  beforeEach(() => {
    getToken.mockReturnValue('')
    getRole.mockReturnValue('')
    clearAuthStorage.mockClear()
  })

  it('allows authenticated admins to browse portal pages directly', () => {
    expect(
      resolveRouteAccess(
        {
          path: '/portal/forum',
          fullPath: '/portal/forum',
          requiresAuth: false
        },
        {
          token: 'admin-token',
          role: 'ADMIN'
        }
      )
    ).toEqual({
      shouldClearAuth: false
    })
  })

  it('redirects volunteers away from admin pages', () => {
    expect(
      resolveRouteAccess(
        {
          path: '/admin/users',
          fullPath: '/admin/users',
          requiresAuth: true,
          requiredRoles: ['ADMIN']
        },
        {
          token: 'volunteer-token',
          role: 'VOLUNTEER'
        }
      )
    ).toEqual({
      redirect: PORTAL_PATHS.home,
      shouldClearAuth: false
    })
  })

  it('redirects admins away from volunteer self-service pages', () => {
    expect(
      resolveRouteAccess(
        {
          path: PORTAL_PATHS.selfServicePosts,
          fullPath: PORTAL_PATHS.selfServicePosts,
          requiresAuth: true,
          requiredRoles: ['VOLUNTEER']
        },
        {
          token: 'admin-token',
          role: 'ADMIN'
        }
      )
    ).toEqual({
      redirect: '/admin/dashboard',
      shouldClearAuth: false
    })
  })

  it('redirects guests from self-service pages to login with redirect target', () => {
    expect(
      resolveRouteAccess(
        {
          path: PORTAL_PATHS.selfServiceOrders,
          fullPath: PORTAL_PATHS.selfServiceOrders,
          requiresAuth: true,
          requiredRoles: ['VOLUNTEER']
        },
        {
          token: '',
          role: ''
        }
      )
    ).toEqual({
      redirect: `/login?redirect=${encodeURIComponent(PORTAL_PATHS.selfServiceOrders)}`,
      shouldClearAuth: false
    })
  })

  it('redirects authenticated volunteers away from login to portal home', () => {
    expect(
      resolveRouteAccess(
        {
          path: '/login',
          fullPath: '/login',
          requiresAuth: false
        },
        {
          token: 'volunteer-token',
          role: 'VOLUNTEER'
        }
      )
    ).toEqual({
      redirect: PORTAL_PATHS.home,
      shouldClearAuth: false
    })
  })

  it('keeps legacy volunteer routes mapped to the merged self-service portal', () => {
    const legacyProfileRoute = privateRoutes.find((route) => route.path === '/volunteer/profile')

    expect(legacyProfileRoute?.redirect).toBe(PORTAL_PATHS.selfServiceProfile)
  })

  it('keeps legacy volunteer check-record route mapped to the portal self-service page', () => {
    const legacyCheckRecordRoute = privateRoutes.find((route) => route.path === '/volunteer/check-records')

    expect(legacyCheckRecordRoute?.redirect).toBe(PORTAL_PATHS.selfServiceCheckRecords)
  })

  it('redirects legacy content-manage tabs to the split admin governance routes', () => {
    const adminRootRoute = privateRoutes.find((route) => route.path === '/admin')
    const legacyContentManageRoute = adminRootRoute?.children?.find((route) => route.path === 'content-manage')
    const redirect =
      typeof legacyContentManageRoute?.redirect === 'function'
        ? (legacyContentManageRoute.redirect as (
            to: { query: Record<string, unknown> },
            from: unknown
          ) => unknown)
        : undefined

    expect(redirect?.({ query: { tab: 'post' } } as never, undefined as never)).toEqual({
      path: '/admin/forum',
      query: { tab: 'post' }
    })
    expect(redirect?.({ query: { tab: 'exchangeOrder' } } as never, undefined as never)).toEqual({
      path: '/admin/mall',
      query: { tab: 'exchangeOrder' }
    })
  })

  it('marks invalid role state for cleanup before forcing login', () => {
    expect(
      resolveRouteAccess(
        {
          path: '/admin/dashboard',
          fullPath: '/admin/dashboard',
          requiresAuth: true,
          requiredRoles: ['ADMIN']
        },
        {
          token: 'broken-token',
          role: 'GUEST'
        }
      )
    ).toEqual({
      redirect: '/login?redirect=%2Fadmin%2Fdashboard',
      shouldClearAuth: true
    })
  })
})
