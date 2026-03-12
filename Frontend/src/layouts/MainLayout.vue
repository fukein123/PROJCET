<template>
  <div class="main-layout" :class="roleClass">
    <aside class="side-panel" :class="{ collapsed: appStore.sidebarCollapsed }">
      <div class="brand">
        <span class="brand-dot"></span>
        <span v-if="!appStore.sidebarCollapsed">社区志愿服务</span>
      </div>
      <div class="role-chip" v-if="!appStore.sidebarCollapsed">{{ roleLabel }}工作台</div>
      <el-menu :default-active="route.path" class="menu" :collapse="appStore.sidebarCollapsed" router>
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <main class="content-shell">
      <header class="top-bar">
        <div class="top-left">
          <el-button text @click="appStore.toggleSidebar()">
            {{ appStore.sidebarCollapsed ? '展开菜单' : '收起菜单' }}
          </el-button>
          <span class="page-title">{{ currentTitle }}</span>
        </div>
        <div class="top-right">
          <span class="role-badge">{{ roleLabel }}</span>
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
const roleLabel = computed(() => (userStore.role === 'ADMIN' ? '管理员' : '志愿者'))
const roleClass = computed(() => (userStore.role === 'ADMIN' ? 'role-admin' : 'role-volunteer'))

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
  router.push('/portal')
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
  grid-template-columns: 268px 1fr;
  min-height: 100vh;
  background: var(--cvs-bg);
  position: relative;
  overflow: hidden;

  --layout-side-start: #0f4d35;
  --layout-side-end: #0a2b1f;
  --layout-side-border: #1f694b;
  --layout-brand-dot: #94ffd0;
  --layout-brand-ring: rgba(148, 255, 208, 0.2);
  --layout-chip-border: rgba(148, 255, 208, 0.35);
  --layout-chip-text: #d9ffee;
  --layout-menu-active-bg: rgba(148, 255, 208, 0.18);
  --layout-menu-active-text: #ddfff1;
  --layout-badge-border: rgba(31, 122, 84, 0.3);
  --layout-badge-text: #1f7a54;
  --layout-top-bg: rgba(255, 255, 255, 0.84);
}

.main-layout::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(circle at 12% 8%, var(--layout-atmo-1), transparent 36%),
    radial-gradient(circle at 88% 12%, var(--layout-atmo-2), transparent 34%);
}

.side-panel {
  background: linear-gradient(180deg, var(--layout-side-start) 0%, var(--layout-side-end) 100%);
  color: #e5f6ef;
  border-right: 1px solid var(--layout-side-border);
  transition: width 0.2s ease;
  width: 268px;
  z-index: 1;
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
  background: var(--layout-brand-dot);
  box-shadow: 0 0 0 6px var(--layout-brand-ring);
}

.role-chip {
  margin: 0 16px 8px;
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid var(--layout-chip-border);
  color: var(--layout-chip-text);
  font-size: 12px;
  letter-spacing: 0.06em;
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
  background: var(--layout-menu-active-bg);
  color: var(--layout-menu-active-text);
}

.content-shell {
  display: grid;
  grid-template-rows: 70px 1fr;
  z-index: 1;
}

.top-bar {
  background: var(--layout-top-bg);
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

.role-badge {
  border: 1px solid var(--layout-badge-border);
  color: var(--layout-badge-text);
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.main-layout.role-admin {
  --layout-side-start: #5a1326;
  --layout-side-end: #300713;
  --layout-side-border: #7b1f36;
  --layout-brand-dot: #ffb4c8;
  --layout-brand-ring: rgba(255, 180, 200, 0.26);
  --layout-chip-border: rgba(255, 202, 217, 0.42);
  --layout-chip-text: #ffe0e9;
  --layout-menu-active-bg: rgba(255, 197, 214, 0.2);
  --layout-menu-active-text: #ffe9ef;
  --layout-badge-border: rgba(216, 76, 115, 0.36);
  --layout-badge-text: #be3056;
  --layout-top-bg: rgba(255, 246, 249, 0.88);
  --layout-atmo-1: rgba(220, 66, 110, 0.16);
  --layout-atmo-2: rgba(245, 170, 90, 0.12);
}

.main-layout.role-volunteer {
  --layout-side-start: #0f4d35;
  --layout-side-end: #0a2b1f;
  --layout-side-border: #1f694b;
  --layout-brand-dot: #94ffd0;
  --layout-brand-ring: rgba(148, 255, 208, 0.2);
  --layout-chip-border: rgba(148, 255, 208, 0.35);
  --layout-chip-text: #d9ffee;
  --layout-menu-active-bg: rgba(148, 255, 208, 0.18);
  --layout-menu-active-text: #ddfff1;
  --layout-badge-border: rgba(31, 122, 84, 0.3);
  --layout-badge-text: #1f7a54;
  --layout-top-bg: rgba(245, 255, 251, 0.86);
  --layout-atmo-1: rgba(26, 141, 95, 0.14);
  --layout-atmo-2: rgba(75, 175, 138, 0.14);
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
