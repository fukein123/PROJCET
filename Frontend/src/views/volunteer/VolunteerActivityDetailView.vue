<template>
  <div class="fade-up" v-loading="loading">
    <el-card class="module" shadow="never">
      <template #header>
        <div class="head">
          <span>活动详情</span>
          <el-button text @click="router.back()">返回活动中心</el-button>
        </div>
      </template>

      <div v-if="activity" class="detail-layout">
        <img class="cover" :src="activity.coverImage || DEFAULT_ACTIVITY_COVER" alt="活动封面" />
        <div class="meta">
          <h2>{{ activity.title }}</h2>
          <p class="line"><span>活动内容：</span>{{ activity.content }}</p>
          <p class="line"><span>活动地点：</span>{{ activity.address }}</p>
          <p class="line"><span>活动时间：</span>{{ formatDateTime(activity.startTime) }} - {{ formatDateTime(activity.endTime) }}</p>
          <p class="line"><span>志愿者人数：</span>{{ activity.volunteerQuota }}</p>
          <p class="line"><span>目标人数：</span>{{ activity.targetCount }}</p>
          <p class="line"><span>活动状态：</span>{{ getActivityStatusLabel(activity.status) }}</p>
          <div class="actions">
            <el-button type="success" @click="apply">报名活动</el-button>
            <el-button type="warning" plain @click="collect">收藏活动</el-button>
          </div>
        </div>
      </div>

      <el-divider />

      <section class="section">
        <h3>活动详细说明</h3>
        <p class="description">{{ activity?.description || '暂无详细说明' }}</p>
      </section>

      <el-divider />

      <section class="section">
        <h3>活动评价</h3>
        <div class="comment-create">
          <el-input v-model="commentText" type="textarea" :rows="3" placeholder="欢迎填写你的活动评价与建议" />
          <div class="comment-actions">
            <el-button type="primary" @click="submitComment">发布评价</el-button>
          </div>
        </div>

        <el-table :data="comments" border>
          <el-table-column prop="id" label="评论 ID" width="90" />
          <el-table-column prop="userId" label="用户 ID" width="90" />
          <el-table-column prop="content" label="评价内容" min-width="420" />
        </el-table>

        <div class="footer">
          <el-pagination
            layout="total, prev, pager, next"
            :current-page="commentQuery.current"
            :page-size="commentQuery.size"
            :total="commentTotal"
            @current-change="handleCommentPage"
          />
        </div>
      </section>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { activityDetailApi, applyActivityApi, type ActivityModel } from '@/api/activity'
import { addCommentApi, createFavoriteApi, pageCommentsApi, type CommentModel } from '@/api/content'
import { DEFAULT_ACTIVITY_COVER, formatDateTime, getActivityStatusLabel } from '@/utils/display'

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
  await applyActivityApi(activityId)
  ElMessage.success('报名申请已提交，请等待审核')
}

async function collect() {
  await createFavoriteApi({ activityId })
  ElMessage.success('已加入收藏')
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
.module {
  border: 1px solid var(--cvs-border);
  border-radius: 16px;
}

.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.meta h2 {
  margin: 0 0 10px;
}

.line {
  margin: 0 0 8px;
  line-height: 1.7;
  color: #374540;
}

.line span {
  color: #5f6c67;
}

.actions {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.section h3 {
  margin: 0 0 10px;
}

.description {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.85;
  color: #3e4b46;
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
