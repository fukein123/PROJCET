import { defineStore } from 'pinia'

interface AppState {
  sidebarCollapsed: boolean
  themeName: string
}

export const useAppStore = defineStore('app', {
  state: (): AppState => ({
    sidebarCollapsed: false,
    themeName: 'civic-light'
  }),
  actions: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    }
  }
})

