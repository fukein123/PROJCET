import { describe, expect, it } from 'vitest'
import {
  findAdminNavItemByKey,
  normalizeContentTab,
  normalizeForumTab,
  normalizeMallTab,
  normalizeUserTab,
  resolveLegacyContentManageLocation,
  resolveAdminActiveMenuKey
} from '../admin-navigation'

describe('admin navigation helpers', () => {
  it('normalizes invalid content tab values to dynamic', () => {
    expect(normalizeContentTab('invalid')).toBe('dynamic')
    expect(normalizeContentTab('notice')).toBe('notice')
  })

  it('normalizes invalid user tab values to volunteer', () => {
    expect(normalizeUserTab('invalid')).toBe('volunteer')
    expect(normalizeUserTab('admin')).toBe('admin')
  })

  it('normalizes domain tabs for split admin routes', () => {
    expect(normalizeForumTab('invalid')).toBe('forumCategory')
    expect(normalizeForumTab('comment')).toBe('comment')
    expect(normalizeMallTab('invalid')).toBe('mallProduct')
    expect(normalizeMallTab('exchangeOrder')).toBe('exchangeOrder')
  })

  it('resolves active menu keys for split admin pages and compatibility routes', () => {
    expect(
      resolveAdminActiveMenuKey({
        path: '/admin/content-manage',
        query: { tab: 'exchangeOrder' }
      } as never)
    ).toBe('/admin/mall')

    expect(
      resolveAdminActiveMenuKey({
        path: '/admin/users',
        query: { tab: 'admin' }
      } as never)
    ).toBe('user:admin')

    expect(
      resolveAdminActiveMenuKey({
        path: '/admin/forum',
        query: { tab: 'post' }
      } as never)
    ).toBe('/admin/forum')
  })

  it('maps legacy content tabs to split domain routes', () => {
    expect(resolveLegacyContentManageLocation('dynamic')).toEqual({
      path: '/admin/content',
      query: { tab: 'dynamic' }
    })
    expect(resolveLegacyContentManageLocation('post')).toEqual({
      path: '/admin/forum',
      query: { tab: 'post' }
    })
    expect(resolveLegacyContentManageLocation('exchangeOrder')).toEqual({
      path: '/admin/mall',
      query: { tab: 'exchangeOrder' }
    })
  })

  it('returns navigation metadata for grouped menu items', () => {
    const item = findAdminNavItemByKey('/admin/forum')

    expect(item?.title).toBe('论坛治理')
    expect(item?.groupKey).toBe('forum')
  })
})
