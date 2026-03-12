<template>
  <div>
    <PortalNavBar />
    <main class="portal-wrap">
      <section class="layout">
        <el-card class="module main" shadow="never">
          <div class="toolbar">
            <el-select v-model="query.type" clearable placeholder="全部类型" style="width: 130px">
              <el-option label="社区新闻" value="NEWS" />
              <el-option label="活动动态" value="DYNAMIC" />
            </el-select>
            <el-input v-model.trim="query.keyword" placeholder="搜索标题或内容" clearable @keyup.enter="load" />
            <el-button type="primary" @click="load">查询</el-button>
          </div>

          <div class="news-list">
            <article v-for="item in list" :key="item.id" class="news-item">
              <div class="thumb" :style="{ backgroundImage: `url(${item.imageUrl || fallbackImage(item.id)})` }"></div>
              <div class="content">
                <h3>{{ item.title }}</h3>
                <p>{{ item.content }}</p>
                <div class="meta">
                  <span>{{ typeLabel(item.type) }}</span>
                  <span>浏览 {{ item.views || 0 }}</span>
                  <span>{{ formatTime(item.publishTime) }}</span>
                </div>
              </div>
            </article>
            <el-empty v-if="!list.length" description="暂无动态信息" />
          </div>

          <div class="footer">
            <el-pagination
              layout="total, prev, pager, next"
              :current-page="query.current"
              :page-size="query.size"
              :total="total"
              @current-change="handlePage"
            />
          </div>
        </el-card>

        <el-card class="module side" shadow="never">
          <h3 class="side-title">热门动态</h3>
          <ul class="hot-list">
            <li v-for="item in hotList" :key="item.id">
              <strong>{{ item.title }}</strong>
              <span>浏览 {{ item.views || 0 }}</span>
            </li>
          </ul>
        </el-card>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { pageDynamicsApi, type DynamicModel } from '@/api/content'
import PortalNavBar from './PortalNavBar.vue'

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

async function load() {
  const res = await pageDynamicsApi({
    current: query.current,
    size: query.size,
    type: query.type || undefined,
    keyword: query.keyword || undefined,
    onlyPublished: true
  })
  list.value = res.records
  total.value = res.total
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
}

.layout {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 12px;
}

.module {
  border-radius: 16px;
  border: 1px solid var(--cvs-border);
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

.footer {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.side-title {
  margin: 0 0 12px;
  font-size: 18px;
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

.hot-list strong {
  font-size: 14px;
  line-height: 1.6;
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
