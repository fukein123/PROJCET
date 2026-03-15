import { createRouter, createWebHistory, type RouterHistory } from 'vue-router'
import { clearAuthStorage, getRole, getToken } from '@/utils/auth'
import { privateRoutes, publicRoutes } from './routes'

interface AuthSnapshot {
  token: string
  role: string
}

interface GuardTarget {
  path: string
  fullPath: string
  requiresAuth: boolean
  requiredRoles?: string[]
}

interface GuardDecision {
  redirect?: string
  shouldClearAuth: boolean
}

export function roleHomePath(role: string) {
  if (role === 'ADMIN') return '/admin/dashboard'
  if (role === 'VOLUNTEER') return '/portal'
  return '/login'
}

export function resolveRouteAccess(target: GuardTarget, auth: AuthSnapshot): GuardDecision {
  const isValidRole = auth.role === 'ADMIN' || auth.role === 'VOLUNTEER'

  if (auth.token && !isValidRole) {
    return {
      redirect: `/login?redirect=${encodeURIComponent(target.fullPath)}`,
      shouldClearAuth: true
    }
  }

  if ((target.path === '/login' || target.path === '/register') && auth.token) {
    return {
      redirect: roleHomePath(auth.role),
      shouldClearAuth: false
    }
  }

  if (target.requiresAuth && !auth.token) {
    return {
      redirect: `/login?redirect=${encodeURIComponent(target.fullPath)}`,
      shouldClearAuth: false
    }
  }

  if (target.requiredRoles?.length && !target.requiredRoles.includes(auth.role)) {
    return {
      redirect: roleHomePath(auth.role),
      shouldClearAuth: false
    }
  }

  return {
    shouldClearAuth: false
  }
}

export function createAppRouter(history: RouterHistory = createWebHistory()) {
  const router = createRouter({
    history,
    routes: [...publicRoutes, ...privateRoutes]
  })

  router.beforeEach((to) => {
    const decision = resolveRouteAccess(
      {
        path: to.path,
        fullPath: to.fullPath,
        requiresAuth: to.matched.some((record) => record.meta.requiresAuth),
        requiredRoles: to.meta.roles as string[] | undefined
      },
      {
        token: getToken(),
        role: getRole()
      }
    )

    if (decision.shouldClearAuth) {
      clearAuthStorage()
    }

    return decision.redirect ?? true
  })

  router.afterEach((to) => {
    const title = (to.meta.title as string | undefined) ?? '社区志愿服务平台'
    document.title = `${title} - 社区志愿服务`
  })

  return router
}
