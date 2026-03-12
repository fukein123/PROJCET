<template>
  <div>
    <PortalNavBar />
    <main class="portal-wrap">
      <section class="hero fade-up">
        <div class="hero-left">
          <p class="hero-tag">社区志愿服务平台</p>
          <h1>以志愿活动与社区论坛为核心，构建可持续的服务协作网络</h1>
          <p>
            统一连接活动发布、志愿报名、服务打卡与社区交流。无论你是管理员还是志愿者，都能在这里找到清晰的工作入口。
          </p>
          <div class="hero-actions">
            <el-button type="primary" @click="router.push('/portal/activities')">查看志愿活动</el-button>
            <el-button @click="router.push('/portal/forum')">进入社区论坛</el-button>
          </div>
        </div>
        <div class="hero-right">
          <article>
            <h3>最新活动</h3>
            <strong>{{ activities.length }}</strong>
          </article>
          <article>
            <h3>系统公告</h3>
            <strong>{{ notices.length }}</strong>
          </article>
          <article>
            <h3>热门帖子</h3>
            <strong>{{ hotPosts.length }}</strong>
          </article>
        </div>
      </section>

      <section class="module">
        <div class="section-head">
          <h2 class="section-title">志愿活动</h2>
          <el-button text @click="router.push('/portal/activities')">更多活动</el-button>
        </div>
        <div class="activity-grid">
          <article v-for="item in activities" :key="item.id" class="activity-card">
            <h3>{{ item.title }}</h3>
            <p>{{ item.address }}</p>
            <span>{{ formatTime(item.startTime) }} - {{ formatTime(item.endTime) }}</span>
            <el-button size="small" type="primary" @click="goApply(item.id)">我要报名</el-button>
          </article>
        </div>
      </section>

      <section class="split">
        <article class="module">
          <div class="section-head">
            <h2 class="section-title">社区资讯</h2>
            <el-button text @click="router.push('/portal/news')">查看全部</el-button>
          </div>
          <el-timeline>
            <el-timeline-item v-for="news in dynamics" :key="news.id" :timestamp="formatTime(news.publishTime)">
              {{ news.title }}
            </el-timeline-item>
          </el-timeline>
        </article>

        <article class="module">
          <div class="section-head">
            <h2 class="section-title">系统公告</h2>
            <el-button text @click="router.push('/portal/notices')">查看全部</el-button>
          </div>
          <el-timeline>
            <el-timeline-item v-for="notice in notices" :key="notice.id" :timestamp="formatTime(notice.publishTime)">
              {{ notice.title }}
            </el-timeline-item>
          </el-timeline>
        </article>
      </section>

      <section class="module">
        <div class="section-head">
          <h2 class="section-title">论坛热帖</h2>
          <el-button text @click="router.push('/portal/forum')">进入论坛</el-button>
        </div>
        <div class="post-list">
          <article v-for="post in hotPosts" :key="post.id">
            <h3>{{ post.title }}</h3>
            <p>{{ post.content }}</p>
          </article>
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
import { homeApi, type DynamicModel, type NoticeModel, type PostModel } from '@/api/content'
import { usePortalNavigation } from '@/composables/usePortalNavigation'
import PortalNavBar from './PortalNavBar.vue'

const router = useRouter()
const portalNav = usePortalNavigation()
const activities = ref<ActivityModel[]>([])
const dynamics = ref<DynamicModel[]>([])
const notices = ref<NoticeModel[]>([])
const hotPosts = ref<PostModel[]>([])

function formatTime(time: string) {
  return dayjs(time).format('MM-DD HH:mm')
}

function goApply(activityId: number) {
  portalNav.toLogin(`/portal/activities?apply=${activityId}`)
}

onMounted(async () => {
  const [activityRes, homeRes] = await Promise.all([
    pageActivitiesApi({ current: 1, size: 4, status: 'PUBLISHED' }),
    homeApi()
  ])
  activities.value = activityRes.records
  dynamics.value = homeRes.hotDynamics
  notices.value = homeRes.notices
  hotPosts.value = homeRes.hotPosts
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
  border-radius: 22px;
  padding: 24px;
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 18px;
  background:
    linear-gradient(130deg, rgba(26, 117, 80, 0.88), rgba(224, 174, 77, 0.72)),
    url('https://images.unsplash.com/photo-1517486808906-6ca8b3f04846?auto=format&fit=crop&w=1400&q=80')
      center/cover no-repeat;
  color: white;
}

.hero-tag {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.12em;
  opacity: 0.9;
}

.hero h1 {
  margin: 10px 0 0;
  font-size: clamp(26px, 4vw, 42px);
  line-height: 1.2;
}

.hero p {
  margin: 12px 0 0;
  max-width: 620px;
  line-height: 1.75;
}

.hero-actions {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hero-right {
  display: grid;
  gap: 10px;
}

.hero-right article {
  border: 1px solid rgba(255, 255, 255, 0.26);
  border-radius: 14px;
  padding: 14px;
  background: rgba(0, 0, 0, 0.12);
}

.hero-right h3 {
  margin: 0;
  font-size: 13px;
  font-weight: 500;
  opacity: 0.9;
}

.hero-right strong {
  font-size: 28px;
}

.module {
  border: 1px solid var(--cvs-border);
  border-radius: 16px;
  background: #fff;
  padding: 16px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
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
  display: grid;
  gap: 8px;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.activity-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 20px rgba(26, 48, 39, 0.08);
}

.activity-card h3 {
  margin: 0;
}

.activity-card p {
  margin: 0;
  color: var(--cvs-text-sub);
}

.activity-card span {
  color: #678075;
  font-size: 13px;
}

.split {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.post-list {
  display: grid;
  gap: 10px;
}

.post-list article {
  border: 1px solid var(--cvs-border);
  border-radius: 12px;
  padding: 12px;
}

.post-list h3 {
  margin: 0;
}

.post-list p {
  margin: 8px 0 0;
  color: var(--cvs-text-sub);
  line-height: 1.7;
}

@media (max-width: 960px) {
  .hero {
    grid-template-columns: 1fr;
  }

  .split {
    grid-template-columns: 1fr;
  }
}
</style>
