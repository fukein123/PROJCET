<template>
  <div class="home-root fade-up">
    <el-card class="hero" shadow="never">
      <p class="hero-tag">志愿者工作台</p>
      <h2>欢迎回来，{{ userStore.username }}</h2>
      <p>在这里快速完成活动报名、社区交流与服务记录管理，让每次志愿行动都可追踪、可沉淀。</p>
      <div class="hero-actions">
        <el-button type="primary" @click="router.push('/volunteer/activity-center')">去报名活动</el-button>
        <el-button @click="router.push('/volunteer/apply-records')">查看报名记录</el-button>
        <el-button @click="router.push('/portal/forum')">进入社区论坛</el-button>
      </div>
    </el-card>

    <div class="card-grid">
      <article class="metric-card">
        <div class="metric-title">社区资讯</div>
        <div class="metric-value">{{ home.hotDynamics.length }}</div>
      </article>
      <article class="metric-card">
        <div class="metric-title">近期活动</div>
        <div class="metric-value">{{ latestActivities.length }}</div>
      </article>
      <article class="metric-card">
        <div class="metric-title">热门帖子</div>
        <div class="metric-value">{{ home.hotPosts.length }}</div>
      </article>
      <article class="metric-card">
        <div class="metric-title">系统公告</div>
        <div class="metric-value">{{ home.notices.length }}</div>
      </article>
    </div>

    <div class="split-grid">
      <el-card class="module" shadow="never">
        <template #header>
          <div class="module-head">
            <span>系统公告</span>
            <el-button text @click="router.push('/portal/notices')">查看全部</el-button>
          </div>
        </template>
        <ul class="brief-list">
          <li v-for="item in home.notices" :key="item.id">{{ item.title }}</li>
          <li v-if="!home.notices.length">暂无公告</li>
        </ul>
      </el-card>

      <el-card class="module" shadow="never">
        <template #header>
          <div class="module-head">
            <span>论坛热帖</span>
            <el-button text @click="router.push('/portal/forum')">查看全部</el-button>
          </div>
        </template>
        <ul class="brief-list">
          <li v-for="post in home.hotPosts" :key="post.id">{{ post.title }}</li>
          <li v-if="!home.hotPosts.length">暂无帖子</li>
        </ul>
      </el-card>
    </div>

    <el-card class="module" shadow="never">
      <template #header>
        <div class="module-head">
          <span>近期可报名活动</span>
          <el-button text @click="router.push('/volunteer/activity-center')">前往活动中心</el-button>
        </div>
      </template>
      <ul class="brief-list">
        <li v-for="item in latestActivities" :key="item.id">
          {{ item.title }}（{{ formatTime(item.startTime) }}）
        </li>
        <li v-if="!latestActivities.length">暂无可报名活动</li>
      </ul>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { homeApi, type HomePayload } from '@/api/content'
import { pageActivitiesApi, type ActivityModel } from '@/api/activity'

const router = useRouter()
const userStore = useUserStore()
const home = ref<HomePayload>({
  banners: [],
  hotDynamics: [],
  notices: [],
  hotPosts: []
})
const latestActivities = ref<ActivityModel[]>([])

function formatTime(value: string) {
  return dayjs(value).format('MM-DD HH:mm')
}

onMounted(async () => {
  const [homeRes, activityRes] = await Promise.all([
    homeApi(),
    pageActivitiesApi({ current: 1, size: 4, status: 'PUBLISHED' })
  ])
  home.value = homeRes
  latestActivities.value = activityRes.records
})
</script>

<style scoped>
.home-root {
  display: grid;
  gap: 14px;
}

.hero {
  border: 1px solid var(--cvs-border);
  border-radius: 16px;
  background: linear-gradient(120deg, rgba(31, 122, 84, 0.2), rgba(116, 205, 175, 0.18));
}

.hero-tag {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.08em;
  color: #2e6d55;
  font-weight: 700;
}

.hero h2 {
  margin: 8px 0 0;
}

.hero p {
  margin: 10px 0 0;
  color: var(--cvs-text-sub);
}

.hero-actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.module {
  border: 1px solid var(--cvs-border);
  border-radius: 16px;
}

.module-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.brief-list {
  margin: 0;
  padding-left: 18px;
  display: grid;
  gap: 8px;
  color: var(--cvs-text-sub);
}

.split-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

@media (max-width: 900px) {
  .split-grid {
    grid-template-columns: 1fr;
  }
}
</style>
