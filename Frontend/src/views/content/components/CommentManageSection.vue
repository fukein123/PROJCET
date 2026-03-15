<template>
  <AdminContentSection
    title="评论信息"
    description="统一查看活动与帖子评论，支持单条删除和批量删除，测试数据与业务数据会明确区分。"
    :loading="loading && !records.length"
    :empty="!loading && !records.length"
    loading-title="正在加载评论信息"
    loading-description="请稍候，系统正在同步评论列表。"
    empty-title="当前暂无评论信息"
    empty-description="活动或论坛产生评论后，会在这里展示。"
  >
    <template #actions>
      <el-button type="danger" plain :disabled="!selectedIds.length" @click="batchDelete">批量删除</el-button>
    </template>

    <SearchForm @search="search" @reset="resetQuery">
      <el-form-item label="所属模块">
        <el-select v-model="query.targetType" clearable style="width: 180px">
          <el-option label="论坛帖子" value="POST" />
          <el-option label="活动评论" value="ACTIVITY" />
          <el-option label="信息动态" value="DYNAMIC" />
          <el-option label="公告评论" value="NOTICE" />
        </el-select>
      </el-form-item>
      <el-form-item label="关联 ID">
        <el-input v-model="query.targetIdText" inputmode="numeric" placeholder="按内容 ID 精确筛选" />
      </el-form-item>
      <el-form-item label="数据范围">
        <el-select v-model="query.includeTestData" style="width: 180px">
          <el-option label="业务 + 测试数据" :value="true" />
          <el-option label="仅业务数据" :value="false" />
        </el-select>
      </el-form-item>
    </SearchForm>

    <el-table :data="records" border v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" />
      <el-table-column label="所属模块" width="120">
        <template #default="{ row }">
          {{ getCommentTargetLabel(row.targetType) }}
        </template>
      </el-table-column>
      <el-table-column prop="targetId" label="关联ID" width="120" />
      <el-table-column prop="userId" label="志愿者ID" width="120" />
      <el-table-column label="数据类型" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.testDataTag" type="warning">测试数据</el-tag>
          <span v-else>业务数据</span>
        </template>
      </el-table-column>
      <el-table-column label="评论内容" min-width="280" show-overflow-tooltip>
        <template #default="{ row }">
          {{ commentPreview(row.content) }}
        </template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="170">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <div class="action-cell">
            <el-tooltip content="删除评论" placement="top">
              <el-button link type="danger" :icon="Delete" @click="remove(row.id)" />
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
</template>

<script setup lang="ts">
import { Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  batchDeleteCommentsApi,
  deleteCommentApi,
  pageCommentsApi,
  type CommentModel
} from '@/api/content'
import SearchForm from '@/components/SearchForm.vue'
import AdminContentSection from '@/components/admin/AdminContentSection.vue'
import { useSelectionIds } from '@/composables/useSelectionIds'
import { useTable } from '@/composables/useTable'
import { runConfirmedAction } from '@/utils/confirmed-action'
import { formatDateTime, getCommentTargetLabel } from '@/utils/display'
import { richTextToPlainText } from '@/utils/rich-text'

interface CommentQuery {
  current: number
  size: number
  targetType?: string
  targetIdText: string
  includeTestData: boolean
}

const pageSizes = [10, 20, 30, 50]

const { loading, records, total, query, load, handlePage, handleSizeChange, reset } = useTable<
  CommentModel,
  CommentQuery
>({
  initialQuery: {
    current: 1,
    size: 10,
    targetType: undefined,
    targetIdText: '',
    includeTestData: true
  },
  fetcher: (params) =>
    pageCommentsApi({
      current: params.current,
      size: params.size,
      targetType: params.targetType || undefined,
      targetId: normalizeTargetId(params.targetIdText),
      includeTestData: params.includeTestData
    })
})

const { selectedIds, handleSelectionChange, clearSelection } = useSelectionIds<CommentModel>()

function normalizeTargetId(value: string) {
  const trimmed = value.trim()
  if (!trimmed) {
    return undefined
  }

  const parsed = Number(trimmed)
  return Number.isInteger(parsed) && parsed > 0 ? parsed : undefined
}

function commentPreview(content?: string) {
  return richTextToPlainText(content) || '图片或富文本评论'
}

async function search() {
  if (query.targetIdText.trim() && !query.targetType) {
    ElMessage.warning('按关联 ID 筛选评论时，请先选择所属模块')
    return
  }
  clearSelection()
  await load({ current: 1 })
}

function resetQuery() {
  clearSelection()
  void reset({
    targetType: undefined,
    targetIdText: '',
    includeTestData: true
  })
}

async function remove(id: number) {
  await runConfirmedAction({
    message: '确认删除这条评论吗？',
    title: '删除评论',
    type: 'warning',
    action: () => deleteCommentApi(id),
    successMessage: '评论已删除',
    afterSuccess: () => load()
  })
}

async function batchDelete() {
  if (!selectedIds.value.length) {
    return
  }

  await runConfirmedAction({
    message: `确认批量删除 ${selectedIds.value.length} 条评论吗？`,
    title: '批量删除评论',
    type: 'warning',
    action: () => batchDeleteCommentsApi(selectedIds.value),
    successMessage: '评论已批量删除',
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
</style>
