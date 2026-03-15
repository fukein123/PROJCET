<template>
  <AdminContentSection
    title="系统公告"
    description="统一维护门户公告，支持归档下线与恢复发布，不做物理删除。"
    :loading="loading && !records.length"
    :empty="!loading && !records.length"
    loading-title="正在加载公告列表"
    loading-description="请稍候，系统正在同步最新公告。"
    empty-title="当前暂无公告"
    empty-description="可以先发布一条新的系统公告。"
  >
    <template #actions>
      <el-button type="warning" plain :disabled="!selectedIds.length" @click="batchArchive">批量归档</el-button>
      <el-button plain :disabled="!selectedIds.length" @click="batchRestore">恢复发布</el-button>
      <el-button type="primary" @click="openCreate">新增公告</el-button>
    </template>

    <template #emptyActions>
      <el-button type="primary" @click="openCreate">立即新增公告</el-button>
    </template>

    <el-table :data="records" border v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" />
      <el-table-column prop="title" label="标题" min-width="220" />
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
            <el-tooltip content="编辑公告" placement="top">
              <el-button link type="primary" :icon="EditPen" @click="openEdit(row)" />
            </el-tooltip>
            <el-tooltip v-if="row.status === 1" content="归档公告" placement="top">
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
    :title="form.id ? '编辑公告' : '新增公告'"
    width="min(92vw, 720px)"
    top="5vh"
    append-to-body
    class="manage-dialog"
  >
    <el-form :model="form" label-position="top">
      <el-form-item label="标题">
        <el-input v-model="form.title" />
      </el-form-item>
      <el-form-item label="状态">
        <el-switch v-model="statusSwitch" />
      </el-form-item>
      <el-form-item label="内容">
        <RichTextEditor v-model="form.content" placeholder="请输入公告正文内容" />
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
import { ElMessage } from 'element-plus'
import { Delete, EditPen, RefreshRight } from '@element-plus/icons-vue'
import {
  batchArchiveNoticesApi,
  batchRestoreNoticesApi,
  deleteNoticeApi,
  pageNoticesApi,
  saveNoticeApi,
  updateNoticeApi,
  type NoticeModel
} from '@/api/content'
import AdminContentSection from '@/components/admin/AdminContentSection.vue'
import RichTextEditor from '@/components/shared/RichTextEditor.vue'
import { useSelectionIds } from '@/composables/useSelectionIds'
import { useTable, type TableQuery } from '@/composables/useTable'
import { runConfirmedAction } from '@/utils/confirmed-action'
import { richTextToPlainText, sanitizeRichText } from '@/utils/rich-text'

const pageSizes = [10, 20, 30, 50]

const { loading, records, total, query, load, handlePage, handleSizeChange } = useTable<NoticeModel, TableQuery>({
  initialQuery: {
    current: 1,
    size: 10
  },
  fetcher: (params) =>
    pageNoticesApi({
      current: params.current,
      size: params.size,
      onlyPublished: false
    })
})

const { selectedIds, handleSelectionChange, clearSelection } = useSelectionIds<NoticeModel>()
const visible = ref(false)
const form = reactive<Partial<NoticeModel>>({
  id: undefined,
  title: '',
  content: '',
  status: 1
})

const statusSwitch = computed({
  get: () => form.status === 1,
  set: (value: boolean) => {
    form.status = value ? 1 : 0
  }
})

function resetForm() {
  Object.assign(form, {
    id: undefined,
    title: '',
    content: '',
    status: 1
  })
}

function openCreate() {
  resetForm()
  visible.value = true
}

function openEdit(row: NoticeModel) {
  Object.assign(form, row)
  visible.value = true
}

async function submit() {
  form.title = (form.title || '').trim()
  form.content = sanitizeRichText(form.content)
  if (!form.title) {
    ElMessage.warning('请输入公告标题')
    return
  }
  if (!richTextToPlainText(form.content)) {
    ElMessage.warning('请输入公告内容')
    return
  }
  if (form.id) {
    await updateNoticeApi(form.id, form)
  } else {
    await saveNoticeApi(form)
  }
  visible.value = false
  ElMessage.success('公告保存成功')
  await load()
}

async function archiveOne(id: number) {
  await runConfirmedAction({
    message: '确认归档该公告吗？归档后门户端不再展示，但历史发布时间会保留。',
    title: '归档公告',
    type: 'warning',
    confirmButtonText: '确认归档',
    action: () => deleteNoticeApi(id),
    successMessage: '公告已归档',
    afterSuccess: () => load()
  })
}

async function restoreOne(id: number) {
  await runConfirmedAction({
    message: '确认恢复发布该公告吗？恢复后门户端可再次查看。',
    title: '恢复发布公告',
    action: () => batchRestoreNoticesApi([id]),
    successMessage: '公告已恢复发布',
    afterSuccess: () => load()
  })
}

async function batchArchive() {
  if (!selectedIds.value.length) {
    return
  }
  await runConfirmedAction({
    message: `确认批量归档 ${selectedIds.value.length} 条公告吗？归档后门户端不再展示，但历史发布时间会保留。`,
    title: '批量归档公告',
    type: 'warning',
    confirmButtonText: '确认归档',
    action: () => batchArchiveNoticesApi(selectedIds.value),
    successMessage: '公告已批量归档',
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
    message: `确认恢复发布 ${selectedIds.value.length} 条公告吗？`,
    title: '恢复发布公告',
    action: () => batchRestoreNoticesApi(selectedIds.value),
    successMessage: '公告已批量恢复发布',
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
.action-cell {
  display: flex;
  align-items: center;
  gap: 4px;
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
