<template>
  <div>
    <PortalNavBar />
    <main class="portal-wrap">
      <WorkspaceHero
        tone="portal"
        eyebrow="社区志愿服务门户"
        title="以志愿活动、信息动态与社区论坛，连接社区协作与持续服务"
        description="统一承接活动浏览、报名入口、社区交流与公告通知，让居民、志愿者与管理员都能在同一套品牌体验下协同工作。"
      >
        <template #actions>
          <el-button type="primary" @click="router.push('/portal/activities')">查看志愿活动</el-button>
          <el-button @click="router.push('/portal/forum')">进入社区论坛</el-button>
        </template>
        <template #aside>
          <div class="hero-stat-grid">
            <article class="hero-stat">
              <h3>最新活动</h3>
              <strong>{{ activities.length }}</strong>
              <span>当前公开可报名的社区志愿活动</span>
            </article>
            <article class="hero-stat">
              <h3>系统公告</h3>
              <strong>{{ notices.length }}</strong>
              <span>平台统一发布的通知与提醒</span>
            </article>
            <article class="hero-stat">
              <h3>论坛热帖</h3>
              <strong>{{ hotPosts.length }}</strong>
              <span>社区居民与志愿者的热门讨论</span>
            </article>
          </div>
        </template>
      </WorkspaceHero>

      <section class="module-card fade-up">
        <div class="module-head">
          <div>
            <p class="module-eyebrow">活动广场</p>
            <h2 class="section-title">近期志愿活动</h2>
          </div>
          <el-button text @click="router.push('/portal/activities')">查看更多</el-button>
        </div>
        <StatePanel
          v-if="loading && !activities.length"
          state="loading"
          tone="portal"
          title="正在同步活动列表"
          description="正在加载最新公开活动与报名入口。"
        />
        <StatePanel
          v-else-if="!activities.length"
          tone="portal"
          title="暂无开放活动"
          description="管理员发布新的社区活动后，会优先展示在这里。"
        />
        <div v-else class="activity-grid">
          <article v-for="item in activities" :key="item.id" class="activity-card">
            <h3>{{ item.title }}</h3>
            <p>{{ item.address || '地点待补充' }}</p>
            <span>{{ formatTime(item.startTime) }} - {{ formatTime(item.endTime) }}</span>
            <el-button size="small" type="primary" @click="goApply(item.id)">我要报名</el-button>
          </article>
        </div>
      </section>

      <section class="split fade-up">
        <article class="module-card">
          <div class="module-head">
            <div>
              <p class="module-eyebrow">社区资讯</p>
              <h2 class="section-title">信息动态</h2>
            </div>
            <el-button text @click="router.push('/portal/news')">查看全部</el-button>
          </div>
          <StatePanel
            v-if="loading && !dynamics.length"
            state="loading"
            tone="portal"
            title="正在加载动态"
            description="正在获取最新社区资讯与活动动态。"
            compact
          />
          <StatePanel
            v-else-if="!dynamics.length"
            tone="portal"
            title="暂无动态信息"
            description="新的社区新闻与活动动态会展示在这里。"
            compact
          />
          <el-timeline v-else>
            <el-timeline-item v-for="news in dynamics" :key="news.id" :timestamp="formatTime(news.publishTime)">
              {{ news.title }}
            </el-timeline-item>
          </el-timeline>
        </article>

        <article class="module-card">
          <div class="module-head">
            <div>
              <p class="module-eyebrow">统一公告</p>
              <h2 class="section-title">系统公告</h2>
            </div>
            <el-button text @click="router.push('/portal/notices')">查看全部</el-button>
          </div>
          <StatePanel
            v-if="loading && !notices.length"
            state="loading"
            tone="portal"
            title="正在加载公告"
            description="正在获取平台最新通知。"
            compact
          />
          <StatePanel
            v-else-if="!notices.length"
            tone="portal"
            title="暂无系统公告"
            description="新的平台通知会在这里统一展示。"
            compact
          />
          <el-timeline v-else>
            <el-timeline-item v-for="notice in notices" :key="notice.id" :timestamp="formatTime(notice.publishTime)">
              {{ notice.title }}
            </el-timeline-item>
          </el-timeline>
        </article>
      </section>

      <section class="module-card fade-up">
        <div class="module-head">
          <div>
            <p class="module-eyebrow">社区论坛</p>
            <h2 class="section-title">论坛热帖</h2>
          </div>
          <el-button text @click="router.push('/portal/forum')">进入论坛</el-button>
        </div>
        <StatePanel
          v-if="loading && !hotPosts.length"
          state="loading"
          tone="portal"
          title="正在加载论坛内容"
          description="正在获取社区居民与志愿者的热门讨论。"
        />
        <StatePanel
          v-else-if="!hotPosts.length"
          tone="portal"
          title="暂无热门帖子"
          description="登录后可前往论坛发帖或参与评论。"
        />
        <div v-else class="post-list">
          <article v-for="post in hotPosts" :key="post.id">
            <h3>{{ post.title }}</h3>
            <p>{{ post.content || '当前帖子暂无摘要内容。' }}</p>
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
import StatePanel from '@/components/shared/StatePanel.vue'
import WorkspaceHero from '@/components/shared/WorkspaceHero.vue'
import { usePortalNavigation } from '@/composables/usePortalNavigation'
import PortalNavBar from './PortalNavBar.vue'

const router = useRouter()
const portalNav = usePortalNavigation()
const loading = ref(false)
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
  loading.value = true
  try {
    const [activityRes, homeRes] = await Promise.all([
      pageActivitiesApi({ current: 1, size: 4, status: 'PUBLISHED' }),
      homeApi()
    ])
    activities.value = activityRes.records
    dynamics.value = homeRes.hotDynamics
    notices.value = homeRes.notices
    hotPosts.value = homeRes.hotPosts
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.portal-wrap {
  width: min(1200px, calc(100% - 24px));
  margin: 14px auto 40px;
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
  color: #2a7a5f;
  font-size: var(--cvs-font-size-xs);
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.module-head :deep(.section-title) {
  margin-bottom: 0;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 12px;
}

.activity-card {
  border: 1px solid var(--cvs-border);
  border-radius: 14px;
  padding: 14px;
  display: grid;
  gap: 8px;
  transition:
    transform var(--cvs-motion-fast) var(--cvs-ease-standard),
    box-shadow var(--cvs-motion-fast) var(--cvs-ease-standard);
}

.activity-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--cvs-shadow-card-hover);
}

.activity-card h3,
.post-list h3 {
  margin: 0;
}

.activity-card p,
.post-list p {
  margin: 0;
  color: var(--cvs-text-sub);
  line-height: 1.7;
}

.activity-card span {
  color: #5d7268;
  font-size: var(--cvs-font-size-sm);
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
  border-radius: 14px;
  padding: 14px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(242, 247, 244, 0.9));
}

.post-list p {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

@media (max-width: 960px) {
  .split {
    grid-template-columns: 1fr;
  }
}
</style>
