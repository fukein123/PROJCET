import { defineStore } from 'pinia'
import { apiMe, apiLogin, type LoginType, type UserMe } from '@/api/auth'
import { clearRole, clearToken, getRole, getToken, setRole, setToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() as string | null,
    role: getRole() as string | null,
    me: null as UserMe | null
  }),
  actions: {
    async login(username: string, password: string, loginType: LoginType) {
      const res = await apiLogin({ username, password, loginType })
      this.token = res.token
      this.me = res.user
      this.role = res.user.role
      setToken(res.token)
      setRole(res.user.role)
    },
    async fetchMe() {
      this.me = await apiMe()
      this.role = this.me?.role ?? null
      if (this.role) setRole(this.role)
    },
    logout() {
      this.token = null
      this.role = null
      this.me = null
      clearToken()
      clearRole()
    }
  }
})

