import { createRouter, createWebHistory } from 'vue-router'
import { getRole, getToken } from '@/utils/auth'
import { privateRoutes, publicRoutes } from './routes'

const router = createRouter({
  history: createWebHistory(),
  routes: [...publicRoutes, ...privateRoutes]
})

router.beforeEach((to) => {
  const token = getToken()
  const role = getRole()
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)
  const requiredRoles = to.meta.roles as string[] | undefined

  if ((to.path === '/login' || to.path === '/register') && token) {
    if (role === 'ADMIN') return '/admin/dashboard'
    if (role === 'VOLUNTEER') return '/volunteer/home'
  }

  if (requiresAuth && !token) {
    return `/login?redirect=${encodeURIComponent(to.fullPath)}`
  }

  if (requiredRoles && requiredRoles.length && !requiredRoles.includes(role)) {
    if (role === 'ADMIN') return '/admin/dashboard'
    if (role === 'VOLUNTEER') return '/volunteer/home'
    return '/login'
  }

  return true
})

router.afterEach((to) => {
  const title = (to.meta.title as string | undefined) ?? '社区志愿服务平台'
  document.title = `${title} - CVS`
})

export default router

