<template>
  <div class="activity-detail fade-up">
    <WorkspaceHero
      compact
      eyebrow="活动详情"
      :title="activity?.title || '查看社区志愿活动详情'"
      :description="activitySummary || '统一查看活动说明、评价与报名入口，保持与门户端一致的信息层级。'"
    >
      <template #actions>
        <el-button type="success" :disabled="!activity || loading" @click="apply">报名活动</el-button>
        <el-button type="warning" plain :disabled="!activity || loading" @click="collect">收藏活动</el-button>
        <el-button @click="router.back()">返回活动中心</el-button>
      </template>
      <template #aside>
        <div v-if="activity" class="hero-stat-grid">
          <article class="hero-stat">
            <h3>活动状态</h3>
            <strong>{{ getActivityStatusLabel(activity.status) }}</strong>
            <span>报名与服务安排以当前状态为准</span>
          </article>
          <article class="hero-stat">
            <h3>志愿者人数</h3>
            <strong>{{ activity.volunteerQuota }}</strong>
            <span>当前活动设置的志愿者参与容量</span>
          </article>
          <article class="hero-stat">
            <h3>目标人数</h3>
            <strong>{{ activity.targetCount }}</strong>
            <span>活动整体服务对象或预期参与规模</span>
          </article>
        </div>
      </template>
    </WorkspaceHero>

    <StatePanel
      v-if="loading"
      state="loading"
      title="正在加载活动详情"
      description="正在同步活动信息、详细说明与评价数据。"
    />

    <template v-else-if="activity">
      <section class="module-card">
        <div class="module-head">
          <div>
            <p class="module-eyebrow">基础信息</p>
            <h2 class="section-title">活动概览</h2>
          </div>
        </div>

        <div class="detail-layout">
          <img class="cover" :src="activity.coverImage || DEFAULT_ACTIVITY_COVER" alt="活动封面" />
          <div class="meta">
            <h3>{{ activity.title }}</h3>
            <p class="line"><span>活动内容：</span>{{ activitySummary || '待补充' }}</p>
            <p class="line"><span>活动地点：</span>{{ activity.address }}</p>
            <p class="line"><span>活动时间：</span>{{ formatDateTime(activity.startTime) }} - {{ formatDateTime(activity.endTime) }}</p>
            <p class="line"><span>志愿者人数：</span>{{ activity.volunteerQuota }}</p>
            <p class="line"><span>目标人数：</span>{{ activity.targetCount }}</p>
            <p class="line"><span>活动状态：</span>{{ getActivityStatusLabel(activity.status) }}</p>
          </div>
        </div>
      </section>

      <section class="module-card">
        <div class="module-head">
          <div>
            <p class="module-eyebrow">详细说明</p>
            <h2 class="section-title">活动说明</h2>
          </div>
        </div>
        <RichTextRenderer class="description" :value="activity?.description" empty-html="<p>暂无详细说明</p>" />
      </section>

      <section class="module-card">
        <div class="module-head">
          <div>
            <p class="module-eyebrow">活动评价</p>
            <h2 class="section-title">参与反馈</h2>
          </div>
        </div>

        <div class="comment-create">
          <el-input v-model="commentText" type="textarea" :rows="3" placeholder="欢迎填写你的活动评价与建议" />
          <div class="comment-actions">
            <el-button type="primary" @click="submitComment">发布评价</el-button>
          </div>
        </div>

        <el-table :data="comments" border empty-text="">
          <template #empty>
            <StatePanel compact title="暂无评价" description="参与活动后欢迎留下你的反馈。" />
          </template>
          <el-table-column prop="id" label="评论 ID" width="90" />
          <el-table-column prop="userId" label="用户 ID" width="90" />
          <el-table-column prop="content" label="评价内容" min-width="420" />
        </el-table>

        <div v-if="commentTotal > 0" class="footer">
          <el-pagination
            layout="total, prev, pager, next"
            :current-page="commentQuery.current"
            :page-size="commentQuery.size"
            :total="commentTotal"
            @current-change="handleCommentPage"
          />
        </div>
      </section>
    </template>

    <StatePanel
      v-else
      title="活动不存在或已下线"
      description="可以返回活动中心查看其他社区志愿活动。"
    >
      <template #actions>
        <el-button type="primary" @click="router.push(PORTAL_PATHS.activities)">返回活动中心</el-button>
      </template>
    </StatePanel>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { activityDetailApi, type ActivityModel } from '@/api/activity'
import { addCommentApi, createFavoriteApi, pageCommentsApi, type CommentModel } from '@/api/content'
import RichTextRenderer from '@/components/shared/RichTextRenderer.vue'
import StatePanel from '@/components/shared/StatePanel.vue'
import WorkspaceHero from '@/components/shared/WorkspaceHero.vue'
import { PORTAL_PATHS } from '@/constants/portal-routes'
import { submitActivityApplicationWithUndo } from '@/utils/activity-apply'
import { runConfirmedAction } from '@/utils/confirmed-action'
import { DEFAULT_ACTIVITY_COVER, formatDateTime, getActivityStatusLabel } from '@/utils/display'
import { richTextToPlainText } from '@/utils/rich-text'

const route = useRoute()
const router = useRouter()
const activityId = Number(route.params.id)

const loading = ref(false)
const activity = ref<ActivityModel>()
const comments = ref<CommentModel[]>([])
const commentTotal = ref(0)
const commentText = ref('')
const commentQuery = reactive({
  current: 1,
  size: 8
})

const activitySummary = computed(() => richTextToPlainText(activity.value?.content))

async function loadDetail() {
  activity.value = await activityDetailApi(activityId)
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
  loadComments()
}

async function apply() {
  await submitActivityApplicationWithUndo({
    activityId,
    refresh: loadDetail
  })
}

async function collect() {
  await runConfirmedAction({
    message: '确认将该活动加入收藏吗？',
    title: '加入收藏',
    type: 'info',
    confirmButtonText: '确认收藏',
    action: () => createFavoriteApi({ activityId }),
    successMessage: '已加入收藏'
  })
}

async function submitComment() {
  if (!commentText.value.trim()) {
    ElMessage.warning('请先输入评价内容')
    return
  }

  await addCommentApi({
    targetType: 'ACTIVITY',
    targetId: activityId,
    content: commentText.value.trim()
  })
  commentText.value = ''
  ElMessage.success('评价发布成功')
  commentQuery.current = 1
  await loadComments()
}

onMounted(async () => {
  loading.value = true
  try {
    await loadDetail()
    await loadComments()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.activity-detail {
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
  color: #1f7a54;
  font-size: var(--cvs-font-size-xs);
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.module-head :deep(.section-title) {
  margin-bottom: 0;
}

.detail-layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 16px;
}

.cover {
  width: 100%;
  height: 210px;
  object-fit: cover;
  border-radius: 14px;
  border: 1px solid var(--cvs-border);
}

.meta h3 {
  margin: 0 0 10px;
  font-size: 24px;
}

.line {
  margin: 0 0 8px;
  line-height: 1.7;
  color: #374540;
}

.line span {
  color: #5f6c67;
}

.description {
  margin: 0;
  line-height: 1.85;
  color: #3e4b46;
}

.rich-text-content :deep(p),
.rich-text-content :deep(ul),
.rich-text-content :deep(ol),
.rich-text-content :deep(blockquote) {
  margin: 0 0 14px;
}

.rich-text-content :deep(ul),
.rich-text-content :deep(ol) {
  padding-left: 22px;
}

.comment-create {
  margin-bottom: 12px;
}

.comment-actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}

.footer {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 900px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }
}
</style>
