<template>
  <header class="portal-nav">
    <div class="portal-nav__shell">
      <div class="brand" @click="router.push('/portal')">
        <BrandMark tone="light" subtitle="社区协作 · 志愿行动 · 服务记录" />
      </div>

      <nav class="nav-list">
        <a :class="{ active: route.path === '/portal' }" @click.prevent="router.push('/portal')">首页</a>
        <a :class="{ active: route.path === '/portal/activities' }" @click.prevent="router.push('/portal/activities')">
          志愿活动
        </a>
        <a :class="{ active: route.path === '/portal/news' }" @click.prevent="router.push('/portal/news')">信息动态</a>
        <a :class="{ active: route.path === '/portal/forum' }" @click.prevent="router.push('/portal/forum')">社区论坛</a>
        <a :class="{ active: route.path === '/portal/notices' }" @click.prevent="router.push('/portal/notices')">系统公告</a>
      </nav>

      <div class="nav-right">
        <template v-if="userStore.isLogin">
          <div class="user-chip" @click="portalNav.toWorkspace">
            <span class="avatar">{{ userInitial }}</span>
            <span class="user-copy">
              <span class="name">{{ userStore.username }}</span>
              <span class="role">{{ portalNav.roleLabel }}工作台</span>
            </span>
          </div>
          <el-button class="ghost-btn" @click="logout">退出</el-button>
        </template>
        <template v-else>
          <el-button class="ghost-btn" @click="portalNav.toLogin('/portal')">登录</el-button>
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
import { usePortalNavigation } from '@/composables/usePortalNavigation'
import { useUserStore } from '@/stores/userStore'

const route = useRoute()
const router = useRouter()
const portalNav = usePortalNavigation()
const userStore = useUserStore()

const userInitial = computed(() => (userStore.username?.slice(0, 1) || '志').toUpperCase())

function logout() {
  portalNav.logoutToPortal()
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
  align-items: center;
  gap: 18px;
}

.brand {
  min-width: 0;
  cursor: pointer;
}

.nav-list {
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
    grid-template-columns: 1fr;
    justify-items: start;
  }

  .nav-list {
    justify-content: flex-start;
    flex-wrap: nowrap;
    overflow-x: auto;
    max-width: 100%;
    padding-bottom: 4px;
  }

  .nav-list a {
    white-space: nowrap;
  }
}

@media (max-width: 640px) {
  .nav-right {
    width: 100%;
    flex-wrap: wrap;
  }

  .user-chip {
    flex: 1 1 auto;
  }
}
</style>
