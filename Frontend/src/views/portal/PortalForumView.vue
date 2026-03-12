<template>
  <div>
    <PortalNavBar />
    <main class="portal-wrap">
      <el-card class="module" shadow="never">
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
            <el-input v-model.trim="keyword" placeholder="请输入帖子名称查询" clearable @keyup.enter="loadPosts" />
            <el-button type="primary" @click="loadPosts">查询</el-button>
            <el-button @click="reset">重置</el-button>
            <el-button type="danger" plain @click="toPublish">我要发帖</el-button>
          </div>
        </section>

        <section class="post-list">
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
              <p>{{ post.content }}</p>
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

          <div v-if="!filteredPosts.length" class="empty">当前筛选条件下暂无帖子</div>
        </section>
      </el-card>
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
import { usePortalNavigation } from '@/composables/usePortalNavigation'
import PortalNavBar from './PortalNavBar.vue'

const router = useRouter()
const portalNav = usePortalNavigation()
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
    if (selectedCategoryId.value && item.categoryId !== selectedCategoryId.value) return false
    return true
  })
)

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
  const res = await pageForumPostsApi({
    current: 1,
    size: 30,
    keyword: keyword.value || undefined,
    onlyApproved: true
  })
  posts.value = res.records
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
  return comments.value.filter((item) => item.targetId === postId)
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
  portalNav.requireLogin(async () => {
    if (portalNav.roleLabel.value === '管理员') {
      router.push('/admin/content-manage')
      return
    }
    router.push('/volunteer/my-posts')
  }, '/portal/forum')
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
}

.module {
  border-radius: 16px;
  border: 1px solid var(--cvs-border);
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
  border: 1px solid #c93154;
  border-radius: 9px;
  background: #fff;
  color: #b1163f;
  font-weight: 700;
  padding: 7px 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.chip.active,
.chip:hover {
  color: #fff;
  background: linear-gradient(120deg, #d91f4c, #f23d64);
  border-color: #d91f4c;
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
  border-radius: 14px;
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

.empty {
  border: 1px dashed #d9dee2;
  border-radius: 12px;
  min-height: 120px;
  display: grid;
  place-items: center;
  color: #75817c;
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
