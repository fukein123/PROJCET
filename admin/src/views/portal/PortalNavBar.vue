<template>
  <header class="portal-nav">
    <div class="logo" @click="router.push('/portal')">CVS 社区志愿</div>
    <nav>
      <a :class="{ active: route.path === '/portal' }" @click.prevent="router.push('/portal')">首页</a>
      <a :class="{ active: route.path === '/portal/activities' }" @click.prevent="router.push('/portal/activities')">志愿活动</a>
      <a :class="{ active: route.path === '/portal/news' }" @click.prevent="router.push('/portal/news')">社区新闻</a>
      <a :class="{ active: route.path === '/portal/notices' }" @click.prevent="router.push('/portal/notices')">社区公告</a>
      <a :class="{ active: route.path === '/portal/forum' }" @click.prevent="router.push('/portal/forum')">社区论坛</a>
    </nav>
    <div class="nav-right">
      <el-button text @click="router.push('/login')">登录</el-button>
      <el-button type="primary" @click="router.push('/register')">注册</el-button>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
</script>

<style scoped>
.portal-nav {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 18px;
  border-bottom: 1px solid var(--cvs-border);
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
}

.logo {
  font-weight: 800;
  letter-spacing: 0.04em;
  cursor: pointer;
}

nav {
  display: flex;
  gap: 18px;
}

nav a {
  color: var(--cvs-text-sub);
  font-weight: 600;
}

nav a.active {
  color: var(--cvs-primary);
}

.nav-right {
  display: flex;
  gap: 8px;
}

@media (max-width: 960px) {
  nav {
    display: none;
  }
}
</style>

