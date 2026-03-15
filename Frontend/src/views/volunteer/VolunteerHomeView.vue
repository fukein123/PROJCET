<template>
  <div class="home-root">
    <WorkspaceHero
      eyebrow="志愿者工作台"
      :title="`欢迎回来，${userStore.username}`"
      description="在这里统一完成活动报名、社区交流、公告查看与服务记录跟踪，让每一次志愿行动都可追溯、可沉淀。"
    >
      <template #actions>
        <el-button type="primary" @click="router.push(PORTAL_PATHS.activities)">去报名活动</el-button>
        <el-button @click="router.push(PORTAL_PATHS.selfServiceApplications)">查看报名记录</el-button>
        <el-button @click="router.push(PORTAL_PATHS.forum)">进入社区论坛</el-button>
      </template>
      <template #aside>
        <div class="hero-stat-grid">
          <article class="hero-stat">
            <h3>社区资讯</h3>
            <strong>{{ home.hotDynamics.length }}</strong>
            <span>同步门户端最新动态与社区新闻</span>
          </article>
          <article class="hero-stat">
            <h3>近期活动</h3>
            <strong>{{ latestActivities.length }}</strong>
            <span>当前可报名的社区志愿服务活动</span>
          </article>
          <article class="hero-stat">
            <h3>热门帖子</h3>
            <strong>{{ home.hotPosts.length }}</strong>
            <span>持续跟进社区讨论与服务经验分享</span>
          </article>
        </div>
      </template>
    </WorkspaceHero>

    <StatePanel
      v-if="loading"
      state="loading"
      title="正在同步工作台数据"
      description="正在获取公告、热门帖子与近期可报名活动。"
    />

    <template v-else>
      <div class="split-grid fade-up">
        <section class="module-card">
          <div class="module-head">
            <div>
              <p class="module-eyebrow">统一公告</p>
              <h2 class="section-title">系统公告</h2>
            </div>
            <el-button text @click="router.push(PORTAL_PATHS.notices)">查看全部</el-button>
          </div>
          <StatePanel
            v-if="!home.notices.length"
            compact
            title="暂无公告"
            description="新的平台通知会展示在这里。"
          />
          <ul v-else class="brief-list">
            <li v-for="item in home.notices" :key="item.id">{{ item.title }}</li>
          </ul>
        </section>

        <section class="module-card">
          <div class="module-head">
            <div>
              <p class="module-eyebrow">社区论坛</p>
              <h2 class="section-title">论坛热帖</h2>
            </div>
            <el-button text @click="router.push(PORTAL_PATHS.forum)">查看全部</el-button>
          </div>
          <StatePanel
            v-if="!home.hotPosts.length"
            compact
            title="暂无帖子"
            description="登录后可前往论坛发帖或参与评论。"
          />
          <ul v-else class="brief-list">
            <li v-for="post in home.hotPosts" :key="post.id">{{ post.title }}</li>
          </ul>
        </section>
      </div>

      <section class="module-card fade-up">
        <div class="module-head">
          <div>
            <p class="module-eyebrow">活动报名</p>
            <h2 class="section-title">近期可报名活动</h2>
          </div>
          <el-button text @click="router.push(PORTAL_PATHS.activities)">前往活动中心</el-button>
        </div>
        <StatePanel
          v-if="!latestActivities.length"
          title="暂无可报名活动"
          description="管理员发布新的志愿活动后会在这里展示。"
        />
        <ul v-else class="brief-list">
          <li v-for="item in latestActivities" :key="item.id">
            {{ item.title }}（{{ formatTime(item.startTime) }}）
          </li>
        </ul>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { homeApi, type HomePayload } from '@/api/content'
import { pageActivitiesApi, type ActivityModel } from '@/api/activity'
import StatePanel from '@/components/shared/StatePanel.vue'
import WorkspaceHero from '@/components/shared/WorkspaceHero.vue'
import { PORTAL_PATHS } from '@/constants/portal-routes'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
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
  loading.value = true
  try {
    const [homeRes, activityRes] = await Promise.all([
      homeApi(),
      pageActivitiesApi({ current: 1, size: 4, status: 'PUBLISHED' })
    ])
    home.value = homeRes
    latestActivities.value = activityRes.records
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.home-root {
  display: grid;
  gap: 14px;
}

.module-card {
  border: 1px solid var(--cvs-border);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.94);
  padding: 18px;
  box-shadow: var(--cvs-shadow-soft);
}

.module-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 14px;
}

.module-eyebrow {
  margin: 0 0 6px;
  color: #1f7a54;
  font-size: var(--cvs-font-size-xs);
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.module-head :deep(.section-title) {
  margin-bottom: 0;
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
