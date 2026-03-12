import type { RouteRecordRaw } from 'vue-router'

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
    path: '/portal/news',
    name: 'PortalNews',
    component: () => import('@/views/portal/PortalNewsView.vue'),
    meta: { title: '信息动态' }
  },
  {
    path: '/portal/notices',
    name: 'PortalNotices',
    component: () => import('@/views/portal/PortalNoticesView.vue'),
    meta: { title: '系统公告' }
  },
  {
    path: '/portal/forum',
    name: 'PortalForum',
    component: () => import('@/views/portal/PortalForumView.vue'),
    meta: { title: '社区论坛' }
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
        meta: { title: '管理员首页', icon: 'DataLine' }
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
        path: 'content-manage',
        name: 'ContentManage',
        component: () => import('@/views/content/ContentManageView.vue'),
        meta: { title: '内容管理（动态/公告/帖子）', icon: 'ChatLineRound' }
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
    path: '/volunteer',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/volunteer/home',
    meta: { requiresAuth: true, roles: ['VOLUNTEER'] },
    children: [
      {
        path: 'home',
        name: 'VolunteerHome',
        component: () => import('@/views/volunteer/VolunteerHomeView.vue'),
        meta: { title: '志愿者首页', icon: 'House' }
      },
      {
        path: 'activity-center',
        name: 'VolunteerActivityCenter',
        component: () => import('@/views/volunteer/VolunteerActivityCenterView.vue'),
        meta: { title: '活动中心', icon: 'Calendar' }
      },
      {
        path: 'activity-detail/:id',
        name: 'VolunteerActivityDetail',
        component: () => import('@/views/volunteer/VolunteerActivityDetailView.vue'),
        meta: { title: '活动详情', hidden: true }
      },
      {
        path: 'apply-records',
        name: 'VolunteerApplyRecords',
        component: () => import('@/views/volunteer/VolunteerApplyRecordsView.vue'),
        meta: { title: '报名申请', icon: 'DocumentCopy' }
      },
      {
        path: 'check-records',
        name: 'VolunteerCheckRecords',
        component: () => import('@/views/volunteer/VolunteerCheckRecordsView.vue'),
        meta: { title: '打卡记录', icon: 'Clock' }
      },
      {
        path: 'my-posts',
        name: 'VolunteerMyPosts',
        component: () => import('@/views/volunteer/VolunteerMyPostsView.vue'),
        meta: { title: '我的帖子', icon: 'Tickets' }
      },
      {
        path: 'my-comments',
        name: 'VolunteerMyComments',
        component: () => import('@/views/volunteer/VolunteerMyCommentsView.vue'),
        meta: { title: '我的评论', icon: 'ChatDotRound' }
      },
      {
        path: 'my-favorites',
        name: 'VolunteerMyFavorites',
        component: () => import('@/views/volunteer/VolunteerMyFavoritesView.vue'),
        meta: { title: '我的收藏', icon: 'Star' }
      },
      {
        path: 'profile',
        name: 'VolunteerProfile',
        component: () => import('@/views/volunteer/VolunteerProfileView.vue'),
        meta: { title: '个人中心', icon: 'UserFilled' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    redirect: '/portal'
  }
]
