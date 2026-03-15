<template>
  <AdminContentSection
    title="论坛帖子审核"
    description="支持查看帖子封面、摘要、浏览量与审核状态，并提供管理员编辑、审核和归档入口。"
    :loading="loading && !records.length"
    :empty="!loading && !records.length"
    loading-title="正在加载帖子审核列表"
    loading-description="请稍候，系统正在同步论坛审核数据。"
    empty-title="当前暂无待管理帖子"
    empty-description="帖子审核与归档结果会在这里显示。"
  >
    <template #actions>
      <el-button type="warning" plain :disabled="!selectedIds.length" @click="batchArchive">批量归档</el-button>
    </template>

    <SearchForm @search="search" @reset="resetQuery">
      <el-form-item label="审核状态">
        <el-select v-model="query.status" clearable style="width: 180px">
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已拒绝" value="REJECTED" />
          <el-option label="已归档" value="ARCHIVED" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model.trim="query.keyword" placeholder="标题 / 简介 / 作者" />
      </el-form-item>
    </SearchForm>

    <el-table :data="records" border v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" />
      <el-table-column label="封面" width="92">
        <template #default="{ row }">
          <img class="cover-mini" :src="row.coverImage || defaultPostCover" alt="帖子封面" />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column label="志愿者" min-width="120">
        <template #default="{ row }">
          {{ authorLabel(row) }}
        </template>
      </el-table-column>
      <el-table-column label="分类" min-width="120">
        <template #default="{ row }">
          {{ categoryName(row.categoryId) }}
        </template>
      </el-table-column>
      <el-table-column prop="summary" label="帖子简介" min-width="220" show-overflow-tooltip />
      <el-table-column prop="views" label="浏览量" width="90" />
      <el-table-column label="审核状态" width="110">
        <template #default="{ row }">
          <el-tag :type="getForumPostStatusTag(row.status)">{{ getForumPostStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="auditReason" label="审核说明" min-width="180" show-overflow-tooltip />
      <el-table-column label="创建时间" min-width="170">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <div class="action-cell">
            <el-tooltip content="编辑帖子" placement="top">
              <el-button link type="primary" :icon="EditPen" @click="openEdit(row)" />
            </el-tooltip>
            <el-tooltip content="通过审核" placement="top">
              <span>
                <el-button
                  link
                  type="success"
                  :icon="Check"
                  :disabled="row.status !== 'PENDING'"
                  @click="audit(row.id, 'APPROVED')"
                />
              </span>
            </el-tooltip>
            <el-tooltip content="拒绝帖子" placement="top">
              <span>
                <el-button
                  link
                  type="warning"
                  :icon="Close"
                  :disabled="row.status !== 'PENDING'"
                  @click="audit(row.id, 'REJECTED')"
                />
              </span>
            </el-tooltip>
            <el-tooltip content="归档帖子" placement="top">
              <el-button link type="warning" :icon="Delete" @click="archivePost(row.id)" />
            </el-tooltip>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <template #pagination>
      <el-pagination
        layout="total, sizes, prev, pager, next"
        :current-page="query.current"
        :page-size="query.size"
        :page-sizes="pageSizes"
        :total="total"
        @current-change="onPageChange"
        @size-change="onSizeChange"
      />
    </template>
  </AdminContentSection>

  <el-dialog
    v-model="visible"
    title="编辑帖子"
    width="min(92vw, 820px)"
    top="5vh"
    append-to-body
    class="manage-dialog"
  >
    <el-form :model="form" label-position="top">
      <div class="dialog-grid">
        <el-form-item label="帖子标题" class="span-2">
          <el-input v-model.trim="form.title" />
        </el-form-item>

        <el-form-item label="帖子分类">
          <el-select v-model="form.categoryId" style="width: 100%">
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="审核状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已归档" value="ARCHIVED" />
          </el-select>
        </el-form-item>

        <el-form-item label="帖子封面" class="span-2">
          <div class="uploader">
            <img class="cover-preview" :src="form.coverImage || defaultPostCover" alt="帖子封面" />
            <div class="uploader-actions">
              <el-upload
                :show-file-list="false"
                :http-request="handleCoverUpload"
                :before-upload="beforeCoverUpload"
                accept="image/png,image/jpeg,image/jpg,image/webp,image/gif"
              >
                <el-button :loading="coverUploading">上传封面</el-button>
              </el-upload>
              <el-button text @click="form.coverImage = ''">清空封面</el-button>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="帖子简介" class="span-2">
          <el-input
            v-model.trim="form.summary"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="请输入帖子简介"
          />
        </el-form-item>

        <el-form-item v-if="form.status === 'REJECTED'" label="拒绝原因" class="span-2">
          <el-input
            v-model.trim="form.auditReason"
            type="textarea"
            :rows="3"
            placeholder="帖子状态为已拒绝时，必须填写拒绝原因"
          />
        </el-form-item>

        <el-form-item label="帖子内容" class="span-2">
          <RichTextEditor v-model="form.content" placeholder="请输入帖子正文内容" />
          <span class="field-tip">支持基础标题、列表、引用等富文本格式，提交前会自动进行安全清洗。</span>
        </el-form-item>
      </div>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submitEdit">保存修改</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { UploadProps, UploadRequestOptions } from 'element-plus'
import { ElMessage } from 'element-plus'
import { Check, Close, Delete, EditPen } from '@element-plus/icons-vue'
import { uploadImageApi } from '@/api/common'
import {
  auditPostApi,
  batchDeleteForumPostsApi,
  deleteForumPostApi,
  listManageForumCategoriesApi,
  pageForumPostsApi,
  updateForumPostApi,
  type ForumCategoryModel,
  type PostModel
} from '@/api/content'
import AdminContentSection from '@/components/admin/AdminContentSection.vue'
import SearchForm from '@/components/SearchForm.vue'
import RichTextEditor from '@/components/shared/RichTextEditor.vue'
import { useSelectionIds } from '@/composables/useSelectionIds'
import { useTable } from '@/composables/useTable'
import { runConfirmedAction } from '@/utils/confirmed-action'
import { DEFAULT_ACTIVITY_COVER, formatDateTime, getForumPostStatusLabel, getForumPostStatusTag } from '@/utils/display'
import { richTextToPlainText, sanitizeRichText } from '@/utils/rich-text'
import { validateImageFile } from '@/utils/upload'

interface PostQuery {
  current: number
  size: number
  keyword: string
  status?: string
}

const pageSizes = [10, 20, 30, 50]
const defaultPostCover = DEFAULT_ACTIVITY_COVER

const { loading, records, total, query, load, handlePage, handleSizeChange, reset } = useTable<
  PostModel,
  PostQuery
>({
  initialQuery: {
    current: 1,
    size: 10,
    keyword: '',
    status: undefined
  },
  fetcher: (params) =>
    pageForumPostsApi({
      current: params.current,
      size: params.size,
      keyword: params.keyword || undefined,
      status: params.status || undefined,
      onlyApproved: false
    })
})

const { selectedIds, handleSelectionChange, clearSelection } = useSelectionIds<PostModel>()
const categories = ref<ForumCategoryModel[]>([])
const visible = ref(false)
const coverUploading = ref(false)
const form = reactive<Partial<PostModel>>({
  id: undefined,
  title: '',
  coverImage: '',
  summary: '',
  categoryId: undefined,
  content: '',
  status: 'PENDING',
  auditReason: ''
})

const beforeCoverUpload: UploadProps['beforeUpload'] = (rawFile) => validateImageFile(rawFile)

function categoryName(categoryId?: number) {
  const category = categories.value.find((item) => item.id === categoryId)
  return category?.name || (categoryId ? `分类#${categoryId}` : '-')
}

function authorLabel(row: PostModel) {
  return row.userName || row.realName || (row.userId ? `志愿者#${row.userId}` : '-')
}

async function handleCoverUpload(option: UploadRequestOptions) {
  coverUploading.value = true
  try {
    const file = option.file as File
    const res = await uploadImageApi(file)
    form.coverImage = res.url
    ElMessage.success('封面上传成功')
    option.onSuccess?.(res)
  } catch (error) {
    option.onError?.(error as never)
  } finally {
    coverUploading.value = false
  }
}

function openEdit(row: PostModel) {
  Object.assign(form, {
    id: row.id,
    title: row.title,
    coverImage: row.coverImage || '',
    summary: row.summary || '',
    categoryId: row.categoryId,
    content: row.content || '',
    status: row.status || 'PENDING',
    auditReason: row.auditReason || ''
  })
  visible.value = true
}

async function search() {
  clearSelection()
  await load({ current: 1 })
}

function resetQuery() {
  clearSelection()
  void reset({
    keyword: '',
    status: undefined
  })
}

async function submitEdit() {
  form.title = (form.title || '').trim()
  form.summary = (form.summary || '').trim()
  form.auditReason = (form.auditReason || '').trim()
  form.content = sanitizeRichText(form.content)

  if (!form.id) {
    return
  }
  if (!form.title) {
    ElMessage.warning('请输入帖子标题')
    return
  }
  if (!form.categoryId) {
    ElMessage.warning('请选择帖子分类')
    return
  }
  if (!form.coverImage) {
    ElMessage.warning('请上传帖子封面')
    return
  }
  if (!form.summary) {
    ElMessage.warning('请输入帖子简介')
    return
  }
  if (!richTextToPlainText(form.content)) {
    ElMessage.warning('请输入帖子内容')
    return
  }
  if (form.status === 'REJECTED' && !form.auditReason) {
    ElMessage.warning('帖子状态为已拒绝时，请填写拒绝原因')
    return
  }

  await updateForumPostApi(form.id, form)
  visible.value = false
  ElMessage.success('帖子修改成功')
  await load()
}

async function audit(id: number, status: string) {
  await runConfirmedAction({
    message: status === 'APPROVED' ? '确认通过该帖子审核吗？' : '请输入拒绝原因',
    title: '帖子审核',
    type: status === 'APPROVED' ? 'success' : 'warning',
    confirmButtonText: status === 'APPROVED' ? '确认通过' : '确认拒绝',
    prompt:
      status === 'REJECTED'
        ? {
            message: '请输入拒绝原因',
            inputPlaceholder: '拒绝原因必填',
            inputType: 'textarea',
            inputValidator: (value) => (value.trim() ? true : '请输入拒绝原因')
          }
        : undefined,
    action: (reason) =>
      auditPostApi(id, {
        status,
        reason: status === 'REJECTED' ? (reason || '').trim() : ''
      }),
    successMessage: status === 'APPROVED' ? '帖子已通过审核' : '帖子已拒绝',
    afterSuccess: () => load()
  })
}

async function archivePost(id: number) {
  await runConfirmedAction({
    message: '确认归档该帖子吗？归档后门户端不再展示，但评论与审核历史会保留。',
    title: '归档帖子',
    type: 'warning',
    confirmButtonText: '确认归档',
    action: () => deleteForumPostApi(id),
    successMessage: '帖子已归档',
    afterSuccess: () => load()
  })
}

async function batchArchive() {
  if (!selectedIds.value.length) {
    return
  }
  await runConfirmedAction({
    message: `确认批量归档 ${selectedIds.value.length} 条帖子吗？归档后门户端不再展示，但评论与审核历史会保留。`,
    title: '批量归档帖子',
    type: 'warning',
    confirmButtonText: '确认归档',
    action: () => batchDeleteForumPostsApi(selectedIds.value),
    successMessage: '帖子已批量归档',
    afterSuccess: async () => {
      clearSelection()
      await load()
    }
  })
}

async function onPageChange(page: number) {
  clearSelection()
  await handlePage(page)
}

async function onSizeChange(size: number) {
  clearSelection()
  await handleSizeChange(size)
}

onMounted(async () => {
  categories.value = await listManageForumCategoriesApi()
  await load()
})
</script>

<style scoped>
.cover-mini {
  width: 62px;
  height: 42px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid var(--cvs-border);
}

.action-cell {
  display: flex;
  align-items: center;
  gap: 4px;
}

.dialog-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.span-2 {
  grid-column: span 2;
}

.uploader {
  display: grid;
  grid-template-columns: 160px 1fr;
  gap: 12px;
  align-items: center;
}

.cover-preview {
  width: 160px;
  height: 100px;
  border-radius: 12px;
  border: 1px solid var(--cvs-border);
  object-fit: cover;
}

.uploader-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.field-tip {
  display: block;
  margin-top: 8px;
  color: var(--cvs-text-sub);
  font-size: 12px;
  line-height: 1.6;
}

:deep(.manage-dialog .el-dialog__body) {
  max-height: calc(100vh - 260px);
  overflow: auto;
}

@media (max-width: 900px) {
  .dialog-grid {
    grid-template-columns: 1fr;
  }

  .span-2 {
    grid-column: span 1;
  }

  .uploader {
    grid-template-columns: 1fr;
  }
}
</style>
