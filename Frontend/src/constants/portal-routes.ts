export const PORTAL_PATHS = {
  home: '/portal',
  activities: '/portal/activities',
  activityDetail: (id: number | string) => `/portal/activities/${id}`,
  mall: '/portal/mall',
  news: '/portal/news',
  newsDetail: (id: number | string) => `/portal/news/${id}`,
  notices: '/portal/notices',
  noticeDetail: (id: number | string) => `/portal/notices/${id}`,
  forum: '/portal/forum',
  forumDetail: (id: number | string) => `/portal/forum/${id}`,
  selfServiceHome: '/portal/me/home',
  selfServiceApplications: '/portal/me/applications',
  selfServiceCheckRecords: '/portal/me/check-records',
  selfServiceOrders: '/portal/me/orders',
  selfServicePosts: '/portal/me/posts',
  selfServiceComments: '/portal/me/comments',
  selfServiceFavorites: '/portal/me/favorites',
  selfServiceProfile: '/portal/me/profile'
} as const

export type PortalProfileSection = 'profile' | 'certification' | 'security'

export function buildPortalProfileLocation(section: PortalProfileSection = 'profile') {
  if (section === 'profile') {
    return { path: PORTAL_PATHS.selfServiceProfile }
  }

  return {
    path: PORTAL_PATHS.selfServiceProfile,
    query: { section }
  }
}
