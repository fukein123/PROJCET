<template>
  <div>
    <PortalNavBar />
    <main class="portal-detail-wrap">
      <StatePanel
        v-if="loading"
        state="loading"
        tone="portal"
        title="正在加载活动详情"
        description="正在同步活动信息、详细说明与评价内容。"
      />

      <template v-else-if="activity">
        <section class="hero-card fade-up">
          <div class="hero-layout">
            <div class="cover-shell">
              <img class="cover" :src="activity.coverImage || DEFAULT_ACTIVITY_COVER" alt="活动封面" />
            </div>

            <div class="hero-main">
              <div class="hero-main-head">
                <div>
                  <h1>{{ activity.title }}</h1>
                  <p class="category-line">活动分类：{{ categoryLabel(activity.categoryId) }}</p>
                </div>
                <button class="favorite-button" type="button" @click="collect" aria-label="收藏活动">☆</button>
              </div>

              <div class="meta-list">
                <p><span>目标人数：</span>{{ activity.targetCount }}</p>
                <p><span>志愿名额：</span>{{ activity.volunteerQuota }}</p>
                <p>
                  <span>活动状态：</span>
                  <el-tag :type="getActivityStatusTag(activity.status)">{{ getActivityStatusLabel(activity.status) }}</el-tag>
                </p>
                <p><span>开始时间：</span>{{ formatDateTime(activity.startTime) }}</p>
                <p><span>结束时间：</span>{{ formatDateTime(activity.endTime) }}</p>
                <p><span>活动地址：</span>{{ activity.address || '待补充' }}</p>
                <p class="reward-line">
                  <span>活动积分：</span>
                  {{ activity.pointReward ?? 0 }}
                  <em>必须在规定时间内签到签退才能获得积分</em>
                </p>
                <p><span>活动简介：</span>{{ activitySummary || '暂无简介' }}</p>
              </div>

              <div class="hero-actions">
                <el-button type="primary" :disabled="loading" @click="apply">立即参加</el-button>
                <p class="apply-hint">
                  {{ applyHint }}
                  <button v-if="showProfileLink" type="button" class="profile-link" @click="router.push(PORTAL_PATHS.selfServiceProfile)">
                    点击前往个人中心
                  </button>
                </p>
              </div>
            </div>
          </div>
        </section>

        <section class="detail-card fade-up">
          <el-tabs v-model="activeTab" class="detail-tabs">
            <el-tab-pane label="活动详情" name="detail">
              <div class="tab-panel">
                <h2>{{ activity.title }}公益活动详细介绍</h2>
                <RichTextRenderer
                  class="body-copy"
                  :value="activity.description"
                  empty-html="<p>当前活动暂无详细说明。</p>"
                />
              </div>
            </el-tab-pane>
            <el-tab-pane label="活动评价" name="comments">
              <div class="tab-panel">
                <div class="comment-thread">
                  <article v-for="item in comments" :key="item.id" class="comment-item">
                    <div class="comment-item-head">
                      <strong>用户 #{{ item.userId }}</strong>
                      <span>{{ formatDateTime(item.createTime) }}</span>
                    </div>
                    <p class="comment-item-content">{{ item.content }}</p>
                  </article>
                  <StatePanel
                    v-if="!commentTotal"
                    compact
                    tone="portal"
                    title="暂无评价"
                    description="参与活动后欢迎留下你的反馈。"
                  />
                </div>

                <div v-if="commentTotal > 0" class="footer">
                  <el-pagination
                    layout="total, prev, pager, next"
                    :current-page="commentQuery.current"
                    :page-size="commentQuery.size"
                    :total="commentTotal"
                    @current-change="handleCommentPage"
                  />
                </div>

                <div class="comment-create">
                  <el-input v-model="commentText" type="textarea" :rows="4" placeholder="欢迎填写你的活动评价与建议" />
                  <div class="comment-actions">
                    <el-button type="primary" @click="submitComment">发布评价</el-button>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </section>
      </template>

      <StatePanel
        v-else
        tone="portal"
        title="活动不存在或已下线"
        description="可以返回活动列表查看其他社区志愿服务。"
      >
        <template #actions>
          <el-button type="primary" @click="router.push(PORTAL_PATHS.activities)">返回活动列表</el-button>
        </template>
      </StatePanel>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { activityDetailApi, listCategoriesApi, type ActivityCategory, type ActivityModel } from '@/api/activity'
import { addCommentApi, createFavoriteApi, pageCommentsApi, type CommentModel } from '@/api/content'
import RichTextRenderer from '@/components/shared/RichTextRenderer.vue'
import StatePanel from '@/components/shared/StatePanel.vue'
import { PORTAL_PATHS } from '@/constants/portal-routes'
import { usePortalNavigation } from '@/composables/usePortalNavigation'
import { useUserStore } from '@/stores/userStore'
import { submitActivityApplicationWithUndo } from '@/utils/activity-apply'
import { DEFAULT_ACTIVITY_COVER, formatDateTime, getActivityStatusLabel, getActivityStatusTag } from '@/utils/display'
import { richTextToPlainText } from '@/utils/rich-text'
import PortalNavBar from './PortalNavBar.vue'

const route = useRoute()
const router = useRouter()
const portalNav = usePortalNavigation()
const userStore = useUserStore()
const activityId = Number(route.params.id)

const loading = ref(false)
const activity = ref<ActivityModel>()
const categories = ref<ActivityCategory[]>([])
const comments = ref<CommentModel[]>([])
const commentTotal = ref(0)
const commentText = ref('')
const activeTab = ref<'detail' | 'comments'>('detail')
const commentQuery = reactive({
  current: 1,
  size: 8
})

const activitySummary = computed(() => richTextToPlainText(activity.value?.content))
const showProfileLink = computed(() => userStore.role === 'VOLUNTEER' && userStore.profile?.certified !== 1)
const applyHint = computed(() => {
  if (userStore.role !== 'VOLUNTEER') {
    return '请使用志愿者账号登录后参加活动。'
  }
  if (userStore.profile?.certified === 1) {
    return '认证信息已通过，可以直接参加活动。'
  }
  return '认证信息未完成，参加活动前请先完善个人中心中的实名认证。'
})

function categoryLabel(categoryId: number) {
  return categories.value.find((item) => item.id === categoryId)?.name || `分类 ${categoryId}`
}

async function loadDetail() {
  activity.value = await activityDetailApi(activityId)
}

async function loadCategories() {
  categories.value = await listCategoriesApi()
}

async function loadComments() {
  const res = await pageCommentsApi({
    current: commentQuery.current,
    size: commentQuery.size,
    targetType: 'ACTIVITY',
    targetId: activityId
  })
  comments.value = res.records
  commentTotal.value = res.total
}

function handleCommentPage(page: number) {
  commentQuery.current = page
  void loadComments()
}

async function apply() {
  await portalNav.requireLogin(
    async () => {
      if (userStore.role !== 'VOLUNTEER') {
        ElMessage.warning('请使用志愿者账号登录后报名活动')
        return
      }

      await submitActivityApplicationWithUndo({
        activityId,
        refresh: loadDetail
      })
    },
    route.fullPath
  )
}

async function collect() {
  await portalNav.requireLogin(
    async () => {
      if (userStore.role !== 'VOLUNTEER') {
        ElMessage.warning('请使用志愿者账号登录后收藏活动')
        return
      }

      await createFavoriteApi({ activityId })
      ElMessage.success('已加入收藏')
    },
    route.fullPath
  )
}

async function submitComment() {
  const content = commentText.value.trim()
  if (!content) {
    ElMessage.warning('请先输入评价内容')
    return
  }

  const done = await portalNav.requireLogin(
    async () => {
      await addCommentApi({
        targetType: 'ACTIVITY',
        targetId: activityId,
        content
      })
      commentText.value = ''
      commentQuery.current = 1
      ElMessage.success('评价发布成功')
      await loadComments()
      activeTab.value = 'comments'
    },
    route.fullPath
  )

  if (!done) {
    ElMessage.info('登录后可发布活动评价')
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([loadDetail(), loadCategories(), loadComments()])
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.portal-detail-wrap {
  width: min(1280px, calc(100% - 24px));
  margin: 18px auto 40px;
  display: grid;
  gap: 16px;
}

.hero-card,
.detail-card {
  border: 1px solid var(--cvs-border);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: var(--cvs-shadow-soft);
}

.hero-card {
  padding: 28px;
}

.hero-layout {
  display: grid;
  grid-template-columns: 460px minmax(0, 1fr);
  gap: 22px;
  align-items: start;
}

.cover-shell {
  border-radius: 24px;
  overflow: hidden;
}

.cover {
  display: block;
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: cover;
}

.hero-main {
  display: grid;
  gap: 18px;
}

.hero-main-head {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: start;
}

.hero-main-head h1 {
  margin: 0 0 10px;
  font-size: 48px;
  line-height: 1.1;
  color: #182f24;
}

.category-line {
  margin: 0;
  color: #667771;
  font-size: 18px;
}

.favorite-button {
  border: none;
  background: transparent;
  font-size: 40px;
  line-height: 1;
  cursor: pointer;
  color: #1f3c30;
}

.meta-list {
  display: grid;
  gap: 12px;
}

.meta-list p {
  margin: 0;
  color: #32453e;
  font-size: 17px;
  line-height: 1.7;
}

.meta-list span {
  color: #5f7069;
}

.reward-line em {
  margin-left: 8px;
  color: #d93025;
  font-style: normal;
}

.hero-actions {
  display: flex;
  align-items: center;
  gap: 18px;
  flex-wrap: wrap;
}

.apply-hint {
  margin: 0;
  color: #5f7069;
  font-size: 16px;
}

.profile-link {
  border: none;
  background: transparent;
  color: #c84a00;
  font-weight: 700;
  cursor: pointer;
}

.detail-card {
  padding: 8px 22px 22px;
}

.detail-tabs :deep(.el-tabs__header) {
  margin-bottom: 18px;
}

.detail-tabs :deep(.el-tabs__item) {
  font-size: 18px;
  padding: 0 18px;
}

.tab-panel {
  display: grid;
  gap: 18px;
}

.tab-panel h2 {
  margin: 0;
  font-size: 34px;
  color: #1a3025;
}

.body-copy {
  line-height: 1.95;
  color: #3a4d46;
}

.comment-thread {
  display: grid;
  gap: 12px;
}

.comment-item {
  border: 1px solid rgba(205, 216, 210, 0.92);
  border-radius: 16px;
  background: rgba(248, 251, 249, 0.96);
  padding: 16px;
  display: grid;
  gap: 10px;
}

.comment-item-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  color: #62736d;
  font-size: 14px;
}

.comment-item-content {
  margin: 0;
  color: #31433d;
  line-height: 1.8;
}

.comment-create {
  display: grid;
  gap: 10px;
}

.comment-actions,
.footer {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 960px) {
  .hero-layout {
    grid-template-columns: 1fr;
  }

  .hero-main-head h1 {
    font-size: 34px;
  }
}

@media (max-width: 640px) {
  .hero-card {
    padding: 18px;
  }

  .detail-card {
    padding: 8px 16px 16px;
  }

  .hero-main-head,
  .hero-actions {
    display: grid;
    grid-template-columns: 1fr;
  }
}
</style>
