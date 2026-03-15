<template>
  <div>
    <PortalNavBar />
    <main class="portal-wrap">
      <WorkspaceHero
        tone="portal"
        eyebrow="社区志愿服务门户"
        title="围绕志愿活动、热点信息与社区论坛，建立统一的社区志愿服务主页面"
        description="首页只承担门户入口职责，集中展示热点信息、最新活动和热门帖子，公告统一进入公告页查看。"
      >
        <template #actions>
          <el-button type="primary" @click="router.push(PORTAL_PATHS.activities)">查看志愿活动</el-button>
          <el-button @click="router.push(PORTAL_PATHS.forum)">进入社区论坛</el-button>
        </template>
        <template #aside>
          <div class="hero-stat-grid">
            <article class="hero-stat">
              <h3>最新活动</h3>
              <strong>{{ activities.length }}</strong>
              <span>当前公开可报名的志愿活动</span>
            </article>
            <article class="hero-stat">
              <h3>热点信息</h3>
              <strong>{{ featuredDynamics.length }}</strong>
              <span>首页重点展示的新闻与活动动态</span>
            </article>
            <article class="hero-stat">
              <h3>热门帖子</h3>
              <strong>{{ featuredPosts.length }}</strong>
              <span>社区居民与志愿者的热门讨论</span>
            </article>
          </div>
        </template>
      </WorkspaceHero>

      <section v-if="homeBanners.length" class="module-card fade-up banner-shell">
        <div class="module-head">
          <div>
            <p class="module-eyebrow">首页轮播</p>
            <h2 class="section-title">社区服务推荐</h2>
          </div>
          <span class="banner-tip">点击轮播图可直达关联活动详情</span>
        </div>
        <el-carousel height="196px" indicator-position="outside" trigger="click">
          <el-carousel-item v-for="item in homeBanners" :key="item.id">
            <button class="banner-slide" type="button" @click="openBanner(item.activityId)">
              <img class="banner-image" :src="item.imageUrl" :alt="item.title" />
              <div class="banner-mask">
                <p class="banner-eyebrow">社区志愿服务轮播</p>
                <h3>{{ item.title }}</h3>
                <span>{{ item.activityId ? '查看关联活动详情' : '当前轮播未关联活动' }}</span>
              </div>
            </button>
          </el-carousel-item>
        </el-carousel>
      </section>

      <section class="module-card fade-up">
        <div class="module-head">
          <div>
            <p class="module-eyebrow">活动广场</p>
            <h2 class="section-title">近期志愿活动</h2>
          </div>
          <el-button text @click="router.push(PORTAL_PATHS.activities)">查看更多</el-button>
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
            <p>{{ item.content || '当前活动暂无简介。' }}</p>
            <span>{{ formatTime(item.startTime) }} - {{ formatTime(item.endTime) }}</span>
            <div class="activity-actions">
              <el-button size="small" type="primary" @click="openActivityDetail(item.id)">查看详情</el-button>
              <el-button size="small" plain @click="openActivityDetail(item.id)">我要报名</el-button>
            </div>
          </article>
        </div>
      </section>

      <section class="split fade-up">
        <article class="module-card">
          <div class="module-head">
            <div>
              <p class="module-eyebrow">热点信息</p>
              <h2 class="section-title">首页重点推荐</h2>
            </div>
            <el-button text @click="router.push(PORTAL_PATHS.news)">进入动态页</el-button>
          </div>

          <StatePanel
            v-if="loading && !featuredDynamics.length"
            state="loading"
            tone="portal"
            title="正在加载热点信息"
            description="正在整理首页重点展示的社区新闻与活动动态。"
            compact
          />
          <StatePanel
            v-else-if="!featuredDynamics.length"
            tone="portal"
            title="暂无热点信息"
            description="新的热点内容发布后会优先展示在这里。"
            compact
          />
          <div v-else class="highlight-list">
            <button
              v-for="(item, index) in featuredDynamics"
              :key="item.id"
              type="button"
              class="highlight-card"
              :class="{ featured: index === 0 }"
              @click="router.push(PORTAL_PATHS.newsDetail(item.id))"
            >
              <div class="highlight-copy">
                <p class="highlight-tag">{{ typeLabel(item.type) }}</p>
                <h3>{{ item.title }}</h3>
                <span>{{ dynamicPreview(item) }}</span>
              </div>
              <small>{{ formatTime(item.publishTime) }}</small>
            </button>
          </div>
        </article>

        <article class="module-card">
          <div class="module-head">
            <div>
              <p class="module-eyebrow">社区论坛</p>
              <h2 class="section-title">热门论坛帖子</h2>
            </div>
            <el-button text @click="router.push(PORTAL_PATHS.forum)">进入论坛</el-button>
          </div>
          <StatePanel
            v-if="loading && !featuredPosts.length"
            state="loading"
            tone="portal"
            title="正在加载论坛内容"
            description="正在获取社区居民与志愿者的热门讨论。"
            compact
          />
          <StatePanel
            v-else-if="!featuredPosts.length"
            tone="portal"
            title="暂无热门帖子"
            description="登录后可前往论坛发帖或参与评论。"
            compact
          />
          <div v-else class="post-highlight-list">
            <button
              v-for="post in featuredPosts"
              :key="post.id"
              type="button"
              class="post-highlight-card"
              @click="router.push(PORTAL_PATHS.forumDetail(post.id))"
            >
              <div class="post-highlight-copy">
                <h3>{{ post.title }}</h3>
                <p>{{ postPreview(post) }}</p>
              </div>
              <div class="post-highlight-meta">
                <span>浏览 {{ post.views ?? 0 }}</span>
                <small>{{ formatTime(post.createTime || '') }}</small>
              </div>
            </button>
          </div>
        </article>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import { pageActivitiesApi, type ActivityModel } from '@/api/activity'
import { homeApi, type BannerModel, type DynamicModel, type PostModel } from '@/api/content'
import StatePanel from '@/components/shared/StatePanel.vue'
import WorkspaceHero from '@/components/shared/WorkspaceHero.vue'
import { PORTAL_PATHS } from '@/constants/portal-routes'
import { richTextToPlainText } from '@/utils/rich-text'
import PortalNavBar from './PortalNavBar.vue'

const router = useRouter()
const loading = ref(false)
const activities = ref<ActivityModel[]>([])
const homeBanners = ref<BannerModel[]>([])
const dynamics = ref<DynamicModel[]>([])
const hotPosts = ref<PostModel[]>([])

const featuredDynamics = computed(() => dynamics.value.slice(0, 4))
const featuredPosts = computed(() => hotPosts.value.slice(0, 4))

function formatTime(time?: string) {
  if (!time) {
    return '--'
  }
  return dayjs(time).format('MM-DD HH:mm')
}

function typeLabel(type?: string) {
  return type === 'DYNAMIC' ? '活动动态' : '社区新闻'
}

function dynamicPreview(item: DynamicModel) {
  return richTextToPlainText(item.content) || '当前信息暂无摘要内容。'
}

function postPreview(post: PostModel) {
  return post.summary || richTextToPlainText(post.content) || '当前帖子暂无正文摘要。'
}

function openActivityDetail(activityId: number) {
  router.push(PORTAL_PATHS.activityDetail(activityId))
}

function openBanner(activityId?: number | null) {
  if (!activityId) {
    return
  }
  openActivityDetail(activityId)
}

onMounted(async () => {
  loading.value = true
  try {
    const [activityRes, homeRes] = await Promise.all([
      pageActivitiesApi({ current: 1, size: 4, status: 'PUBLISHED' }),
      homeApi()
    ])
    activities.value = activityRes.records
    homeBanners.value = homeRes.banners
    dynamics.value = homeRes.hotDynamics
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

.banner-shell {
  overflow: hidden;
}

.banner-tip {
  color: var(--cvs-text-sub);
  font-size: var(--cvs-font-size-sm);
}

.banner-slide {
  width: 100%;
  height: 100%;
  border: 0;
  padding: 0;
  position: relative;
  cursor: pointer;
  border-radius: 18px;
  overflow: hidden;
  background: #10281f;
}

.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.banner-mask {
  position: absolute;
  inset: auto 0 0 0;
  padding: 18px 22px;
  color: #fff;
  background: linear-gradient(180deg, rgba(7, 18, 14, 0) 0%, rgba(7, 18, 14, 0.82) 100%);
  text-align: left;
}

.banner-eyebrow {
  margin: 0 0 8px;
  letter-spacing: 0.14em;
  font-size: 12px;
  text-transform: uppercase;
  opacity: 0.84;
}

.banner-mask h3 {
  margin: 0;
  font-size: 22px;
}

.banner-mask span {
  display: inline-flex;
  margin-top: 8px;
  color: rgba(255, 255, 255, 0.82);
  font-size: 13px;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
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
.post-highlight-copy h3 {
  margin: 0;
}

.activity-card p,
.post-highlight-copy p {
  margin: 0;
  color: var(--cvs-text-sub);
  line-height: 1.7;
}

.activity-card span {
  color: #5d7268;
  font-size: var(--cvs-font-size-sm);
}

.activity-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.split {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  align-items: start;
}

.highlight-list {
  display: grid;
  gap: 10px;
}

.highlight-card {
  border: 1px solid rgba(31, 122, 84, 0.16);
  border-radius: 16px;
  padding: 14px 16px;
  background: linear-gradient(180deg, rgba(248, 251, 249, 0.94), rgba(239, 246, 242, 0.92));
  text-align: left;
  cursor: pointer;
  display: grid;
  gap: 10px;
  transition:
    transform var(--cvs-motion-fast) var(--cvs-ease-standard),
    box-shadow var(--cvs-motion-fast) var(--cvs-ease-standard),
    border-color var(--cvs-motion-fast) var(--cvs-ease-standard);
}

.highlight-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--cvs-shadow-card-hover);
  border-color: rgba(31, 122, 84, 0.3);
}

.highlight-card.featured {
  background: linear-gradient(135deg, rgba(21, 94, 67, 0.96), rgba(17, 62, 46, 0.98));
  color: #ffffff;
}

.highlight-copy {
  display: grid;
  gap: 6px;
}

.highlight-tag {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #2a7a5f;
}

.highlight-card.featured .highlight-tag,
.highlight-card.featured small {
  color: rgba(255, 255, 255, 0.84);
}

.highlight-card h3 {
  margin: 0;
  font-size: 20px;
  line-height: 1.4;
}

.highlight-card span {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.75;
  color: inherit;
  opacity: 0.8;
}

.post-highlight-list {
  display: grid;
  gap: 10px;
}

.post-highlight-card {
  border: 1px solid rgba(31, 122, 84, 0.14);
  border-radius: 16px;
  padding: 14px 16px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(242, 247, 244, 0.9));
  text-align: left;
  cursor: pointer;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: end;
  transition:
    transform var(--cvs-motion-fast) var(--cvs-ease-standard),
    box-shadow var(--cvs-motion-fast) var(--cvs-ease-standard),
    border-color var(--cvs-motion-fast) var(--cvs-ease-standard);
}

.post-highlight-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--cvs-shadow-card-hover);
  border-color: rgba(31, 122, 84, 0.3);
}

.post-highlight-copy {
  display: grid;
  gap: 6px;
  min-width: 0;
}

.post-highlight-copy h3,
.post-highlight-copy p {
  margin: 0;
}

.post-highlight-copy p {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  color: var(--cvs-text-sub);
  line-height: 1.7;
}

.post-highlight-meta {
  display: grid;
  justify-items: end;
  align-content: end;
  gap: 6px;
  color: #60716a;
  font-size: 13px;
  white-space: nowrap;
}

@media (max-width: 1080px) {
  .activity-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .split {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .activity-grid {
    grid-template-columns: 1fr;
  }

  .post-highlight-card {
    grid-template-columns: 1fr;
  }

  .post-highlight-meta {
    justify-items: start;
  }
}
</style>
