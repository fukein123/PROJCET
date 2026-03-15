<template>
  <AdminContentSection
    title="信息动态"
    description="统一维护平台动态与热点资讯，支持归档下线与恢复发布，不做物理删除。"
    :loading="loading && !records.length"
    :empty="!loading && !records.length"
    loading-title="正在加载动态列表"
    loading-description="请稍候，系统正在同步最新动态内容。"
    empty-title="当前暂无动态"
    empty-description="可以先发布一条新的信息动态。"
  >
    <template #actions>
      <el-button type="warning" plain :disabled="!selectedIds.length" @click="batchArchive">批量归档</el-button>
      <el-button plain :disabled="!selectedIds.length" @click="batchRestore">恢复发布</el-button>
      <el-button type="primary" @click="openCreate">新增动态</el-button>
    </template>

    <template #emptyActions>
      <el-button type="primary" @click="openCreate">立即新增动态</el-button>
    </template>

    <SearchForm @search="search" @reset="resetQuery">
      <el-form-item label="动态类型">
        <el-select v-model="query.type" clearable style="width: 180px">
          <el-option label="社区新闻" value="NEWS" />
          <el-option label="活动动态" value="DYNAMIC" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model.trim="query.keyword" placeholder="标题 / 来源" />
      </el-form-item>
    </SearchForm>

    <el-table :data="records" border v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" />
      <el-table-column label="图片" width="90">
        <template #default="{ row }">
          <img class="thumb" :src="row.imageUrl || dynamicFallback" alt="动态配图" />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="source" label="来源" min-width="160" />
      <el-table-column prop="type" label="类型" width="120" />
      <el-table-column prop="views" label="浏览量" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '已发布' : '已归档' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="publishTime" label="发布时间" min-width="180" />
      <el-table-column label="操作" width="156" fixed="right">
        <template #default="{ row }">
          <div class="action-cell">
            <el-tooltip content="编辑动态" placement="top">
              <el-button link type="primary" :icon="EditPen" @click="openEdit(row)" />
            </el-tooltip>
            <el-tooltip v-if="row.status === 1" content="归档动态" placement="top">
              <el-button link type="warning" :icon="Delete" @click="archiveOne(row.id)" />
            </el-tooltip>
            <el-tooltip v-else content="恢复发布" placement="top">
              <el-button link type="success" :icon="RefreshRight" @click="restoreOne(row.id)" />
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
    :title="form.id ? '编辑动态' : '新增动态'"
    width="min(92vw, 760px)"
    top="5vh"
    append-to-body
    class="manage-dialog"
  >
    <el-form :model="form" label-position="top">
      <el-form-item label="标题">
        <el-input v-model="form.title" />
      </el-form-item>
      <el-form-item label="来源">
        <el-input v-model="form.source" placeholder="如：社区服务中心、平台发布" />
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="form.type">
          <el-option label="社区新闻" value="NEWS" />
          <el-option label="活动动态" value="DYNAMIC" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-switch v-model="statusSwitch" />
      </el-form-item>
      <el-form-item label="图片">
        <div class="uploader">
          <img class="thumb-lg" :src="form.imageUrl || dynamicFallback" alt="动态配图" />
          <el-upload
            :show-file-list="false"
            :http-request="handleImageUpload"
            :before-upload="beforeImageUpload"
            accept="image/png,image/jpeg,image/jpg,image/webp,image/gif"
          >
            <el-button :loading="imageUploading">上传图片</el-button>
          </el-upload>
        </div>
      </el-form-item>
      <el-form-item label="内容">
        <RichTextEditor v-model="form.content" placeholder="请输入动态正文内容" />
        <span class="field-tip">支持基础标题、列表、引用等富文本格式，提交前会自动进行安全清洗。</span>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import type { UploadProps, UploadRequestOptions } from 'element-plus'
import { ElMessage } from 'element-plus'
import { Delete, EditPen, RefreshRight } from '@element-plus/icons-vue'
import { uploadImageApi } from '@/api/common'
import {
  batchArchiveDynamicsApi,
  batchRestoreDynamicsApi,
  deleteDynamicApi,
  pageDynamicsApi,
  saveDynamicApi,
  updateDynamicApi,
  type DynamicModel
} from '@/api/content'
import AdminContentSection from '@/components/admin/AdminContentSection.vue'
import SearchForm from '@/components/SearchForm.vue'
import RichTextEditor from '@/components/shared/RichTextEditor.vue'
import { useSelectionIds } from '@/composables/useSelectionIds'
import { useTable } from '@/composables/useTable'
import { runConfirmedAction } from '@/utils/confirmed-action'
import { richTextToPlainText, sanitizeRichText } from '@/utils/rich-text'
import { validateImageFile } from '@/utils/upload'

interface DynamicQuery {
  current: number
  size: number
  type?: string
  keyword: string
}

const dynamicFallback =
  'https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?auto=format&fit=crop&w=900&q=80'
const pageSizes = [10, 20, 30, 50]

const { loading, records, total, query, load, handlePage, handleSizeChange, reset } = useTable<
  DynamicModel,
  DynamicQuery
>({
  initialQuery: {
    current: 1,
    size: 10,
    type: undefined,
    keyword: ''
  },
  fetcher: (params) =>
    pageDynamicsApi({
      current: params.current,
      size: params.size,
      type: params.type || undefined,
      keyword: params.keyword || undefined,
      onlyPublished: false
    })
})

const { selectedIds, handleSelectionChange, clearSelection } = useSelectionIds<DynamicModel>()
const visible = ref(false)
const imageUploading = ref(false)
const form = reactive<Partial<DynamicModel>>({
  id: undefined,
  title: '',
  source: '',
  content: '',
  imageUrl: '',
  type: 'NEWS',
  status: 1
})

const statusSwitch = computed({
  get: () => form.status === 1,
  set: (value: boolean) => {
    form.status = value ? 1 : 0
  }
})

const beforeImageUpload: UploadProps['beforeUpload'] = (rawFile) => validateImageFile(rawFile)

async function handleImageUpload(option: UploadRequestOptions) {
  imageUploading.value = true
  try {
    const file = option.file as File
    const res = await uploadImageApi(file)
    form.imageUrl = res.url
    ElMessage.success('图片上传成功')
    option.onSuccess?.(res)
  } catch (error) {
    option.onError?.(error as never)
  } finally {
    imageUploading.value = false
  }
}

function resetForm() {
  Object.assign(form, {
    id: undefined,
    title: '',
    source: '',
    content: '',
    imageUrl: '',
    type: 'NEWS',
    status: 1
  })
}

function openCreate() {
  resetForm()
  visible.value = true
}

function openEdit(row: DynamicModel) {
  Object.assign(form, row)
  visible.value = true
}

async function search() {
  clearSelection()
  await load({ current: 1 })
}

function resetQuery() {
  clearSelection()
  void reset({
    type: undefined,
    keyword: ''
  })
}

async function submit() {
  form.title = (form.title || '').trim()
  form.source = (form.source || '').trim()
  form.content = sanitizeRichText(form.content)
  if (!form.title) {
    ElMessage.warning('请输入动态标题')
    return
  }
  if (!form.source) {
    ElMessage.warning('请输入动态来源')
    return
  }
  if (!richTextToPlainText(form.content)) {
    ElMessage.warning('请输入动态内容')
    return
  }
  if (form.id) {
    await updateDynamicApi(form.id, form)
  } else {
    await saveDynamicApi(form)
  }
  visible.value = false
  ElMessage.success('动态保存成功')
  await load()
}

async function archiveOne(id: number) {
  await runConfirmedAction({
    message: '确认归档该动态吗？归档后门户端不再展示，但历史浏览统计会保留。',
    title: '归档动态',
    type: 'warning',
    confirmButtonText: '确认归档',
    action: () => deleteDynamicApi(id),
    successMessage: '动态已归档',
    afterSuccess: () => load()
  })
}

async function restoreOne(id: number) {
  await runConfirmedAction({
    message: '确认恢复发布该动态吗？恢复后门户端可再次查看。',
    title: '恢复发布动态',
    action: () => batchRestoreDynamicsApi([id]),
    successMessage: '动态已恢复发布',
    afterSuccess: () => load()
  })
}

async function batchArchive() {
  if (!selectedIds.value.length) {
    return
  }
  await runConfirmedAction({
    message: `确认批量归档 ${selectedIds.value.length} 条动态吗？归档后门户端不再展示，但历史浏览统计会保留。`,
    title: '批量归档动态',
    type: 'warning',
    confirmButtonText: '确认归档',
    action: () => batchArchiveDynamicsApi(selectedIds.value),
    successMessage: '动态已批量归档',
    afterSuccess: async () => {
      clearSelection()
      await load()
    }
  })
}

async function batchRestore() {
  if (!selectedIds.value.length) {
    return
  }
  await runConfirmedAction({
    message: `确认恢复发布 ${selectedIds.value.length} 条动态吗？`,
    title: '恢复发布动态',
    action: () => batchRestoreDynamicsApi(selectedIds.value),
    successMessage: '动态已批量恢复发布',
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

void load()
</script>

<style scoped>
.thumb {
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

.uploader {
  display: flex;
  gap: 12px;
  align-items: center;
}

.thumb-lg {
  width: 160px;
  height: 94px;
  border-radius: 10px;
  object-fit: cover;
  border: 1px solid var(--cvs-border);
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
</style>
