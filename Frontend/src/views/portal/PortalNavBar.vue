<template>
  <header class="portal-nav">
    <div class="portal-nav__shell">
      <div class="brand" @click="router.push(PORTAL_PATHS.home)">
        <BrandMark tone="light" subtitle="社区协作 · 志愿行动 · 服务记录" />
      </div>

      <nav class="nav-list">
        <a :class="{ active: isActive(PORTAL_PATHS.home) }" @click.prevent="router.push(PORTAL_PATHS.home)">首页</a>
        <a :class="{ active: isActive(PORTAL_PATHS.activities) }" @click.prevent="router.push(PORTAL_PATHS.activities)">
          志愿活动
        </a>
        <a :class="{ active: isActive(PORTAL_PATHS.news) }" @click.prevent="router.push(PORTAL_PATHS.news)">信息动态</a>
        <a :class="{ active: isActive(PORTAL_PATHS.forum) }" @click.prevent="router.push(PORTAL_PATHS.forum)">社区论坛</a>
        <a :class="{ active: isActive(PORTAL_PATHS.mall) }" @click.prevent="router.push(PORTAL_PATHS.mall)">积分商城</a>
        <a :class="{ active: isActive(PORTAL_PATHS.notices) }" @click.prevent="router.push(PORTAL_PATHS.notices)">系统公告</a>
      </nav>

      <div class="nav-right">
        <template v-if="userStore.isLogin">
          <el-dropdown placement="bottom-end" trigger="click" @command="handleCommand">
            <div class="user-chip">
              <span class="avatar">{{ userInitial }}</span>
              <span class="user-copy">
                <span class="name">{{ userStore.username }}</span>
                <span class="role">{{ dropdownLabel }}</span>
              </span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="item in dropdownItems"
                  :key="item.command"
                  :command="item.command"
                  :divided="item.divided"
                >
                  {{ item.label }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button class="ghost-btn" @click="portalNav.toLogin(PORTAL_PATHS.home)">登录</el-button>
          <el-button type="warning" @click="portalNav.toRegister">注册</el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BrandMark from '@/components/shared/BrandMark.vue'
import { PORTAL_PATHS } from '@/constants/portal-routes'
import { usePortalNavigation } from '@/composables/usePortalNavigation'
import { useUserStore } from '@/stores/userStore'

type DropdownCommand = 'workspace' | 'applications' | 'checks' | 'orders' | 'comments' | 'posts' | 'favorites' | 'profile' | 'logout'

const route = useRoute()
const router = useRouter()
const portalNav = usePortalNavigation()
const userStore = useUserStore()

const userInitial = computed(() => (userStore.username?.slice(0, 1) || '志').toUpperCase())
const dropdownLabel = computed(() => (userStore.role === 'ADMIN' ? '管理员后台' : '志愿者自助'))
const dropdownItems = computed(() =>
  userStore.role === 'ADMIN'
    ? [
        { command: 'workspace', label: '进入后台' },
        { command: 'logout', label: '退出登录', divided: true }
      ]
    : [
        { command: 'workspace', label: '我的服务' },
        { command: 'applications', label: '报名申请' },
        { command: 'checks', label: '打卡记录' },
        { command: 'orders', label: '兑换订单' },
        { command: 'comments', label: '我的评论' },
        { command: 'posts', label: '我的帖子' },
        { command: 'favorites', label: '我的收藏' },
        { command: 'profile', label: '个人中心' },
        { command: 'logout', label: '退出登录', divided: true }
      ]
)

function isActive(path: string) {
  return route.path === path || route.path.startsWith(`${path}/`)
}

function handleCommand(command: DropdownCommand) {
  switch (command) {
    case 'workspace':
      portalNav.toWorkspace()
      return
    case 'applications':
      router.push(PORTAL_PATHS.selfServiceApplications)
      return
    case 'checks':
      router.push(PORTAL_PATHS.selfServiceCheckRecords)
      return
    case 'orders':
      router.push(PORTAL_PATHS.selfServiceOrders)
      return
    case 'comments':
      router.push(PORTAL_PATHS.selfServiceComments)
      return
    case 'posts':
      router.push(PORTAL_PATHS.selfServicePosts)
      return
    case 'favorites':
      router.push(PORTAL_PATHS.selfServiceFavorites)
      return
    case 'profile':
      router.push(PORTAL_PATHS.selfServiceProfile)
      return
    case 'logout':
      portalNav.logoutToPortal()
  }
}
</script>

<style scoped>
.portal-nav {
  position: sticky;
  top: 0;
  z-index: 30;
  padding: 12px 0;
  background:
    linear-gradient(135deg, rgba(13, 76, 53, 0.96), rgba(31, 122, 84, 0.94)),
    linear-gradient(180deg, rgba(255, 255, 255, 0.08), rgba(255, 255, 255, 0));
  box-shadow: 0 12px 26px rgba(17, 50, 36, 0.22);
}

.portal-nav__shell {
  width: min(1240px, calc(100% - 24px));
  margin: 0 auto;
  display: grid;
  grid-template-columns: auto 1fr auto;
  grid-template-areas: 'brand nav actions';
  align-items: center;
  gap: 18px;
}

.brand {
  grid-area: brand;
  min-width: 0;
  cursor: pointer;
}

.nav-list {
  grid-area: nav;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
  min-width: 0;
}

.nav-list a {
  padding: 9px 14px;
  border-radius: var(--cvs-radius-pill);
  font-size: 15px;
  font-weight: var(--cvs-font-weight-bold);
  color: rgba(255, 252, 247, 0.84);
  transition:
    background-color var(--cvs-motion-fast) var(--cvs-ease-standard),
    color var(--cvs-motion-fast) var(--cvs-ease-standard),
    transform var(--cvs-motion-fast) var(--cvs-ease-standard);
}

.nav-list a.active,
.nav-list a:hover {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.16);
}

.nav-right {
  grid-area: actions;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 5px 12px 5px 5px;
  border-radius: var(--cvs-radius-pill);
  cursor: pointer;
  border: 1px solid rgba(255, 248, 238, 0.26);
  background: rgba(255, 255, 255, 0.12);
  min-width: 0;
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-weight: var(--cvs-font-weight-heavy);
  color: #12553a;
  background: #f7f9f4;
  flex-shrink: 0;
}

.user-copy {
  display: grid;
  min-width: 0;
}

.name {
  color: #fffef8;
  font-weight: var(--cvs-font-weight-bold);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.role {
  color: rgba(255, 252, 247, 0.74);
  font-size: var(--cvs-font-size-xs);
}

.ghost-btn {
  --el-text-color-regular: #fffdf8;
  border-color: rgba(255, 255, 255, 0.34);
  color: #fffdf8;
  background: rgba(255, 255, 255, 0.08);
}

.ghost-btn:hover {
  border-color: rgba(255, 255, 255, 0.56);
  background: rgba(255, 255, 255, 0.16);
}

@media (max-width: 1160px) {
  .portal-nav__shell {
    grid-template-columns: 1fr auto;
    grid-template-areas:
      'brand actions'
      'nav nav';
  }

  .nav-list {
    justify-content: flex-start;
  }
}

@media (max-width: 760px) {
  .portal-nav {
    padding: 10px 0;
  }

  .portal-nav__shell {
    width: min(1240px, calc(100% - 18px));
    gap: 12px;
  }

  .nav-list {
    gap: 6px;
  }

  .nav-list a {
    padding: 8px 12px;
    font-size: 14px;
  }

  .user-chip {
    padding-right: 10px;
  }
}
</style>
