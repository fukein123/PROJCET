<template>
  <div class="main-layout" :class="roleClass">
    <aside class="side-panel" :class="{ collapsed: appStore.sidebarCollapsed }">
      <div class="brand">
        <BrandMark
          tone="light"
          :compact="appStore.sidebarCollapsed"
          subtitle="统一工作台导航"
        />
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
          <el-button text class="menu-toggle" @click="appStore.toggleSidebar()">
            {{ appStore.sidebarCollapsed ? '展开菜单' : '收起菜单' }}
          </el-button>
          <BrandMark class="top-brand" compact subtitle="" />
          <div class="page-copy">
            <span class="page-eyebrow">{{ roleLabel }}工作台</span>
            <span class="page-title">{{ currentTitle }}</span>
          </div>
        </div>
        <div class="top-right">
          <el-button v-if="userStore.role !== 'ADMIN'" class="portal-entry" @click="router.push('/portal')">
            返回门户
          </el-button>
          <span class="role-badge">{{ roleLabel }}</span>
          <el-dropdown>
            <span class="drop-link">
              <span class="user-avatar">{{ userInitial }}</span>
              <span class="user-name">{{ userStore.username }}</span>
            </span>
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
import BrandMark from '@/components/shared/BrandMark.vue'
import { useAppStore } from '@/stores/appStore'
import { useUserStore } from '@/stores/userStore'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const basePath = computed(() => (route.path.startsWith('/admin') ? '/admin' : '/volunteer'))
const roleLabel = computed(() => (userStore.role === 'ADMIN' ? '管理员' : '志愿者'))
const roleClass = computed(() => (userStore.role === 'ADMIN' ? 'role-admin' : 'role-volunteer'))
const userInitial = computed(() => (userStore.username?.slice(0, 1) || '志').toUpperCase())

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
  const role = userStore.role
  userStore.logout()
  router.push(role === 'ADMIN' ? '/login' : '/portal')
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
  transition: width var(--cvs-motion-base) var(--cvs-ease-standard);
  width: 268px;
  z-index: 1;
}

.side-panel.collapsed {
  width: 64px;
}

.brand {
  padding: 16px 14px 10px;
}

.role-chip {
  margin: 0 16px 8px;
  padding: 6px 10px;
  border-radius: var(--cvs-radius-pill);
  border: 1px solid var(--layout-chip-border);
  color: var(--layout-chip-text);
  font-size: var(--cvs-font-size-xs);
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
  grid-template-rows: 76px 1fr;
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
  gap: 12px;
}

.top-brand {
  display: none;
}

.page-copy {
  display: grid;
}

.page-eyebrow {
  font-size: var(--cvs-font-size-xs);
  color: var(--cvs-text-sub);
  letter-spacing: 0.08em;
}

.page-title {
  font-weight: var(--cvs-font-weight-bold);
  font-size: 18px;
  color: var(--cvs-text-main);
}

.top-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.portal-entry {
  border-color: var(--layout-badge-border);
  color: var(--layout-badge-text);
  background: rgba(255, 255, 255, 0.48);
}

.role-badge {
  border: 1px solid var(--layout-badge-border);
  color: var(--layout-badge-text);
  padding: 4px 10px;
  border-radius: var(--cvs-radius-pill);
  font-size: var(--cvs-font-size-xs);
  font-weight: var(--cvs-font-weight-bold);
}

.drop-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-weight: var(--cvs-font-weight-semibold);
}

.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: #ffffff;
  background: linear-gradient(135deg, var(--layout-side-start), var(--layout-side-end));
  font-size: 13px;
  font-weight: var(--cvs-font-weight-bold);
}

.user-name {
  max-width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.main-layout.role-admin {
  --layout-side-start: #5a1326;
  --layout-side-end: #300713;
  --layout-side-border: #7b1f36;
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

  .top-brand {
    display: inline-flex;
  }
}

@media (max-width: 720px) {
  .top-bar {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
    padding: 14px 16px;
  }

  .top-left,
  .top-right {
    justify-content: space-between;
    flex-wrap: wrap;
  }

  .user-name {
    max-width: 84px;
  }
}
</style>
