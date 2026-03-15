<template>
  <div>
    <PortalNavBar />
    <main class="portal-wrap">
      <WorkspaceHero
        tone="portal"
        compact
        eyebrow="社区论坛"
        title="这里是志愿者和社区居民的交流区，发帖、评论互动和经验沉淀都在论坛完成"
        description="当前页面只负责论坛帖子、评论查看和发帖交互，不再把无关操作混在一起。"
      >
        <template #actions>
          <el-button type="primary" @click="openComposer">我要发帖</el-button>
          <el-button @click="router.push(PORTAL_PATHS.activities)">查看志愿活动</el-button>
        </template>
      </WorkspaceHero>

      <section class="module-card fade-up">
        <div class="module-head">
          <div>
            <p class="module-eyebrow">发帖入口</p>
            <h2 class="section-title">弹窗式发布</h2>
            <p class="module-summary">
              点击“我要发帖”后，以模态框集中完成标题、封面、摘要和正文编辑。帖子分类仍用于后台治理，但不再在前台帖子卡片上反复展示。
            </p>
          </div>
          <el-button type="primary" plain @click="openComposer">打开发帖弹窗</el-button>
        </div>
      </section>

      <section class="module-card fade-up">
        <div class="module-head">
          <div>
            <p class="module-eyebrow">话题列表</p>
            <h2 class="section-title">论坛帖子</h2>
            <p class="module-summary">
              先看别人评论，再决定是否在当前页打开评论编辑。评论查看和评论输入现在是两个明确的动作。
            </p>
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
            <el-input
              v-model.trim="keyword"
              placeholder="请输入帖子标题关键词"
              clearable
              @keyup.enter="loadPosts"
            />
            <el-button type="primary" @click="loadPosts">查询</el-button>
            <el-button @click="resetFilters">重置</el-button>
            <el-button type="danger" plain @click="openComposer">我要发帖</el-button>
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
          <article v-for="post in filteredPosts" :id="`post-${post.id}`" :key="post.id" class="post-card">
            <div class="cover-shell">
              <img class="cover-image" :src="post.coverImage || coverFor(post.id)" alt="帖子封面" />
            </div>

            <div class="post-main">
              <div class="post-head">
                <div>
                  <h3>{{ post.title }}</h3>
                  <p class="post-summary">{{ postPreview(post) }}</p>
                </div>
              </div>

              <div class="post-meta">
                <span>{{ authorLabel(post) }}</span>
                <span>浏览 {{ post.views ?? 0 }}</span>
                <span>{{ formatTime(post.createTime) }}</span>
              </div>

              <div class="post-actions">
                <el-button type="primary" plain @click="router.push(PORTAL_PATHS.forumDetail(post.id))">查看详情</el-button>
                <el-button @click="openInlineDiscussion(post, false)">查看评论</el-button>
                <el-button @click="openInlineDiscussion(post, true)">我要评论</el-button>
              </div>
            </div>

            <div v-if="discussionPostId === post.id" class="comment-inline">
              <div class="comment-inline-head">
                <div>
                  <p class="comment-eyebrow">帖子评论</p>
                  <strong>{{ post.title }}</strong>
                </div>
                <div class="comment-inline-head-actions">
                  <el-button text @click="router.push(PORTAL_PATHS.forumDetail(post.id))">查看详情页</el-button>
                  <el-button text @click="closeInlineDiscussion">收起</el-button>
                </div>
              </div>

              <StatePanel
                v-if="commentsLoading"
                compact
                tone="portal"
                title="正在加载评论"
                description="正在同步当前帖子的评论列表。"
              />
              <div v-else class="comment-thread">
                <article v-for="item in inlineComments" :key="item.id" class="comment-item">
                  <div class="comment-item-head">
                    <strong>用户 #{{ item.userId }}</strong>
                    <span>{{ formatTime(item.createTime) }}</span>
                  </div>
                  <RichTextRenderer class="comment-item-content" :value="item.content" empty-html="<p>暂无评论内容</p>" />
                </article>
                <StatePanel
                  v-if="!inlineComments.length"
                  compact
                  tone="portal"
                  title="暂无评论"
                  description="这条帖子还没有评论，欢迎成为第一个发言的人。"
                />
              </div>

              <div v-if="commentTotal > inlineComments.length" class="comment-count-tip">
                当前展示最近 {{ inlineComments.length }} 条评论，共 {{ commentTotal }} 条。
              </div>

              <div v-if="commentComposerVisible" class="comment-composer">
                <RichTextEditor
                  :ref="setCommentEditorRef"
                  v-model="commentDraft"
                  :min-height="180"
                  placeholder="输入你的评论观点、补充信息或社区建议"
                />

                <div class="comment-inline-actions">
                  <el-button @click="commentComposerVisible = false">暂不评论</el-button>
                  <el-button type="primary" :loading="submittingComment" @click="submitInlineComment(post.id)">
                    发布评论
                  </el-button>
                </div>
              </div>
              <div v-else class="comment-inline-actions">
                <el-button type="primary" plain @click="openInlineDiscussion(post, true)">打开评论编辑</el-button>
              </div>
            </div>
          </article>
        </section>

        <StatePanel
          v-else
          tone="portal"
          title="暂无匹配帖子"
          description="可以调整筛选条件，或登录后直接发布新的社区帖子。"
        />
      </section>
    </main>

    <el-dialog
      v-model="composerDialogVisible"
      title="发布帖子"
      width="min(94vw, 860px)"
      top="4vh"
      append-to-body
      class="forum-composer-dialog"
      @opened="handleComposerOpened"
    >
      <el-form ref="composerFormRef" :model="composerForm" :rules="composerRules" label-position="top">
        <div class="composer-grid">
          <el-form-item label="帖子标题" prop="title">
            <el-input v-model.trim="composerForm.title" maxlength="60" show-word-limit placeholder="请输入帖子标题" />
          </el-form-item>
          <el-form-item label="帖子分类" prop="categoryId">
            <el-select v-model="composerForm.categoryId" placeholder="请选择帖子分类">
              <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item label="帖子封面" prop="coverImage">
          <div class="cover-uploader">
            <img class="cover-preview" :src="composerForm.coverImage || defaultPostCover" alt="帖子封面预览" />
            <div class="cover-copy">
              <div class="cover-actions">
                <input
                  ref="coverInputRef"
                  class="cover-file-input"
                  type="file"
                  accept="image/png,image/jpeg,image/jpg,image/webp,image/gif"
                  @change="handleCoverInputChange"
                />
                <el-button :loading="coverUploading" @click="openCoverPicker">上传封面</el-button>
                <el-button plain :disabled="!composerForm.coverImage" @click="resetCoverImage">改用推荐封面</el-button>
              </div>
              <span class="field-tip">
                建议使用横向图片，支持 JPG / PNG / WEBP / GIF，单张不超过 5MB。点击“改用推荐封面”后会移除自定义图片。
              </span>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="帖子摘要" prop="summary">
          <el-input
            v-model.trim="composerForm.summary"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="请用 1-2 句话概括帖子内容，便于列表展示"
          />
        </el-form-item>

        <el-form-item label="帖子正文">
          <RichTextEditor
            ref="postEditorRef"
            v-model="composerForm.content"
            :min-height="300"
            placeholder="请输入帖子正文内容，可以插入标题、列表、引用、颜色、链接、图片、视频和表格"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="composer-actions">
          <el-button @click="resetComposer">重置</el-button>
          <el-button @click="composerDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submittingPost" @click="submitPost">提交审核</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import type { ComponentPublicInstance } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import { uploadImageApi } from '@/api/common'
import {
  addCommentApi,
  listForumCategoriesApi,
  pageCommentsApi,
  pageForumPostsApi,
  saveMyPostApi,
  undoMyPostApi,
  type CommentModel,
  type ForumCategoryModel,
  type PostModel
} from '@/api/content'
import RichTextEditor from '@/components/shared/RichTextEditor.vue'
import RichTextRenderer from '@/components/shared/RichTextRenderer.vue'
import StatePanel from '@/components/shared/StatePanel.vue'
import WorkspaceHero from '@/components/shared/WorkspaceHero.vue'
import { PORTAL_PATHS } from '@/constants/portal-routes'
import { usePortalNavigation } from '@/composables/usePortalNavigation'
import { DEFAULT_ACTIVITY_COVER } from '@/utils/display'
import { validateElementForm } from '@/utils/form'
import { hasMeaningfulRichText, richTextToPlainText, sanitizeRichText } from '@/utils/rich-text'
import { DEFAULT_UNDO_WINDOW_SECONDS, showTimedUndoNotification } from '@/utils/timed-undo'
import { validateImageFile } from '@/utils/upload'
import PortalNavBar from './PortalNavBar.vue'

interface ForumComposerForm {
  title: string
  coverImage: string
  summary: string
  categoryId?: number
  content: string
}

type RichTextEditorInstance = InstanceType<typeof RichTextEditor>

const router = useRouter()
const portalNav = usePortalNavigation()

const loading = ref(false)
const posts = ref<PostModel[]>([])
const categories = ref<ForumCategoryModel[]>([])
const keyword = ref('')
const selectedCategoryId = ref<number | undefined>()
const composerDialogVisible = ref(false)
const coverUploading = ref(false)
const coverInputRef = ref<HTMLInputElement>()
const submittingPost = ref(false)
const submittingComment = ref(false)
const discussionPostId = ref<number>()
const commentComposerVisible = ref(false)
const commentsLoading = ref(false)
const inlineComments = ref<CommentModel[]>([])
const commentTotal = ref(0)
const commentDraft = ref('')
const composerFormRef = ref<FormInstance>()
const postEditorRef = ref<RichTextEditorInstance>()
const commentEditorRef = ref<RichTextEditorInstance | null>(null)

const defaultPostCover = DEFAULT_ACTIVITY_COVER
const composerForm = reactive<ForumComposerForm>({
  title: '',
  coverImage: '',
  summary: '',
  categoryId: undefined,
  content: ''
})

const composerRules: FormRules<ForumComposerForm> = {
  title: [{ required: true, message: '请输入帖子标题', trigger: 'blur' }],
  coverImage: [{ required: true, message: '请上传帖子封面', trigger: 'change' }],
  summary: [{ required: true, message: '请输入帖子摘要', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择帖子分类', trigger: 'change' }]
}

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

function coverFor(id: number) {
  return coverList[id % coverList.length]
}

function formatTime(time?: string) {
  if (!time) {
    return '时间未知'
  }
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

function authorLabel(post: PostModel) {
  return post.realName || post.userName || `用户 #${post.userId}`
}

function postPreview(post: PostModel) {
  return post.summary || richTextToPlainText(post.content) || '当前帖子暂无正文摘要。'
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

async function loadCategories() {
  categories.value = await listForumCategoriesApi()
  if (!composerForm.categoryId) {
    composerForm.categoryId = categories.value[0]?.id
  }
}

async function loadInlineComments(postId: number) {
  commentsLoading.value = true
  try {
    const res = await pageCommentsApi({
      current: 1,
      size: 6,
      targetType: 'POST',
      targetId: postId
    })
    inlineComments.value = res.records
    commentTotal.value = res.total
  } finally {
    commentsLoading.value = false
  }
}

function changeCategory(categoryId: number | undefined) {
  selectedCategoryId.value = categoryId
}

function resetFilters() {
  keyword.value = ''
  selectedCategoryId.value = undefined
  void loadPosts()
}

function resetComposer() {
  Object.assign(composerForm, {
    title: '',
    coverImage: '',
    summary: '',
    categoryId: categories.value[0]?.id,
    content: ''
  })
}

function handleComposerOpened() {
  postEditorRef.value?.focusEditor()
}

function setCommentEditorRef(instance: Element | ComponentPublicInstance | null) {
  if (instance && 'focusEditor' in instance && typeof instance.focusEditor === 'function') {
    commentEditorRef.value = instance as RichTextEditorInstance
    return
  }
  commentEditorRef.value = null
}

async function uploadCoverFile(file: File) {
  if (!validateImageFile(file)) {
    return
  }

  coverUploading.value = true
  try {
    const res = await uploadImageApi(file)
    composerForm.coverImage = res.url
    ElMessage.success('帖子封面上传成功')
  } finally {
    coverUploading.value = false
  }
}

function openCoverPicker() {
  if (!coverUploading.value) {
    coverInputRef.value?.click()
  }
}

async function handleCoverInputChange(event: Event) {
  const target = event.target as HTMLInputElement | null
  const file = target?.files?.[0]
  if (target) {
    target.value = ''
  }
  if (!file) {
    return
  }

  await uploadCoverFile(file)
}

function resetCoverImage() {
  composerForm.coverImage = ''
}

async function openComposer() {
  const done = await portalNav.requireLogin(
    async () => {
      if (!categories.value.length) {
        await loadCategories()
      }
      composerDialogVisible.value = true
      await nextTick()
      postEditorRef.value?.focusEditor()
    },
    PORTAL_PATHS.forum
  )

  if (!done) {
    ElMessage.info('登录后可发布帖子')
  }
}

async function submitPost() {
  if (!(await validateElementForm(composerFormRef.value))) {
    return
  }

  const content = sanitizeRichText(composerForm.content)
  if (!hasMeaningfulRichText(content)) {
    ElMessage.warning('请输入帖子正文内容')
    return
  }

  submittingPost.value = true
  try {
    const post = await saveMyPostApi({
      title: composerForm.title.trim(),
      coverImage: composerForm.coverImage.trim(),
      summary: composerForm.summary.trim(),
      categoryId: composerForm.categoryId,
      content
    })
    resetComposer()
    composerDialogVisible.value = false
    await loadPosts()
    showTimedUndoNotification({
      title: '帖子已提交',
      message: `你的帖子已进入待审核队列，${DEFAULT_UNDO_WINDOW_SECONDS} 秒内可以撤销本次发布。`,
      undoLabel: '撤销发帖',
      undoSuccessMessage: '帖子已撤销',
      onUndo: async () => {
        await undoMyPostApi(post.id)
        await loadPosts()
      }
    })
  } finally {
    submittingPost.value = false
  }
}

function closeInlineDiscussion() {
  discussionPostId.value = undefined
  commentComposerVisible.value = false
  commentDraft.value = ''
  inlineComments.value = []
  commentTotal.value = 0
  commentEditorRef.value = null
}

async function openInlineDiscussion(post: PostModel, openComposerPanel: boolean) {
  if (openComposerPanel) {
    const done = await portalNav.requireLogin(
      async () => {
        discussionPostId.value = post.id
        commentComposerVisible.value = true
        await loadInlineComments(post.id)
        await nextTick()
        document.getElementById(`post-${post.id}`)?.scrollIntoView({ behavior: 'smooth', block: 'center' })
        window.setTimeout(() => commentEditorRef.value?.focusEditor(), 120)
      },
      PORTAL_PATHS.forum
    )

    if (!done) {
      ElMessage.info('登录后可在当前页发表评论')
    }
    return
  }

  discussionPostId.value = post.id
  commentComposerVisible.value = false
  await loadInlineComments(post.id)
  await nextTick()
  document.getElementById(`post-${post.id}`)?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

async function submitInlineComment(postId: number) {
  const content = sanitizeRichText(commentDraft.value)
  if (!hasMeaningfulRichText(content)) {
    ElMessage.warning('请输入评论内容')
    return
  }

  submittingComment.value = true
  try {
    await addCommentApi({
      targetType: 'POST',
      targetId: postId,
      content
    })
    ElMessage.success('评论发布成功')
    commentDraft.value = ''
    await loadInlineComments(postId)
  } finally {
    submittingComment.value = false
  }
}

onMounted(async () => {
  try {
    await Promise.all([loadCategories(), loadPosts()])
  } catch {
    categories.value = []
    posts.value = []
    ElMessage.warning('论坛数据加载失败，请检查后端服务是否已经启动')
  }
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
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 14px;
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

.module-summary {
  margin: 8px 0 0;
  color: #5b7066;
  line-height: 1.7;
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
  gap: 14px;
}

.post-card {
  border: 1px solid rgba(205, 216, 210, 0.96);
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(247, 250, 248, 0.95));
  display: grid;
  grid-template-columns: 208px minmax(0, 1fr);
  overflow: hidden;
}

.cover-shell {
  padding: 18px 0 18px 18px;
}

.cover-image {
  width: 100%;
  height: 164px;
  object-fit: cover;
  border-radius: 16px;
  display: block;
}

.post-main {
  padding: 18px;
  display: grid;
  gap: 12px;
  align-content: start;
  min-width: 0;
}

.post-head h3,
.post-summary {
  margin: 0;
}

.post-head h3 {
  font-size: 28px;
  line-height: 1.25;
  color: #172f24;
}

.post-summary {
  margin-top: 8px;
  color: #50635a;
  line-height: 1.8;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  color: #677670;
  font-size: 13px;
}

.post-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.comment-inline {
  grid-column: 1 / -1;
  border-top: 1px solid rgba(208, 217, 212, 0.92);
  padding: 18px;
  background: rgba(245, 249, 247, 0.92);
  display: grid;
  gap: 14px;
}

.comment-inline-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.comment-inline-head-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.comment-eyebrow {
  margin: 0 0 6px;
  color: #2a7a5f;
  font-size: 12px;
  letter-spacing: 0.08em;
}

.comment-thread {
  display: grid;
  gap: 10px;
}

.comment-item {
  border: 1px solid rgba(205, 216, 210, 0.9);
  border-radius: 14px;
  background: #ffffff;
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

.comment-item-content {
  line-height: 1.75;
  color: #3f4f49;
}

.comment-count-tip {
  color: #677870;
  font-size: 13px;
}

.comment-composer {
  display: grid;
  gap: 12px;
}

.comment-inline-actions,
.composer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.composer-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 14px;
}

.cover-uploader {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 14px;
  align-items: center;
}

.cover-preview {
  width: 100%;
  aspect-ratio: 16 / 10;
  border-radius: 16px;
  object-fit: cover;
  border: 1px solid rgba(197, 209, 202, 0.95);
  background: #f2f5f3;
}

.cover-copy {
  display: grid;
  gap: 10px;
}

.cover-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.cover-file-input {
  display: none;
}

.field-tip {
  color: #677870;
  font-size: 13px;
  line-height: 1.6;
}

@media (max-width: 920px) {
  .post-card {
    grid-template-columns: 1fr;
  }

  .cover-shell {
    padding: 18px 18px 0;
  }

  .query-panel,
  .composer-grid,
  .cover-uploader {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .module-head,
  .comment-inline-head,
  .comment-inline-actions,
  .composer-actions {
    display: grid;
    grid-template-columns: 1fr;
  }
}
</style>
