import type { RouteRecordRaw } from 'vue-router'
import { PORTAL_PATHS } from '@/constants/portal-routes'
import { resolveLegacyContentManageLocation } from '@/constants/admin-navigation'

export const publicRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/portal'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/portal',
    name: 'PortalHome',
    component: () => import('@/views/portal/PortalHomeView.vue'),
    meta: { title: '社区志愿服务平台' }
  },
  {
    path: '/portal/activities',
    name: 'PortalActivities',
    component: () => import('@/views/portal/PortalActivitiesView.vue'),
    meta: { title: '志愿活动' }
  },
  {
    path: '/portal/activities/:id',
    name: 'PortalActivityDetail',
    component: () => import('@/views/portal/PortalActivityDetailView.vue'),
    meta: { title: '活动详情' }
  },
  {
    path: '/portal/mall',
    name: 'PortalMall',
    component: () => import('@/views/portal/PortalMallView.vue'),
    meta: { title: '积分商城' }
  },
  {
    path: '/portal/news',
    name: 'PortalNews',
    component: () => import('@/views/portal/PortalNewsView.vue'),
    meta: { title: '信息动态' }
  },
  {
    path: '/portal/news/:id',
    name: 'PortalNewsDetail',
    component: () => import('@/views/portal/PortalNewsDetailView.vue'),
    meta: { title: '动态详情' }
  },
  {
    path: '/portal/notices',
    name: 'PortalNotices',
    component: () => import('@/views/portal/PortalNoticesView.vue'),
    meta: { title: '系统公告' }
  },
  {
    path: '/portal/notices/:id',
    name: 'PortalNoticeDetail',
    component: () => import('@/views/portal/PortalNoticeDetailView.vue'),
    meta: { title: '公告详情' }
  },
  {
    path: '/portal/forum',
    name: 'PortalForum',
    component: () => import('@/views/portal/PortalForumView.vue'),
    meta: { title: '社区论坛' }
  },
  {
    path: '/portal/forum/:id',
    name: 'PortalForumPostDetail',
    component: () => import('@/views/portal/PortalForumPostDetailView.vue'),
    meta: { title: '帖子详情' }
  }
]

export const privateRoutes: RouteRecordRaw[] = [
  {
    path: '/admin',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { requiresAuth: true, roles: ['ADMIN'] },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/dashboard/AdminDashboardView.vue'),
        meta: { title: '系统首页', icon: 'DataLine' }
      },
      {
        path: 'weekly-ranking',
        name: 'VolunteerWeeklyRanking',
        component: () => import('@/views/dashboard/VolunteerWeeklyRankingView.vue'),
        meta: { title: '志愿者周统计', icon: 'Histogram' }
      },
      {
        path: 'activity-category',
        name: 'ActivityCategory',
        component: () => import('@/views/activity/ActivityCategoryView.vue'),
        meta: { title: '活动分类', icon: 'CollectionTag' }
      },
      {
        path: 'activity-manage',
        name: 'ActivityManage',
        component: () => import('@/views/activity/ActivityManageView.vue'),
        meta: { title: '志愿活动管理', icon: 'Calendar' }
      },
      {
        path: 'application-audit',
        name: 'ApplicationAudit',
        component: () => import('@/views/audit/ApplicationAuditView.vue'),
        meta: { title: '活动报名审核', icon: 'Select' }
      },
      {
        path: 'check-records',
        name: 'ActivityCheckRecords',
        component: () => import('@/views/activity/ActivityCheckRecordView.vue'),
        meta: { title: '活动打卡记录', icon: 'Clock' }
      },
      {
        path: 'content-manage',
        name: 'ContentManage',
        redirect: (to) => resolveLegacyContentManageLocation(to.query.tab),
        meta: { title: '内容管理兼容入口', icon: 'ChatLineRound' }
      },
      {
        path: 'content',
        name: 'AdminContentGovernance',
        component: () => import('@/views/content/AdminContentGovernanceView.vue'),
        meta: { title: '内容治理', icon: 'Files' }
      },
      {
        path: 'forum',
        name: 'AdminForumGovernance',
        component: () => import('@/views/content/AdminForumGovernanceView.vue'),
        meta: { title: '论坛治理', icon: 'ChatDotSquare' }
      },
      {
        path: 'mall',
        name: 'AdminMallGovernance',
        component: () => import('@/views/content/AdminMallGovernanceView.vue'),
        meta: { title: '商城治理', icon: 'Goods' }
      },
      {
        path: 'audit',
        name: 'AdminOperationAudit',
        component: () => import('@/views/audit/AdminOperationAuditView.vue'),
        meta: { title: '审计中心', icon: 'Document' }
      },
      {
        path: 'analytics',
        name: 'AdminAnalytics',
        component: () => import('@/views/dashboard/AdminAnalyticsView.vue'),
        meta: { title: '统计分析', icon: 'DataAnalysis' }
      },
      {
        path: 'users',
        name: 'UserManage',
        component: () => import('@/views/user/UserManageView.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/settings/SystemSettingsView.vue'),
        meta: { title: '系统设置', icon: 'Setting' }
      }
    ]
  },
  {
    path: '/portal/me',
    component: () => import('@/layouts/PortalSelfServiceLayout.vue'),
    redirect: PORTAL_PATHS.selfServiceHome,
    meta: { requiresAuth: true, roles: ['VOLUNTEER'] },
    children: [
      {
        path: 'home',
        name: 'PortalSelfServiceHome',
        component: () => import('@/views/volunteer/VolunteerHomeView.vue'),
        meta: { title: '我的服务', icon: 'House' }
      },
      {
        path: 'applications',
        name: 'PortalSelfServiceApplications',
        component: () => import('@/views/volunteer/VolunteerApplyRecordsView.vue'),
        meta: { title: '报名申请', icon: 'DocumentCopy' }
      },
      {
        path: 'check-records',
        name: 'PortalSelfServiceCheckRecords',
        component: () => import('@/views/volunteer/VolunteerCheckRecordsView.vue'),
        meta: { title: '打卡记录', icon: 'Clock' }
      },
      {
        path: 'orders',
        name: 'PortalSelfServiceOrders',
        component: () => import('@/views/volunteer/VolunteerExchangeOrdersView.vue'),
        meta: { title: '兑换订单', icon: 'Box' }
      },
      {
        path: 'posts',
        name: 'PortalSelfServicePosts',
        component: () => import('@/views/volunteer/VolunteerMyPostsView.vue'),
        meta: { title: '我的帖子', icon: 'Tickets' }
      },
      {
        path: 'comments',
        name: 'PortalSelfServiceComments',
        component: () => import('@/views/volunteer/VolunteerMyCommentsView.vue'),
        meta: { title: '我的评论', icon: 'ChatDotRound' }
      },
      {
        path: 'favorites',
        name: 'PortalSelfServiceFavorites',
        component: () => import('@/views/volunteer/VolunteerMyFavoritesView.vue'),
        meta: { title: '我的收藏', icon: 'Star' }
      },
      {
        path: 'profile',
        name: 'PortalSelfServiceProfile',
        component: () => import('@/views/volunteer/VolunteerProfileView.vue'),
        meta: { title: '个人中心', icon: 'UserFilled' }
      }
    ]
  },
  {
    path: '/volunteer',
    redirect: PORTAL_PATHS.selfServiceHome
  },
  {
    path: '/volunteer/home',
    redirect: PORTAL_PATHS.selfServiceHome
  },
  {
    path: '/volunteer/activity-center',
    redirect: PORTAL_PATHS.activities
  },
  {
    path: '/volunteer/activity-detail/:id',
    redirect: (to) => PORTAL_PATHS.activityDetail(String(to.params.id))
  },
  {
    path: '/volunteer/apply-records',
    redirect: PORTAL_PATHS.selfServiceApplications
  },
  {
    path: '/volunteer/check-records',
    redirect: PORTAL_PATHS.selfServiceCheckRecords
  },
  {
    path: '/volunteer/my-posts',
    redirect: PORTAL_PATHS.selfServicePosts
  },
  {
    path: '/volunteer/my-comments',
    redirect: PORTAL_PATHS.selfServiceComments
  },
  {
    path: '/volunteer/my-favorites',
    redirect: PORTAL_PATHS.selfServiceFavorites
  },
  {
    path: '/volunteer/profile',
    redirect: PORTAL_PATHS.selfServiceProfile
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    redirect: '/portal'
  }
]
