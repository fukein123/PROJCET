import { defineStore } from 'pinia'
import { getRole, getToken, getUsername, setRole, setToken, setUsername, clearAuthStorage } from '@/utils/auth'
import { loginApi, type LoginPayload } from '@/api/auth'
import { getMyProfileApi, type UserModel } from '@/api/user'

interface UserState {
  token: string
  role: string
  username: string
  profile: UserModel | null
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: getToken(),
    role: getRole(),
    username: getUsername(),
    profile: null
  }),
  getters: {
    isLogin: (state) => Boolean(state.token),
    isAdmin: (state) => state.role === 'ADMIN'
  },
  actions: {
    async login(payload: LoginPayload) {
      const res = await loginApi(payload)
      this.token = res.token
      this.role = res.role
      this.username = res.username
      setToken(res.token)
      setRole(res.role)
      setUsername(res.username)
    },
    async fetchProfile() {
      this.profile = await getMyProfileApi()
    },
    logout() {
      this.token = ''
      this.role = ''
      this.username = ''
      this.profile = null
      clearAuthStorage()
    }
  }
})

