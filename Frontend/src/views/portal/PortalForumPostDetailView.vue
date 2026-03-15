<template>
  <div>
    <PortalNavBar />
    <main class="portal-forum-wrap">
      <WorkspaceHero
        tone="portal"
        eyebrow="论坛帖子详情"
        :title="post?.title || '查看社区论坛帖子详情'"
        description="先看帖子正文和已有评论，再决定是否打开评论编辑。当前页不再默认直接展开评论输入区。"
      >
        <template #actions>
          <el-button type="primary" @click="router.push(PORTAL_PATHS.forum)">返回论坛</el-button>
          <el-button type="warning" plain @click="toggleCommentComposer">
            {{ commentComposerVisible ? '收起评论编辑' : '我要评论' }}
          </el-button>
        </template>
      </WorkspaceHero>

      <StatePanel
        v-if="loading"
        state="loading"
        tone="portal"
        title="正在加载帖子详情"
        description="正在同步帖子正文和评论内容。"
      />

      <template v-else-if="post">
        <article class="post-card fade-up">
          <img v-if="post.coverImage" class="cover" :src="post.coverImage" alt="帖子封面" />
          <div class="meta-row">
            <span>{{ authorLabel(post) }}</span>
            <span>浏览 {{ post.views || 0 }}</span>
            <span>{{ formatDateTime(post.createTime) }}</span>
          </div>
          <p class="summary">{{ post.summary || '当前帖子暂无摘要。' }}</p>
          <RichTextRenderer class="body-copy" :value="post.content" empty-html="<p>当前帖子暂无正文内容。</p>" />
        </article>

        <section class="comment-card fade-up">
          <div class="section-head">
            <div>
              <p class="section-eyebrow">互动评论</p>
              <h2 class="section-title">社区留言</h2>
            </div>
            <el-button plain @click="toggleCommentComposer">
              {{ commentComposerVisible ? '收起评论编辑' : '打开评论编辑' }}
            </el-button>
          </div>

          <div class="comment-thread">
            <article v-for="item in comments" :key="item.id" class="comment-item">
              <div class="comment-item-head">
                <strong>用户 #{{ item.userId }}</strong>
                <span>{{ formatDateTime(item.createTime) }}</span>
              </div>
              <RichTextRenderer class="comment-rich" :value="item.content" empty-html="<p>暂无评论内容。</p>" />
            </article>
            <StatePanel
              v-if="!commentTotal"
              tone="portal"
              compact
              title="暂无评论"
              description="欢迎在这里补充你的社区经验。"
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

          <div v-if="commentComposerVisible" ref="commentComposerRef" class="comment-create">
            <RichTextEditor
              ref="commentEditorRef"
              v-model="commentText"
              :min-height="180"
              placeholder="输入你的看法、建议或补充信息"
            />
            <div class="comment-actions">
              <el-button @click="commentComposerVisible = false">暂不评论</el-button>
              <el-button type="primary" @click="submitComment">发布评论</el-button>
            </div>
          </div>
        </section>
      </template>

      <StatePanel
        v-else
        tone="portal"
        title="帖子不存在或暂不可见"
        description="可以返回论坛查看其他已通过审核的社区帖子。"
      >
        <template #actions>
          <el-button type="primary" @click="router.push(PORTAL_PATHS.forum)">返回论坛</el-button>
        </template>
      </StatePanel>
    </main>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import {
  addCommentApi,
  forumPostDetailApi,
  pageCommentsApi,
  type CommentModel,
  type PostModel
} from '@/api/content'
import RichTextRenderer from '@/components/shared/RichTextRenderer.vue'
import StatePanel from '@/components/shared/StatePanel.vue'
import RichTextEditor from '@/components/shared/RichTextEditor.vue'
import WorkspaceHero from '@/components/shared/WorkspaceHero.vue'
import { PORTAL_PATHS } from '@/constants/portal-routes'
import { usePortalNavigation } from '@/composables/usePortalNavigation'
import { formatDateTime } from '@/utils/display'
import { hasMeaningfulRichText, sanitizeRichText } from '@/utils/rich-text'
import PortalNavBar from './PortalNavBar.vue'

const route = useRoute()
const router = useRouter()
const portalNav = usePortalNavigation()
const postId = Number(route.params.id)

const loading = ref(false)
const post = ref<PostModel>()
const comments = ref<CommentModel[]>([])
const commentText = ref('')
const commentTotal = ref(0)
const commentComposerVisible = ref(false)
const commentComposerRef = ref<HTMLElement>()
const commentEditorRef = ref<InstanceType<typeof RichTextEditor>>()
const commentQuery = reactive({
  current: 1,
  size: 8
})

function authorLabel(postValue: PostModel) {
  return postValue.realName || postValue.userName || `用户 #${postValue.userId}`
}

async function focusCommentComposer() {
  await nextTick()
  commentComposerRef.value?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  window.setTimeout(() => commentEditorRef.value?.focusEditor(), 80)
}

function toggleCommentComposer() {
  commentComposerVisible.value = !commentComposerVisible.value
  if (commentComposerVisible.value) {
    void focusCommentComposer()
  }
}

async function loadComments() {
  const res = await pageCommentsApi({
    current: commentQuery.current,
    size: commentQuery.size,
    targetType: 'POST',
    targetId: postId
  })
  comments.value = res.records
  commentTotal.value = res.total
}

function handleCommentPage(page: number) {
  commentQuery.current = page
  void loadComments()
}

async function submitComment() {
  const content = sanitizeRichText(commentText.value)
  if (!hasMeaningfulRichText(content)) {
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
      commentText.value = ''
      commentQuery.current = 1
      ElMessage.success('评论发布成功')
      await loadComments()
    },
    route.fullPath
  )

  if (!done) {
    ElMessage.info('登录后可参与评论')
  }
}

onMounted(async () => {
  loading.value = true
  try {
    post.value = await forumPostDetailApi(postId)
    await loadComments()
  } catch {
    post.value = undefined
    comments.value = []
    commentTotal.value = 0
    ElMessage.warning('帖子详情加载失败，请检查后端服务是否已经启动')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.portal-forum-wrap {
  width: min(1080px, calc(100% - 24px));
  margin: 14px auto 40px;
  display: grid;
  gap: 14px;
}

.post-card,
.comment-card {
  border: 1px solid var(--cvs-border);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.96);
  padding: 18px;
  box-shadow: var(--cvs-shadow-soft);
}

.cover {
  width: 100%;
  max-height: 360px;
  object-fit: cover;
  border-radius: 16px;
  margin-bottom: 14px;
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: #667771;
  font-size: 13px;
}

.summary {
  margin: 14px 0 10px;
  font-size: 18px;
  line-height: 1.8;
  color: #2f403a;
}

.body-copy {
  margin: 0;
  line-height: 1.95;
  color: #3f4f4a;
}

.section-head {
  margin-bottom: 14px;
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: center;
}

.section-eyebrow {
  margin: 0 0 6px;
  color: #2a7a5f;
  font-size: var(--cvs-font-size-xs);
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.section-title {
  margin: 0;
}

.comment-thread {
  display: grid;
  gap: 10px;
}

.comment-item {
  border: 1px solid rgba(205, 216, 210, 0.9);
  border-radius: 14px;
  background: rgba(248, 251, 249, 0.96);
  padding: 14px;
  display: grid;
  gap: 10px;
}

.comment-item-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  color: #64746d;
  font-size: 13px;
}

.comment-create {
  margin-top: 16px;
  display: grid;
  gap: 12px;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.comment-rich {
  line-height: 1.8;
  color: #40504a;
}

.footer {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 760px) {
  .section-head,
  .comment-actions {
    display: grid;
    grid-template-columns: 1fr;
  }
}
</style>
