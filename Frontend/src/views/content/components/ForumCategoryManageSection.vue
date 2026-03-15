<template>
  <AdminContentSection
    title="帖子分类"
    description="维护社区论坛帖子分类、排序与启停状态，支持单条删除与批量删除。"
    :loading="loading && !records.length"
    :empty="!loading && !records.length"
    loading-title="正在加载帖子分类"
    loading-description="请稍候，系统正在同步论坛分类配置。"
    empty-title="当前暂无帖子分类"
    empty-description="新增分类后，前台发帖与筛选都会同步生效。"
  >
    <template #actions>
      <el-button type="danger" plain :disabled="!selectedIds.length" @click="batchDelete">批量删除</el-button>
      <el-button type="primary" @click="openCreate">新增帖子分类</el-button>
    </template>

    <template #emptyActions>
      <el-button type="primary" @click="openCreate">立即新增帖子分类</el-button>
    </template>

    <SearchForm @search="load" @reset="resetQuery">
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable style="width: 160px">
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model.trim="query.keyword" placeholder="分类名称" />
      </el-form-item>
    </SearchForm>

    <el-table :data="filteredRecords" border v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" />
      <el-table-column prop="name" label="分类名称" min-width="220" />
      <el-table-column prop="sort" label="排序" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="170" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <div class="action-cell">
            <el-tooltip content="编辑帖子分类" placement="top">
              <el-button link type="primary" :icon="EditPen" @click="openEdit(row)" />
            </el-tooltip>
            <el-tooltip content="删除帖子分类" placement="top">
              <el-button link type="danger" :icon="Delete" @click="remove(row.id)" />
            </el-tooltip>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </AdminContentSection>

  <el-dialog
    v-model="visible"
    :title="form.id ? '编辑帖子分类' : '新增帖子分类'"
    width="min(92vw, 560px)"
    top="10vh"
    append-to-body
    class="manage-dialog"
  >
    <el-form :model="form" label-position="top">
      <el-form-item label="分类名称" required>
        <el-input v-model.trim="form.name" maxlength="64" show-word-limit placeholder="请输入帖子分类名称" />
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sort" :min="0" />
      </el-form-item>
      <el-form-item label="状态">
        <el-switch v-model="statusSwitch" />
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
import { Delete, EditPen } from '@element-plus/icons-vue'
import {
  batchDeleteForumCategoriesApi,
  deleteForumCategoryApi,
  listManageForumCategoriesApi,
  saveForumCategoryApi,
  updateForumCategoryApi,
  type ForumCategoryModel
} from '@/api/content'
import SearchForm from '@/components/SearchForm.vue'
import AdminContentSection from '@/components/admin/AdminContentSection.vue'
import { useSelectionIds } from '@/composables/useSelectionIds'
import { runConfirmedAction } from '@/utils/confirmed-action'

interface ForumCategoryQuery {
  keyword: string
  status?: number
}

const loading = ref(false)
const records = ref<ForumCategoryModel[]>([])
const { selectedIds, handleSelectionChange, clearSelection } = useSelectionIds<ForumCategoryModel>()
const visible = ref(false)
const query = reactive<ForumCategoryQuery>({
  keyword: '',
  status: undefined
})

const form = reactive<Partial<ForumCategoryModel>>({
  id: undefined,
  name: '',
  sort: 0,
  status: 1
})

const statusSwitch = computed({
  get: () => form.status === 1,
  set: (value: boolean) => {
    form.status = value ? 1 : 0
  }
})

const filteredRecords = computed(() => {
  const keyword = query.keyword.trim().toLowerCase()

  return records.value.filter((item) => {
    if (typeof query.status === 'number' && item.status !== query.status) {
      return false
    }

    if (!keyword) {
      return true
    }

    return item.name.toLowerCase().includes(keyword)
  })
})

function resetQuery() {
  clearSelection()
  query.keyword = ''
  query.status = undefined
}

async function load() {
  clearSelection()
  loading.value = true
  try {
    records.value = await listManageForumCategoriesApi()
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, {
    id: undefined,
    name: '',
    sort: 0,
    status: 1
  })
}

function openCreate() {
  resetForm()
  visible.value = true
}

function openEdit(row: ForumCategoryModel) {
  Object.assign(form, row)
  visible.value = true
}

async function submit() {
  form.name = (form.name || '').trim()
  form.sort = Math.max(0, Number(form.sort || 0))

  if (!form.name) {
    ElMessage.warning('请输入帖子分类名称')
    return
  }

  const payload = {
    ...form,
    sort: Number(form.sort || 0)
  }

  if (form.id) {
    await updateForumCategoryApi(form.id, payload)
  } else {
    await saveForumCategoryApi(payload)
  }

  visible.value = false
  ElMessage.success('帖子分类保存成功')
  await load()
}

async function remove(id: number) {
  await runConfirmedAction({
    message: '确认删除该帖子分类吗？若分类下仍有关联帖子，系统会拒绝删除。',
    title: '删除帖子分类',
    type: 'warning',
    action: () => deleteForumCategoryApi(id),
    successMessage: '帖子分类已删除',
    afterSuccess: () => load()
  })
}

async function batchDelete() {
  if (!selectedIds.value.length) {
    return
  }

  await runConfirmedAction({
    message: `确认批量删除 ${selectedIds.value.length} 个帖子分类吗？若仍存在关联帖子，系统会直接拒绝。`,
    title: '批量删除帖子分类',
    type: 'warning',
    action: () => batchDeleteForumCategoriesApi(selectedIds.value),
    successMessage: '帖子分类已批量删除',
    afterSuccess: async () => {
      clearSelection()
      await load()
    }
  })
}

void load()
</script>

<style scoped>
.action-cell {
  display: flex;
  align-items: center;
  gap: 4px;
}

:deep(.manage-dialog .el-dialog__body) {
  max-height: calc(100vh - 260px);
  overflow: auto;
}
</style>
