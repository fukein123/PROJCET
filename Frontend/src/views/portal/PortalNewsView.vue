<template>
  <div>
    <PortalNavBar />
    <main class="portal-wrap">
      <WorkspaceHero
        tone="portal"
        compact
        eyebrow="信息动态"
        title="统一查看社区新闻与活动动态"
        description="按类型和关键词筛选门户资讯，保持与志愿者端一致的信息层级、内容卡片与空态表达。"
      >
        <template #actions>
          <el-button type="primary" @click="load">刷新动态</el-button>
          <el-button @click="query.type = ''; load()">查看全部</el-button>
        </template>
      </WorkspaceHero>

      <section class="layout fade-up">
        <section class="module-card main">
          <div class="toolbar">
            <el-select v-model="query.type" clearable placeholder="全部类型" style="width: 130px">
              <el-option label="社区新闻" value="NEWS" />
              <el-option label="活动动态" value="DYNAMIC" />
            </el-select>
            <el-input v-model.trim="query.keyword" placeholder="搜索标题或内容" clearable @keyup.enter="load" />
            <el-button type="primary" @click="load">查询</el-button>
          </div>

          <StatePanel
            v-if="loading"
            state="loading"
            tone="portal"
            title="正在加载动态"
            description="正在同步社区新闻与活动动态。"
          />
          <div v-else-if="list.length" class="news-list">
            <article v-for="item in list" :key="item.id" class="news-item">
              <div class="thumb" :style="{ backgroundImage: `url(${item.imageUrl || fallbackImage(item.id)})` }"></div>
              <div class="content">
                <h3>{{ item.title }}</h3>
                <p>{{ preview(item.content) }}</p>
                <div class="meta">
                  <span>{{ typeLabel(item.type) }}</span>
                  <span>{{ item.source || '平台发布' }}</span>
                  <span>浏览 {{ item.views || 0 }}</span>
                  <span>{{ formatTime(item.publishTime) }}</span>
                </div>
                <div class="content-actions">
                  <el-button type="primary" plain @click="router.push(PORTAL_PATHS.newsDetail(item.id))">查看详情</el-button>
                </div>
              </div>
            </article>
          </div>
          <StatePanel
            v-else
            tone="portal"
            title="暂无动态信息"
            description="可以调整筛选条件，或等待管理员发布新的新闻动态。"
          />

          <div v-if="!loading && total > 0" class="footer">
            <el-pagination
              layout="total, prev, pager, next"
              :current-page="query.current"
              :page-size="query.size"
              :total="total"
              @current-change="handlePage"
            />
          </div>
        </section>

        <aside class="module-card side">
          <div class="module-head">
            <div>
              <p class="module-eyebrow">阅读排行</p>
              <h2 class="section-title">热门动态</h2>
            </div>
          </div>
          <StatePanel
            v-if="loading && !hotList.length"
            state="loading"
            tone="portal"
            title="正在加载热门动态"
            description="正在根据浏览量整理热门内容。"
            compact
          />
          <StatePanel
            v-else-if="!hotList.length"
            tone="portal"
            title="暂无热门动态"
            description="新的热门内容会展示在这里。"
            compact
          />
          <ul v-else class="hot-list">
            <li v-for="item in hotList" :key="item.id">
              <button class="hot-link" type="button" @click="router.push(PORTAL_PATHS.newsDetail(item.id))">
                {{ item.title }}
              </button>
              <span>浏览 {{ item.views || 0 }}</span>
            </li>
          </ul>
        </aside>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import { pageDynamicsApi, type DynamicModel } from '@/api/content'
import StatePanel from '@/components/shared/StatePanel.vue'
import WorkspaceHero from '@/components/shared/WorkspaceHero.vue'
import { PORTAL_PATHS } from '@/constants/portal-routes'
import { richTextToPlainText } from '@/utils/rich-text'
import PortalNavBar from './PortalNavBar.vue'

const router = useRouter()
const loading = ref(false)
const list = ref<DynamicModel[]>([])
const hotList = ref<DynamicModel[]>([])
const total = ref(0)
const query = reactive({
  current: 1,
  size: 8,
  type: '',
  keyword: ''
})

const imagePool = [
  'https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1532629345422-7515f3d16bb6?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1511632765486-a01980e01a18?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=900&q=80'
]

function fallbackImage(id: number) {
  return imagePool[id % imagePool.length]
}

function formatTime(value: string) {
  return dayjs(value).format('YYYY-MM-DD HH:mm')
}

function typeLabel(type: string) {
  return type === 'DYNAMIC' ? '活动动态' : '社区新闻'
}

function preview(content?: string) {
  return richTextToPlainText(content) || '当前动态暂无内容摘要。'
}

async function load() {
  loading.value = true
  try {
    const res = await pageDynamicsApi({
      current: query.current,
      size: query.size,
      type: query.type || undefined,
      keyword: query.keyword || undefined,
      onlyPublished: true
    })
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function loadHot() {
  const res = await pageDynamicsApi({
    current: 1,
    size: 6,
    onlyPublished: true
  })
  hotList.value = [...res.records].sort((a, b) => (b.views || 0) - (a.views || 0)).slice(0, 6)
}

function handlePage(page: number) {
  query.current = page
  load()
}

onMounted(async () => {
  await Promise.all([load(), loadHot()])
})
</script>

<style scoped>
.portal-wrap {
  width: min(1220px, calc(100% - 24px));
  margin: 14px auto 40px;
  display: grid;
  gap: 14px;
}

.layout {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 12px;
}

.module-card {
  border-radius: 18px;
  border: 1px solid var(--cvs-border);
  background: rgba(255, 255, 255, 0.94);
  padding: 18px;
  box-shadow: var(--cvs-shadow-soft);
}

.module-head {
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

.toolbar {
  display: grid;
  grid-template-columns: 140px 1fr auto;
  gap: 8px;
  margin-bottom: 12px;
}

.news-list {
  display: grid;
  gap: 10px;
}

.news-item {
  border: 1px solid var(--cvs-border);
  border-radius: 14px;
  overflow: hidden;
  display: grid;
  grid-template-columns: 170px 1fr;
  background: #fff;
}

.thumb {
  min-height: 140px;
  background-size: cover;
  background-position: center;
}

.content {
  padding: 12px 14px;
  display: grid;
  gap: 8px;
}

.content h3 {
  margin: 0;
  font-size: 20px;
}

.content p {
  margin: 0;
  color: var(--cvs-text-sub);
  line-height: 1.75;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta {
  color: #6a7b74;
  font-size: 13px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.content-actions {
  display: flex;
  justify-content: flex-start;
}

.footer {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.hot-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 8px;
}

.hot-list li {
  border: 1px solid var(--cvs-border);
  border-radius: 10px;
  padding: 10px;
  display: grid;
  gap: 6px;
}

.hot-link {
  padding: 0;
  border: 0;
  background: transparent;
  color: #1d5e43;
  cursor: pointer;
  font: inherit;
  font-size: 14px;
  line-height: 1.6;
  text-align: left;
}

.hot-link:hover {
  color: #0f3f2b;
  text-decoration: underline;
}

.hot-list span {
  color: #7c8b86;
  font-size: 12px;
}

@media (max-width: 980px) {
  .layout {
    grid-template-columns: 1fr;
  }

  .toolbar {
    grid-template-columns: 1fr;
  }

  .news-item {
    grid-template-columns: 1fr;
  }
}
</style>
