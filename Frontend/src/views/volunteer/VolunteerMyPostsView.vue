<template>
  <div class="page-shell">
    <section class="page-card">
      <div class="page-head">
        <div>
          <p class="page-eyebrow">论坛管理</p>
          <h1>我的帖子</h1>
        </div>
      </div>

      <div class="toolbar">
        <el-input v-model.trim="keyword" placeholder="请输入帖子标题查询" clearable @keyup.enter="loadPosts" />
        <el-button type="primary" @click="loadPosts">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
        <el-button type="primary" plain @click="openCreate">发布帖子</el-button>
      </div>

      <StatePanel
        v-if="loading"
        state="loading"
        title="正在同步帖子列表"
        description="正在加载你的发帖记录与审核结果。"
      />

      <template v-else-if="posts.length">
        <el-table :data="posts" border>
          <el-table-column prop="title" label="帖子标题" min-width="180" show-overflow-tooltip />
          <el-table-column label="分类名称" min-width="140">
            <template #default="{ row }">
              {{ categoryName(row.categoryId) }}
            </template>
          </el-table-column>
          <el-table-column prop="summary" label="帖子简介" min-width="220" show-overflow-tooltip />
          <el-table-column label="查看内容" width="120">
            <template #default="{ row }">
              <el-button type="primary" plain @click="previewPost(row)">查看内容</el-button>
            </template>
          </el-table-column>
          <el-table-column label="封面图片" width="110">
            <template #default="{ row }">
              <img class="cover-mini" :src="row.coverImage || defaultPostCover" alt="帖子封面" />
            </template>
          </el-table-column>
          <el-table-column prop="views" label="浏览量" width="100" />
          <el-table-column label="帖子状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getForumPostStatusTag(row.status)">{{ getForumPostStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" min-width="180">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <div class="row-actions">
                <el-button circle type="primary" @click="openEdit(row)">编</el-button>
                <el-button circle type="danger" @click="removePost(row)">删</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="pager">
          <el-pagination
            layout="total, prev, pager, next"
            :total="total"
            :current-page="query.current"
            :page-size="query.size"
            @current-change="handlePage"
          />
        </div>
      </template>

      <StatePanel v-else title="暂无个人帖子" description="可以直接点击“发布帖子”，统一进入待审核队列。" />
    </section>

    <el-dialog v-model="previewVisible" title="帖子内容预览" width="min(92vw, 860px)" append-to-body>
      <div v-if="previewRecord" class="preview-panel">
        <div class="preview-meta">
          <span>{{ categoryName(previewRecord.categoryId) }}</span>
          <span>{{ formatDateTime(previewRecord.createTime) }}</span>
          <span>浏览 {{ previewRecord.views ?? 0 }}</span>
        </div>
        <h2>{{ previewRecord.title }}</h2>
        <p class="preview-summary">{{ previewRecord.summary }}</p>
        <RichTextRenderer :value="previewRecord.content" empty-html="<p>暂无正文内容。</p>" />
      </div>
    </el-dialog>

    <el-dialog
      v-model="editorVisible"
      :title="form.id ? '编辑帖子' : '发布帖子'"
      width="min(94vw, 860px)"
      top="4vh"
      append-to-body
      @opened="handleDialogOpened"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="dialog-grid">
          <el-form-item label="标题" prop="title">
            <el-input v-model.trim="form.title" maxlength="60" show-word-limit />
          </el-form-item>
          <el-form-item label="分类" prop="categoryId">
            <el-select v-model="form.categoryId" style="width: 100%">
              <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item label="封面图片" prop="coverImage">
          <div class="uploader">
            <img class="cover-preview" :src="form.coverImage || defaultPostCover" alt="帖子封面预览" />
            <div class="uploader-copy">
              <div class="uploader-actions">
                <input
                  ref="coverInputRef"
                  class="hidden-file-input"
                  type="file"
                  accept="image/png,image/jpeg,image/jpg,image/webp,image/gif"
                  @change="handleCoverInputChange"
                />
                <el-button :loading="coverUploading" @click="coverInputRef?.click()">上传封面</el-button>
                <el-button text :disabled="!form.coverImage" @click="form.coverImage = ''">清空封面</el-button>
              </div>
              <span class="field-tip">建议使用横向封面，单张不超过 5MB。</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="帖子简介" prop="summary">
          <el-input
            v-model.trim="form.summary"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="请简要说明帖子内容，便于审核和列表展示。"
          />
        </el-form-item>

        <el-form-item label="正文">
          <RichTextEditor
            ref="postEditorRef"
            v-model="form.content"
            :min-height="300"
            placeholder="请输入帖子正文内容，可以插入标题、颜色、链接、图片、视频和表格"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-actions">
          <el-button @click="editorVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submit">提交审核</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { uploadImageApi } from '@/api/common'
import {
  deleteForumPostApi,
  listForumCategoriesApi,
  pageForumPostsApi,
  saveMyPostApi,
  undoMyPostApi,
  type ForumCategoryModel,
  type PostModel
} from '@/api/content'
import RichTextEditor from '@/components/shared/RichTextEditor.vue'
import RichTextRenderer from '@/components/shared/RichTextRenderer.vue'
import StatePanel from '@/components/shared/StatePanel.vue'
import { DEFAULT_ACTIVITY_COVER, formatDateTime, getForumPostStatusLabel, getForumPostStatusTag } from '@/utils/display'
import { validateElementForm } from '@/utils/form'
import { hasMeaningfulRichText, sanitizeRichText } from '@/utils/rich-text'
import { DEFAULT_UNDO_WINDOW_SECONDS, showTimedUndoNotification } from '@/utils/timed-undo'
import { validateImageFile } from '@/utils/upload'

interface PageQuery {
  current: number
  size: number
}

const defaultPostCover = DEFAULT_ACTIVITY_COVER
const categories = ref<ForumCategoryModel[]>([])
const posts = ref<PostModel[]>([])
const total = ref(0)
const loading = ref(false)
const keyword = ref('')
const previewVisible = ref(false)
const previewRecord = ref<PostModel>()
const editorVisible = ref(false)
const coverUploading = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const coverInputRef = ref<HTMLInputElement>()
const postEditorRef = ref<InstanceType<typeof RichTextEditor>>()
const query = reactive<PageQuery>({
  current: 1,
  size: 10
})
const form = reactive<Partial<PostModel>>({
  id: undefined,
  title: '',
  coverImage: '',
  summary: '',
  categoryId: undefined,
  content: ''
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入帖子标题', trigger: 'blur' }],
  coverImage: [{ required: true, message: '请上传帖子封面', trigger: 'change' }],
  summary: [{ required: true, message: '请输入帖子简介', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择帖子分类', trigger: 'change' }]
}

async function loadPosts() {
  loading.value = true
  try {
    const res = await pageForumPostsApi({
      current: query.current,
      size: query.size,
      keyword: keyword.value || undefined,
      onlyMine: true,
      onlyApproved: false
    })
    posts.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function handlePage(page: number) {
  query.current = page
  void loadPosts()
}

function resetFilters() {
  keyword.value = ''
  query.current = 1
  void loadPosts()
}

function categoryName(categoryId: number) {
  const category = categories.value.find((item) => item.id === categoryId)
  return category?.name || `分类 ${categoryId}`
}

function previewPost(row: PostModel) {
  previewRecord.value = row
  previewVisible.value = true
}

function resetForm() {
  Object.assign(form, {
    id: undefined,
    title: '',
    coverImage: '',
    summary: '',
    categoryId: categories.value[0]?.id,
    content: ''
  })
}

function openCreate() {
  resetForm()
  editorVisible.value = true
}

function openEdit(row: PostModel) {
  Object.assign(form, {
    ...row,
    content: row.content || ''
  })
  editorVisible.value = true
}

function handleDialogOpened() {
  nextTick(() => postEditorRef.value?.focusEditor())
}

async function handleCoverInputChange(event: Event) {
  const target = event.target as HTMLInputElement | null
  const file = target?.files?.[0]
  if (target) {
    target.value = ''
  }
  if (!file || !validateImageFile(file)) {
    return
  }

  coverUploading.value = true
  try {
    const res = await uploadImageApi(file)
    form.coverImage = res.url
    ElMessage.success('封面上传成功')
  } finally {
    coverUploading.value = false
  }
}

async function submit() {
  if (!(await validateElementForm(formRef.value))) {
    return
  }

  const content = sanitizeRichText(form.content)
  if (!hasMeaningfulRichText(content)) {
    ElMessage.warning('请输入帖子正文内容')
    return
  }

  submitting.value = true
  try {
    const creating = !form.id
    const savedPost = await saveMyPostApi({
      ...form,
      title: (form.title || '').trim(),
      summary: (form.summary || '').trim(),
      coverImage: (form.coverImage || '').trim(),
      content
    })
    editorVisible.value = false
    await loadPosts()

    if (creating) {
      showTimedUndoNotification({
        title: '帖子已提交',
        message: `帖子已进入待审核队列，${DEFAULT_UNDO_WINDOW_SECONDS} 秒内可以撤销本次发布。`,
        undoLabel: '撤销发帖',
        undoSuccessMessage: '帖子已撤销',
        onUndo: async () => {
          await undoMyPostApi(savedPost.id)
          await loadPosts()
        }
      })
      return
    }

    ElMessage.success('帖子已重新提交审核')
  } finally {
    submitting.value = false
  }
}

async function removePost(row: PostModel) {
  await ElMessageBox.confirm(`确认删除帖子“${row.title}”吗？`, '删除帖子', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消'
  })

  await deleteForumPostApi(row.id)
  ElMessage.success('帖子已删除')
  if (posts.value.length === 1 && query.current > 1) {
    query.current -= 1
  }
  await loadPosts()
}

onMounted(async () => {
  categories.value = await listForumCategoriesApi()
  if (!form.categoryId && categories.value[0]) {
    form.categoryId = categories.value[0].id
  }
  await loadPosts()
})
</script>

<style scoped>
.page-shell {
  display: grid;
  gap: 14px;
}

.page-card {
  border: 1px solid var(--cvs-border);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: var(--cvs-shadow-soft);
  padding: 24px;
}

.page-head {
  margin-bottom: 18px;
}

.page-eyebrow {
  margin: 0 0 8px;
  color: #2a7a5f;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.page-head h1 {
  margin: 0;
  font-size: 24px;
  color: #182f24;
}

.toolbar {
  display: grid;
  grid-template-columns: minmax(280px, 1fr) auto auto auto;
  gap: 10px;
  margin-bottom: 18px;
}

.cover-mini {
  width: 62px;
  height: 62px;
  border-radius: 12px;
  object-fit: cover;
  border: 1px solid var(--cvs-border);
}

.row-actions {
  display: flex;
  gap: 8px;
}

.pager {
  margin-top: 18px;
  display: flex;
  justify-content: flex-start;
}

.preview-panel {
  display: grid;
  gap: 14px;
}

.preview-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: #677871;
  font-size: 13px;
}

.preview-panel h2,
.preview-summary {
  margin: 0;
}

.preview-summary {
  color: #51645b;
  line-height: 1.75;
}

.dialog-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 14px;
}

.uploader {
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

.uploader-copy {
  display: grid;
  gap: 10px;
}

.uploader-actions,
.dialog-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.dialog-actions {
  justify-content: flex-end;
}

.hidden-file-input {
  display: none;
}

.field-tip {
  color: #677870;
  font-size: 13px;
}

@media (max-width: 960px) {
  .toolbar,
  .dialog-grid,
  .uploader {
    grid-template-columns: 1fr;
  }
}
</style>
