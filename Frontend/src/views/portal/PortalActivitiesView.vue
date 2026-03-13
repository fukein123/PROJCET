<template>
  <div>
    <PortalNavBar />
    <main class="portal-wrap">
      <WorkspaceHero
        tone="portal"
        compact
        eyebrow="志愿活动广场"
        title="按分类和关键词快速筛选社区志愿活动"
        description="统一展示活动状态、服务时间、地点与详细说明；登录后即可报名、收藏并继续进入论坛交流。"
      >
        <template #actions>
          <el-button type="primary" @click="load">刷新活动</el-button>
          <el-button @click="router.push('/portal/forum')">去论坛交流</el-button>
        </template>
      </WorkspaceHero>

      <section class="module-card fade-up">
        <div class="module-head">
          <div>
            <p class="module-eyebrow">筛选与报名</p>
            <h2 class="section-title">活动列表</h2>
          </div>
        </div>

        <section class="toolbar">
          <div class="category-list">
            <button
              class="chip"
              :class="{ active: selectedCategoryId === undefined }"
              type="button"
              @click="changeCategory(undefined)"
            >
              全部
            </button>
            <button
              v-for="item in categories"
              :key="item.id"
              class="chip"
              :class="{ active: selectedCategoryId === item.id }"
              type="button"
              @click="changeCategory(item.id)"
            >
              {{ item.name }}
            </button>
          </div>

          <div class="query-panel">
            <el-input v-model.trim="keyword" placeholder="请输入活动名称查询" clearable @keyup.enter="load" />
            <el-button type="primary" @click="load">查询</el-button>
            <el-button @click="reset">重置</el-button>
          </div>
        </section>

        <StatePanel
          v-if="loading"
          state="loading"
          tone="portal"
          title="正在加载活动"
          description="正在同步符合筛选条件的社区志愿活动。"
        />
        <section v-else-if="activities.length" class="activity-grid">
          <article v-for="item in activities" :key="item.id" class="activity-card">
            <div class="cover" :style="{ backgroundImage: `url(${item.coverImage || coverFor(item.id)})` }"></div>
            <div class="card-main">
              <h3>{{ item.title }}</h3>
              <div class="line">
                <span>活动状态：</span>
                <el-tag size="small" :type="getActivityStatusTag(item.status)">{{ getActivityStatusLabel(item.status) }}</el-tag>
              </div>
              <div class="line">
                <span>活动时间：</span>
                <strong>{{ formatDateTime(item.startTime) }} - {{ formatDateTime(item.endTime) }}</strong>
              </div>
              <div class="line">
                <span>活动地点：</span>
                <strong>{{ item.address || '待补充' }}</strong>
              </div>
              <div class="line">
                <span>目标人数：</span>
                <strong>{{ item.targetCount }}</strong>
              </div>
              <p class="desc">{{ item.description || '当前活动暂无详细说明。' }}</p>
              <el-collapse>
                <el-collapse-item title="查看详细说明" :name="String(item.id)">
                  <p class="detail">{{ item.description || '暂无详细说明' }}</p>
                </el-collapse-item>
              </el-collapse>
              <div class="actions">
                <el-button type="primary" @click="apply(item.id)">立即报名</el-button>
                <el-button type="warning" plain @click="collect(item.id)">收藏活动</el-button>
                <el-button @click="router.push('/portal/forum')">去论坛交流</el-button>
              </div>
            </div>
          </article>
        </section>
        <StatePanel
          v-else
          tone="portal"
          title="暂无匹配活动"
          description="可以调整分类或关键词后重新查询。"
        />

        <div v-if="!loading && total > 0" class="pager">
          <el-pagination
            layout="total, prev, pager, next"
            :total="total"
            :current-page="query.current"
            :page-size="query.size"
            @current-change="handlePage"
          />
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import {
  applyActivityApi,
  listCategoriesApi,
  pageActivitiesApi,
  type ActivityCategory,
  type ActivityModel
} from '@/api/activity'
import { createFavoriteApi } from '@/api/content'
import StatePanel from '@/components/shared/StatePanel.vue'
import WorkspaceHero from '@/components/shared/WorkspaceHero.vue'
import { usePortalNavigation } from '@/composables/usePortalNavigation'
import { useUserStore } from '@/stores/userStore'
import { formatDateTime, getActivityStatusLabel, getActivityStatusTag } from '@/utils/display'
import PortalNavBar from './PortalNavBar.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const portalNav = usePortalNavigation()
const loading = ref(false)
const keyword = ref('')
const categories = ref<ActivityCategory[]>([])
const activities = ref<ActivityModel[]>([])
const total = ref(0)
const hasAppliedFromQuery = ref(false)
const selectedCategoryId = ref<number | undefined>()
const query = ref({
  current: 1,
  size: 12
})

const coverList = [
  'https://images.unsplash.com/photo-1509062522246-3755977927d7?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1523240795612-9a054b0db644?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1531206715517-5c0ba140b2b8?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1497436072909-60f360e1d4b1?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1515169067868-5387ec356754?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1519751138087-5bf79df62d5b?auto=format&fit=crop&w=900&q=80'
]

function coverFor(id: number) {
  return coverList[id % coverList.length]
}

async function load() {
  loading.value = true
  try {
    const res = await pageActivitiesApi({
      current: query.value.current,
      size: query.value.size,
      keyword: keyword.value || undefined,
      categoryId: selectedCategoryId.value,
      status: 'PUBLISHED'
    })
    activities.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function changeCategory(categoryId: number | undefined) {
  query.value.current = 1
  selectedCategoryId.value = categoryId
  load()
}

function reset() {
  query.value.current = 1
  keyword.value = ''
  selectedCategoryId.value = undefined
  load()
}

function handlePage(page: number) {
  query.value.current = page
  load()
}

async function apply(activityId: number) {
  await portalNav.requireLogin(
    async () => {
      if (userStore.role !== 'VOLUNTEER') {
        ElMessage.warning('请使用志愿者账号登录后报名活动')
        return
      }
      await applyActivityApi(activityId)
      ElMessage.success('报名申请已提交，请等待管理员审核')
    },
    `/portal/activities?apply=${activityId}`
  )
}

async function collect(activityId: number) {
  await portalNav.requireLogin(
    async () => {
      if (userStore.role !== 'VOLUNTEER') {
        ElMessage.warning('请使用志愿者账号登录后收藏活动')
        return
      }
      await createFavoriteApi({ activityId })
      ElMessage.success('已加入收藏')
    },
    '/portal/activities'
  )
}

async function handleAutoApply() {
  const applyId = Number(route.query.apply)
  if (hasAppliedFromQuery.value || !applyId || Number.isNaN(applyId)) {
    return
  }
  if (!portalNav.isLogin.value) {
    return
  }

  hasAppliedFromQuery.value = true
  await apply(applyId)
  router.replace('/portal/activities')
}

onMounted(async () => {
  categories.value = await listCategoriesApi()
  await load()
  await handleAutoApply()
})
</script>

<style scoped>
.portal-wrap {
  width: min(1220px, calc(100% - 24px));
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
  gap: 16px;
  margin-bottom: 16px;
}

.category-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  border: 1px solid rgba(31, 122, 84, 0.28);
  border-radius: 10px;
  background: rgba(228, 241, 234, 0.58);
  color: #18583c;
  font-weight: var(--cvs-font-weight-bold);
  padding: 7px 14px;
  cursor: pointer;
  transition: all var(--cvs-motion-fast) var(--cvs-ease-standard);
}

.chip.active,
.chip:hover {
  color: #ffffff;
  background: linear-gradient(120deg, #1b6544, #2f8a66);
  border-color: #1b6544;
}

.query-panel {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 8px;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 14px;
}

.activity-card {
  border: 1px solid var(--cvs-border);
  border-radius: 16px;
  background: #fff;
  overflow: hidden;
  transition:
    transform var(--cvs-motion-fast) var(--cvs-ease-standard),
    box-shadow var(--cvs-motion-fast) var(--cvs-ease-standard);
}

.activity-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--cvs-shadow-card-hover);
}

.cover {
  height: 170px;
  background-size: cover;
  background-position: center;
}

.card-main {
  padding: 14px;
  display: grid;
  gap: 8px;
}

.card-main h3 {
  margin: 0;
  font-size: 22px;
  line-height: 1.35;
}

.line {
  display: flex;
  gap: 6px;
  align-items: center;
  font-size: 13px;
}

.line span {
  color: #6b7772;
}

.line strong {
  color: #2d3834;
  font-weight: 600;
}

.desc {
  margin: 0;
  color: #586460;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.detail {
  margin: 0;
  color: #3f4f4a;
  line-height: 1.8;
  white-space: pre-wrap;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pager {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 860px) {
  .query-panel {
    grid-template-columns: 1fr;
  }
}
</style>
