<template>
  <div class="fade-up">
    <el-card class="hero" shadow="never">
      <h2>欢迎回来，{{ userStore.username }}</h2>
      <p>今天也来参与一场有意义的社区志愿活动吧。</p>
      <el-button type="primary" @click="router.push('/volunteer/activity-center')">去活动中心</el-button>
    </el-card>

    <div class="card-grid">
      <article class="metric-card">
        <div class="metric-title">热点信息</div>
        <div class="metric-value">{{ home.hotDynamics.length }}</div>
      </article>
      <article class="metric-card">
        <div class="metric-title">最新活动</div>
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
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
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

onMounted(async () => {
  const [homeRes, activityRes] = await Promise.all([
    homeApi(),
    pageActivitiesApi({ current: 1, size: 4 })
  ])
  home.value = homeRes
  latestActivities.value = activityRes.records
})
</script>

<style scoped>
.hero {
  border: 1px solid var(--cvs-border);
  border-radius: 16px;
  margin-bottom: 14px;
  background: linear-gradient(120deg, rgba(31, 122, 84, 0.16), rgba(239, 194, 100, 0.18));
}

.hero h2 {
  margin: 0;
}

.hero p {
  color: var(--cvs-text-sub);
}
</style>

