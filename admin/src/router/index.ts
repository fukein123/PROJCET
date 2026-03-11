import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/userStore'

const publicRoutes: RouteRecordRaw[] = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: () => import('@/views/auth/Login.vue') },
  { path: '/register', component: () => import('@/views/auth/Register.vue') }
]

const appChildren: RouteRecordRaw[] = [
  {
    path: 'dashboard',
    name: 'dashboard',
    meta: { title: '系统首页', roles: ['ADMIN', 'COMMUNITY_ADMIN'] },
    component: () => import('@/views/dashboard/Dashboard.vue')
  },
  {
    path: 'home',
    name: 'home',
    meta: { title: '首页', roles: ['VOLUNTEER'] },
    component: () => import('@/views/home/Home.vue')
  },
  {
    path: 'activity',
    name: 'activity',
    meta: { title: '志愿活动', roles: ['ADMIN', 'COMMUNITY_ADMIN', 'VOLUNTEER'] },
    component: () => import('@/views/activity/ActivityList.vue')
  },
  {
    path: 'news',
    name: 'news',
    meta: { title: '信息动态', roles: ['ADMIN', 'COMMUNITY_ADMIN', 'VOLUNTEER'] },
    component: () => import('@/views/news/NewsList.vue')
  },
  {
    path: 'notice',
    name: 'notice',
    meta: { title: '系统公告', roles: ['ADMIN', 'COMMUNITY_ADMIN', 'VOLUNTEER'] },
    component: () => import('@/views/notice/NoticeList.vue')
  },
  {
    path: 'forum',
    name: 'forum',
    meta: { title: '社区论坛', roles: ['ADMIN', 'COMMUNITY_ADMIN', 'VOLUNTEER'] },
    component: () => import('@/views/forum/ForumList.vue')
  },
  {
    path: 'settings',
    name: 'settings',
    meta: { title: '系统设置', roles: ['ADMIN', 'COMMUNITY_ADMIN', 'VOLUNTEER'] },
    component: () => import('@/views/settings/Settings.vue')
  }
]

const appRoutes: RouteRecordRaw[] = [
  {
    path: '/app',
    component: () => import('@/layouts/AppLayout.vue'),
    children: appChildren
  }
]

export const router = createRouter({
  history: createWebHistory(),
  routes: [...publicRoutes, ...appRoutes]
})

router.beforeEach(async (to) => {
  const user = useUserStore()
  const isPublic = to.path === '/login' || to.path === '/register' || to.path === '/'
  if (isPublic) return true

  if (!user.token) return '/login'

  if (!user.me) {
    try {
      await user.fetchMe()
    } catch {
      user.logout()
      return '/login'
    }
  }

  const roles = (to.meta?.roles as string[] | undefined) ?? []
  if (roles.length > 0 && user.role && !roles.includes(user.role)) {
    return '/app/activity'
  }
  return true
})

