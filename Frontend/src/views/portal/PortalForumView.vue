<template>
  <div>
    <PortalNavBar />
    <main class="portal-wrap">
      <WorkspaceHero
        tone="portal"
        compact
        eyebrow="社区论坛"
        title="围绕志愿服务主题发布帖子、参与评论与沉淀社区经验"
        description="统一展示社区话题、分类筛选、评论入口和发帖流转，保持门户端与志愿者工作台一致的内容层级。"
      >
        <template #actions>
          <el-button type="primary" @click="toPublish">我要发帖</el-button>
          <el-button @click="router.push('/portal/activities')">查看活动</el-button>
        </template>
      </WorkspaceHero>

      <section class="module-card fade-up">
        <div class="module-head">
          <div>
            <p class="module-eyebrow">话题列表</p>
            <h2 class="section-title">论坛帖子</h2>
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
            <el-input v-model.trim="keyword" placeholder="请输入帖子标题查询" clearable @keyup.enter="loadPosts" />
            <el-button type="primary" @click="loadPosts">查询</el-button>
            <el-button @click="reset">重置</el-button>
            <el-button type="danger" plain @click="toPublish">我要发帖</el-button>
          </div>
        </section>

        <StatePanel
          v-if="loading"
          state="loading"
          tone="portal"
          title="正在加载帖子"
          description="正在同步社区居民与志愿者发布的论坛内容。"
        />
        <section v-else-if="filteredPosts.length" class="post-list">
          <article
            v-for="(post, index) in filteredPosts"
            :id="`post-${post.id}`"
            :key="post.id"
            class="post-card"
            :class="{ reverse: index % 2 === 1 }"
          >
            <div class="cover" :style="{ backgroundImage: `url(${coverFor(post.id)})` }"></div>

            <div class="post-main">
              <h3>{{ post.title }}</h3>
              <p>{{ post.content || '当前帖子暂无正文摘要。' }}</p>
              <div class="post-meta">
                <span>{{ authorLabel(post.userId) }}</span>
                <span>{{ categoryLabel(post.categoryId) }}</span>
                <span>浏览 {{ post.views ?? 0 }}</span>
                <span>{{ formatTime(post.createTime) }}</span>
              </div>

              <div class="comment-panel">
                <div class="comment-head">
                  <strong>评论（{{ commentsByPostId(post.id).length }}）</strong>
                </div>
                <ul class="comment-list">
                  <li v-for="comment in commentsByPostId(post.id).slice(0, 3)" :key="comment.id">
                    <span class="author">用户#{{ comment.userId }}</span>
                    <span>{{ comment.content }}</span>
                  </li>
                  <li v-if="!commentsByPostId(post.id).length" class="empty-comment">暂无评论，欢迎留言</li>
                </ul>
                <div class="comment-input">
                  <el-input v-model.trim="commentDraft[post.id]" placeholder="请输入评论内容" />
                  <el-button type="primary" @click="submitComment(post.id)">发布</el-button>
                </div>
              </div>
            </div>
          </article>
        </section>
        <StatePanel
          v-else
          tone="portal"
          title="暂无匹配帖子"
          description="可以调整筛选条件，或者登录后发布新的社区帖子。"
        />
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  addCommentApi,
  listForumCategoriesApi,
  pageCommentsApi,
  pageForumPostsApi,
  type CommentModel,
  type ForumCategoryModel,
  type PostModel
} from '@/api/content'
import StatePanel from '@/components/shared/StatePanel.vue'
import WorkspaceHero from '@/components/shared/WorkspaceHero.vue'
import { usePortalNavigation } from '@/composables/usePortalNavigation'
import PortalNavBar from './PortalNavBar.vue'

const router = useRouter()
const portalNav = usePortalNavigation()
const loading = ref(false)
const posts = ref<PostModel[]>([])
const comments = ref<CommentModel[]>([])
const commentDraft = reactive<Record<number, string>>({})
const categories = ref<ForumCategoryModel[]>([])
const keyword = ref('')
const selectedCategoryId = ref<number | undefined>()

const coverList = [
  'https://images.unsplash.com/photo-1503676260728-1c00da094a0b?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1511988617509-a57c8a288659?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1511632765486-a01980e01a18?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1472653816316-3ad6f10a6592?auto=format&fit=crop&w=900&q=80'
]

const filteredPosts = computed(() =>
  posts.value.filter((item) => {
    if (selectedCategoryId.value && item.categoryId !== selectedCategoryId.value) {
      return false
    }

    return true
  })
)

const commentsMap = computed(() => {
  const map: Record<number, CommentModel[]> = {}
  comments.value.forEach((item) => {
    const bucket = map[item.targetId] ?? []
    bucket.push(item)
    map[item.targetId] = bucket
  })
  return map
})

function coverFor(id: number) {
  return coverList[id % coverList.length]
}

function formatTime(time?: string) {
  if (!time) return '时间未知'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

function authorLabel(userId: number) {
  return `用户#${userId}`
}

function categoryLabel(categoryId: number) {
  const found = categories.value.find((item) => item.id === categoryId)
  return found?.name || `分类#${categoryId}`
}

async function loadPosts() {
  loading.value = true
  try {
    const res = await pageForumPostsApi({
      current: 1,
      size: 30,
      keyword: keyword.value || undefined,
      onlyApproved: true
    })
    posts.value = res.records
  } finally {
    loading.value = false
  }
}

async function loadComments() {
  try {
    const res = await pageCommentsApi({
      current: 1,
      size: 200,
      targetType: 'POST'
    })
    comments.value = res.records
  } catch {
    comments.value = []
  }
}

async function loadCategories() {
  categories.value = await listForumCategoriesApi()
}

function commentsByPostId(postId: number) {
  return commentsMap.value[postId] || []
}

function changeCategory(categoryId: number | undefined) {
  selectedCategoryId.value = categoryId
}

function reset() {
  keyword.value = ''
  selectedCategoryId.value = undefined
  loadPosts()
}

function toPublish() {
  portalNav.requireLogin(
    async () => {
      if (portalNav.roleLabel.value === '管理员') {
        router.push('/admin/content-manage')
        return
      }
      router.push('/volunteer/my-posts')
    },
    '/portal/forum'
  )
}

async function submitComment(postId: number) {
  const content = (commentDraft[postId] || '').trim()
  if (!content) {
    ElMessage.warning('请输入评论内容')
    return
  }

  const done = await portalNav.requireLogin(
    async () => {
      await addCommentApi({
        targetType: 'POST',
        targetId: postId,
        content
      })
      commentDraft[postId] = ''
      ElMessage.success('评论发布成功')
      await loadComments()
    },
    `/portal/forum#post-${postId}`
  )

  if (!done) {
    ElMessage.info('登录后可发表评论')
  }
}

onMounted(async () => {
  await Promise.all([loadCategories(), loadPosts(), loadComments()])
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
  grid-template-columns: 1fr auto auto auto;
  gap: 8px;
}

.post-list {
  display: grid;
  gap: 12px;
}

.post-card {
  border: 1px solid var(--cvs-border);
  border-radius: 16px;
  background: #fff;
  display: grid;
  grid-template-columns: 200px 1fr;
  overflow: hidden;
}

.post-card.reverse {
  grid-template-columns: 1fr 200px;
}

.post-card.reverse .cover {
  order: 2;
}

.post-card.reverse .post-main {
  order: 1;
}

.cover {
  min-height: 200px;
  background-size: cover;
  background-position: center;
}

.post-main {
  padding: 14px 16px;
  display: grid;
  gap: 10px;
}

.post-main h3 {
  margin: 0;
  font-size: 24px;
  line-height: 1.35;
}

.post-main p {
  margin: 0;
  color: #4f5f58;
  line-height: 1.8;
}

.post-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  color: #677670;
  font-size: 13px;
}

.comment-panel {
  border-top: 1px dashed #e0e5e8;
  padding-top: 10px;
}

.comment-head {
  margin-bottom: 8px;
}

.comment-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 8px;
}

.comment-list li {
  border: 1px solid #e8edf0;
  border-radius: 9px;
  background: #fbfcfd;
  padding: 8px 10px;
  display: grid;
  gap: 4px;
}

.author {
  color: #5d6f68;
  font-size: 12px;
}

.empty-comment {
  color: #7f8c87;
}

.comment-input {
  margin-top: 10px;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
}

@media (max-width: 960px) {
  .query-panel {
    grid-template-columns: 1fr;
  }

  .post-card,
  .post-card.reverse {
    grid-template-columns: 1fr;
  }

  .post-card.reverse .cover,
  .post-card.reverse .post-main {
    order: initial;
  }

  .cover {
    min-height: 180px;
  }

  .comment-input {
    grid-template-columns: 1fr;
  }
}
</style>
