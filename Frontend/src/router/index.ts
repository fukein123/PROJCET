import { createRouter, createWebHistory } from 'vue-router'
import { clearAuthStorage, getRole, getToken } from '@/utils/auth'
import { privateRoutes, publicRoutes } from './routes'

const router = createRouter({
  history: createWebHistory(),
  routes: [...publicRoutes, ...privateRoutes]
})

function roleHomePath(role: string) {
  if (role === 'ADMIN') return '/admin/dashboard'
  if (role === 'VOLUNTEER') return '/portal'
  return '/login'
}

router.beforeEach((to) => {
  const token = getToken()
  const role = getRole()
  const isValidRole = role === 'ADMIN' || role === 'VOLUNTEER'
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)
  const requiredRoles = to.meta.roles as string[] | undefined

  if (token && !isValidRole) {
    clearAuthStorage()
    return `/login?redirect=${encodeURIComponent(to.fullPath)}`
  }

  if (token && role === 'ADMIN' && to.path.startsWith('/portal')) {
    return '/admin/dashboard'
  }

  if ((to.path === '/login' || to.path === '/register') && token) {
    return roleHomePath(role)
  }

  if (requiresAuth && !token) {
    return `/login?redirect=${encodeURIComponent(to.fullPath)}`
  }

  if (requiredRoles && requiredRoles.length && !requiredRoles.includes(role)) {
    return roleHomePath(role)
  }

  return true
})

router.afterEach((to) => {
  const title = (to.meta.title as string | undefined) ?? '社区志愿服务平台'
  document.title = `${title} - 社区志愿服务`
})

export default router
