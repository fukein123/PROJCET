<template>
  <div>
    <PortalNavBar />
    <main class="portal-wrap">
      <section class="hero fade-up">
        <h1>让志愿服务可记录、可参与、可持续</h1>
        <p>统一管理活动、报名、打卡与社区互动内容，打造高效的社区志愿服务协作平台。</p>
        <div class="hero-actions">
          <el-button type="primary" @click="router.push('/portal/activities')">进入活动中心</el-button>
          <el-button @click="router.push('/login')">登录后台</el-button>
        </div>
      </section>

      <section class="module">
        <h2 class="section-title">志愿活动</h2>
        <div class="activity-grid">
          <article v-for="item in activities" :key="item.id" class="activity-card">
            <h3>{{ item.title }}</h3>
            <p>{{ item.address }}</p>
            <span>{{ formatTime(item.startTime) }} - {{ formatTime(item.endTime) }}</span>
          </article>
        </div>
      </section>

      <section class="split">
        <div class="module">
          <h2 class="section-title">社区新闻</h2>
          <el-timeline>
            <el-timeline-item v-for="news in dynamics" :key="news.id" :timestamp="formatTime(news.publishTime)">
              {{ news.title }}
            </el-timeline-item>
          </el-timeline>
        </div>
        <div class="module">
          <h2 class="section-title">社区公告</h2>
          <el-timeline>
            <el-timeline-item v-for="notice in notices" :key="notice.id" :timestamp="formatTime(notice.publishTime)">
              {{ notice.title }}
            </el-timeline-item>
          </el-timeline>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import { pageActivitiesApi, type ActivityModel } from '@/api/activity'
import { homeApi, type DynamicModel, type NoticeModel } from '@/api/content'
import PortalNavBar from './PortalNavBar.vue'

const router = useRouter()
const activities = ref<ActivityModel[]>([])
const dynamics = ref<DynamicModel[]>([])
const notices = ref<NoticeModel[]>([])

function formatTime(time: string) {
  return dayjs(time).format('MM-DD HH:mm')
}

onMounted(async () => {
  const [activityRes, homeRes] = await Promise.all([
    pageActivitiesApi({ current: 1, size: 4, status: 'PUBLISHED' }),
    homeApi()
  ])
  activities.value = activityRes.records
  dynamics.value = homeRes.hotDynamics
  notices.value = homeRes.notices
})
</script>

<style scoped>
.portal-wrap {
  width: min(1200px, calc(100% - 24px));
  margin: 14px auto 40px;
  display: grid;
  gap: 14px;
}

.hero {
  border: 1px solid var(--cvs-border);
  border-radius: 20px;
  padding: 28px;
  background:
    linear-gradient(130deg, rgba(31, 122, 84, 0.9), rgba(239, 194, 100, 0.72)),
    url('https://images.unsplash.com/photo-1517486808906-6ca8b3f04846?auto=format&fit=crop&w=1400&q=80')
      center/cover no-repeat;
  color: white;
}

.hero h1 {
  margin: 0;
  font-size: clamp(26px, 4vw, 42px);
}

.hero p {
  margin: 12px 0 18px;
  max-width: 620px;
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  gap: 10px;
}

.module {
  border: 1px solid var(--cvs-border);
  border-radius: 16px;
  background: #fff;
  padding: 16px;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 12px;
}

.activity-card {
  border: 1px solid var(--cvs-border);
  border-radius: 12px;
  padding: 12px;
}

.activity-card h3 {
  margin: 0;
}

.activity-card p {
  margin: 8px 0;
  color: var(--cvs-text-sub);
}

.activity-card span {
  font-size: 13px;
}

.split {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

@media (max-width: 900px) {
  .split {
    grid-template-columns: 1fr;
  }
}
</style>

