<template>
  <header class="portal-nav">
    <div class="brand" @click="router.push('/portal')">
      <img class="brand-icon" src="/community-favicon.svg" alt="社区志愿服务图标" />
      <span class="brand-title">社区志愿服务平台</span>
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
          <span class="name">{{ userStore.username }}</span>
        </div>
        <el-button class="ghost-btn" @click="logout">退出</el-button>
      </template>
      <template v-else>
        <el-button class="ghost-btn" @click="portalNav.toLogin('/portal')">登录</el-button>
        <el-button type="warning" @click="portalNav.toRegister">注册</el-button>
      </template>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
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
  z-index: 20;
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 18px;
  padding: 10px 20px;
  color: #fff4f6;
  background: linear-gradient(90deg, #d91743 0%, #f22a55 58%, #eb204f 100%);
  box-shadow: 0 8px 20px rgba(181, 20, 59, 0.28);
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.brand-icon {
  width: 36px;
  height: 36px;
  display: block;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.14);
  padding: 4px;
}

.brand-title {
  font-weight: 800;
  letter-spacing: 0.03em;
  font-size: 16px;
  font-family: 'Noto Sans SC', sans-serif;
}

.nav-list {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}

.nav-list a {
  padding: 8px 12px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 700;
  color: rgba(255, 244, 246, 0.9);
  transition: background-color 0.2s ease, color 0.2s ease;
}

.nav-list a.active,
.nav-list a:hover {
  background: rgba(255, 255, 255, 0.2);
  color: #ffffff;
}

.nav-right {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px 4px 4px;
  border-radius: 999px;
  cursor: pointer;
  border: 1px solid rgba(255, 239, 243, 0.4);
  background: rgba(255, 255, 255, 0.12);
}

.avatar {
  width: 30px;
  height: 30px;
  border-radius: 999px;
  display: grid;
  place-items: center;
  font-weight: 800;
  color: #d91e4b;
  background: #fff3f6;
}

.name {
  max-width: 80px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 700;
}

.ghost-btn {
  --el-text-color-regular: #fff4f6;
  border-color: rgba(255, 255, 255, 0.45);
  color: #fff4f6;
  background: rgba(255, 255, 255, 0.08);
}

.ghost-btn:hover {
  border-color: rgba(255, 255, 255, 0.75);
  background: rgba(255, 255, 255, 0.2);
}

@media (max-width: 1160px) {
  .portal-nav {
    grid-template-columns: 1fr;
    justify-items: start;
    gap: 10px;
  }

  .nav-list {
    justify-content: flex-start;
    overflow-x: auto;
    max-width: 100%;
    flex-wrap: nowrap;
    padding-bottom: 4px;
  }

  .nav-list a {
    white-space: nowrap;
  }
}
</style>
