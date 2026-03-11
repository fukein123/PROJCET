<template>
  <div class="layout">
    <aside class="sider cvs-card">
      <div class="brand">
        <div class="logo" />
        <div class="title">志愿者服务平台</div>
      </div>
      <el-menu :default-active="active" router class="menu">
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <main class="main">
      <header class="topbar cvs-card">
        <div class="top-left">
          <div class="crumb">{{ pageTitle }}</div>
          <div class="sub cvs-muted">社区志愿服务管理系统</div>
        </div>
        <div class="top-right">
          <div class="user">
            <div class="name">{{ user.me?.username }}</div>
            <div class="role cvs-muted">{{ user.role }}</div>
          </div>
          <el-button type="primary" plain @click="onLogout">退出</el-button>
        </div>
      </header>

      <section class="content">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'

const user = useUserStore()
const route = useRoute()
const router = useRouter()

const active = computed(() => route.path)
const pageTitle = computed(() => String(route.meta?.title ?? ''))

const menus = computed(() => {
  const role = user.role
  const all = [
    { path: '/app/dashboard', label: '系统首页', roles: ['ADMIN', 'COMMUNITY_ADMIN'] },
    { path: '/app/home', label: '首页', roles: ['VOLUNTEER'] },
    { path: '/app/activity', label: '志愿活动', roles: ['ADMIN', 'COMMUNITY_ADMIN', 'VOLUNTEER'] },
    { path: '/app/news', label: '信息动态', roles: ['ADMIN', 'COMMUNITY_ADMIN', 'VOLUNTEER'] },
    { path: '/app/notice', label: '系统公告', roles: ['ADMIN', 'COMMUNITY_ADMIN', 'VOLUNTEER'] },
    { path: '/app/forum', label: '社区论坛', roles: ['ADMIN', 'COMMUNITY_ADMIN', 'VOLUNTEER'] },
    { path: '/app/settings', label: '系统设置', roles: ['ADMIN', 'COMMUNITY_ADMIN', 'VOLUNTEER'] }
  ]
  return all.filter((m) => !role || m.roles.includes(role))
})

function onLogout() {
  user.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 16px;
  padding: 16px;
  box-sizing: border-box;
}
.sider {
  padding: 14px 12px;
  position: sticky;
  top: 16px;
  height: calc(100vh - 32px);
  overflow: auto;
}
.brand {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 10px 10px 14px;
  border-bottom: 1px solid var(--cvs-border);
}
.logo {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--cvs-primary), #ff6b6b);
}
.title {
  font-weight: 700;
  letter-spacing: 0.5px;
}
.menu {
  border-right: none;
  margin-top: 10px;
}
.main {
  display: grid;
  grid-template-rows: auto 1fr;
  gap: 16px;
}
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
}
.crumb {
  font-size: 16px;
  font-weight: 700;
}
.sub {
  margin-top: 2px;
  font-size: 12px;
}
.top-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user {
  text-align: right;
}
.name {
  font-weight: 700;
}
.role {
  font-size: 12px;
}
.content {
  padding-bottom: 24px;
}
</style>

