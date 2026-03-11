<template>
  <div class="main-layout">
    <aside class="side-panel" :class="{ collapsed: appStore.sidebarCollapsed }">
      <div class="brand">
        <span class="brand-dot"></span>
        <span v-if="!appStore.sidebarCollapsed">CVS 管理台</span>
      </div>
      <el-menu
        :default-active="route.path"
        class="menu"
        :collapse="appStore.sidebarCollapsed"
        router
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </aside>
    <main class="content-shell">
      <header class="top-bar">
        <div class="top-left">
          <el-button text @click="appStore.toggleSidebar()">
            {{ appStore.sidebarCollapsed ? '展开' : '收起' }}
          </el-button>
          <span class="page-title">{{ currentTitle }}</span>
        </div>
        <div class="top-right">
          <el-button text @click="router.push('/portal')">返回门户</el-button>
          <el-dropdown>
            <span class="drop-link">{{ userStore.username }}</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goProfile">个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <section class="page-body">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/appStore'
import { useUserStore } from '@/stores/userStore'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const basePath = computed(() => (route.path.startsWith('/admin') ? '/admin' : '/volunteer'))

const menuItems = computed(() => {
  const root = router.getRoutes().find((item) => item.path === basePath.value)
  if (!root?.children) return []
  return root.children
    .filter((item) => !item.meta?.hidden)
    .map((item) => ({
      path: `${basePath.value}/${item.path}`,
      title: (item.meta?.title as string) || item.name || item.path
    }))
})

const currentTitle = computed(() => (route.meta.title as string) || '工作台')

function logout() {
  userStore.logout()
  router.push('/login')
}

function goProfile() {
  if (basePath.value === '/admin') {
    router.push('/admin/settings')
    return
  }
  router.push('/volunteer/profile')
}
</script>

<style scoped>
.main-layout {
  display: grid;
  grid-template-columns: 260px 1fr;
  min-height: 100vh;
  background: var(--cvs-bg);
}

.side-panel {
  background: linear-gradient(180deg, #183a2f 0%, #102920 100%);
  color: #e5f6ef;
  border-right: 1px solid #1f4b3d;
  transition: width 0.2s ease;
  width: 260px;
}

.side-panel.collapsed {
  width: 64px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.brand-dot {
  width: 12px;
  height: 12px;
  border-radius: 999px;
  background: #76e5b6;
  box-shadow: 0 0 0 6px rgba(118, 229, 182, 0.2);
}

.menu {
  border-right: none;
  background: transparent;
}

.menu :deep(.el-menu-item) {
  color: #d6ebe3;
  border-radius: 10px;
  margin: 4px 10px;
}

.menu :deep(.el-menu-item.is-active) {
  background: rgba(118, 229, 182, 0.16);
  color: #a6ffd5;
}

.content-shell {
  display: grid;
  grid-template-rows: 70px 1fr;
}

.top-bar {
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid var(--cvs-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.top-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-title {
  font-weight: 700;
  font-size: 18px;
  color: var(--cvs-text-main);
}

.top-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.drop-link {
  cursor: pointer;
  font-weight: 600;
}

.page-body {
  padding: 16px;
}

@media (max-width: 900px) {
  .main-layout {
    grid-template-columns: 1fr;
  }
  .side-panel {
    display: none;
  }
}
</style>
