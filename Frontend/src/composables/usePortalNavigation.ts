import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { PORTAL_PATHS } from '@/constants/portal-routes'
import { useUserStore } from '@/stores/userStore'

export function usePortalNavigation() {
  const router = useRouter()
  const route = useRoute()
  const userStore = useUserStore()

  const isLogin = computed(() => Boolean(userStore.token))
  const roleLabel = computed(() => (userStore.role === 'ADMIN' ? '管理员' : '志愿者'))
  const workspacePath = computed(() => (userStore.role === 'ADMIN' ? '/admin/dashboard' : PORTAL_PATHS.selfServiceHome))

  function toLogin(redirect = route.fullPath) {
    router.push(`/login?redirect=${encodeURIComponent(redirect)}`)
  }

  function toRegister() {
    router.push('/register')
  }

  function toWorkspace() {
    if (!isLogin.value) {
      toLogin(PORTAL_PATHS.home)
      return
    }
    router.push(workspacePath.value)
  }

  function logoutToPortal() {
    userStore.logout()
    router.push(PORTAL_PATHS.home)
  }

  async function requireLogin(run: () => Promise<void> | void, redirect = route.fullPath) {
    if (!isLogin.value) {
      toLogin(redirect)
      return false
    }
    await run()
    return true
  }

  return {
    isLogin,
    roleLabel,
    toLogin,
    toRegister,
    toWorkspace,
    logoutToPortal,
    requireLogin
  }
}
