import type { RouteLocationNormalizedLoaded, RouteLocationRaw } from 'vue-router'

export const CONTENT_TABS = ['dynamic', 'notice', 'banner'] as const
export const FORUM_TABS = ['forumCategory', 'post', 'comment'] as const
export const MALL_TABS = ['mallProduct', 'exchangeOrder'] as const
export const LEGACY_CONTENT_TABS = [...CONTENT_TABS, ...FORUM_TABS, ...MALL_TABS] as const
export const USER_TABS = ['volunteer', 'admin'] as const

export type ContentTabKey = (typeof CONTENT_TABS)[number]
export type ForumTabKey = (typeof FORUM_TABS)[number]
export type MallTabKey = (typeof MALL_TABS)[number]
export type LegacyContentTabKey = (typeof LEGACY_CONTENT_TABS)[number]
export type UserTabKey = (typeof USER_TABS)[number]
export type AdminNavGroupKey = 'dashboard' | 'activity' | 'content' | 'forum' | 'mall' | 'user' | 'system'

export interface AdminNavItem {
  key: string
  title: string
  shortTitle: string
  description: string
  to: RouteLocationRaw
  groupKey: AdminNavGroupKey
}

export interface AdminNavGroup {
  key: AdminNavGroupKey
  title: string
  description: string
  items: AdminNavItem[]
}

function isStringMember(values: readonly string[], value: unknown): value is string {
  return typeof value === 'string' && values.includes(value)
}

export function normalizeContentTab(value: unknown): ContentTabKey {
  return isStringMember(CONTENT_TABS, value) ? (value as ContentTabKey) : 'dynamic'
}

export function normalizeForumTab(value: unknown): ForumTabKey {
  return isStringMember(FORUM_TABS, value) ? (value as ForumTabKey) : 'forumCategory'
}

export function normalizeMallTab(value: unknown): MallTabKey {
  return isStringMember(MALL_TABS, value) ? (value as MallTabKey) : 'mallProduct'
}

export function normalizeLegacyContentTab(value: unknown): LegacyContentTabKey {
  return isStringMember(LEGACY_CONTENT_TABS, value) ? (value as LegacyContentTabKey) : 'dynamic'
}

export function normalizeUserTab(value: unknown): UserTabKey {
  return isStringMember(USER_TABS, value) ? (value as UserTabKey) : 'volunteer'
}

export function resolveLegacyContentManageLocation(value: unknown): RouteLocationRaw {
  const tab = normalizeLegacyContentTab(value)
  if (CONTENT_TABS.includes(tab as ContentTabKey)) {
    return { path: '/admin/content', query: { tab } }
  }
  if (FORUM_TABS.includes(tab as ForumTabKey)) {
    return { path: '/admin/forum', query: { tab } }
  }
  return { path: '/admin/mall', query: { tab } }
}

export const ADMIN_NAV_GROUPS: AdminNavGroup[] = [
  {
    key: 'dashboard',
    title: '系统首页',
    description: '查看核心指标、待处理事项和最近后台动作',
    items: [
      {
        key: '/admin/dashboard',
        title: '系统首页',
        shortTitle: '首页',
        description: '集中查看核心指标、待办事项和最近后台动作。',
        to: { path: '/admin/dashboard' },
        groupKey: 'dashboard'
      }
    ]
  },
  {
    key: 'activity',
    title: '活动治理',
    description: '围绕活动分类、活动管理、报名审核和打卡记录开展治理',
    items: [
      {
        key: '/admin/activity-category',
        title: '活动分类',
        shortTitle: '活动分类',
        description: '对志愿活动的种类进行划分。',
        to: { path: '/admin/activity-category' },
        groupKey: 'activity'
      },
      {
        key: '/admin/activity-manage',
        title: '志愿活动',
        shortTitle: '志愿活动',
        description: '发布、编辑、归档和管理志愿活动。',
        to: { path: '/admin/activity-manage' },
        groupKey: 'activity'
      },
      {
        key: '/admin/application-audit',
        title: '报名审核',
        shortTitle: '报名审核',
        description: '集中处理志愿活动报名申请。',
        to: { path: '/admin/application-audit' },
        groupKey: 'activity'
      },
      {
        key: '/admin/check-records',
        title: '打卡记录',
        shortTitle: '打卡记录',
        description: '查看志愿者签到、签退与打卡记录。',
        to: { path: '/admin/check-records' },
        groupKey: 'activity'
      }
    ]
  },
  {
    key: 'content',
    title: '内容治理',
    description: '统一管理信息动态、系统公告和轮播图',
    items: [
      {
        key: '/admin/content',
        title: '内容治理',
        shortTitle: '内容治理',
        description: '管理信息动态、系统公告与门户轮播图。',
        to: { path: '/admin/content' },
        groupKey: 'content'
      }
    ]
  },
  {
    key: 'forum',
    title: '论坛治理',
    description: '统一管理帖子分类、帖子审核和评论信息',
    items: [
      {
        key: '/admin/forum',
        title: '论坛治理',
        shortTitle: '论坛治理',
        description: '管理帖子分类、帖子审核与评论治理。',
        to: { path: '/admin/forum' },
        groupKey: 'forum'
      }
    ]
  },
  {
    key: 'mall',
    title: '商城治理',
    description: '统一管理积分商品与兑换订单履约',
    items: [
      {
        key: '/admin/mall',
        title: '商城治理',
        shortTitle: '商城治理',
        description: '管理积分商城商品与兑换订单履约。',
        to: { path: '/admin/mall' },
        groupKey: 'mall'
      }
    ]
  },
  {
    key: 'user',
    title: '用户治理',
    description: '分开管理志愿者信息和管理员信息',
    items: [
      {
        key: 'user:volunteer',
        title: '志愿者信息',
        shortTitle: '志愿者信息',
        description: '对志愿者进行增删改查与状态治理。',
        to: { path: '/admin/users', query: { tab: 'volunteer' } },
        groupKey: 'user'
      },
      {
        key: 'user:admin',
        title: '管理员信息',
        shortTitle: '管理员信息',
        description: '管理管理员账号、角色、电话和邮箱。',
        to: { path: '/admin/users', query: { tab: 'admin' } },
        groupKey: 'user'
      }
    ]
  },
  {
    key: 'system',
    title: '系统治理',
    description: '围绕统计分析、审计追踪和系统设置开展治理',
    items: [
      {
        key: '/admin/analytics',
        title: '统计分析',
        shortTitle: '统计分析',
        description: '查看报名趋势、活动分类分布和运营统计图表。',
        to: { path: '/admin/analytics' },
        groupKey: 'system'
      },
      {
        key: '/admin/audit',
        title: '审计中心',
        shortTitle: '审计中心',
        description: '查看管理员关键操作日志与审计留痕。',
        to: { path: '/admin/audit' },
        groupKey: 'system'
      },
      {
        key: '/admin/settings',
        title: '系统设置',
        shortTitle: '系统设置',
        description: '查看系统参数和后续配置入口。',
        to: { path: '/admin/settings' },
        groupKey: 'system'
      }
    ]
  }
]

export const ADMIN_NAV_ITEMS = ADMIN_NAV_GROUPS.flatMap((group) => group.items)

export function resolveAdminActiveMenuKey(
  route: Pick<RouteLocationNormalizedLoaded, 'path' | 'query'>
) {
  if (route.path === '/admin/content-manage') {
    const target = resolveLegacyContentManageLocation(route.query.tab)
    return typeof target === 'string' ? target : String(target.path)
  }

  if (route.path === '/admin/users') {
    return `user:${normalizeUserTab(route.query.tab)}`
  }

  return route.path
}

export function findAdminNavItemByKey(key: string) {
  return ADMIN_NAV_ITEMS.find((item) => item.key === key)
}

export function findAdminNavGroupByKey(key: AdminNavGroupKey) {
  return ADMIN_NAV_GROUPS.find((group) => group.key === key)
}
