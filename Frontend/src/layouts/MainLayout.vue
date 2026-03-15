<template>
  <div
    class="main-layout"
    :class="roleClass"
    :style="{ '--sidebar-width': appStore.sidebarCollapsed ? '78px' : '268px' }"
  >
    <aside class="side-panel" :class="{ collapsed: appStore.sidebarCollapsed }">
      <div class="brand-block">
        <BrandMark tone="light" :compact="appStore.sidebarCollapsed" subtitle="管理员工作台" />
      </div>

      <ElScrollbar class="nav-scroll">
        <el-menu
          class="admin-menu"
          :default-active="activeMenuKey"
          :default-openeds="defaultOpenGroups"
          :collapse="appStore.sidebarCollapsed"
          :collapse-transition="false"
          unique-opened
          @select="handleMenuSelect"
        >
          <el-menu-item v-if="dashboardMenuItem" :index="dashboardMenuItem.key">
            <el-icon><HomeFilled /></el-icon>
            <template #title>{{ dashboardMenuItem.title }}</template>
          </el-menu-item>

          <el-sub-menu v-for="group in collapsibleGroups" :key="group.key" :index="group.key">
            <template #title>
              <el-icon>
                <component :is="groupIconMap[group.key]" />
              </el-icon>
              <span>{{ group.title }}</span>
            </template>

            <el-menu-item v-for="item in group.items" :key="item.key" :index="item.key">
              <span class="submenu-text">{{ item.title }}</span>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </ElScrollbar>
    </aside>

    <main class="content-shell">
      <header class="top-bar">
        <div class="top-left">
          <el-button text class="menu-toggle" @click="appStore.toggleSidebar()">
            {{ appStore.sidebarCollapsed ? '展开菜单' : '收起菜单' }}
          </el-button>

          <div class="page-copy">
            <span class="page-eyebrow">{{ currentGroupTitle }}</span>
            <span class="page-title">{{ currentTitle }}</span>
            <small class="page-desc">{{ currentDescription }}</small>
          </div>
        </div>

        <div class="top-right">
          <span class="role-badge">{{ roleLabel }}</span>
          <el-dropdown>
            <span class="drop-link">
              <span class="user-avatar">{{ userInitial }}</span>
              <span class="user-copy">
                <span class="user-name">{{ userStore.username }}</span>
                <small class="user-role">后台账号</small>
              </span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goProfile">账号设置</el-dropdown-item>
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
import { ElScrollbar } from 'element-plus'
import {
  Calendar,
  Files,
  Goods,
  HomeFilled,
  Setting,
  UserFilled,
  ChatDotSquare
} from '@element-plus/icons-vue'
import { useRoute, useRouter, type RouteLocationRaw } from 'vue-router'
import {
  ADMIN_NAV_GROUPS,
  type AdminNavGroup,
  findAdminNavGroupByKey,
  findAdminNavItemByKey,
  type AdminNavGroupKey,
  resolveAdminActiveMenuKey
} from '@/constants/admin-navigation'
import BrandMark from '@/components/shared/BrandMark.vue'
import { useAppStore } from '@/stores/appStore'
import { useUserStore } from '@/stores/userStore'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const dashboardMenuItem = ADMIN_NAV_GROUPS.find((group) => group.key === 'dashboard')?.items[0]
type CollapsibleAdminNavGroup = AdminNavGroup & {
  key: Exclude<AdminNavGroupKey, 'dashboard'>
}

const collapsibleGroups = ADMIN_NAV_GROUPS.filter(
  (group): group is CollapsibleAdminNavGroup => group.key !== 'dashboard'
)

const roleLabel = computed(() => (userStore.role === 'ADMIN' ? '管理员后台' : '志愿者工作台'))
const roleClass = computed(() => (userStore.role === 'ADMIN' ? 'role-admin' : 'role-volunteer'))
const userInitial = computed(() => (userStore.username?.slice(0, 1) || '志').toUpperCase())
const activeMenuKey = computed(() => resolveAdminActiveMenuKey(route))
const currentMenuItem = computed(() => findAdminNavItemByKey(activeMenuKey.value))
const currentGroup = computed(() =>
  currentMenuItem.value ? findAdminNavGroupByKey(currentMenuItem.value.groupKey) : undefined
)
const currentGroupTitle = computed(() => currentGroup.value?.title || '后台工作台')
const currentTitle = computed(() => currentMenuItem.value?.title || (route.meta.title as string) || '后台工作台')
const currentDescription = computed(
  () => currentMenuItem.value?.description || '围绕志愿活动、内容治理、用户治理和系统配置开展后台操作'
)
const defaultOpenGroups = computed(() =>
  currentGroup.value?.key && currentGroup.value.key !== 'dashboard' ? [currentGroup.value.key] : []
)

const groupIconMap: Record<Exclude<AdminNavGroupKey, 'dashboard'>, typeof Files> = {
  activity: Calendar,
  content: Files,
  forum: ChatDotSquare,
  mall: Goods,
  user: UserFilled,
  system: Setting
}

function navigate(target: RouteLocationRaw) {
  router.push(target)
}

function handleMenuSelect(index: string) {
  const matched = findAdminNavItemByKey(index)
  if (matched) {
    navigate(matched.to)
  }
}

function logout() {
  userStore.logout()
  router.push('/login')
}

function goProfile() {
  router.push('/admin/settings')
}
</script>

<style scoped>
.main-layout {
  display: grid;
  grid-template-columns: var(--sidebar-width) 1fr;
  min-height: 100vh;
  background:
    radial-gradient(circle at 0 0, rgba(31, 122, 84, 0.1), transparent 26%),
    radial-gradient(circle at 100% 0, rgba(214, 160, 52, 0.08), transparent 24%),
    linear-gradient(180deg, #f5f7f3 0%, #eef3ee 100%);
}

.side-panel {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  gap: 16px;
  padding: 18px 12px;
  background: linear-gradient(180deg, #123d2b 0%, #103424 100%);
  border-right: 1px solid rgba(122, 180, 151, 0.18);
  position: sticky;
  top: 0;
  height: 100vh;
  overflow: hidden;
}

.brand-block {
  min-height: 44px;
}

.nav-scroll {
  min-height: 0;
}

.admin-menu {
  border: 0;
  background: transparent;
}

.side-panel :deep(.el-menu) {
  border-right: 0;
  background: transparent;
}

.side-panel :deep(.el-menu-item),
.side-panel :deep(.el-sub-menu__title) {
  height: 44px;
  line-height: 44px;
  margin-bottom: 6px;
  border-radius: 12px;
  color: rgba(239, 247, 243, 0.84);
  background: transparent;
}

.side-panel :deep(.el-menu-item:hover),
.side-panel :deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.07);
}

.side-panel :deep(.el-menu-item.is-active) {
  color: #ffffff;
  background: linear-gradient(135deg, rgba(39, 140, 95, 0.92), rgba(34, 117, 80, 0.96));
  box-shadow: 0 8px 18px rgba(10, 33, 24, 0.18);
}

.side-panel :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: #ffffff;
}

.side-panel :deep(.el-menu--inline) {
  background: transparent;
}

.side-panel :deep(.el-menu--inline .el-menu-item) {
  height: 38px;
  line-height: 38px;
  margin-bottom: 2px;
  border-radius: 10px;
  padding-left: 52px !important;
  color: rgba(229, 243, 236, 0.78);
}

.side-panel :deep(.el-menu--inline .el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.1);
  box-shadow: none;
}

.side-panel :deep(.el-sub-menu .el-menu) {
  padding: 4px 0 8px;
}

.side-panel.collapsed :deep(.el-menu-item),
.side-panel.collapsed :deep(.el-sub-menu__title) {
  justify-content: center;
  padding-inline: 0 !important;
}

.submenu-text {
  font-size: 14px;
}

.content-shell {
  display: grid;
  grid-template-rows: auto 1fr;
  min-width: 0;
}

.top-bar {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: rgba(252, 253, 250, 0.92);
  backdrop-filter: blur(14px);
  border-bottom: 1px solid rgba(122, 164, 142, 0.18);
}

.top-left,
.top-right {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.menu-toggle {
  border-radius: 999px;
  border: 1px solid rgba(35, 103, 73, 0.12);
  background: rgba(255, 255, 255, 0.82);
  padding-inline: 14px;
  color: #174631;
}

.page-copy {
  display: grid;
  min-width: 0;
}

.page-eyebrow {
  color: #507162;
  font-size: 12px;
  letter-spacing: 0.08em;
}

.page-title {
  color: #183224;
  font-size: 22px;
  font-weight: var(--cvs-font-weight-heavy);
  line-height: 1.28;
}

.page-desc {
  color: #6b7d74;
  line-height: 1.5;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.role-badge {
  border-radius: 999px;
  padding: 6px 12px;
  border: 1px solid rgba(29, 109, 76, 0.16);
  background: rgba(244, 249, 246, 0.94);
  color: #1d6d4c;
  font-size: 12px;
  font-weight: var(--cvs-font-weight-bold);
}

.drop-link {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 6px 8px 6px 6px;
  border-radius: 999px;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(35, 103, 73, 0.1);
}

.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: #ffffff;
  font-weight: var(--cvs-font-weight-heavy);
  background: linear-gradient(135deg, #1b6e4c, #2f8b66);
}

.user-copy {
  display: grid;
}

.user-name {
  color: #1d2e26;
  font-weight: var(--cvs-font-weight-bold);
}

.user-role {
  color: #6b7c75;
  line-height: 1.4;
}

.page-body {
  padding: 18px;
}

@media (max-width: 1080px) {
  .main-layout {
    grid-template-columns: 1fr;
  }

  .side-panel {
    display: none;
  }
}

@media (max-width: 720px) {
  .top-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .top-left,
  .top-right {
    justify-content: space-between;
    flex-wrap: wrap;
  }

  .page-desc {
    white-space: normal;
  }
}
</style>
