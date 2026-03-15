<template>
  <AdminListScaffold title="活动分类管理" description="维护志愿活动分类、排序与启停状态，支持单条删除与批量删除。">
    <template #actions>
      <el-button type="danger" plain :disabled="!selectedIds.length" @click="batchRemove">批量删除</el-button>
      <el-button type="primary" @click="openCreate">新增分类</el-button>
    </template>

    <el-table :data="list" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" />
      <el-table-column prop="name" label="分类名称" min-width="180" />
      <el-table-column prop="description" label="分类说明" min-width="220" show-overflow-tooltip />
      <el-table-column prop="sort" label="排序" width="90" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <div class="action-cell">
            <el-tooltip content="编辑分类" placement="top">
              <el-button link type="primary" :icon="EditPen" @click="openEdit(row)" />
            </el-tooltip>
            <el-tooltip content="删除分类" placement="top">
              <el-button link type="danger" :icon="Delete" @click="removeOne(row.id)" />
            </el-tooltip>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </AdminListScaffold>

  <el-dialog
    v-model="visible"
    :title="editingId ? '编辑活动分类' : '新增活动分类'"
    width="min(92vw, 560px)"
    top="10vh"
    append-to-body
  >
    <el-form :model="form" label-position="top">
      <el-form-item label="分类名称" required>
        <el-input v-model.trim="form.name" maxlength="64" show-word-limit placeholder="请输入分类名称" />
      </el-form-item>
      <el-form-item label="分类说明">
        <el-input
          v-model.trim="form.description"
          type="textarea"
          :rows="3"
          maxlength="200"
          show-word-limit
          placeholder="可选，补充分类用途或适用场景"
        />
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete, EditPen } from '@element-plus/icons-vue'
import AdminListScaffold from '@/components/admin/AdminListScaffold.vue'
import {
  batchDeleteCategoriesApi,
  createCategoryApi,
  deleteCategoryApi,
  listCategoriesApi,
  updateCategoryApi,
  type ActivityCategory
} from '@/api/activity'
import { useSelectionIds } from '@/composables/useSelectionIds'
import { runConfirmedAction } from '@/utils/confirmed-action'

const list = ref<ActivityCategory[]>([])
const { selectedIds, handleSelectionChange, clearSelection } = useSelectionIds<ActivityCategory>()
const visible = ref(false)
const editingId = ref<number>()

const form = reactive<Partial<ActivityCategory>>({
  name: '',
  description: '',
  sort: 0,
  status: 1
})

const statusSwitch = computed({
  get: () => form.status === 1,
  set: (value: boolean) => {
    form.status = value ? 1 : 0
  }
})

async function load() {
  list.value = await listCategoriesApi()
}

function resetForm() {
  Object.assign(form, {
    name: '',
    description: '',
    sort: 0,
    status: 1
  })
}

function openCreate() {
  editingId.value = undefined
  resetForm()
  visible.value = true
}

function openEdit(row: ActivityCategory) {
  editingId.value = row.id
  Object.assign(form, row)
  visible.value = true
}

async function submit() {
  form.name = (form.name || '').trim()
  form.description = (form.description || '').trim()
  form.sort = Math.max(0, Number(form.sort || 0))

  if (!form.name) {
    ElMessage.warning('请输入分类名称')
    return
  }

  const payload = {
    ...form,
    sort: Number(form.sort || 0)
  }

  if (editingId.value) {
    await updateCategoryApi(editingId.value, payload)
    ElMessage.success('活动分类已更新')
  } else {
    await createCategoryApi(payload)
    ElMessage.success('活动分类已创建')
  }

  visible.value = false
  await load()
}

async function removeOne(id: number) {
  await runConfirmedAction({
    message: '删除后将无法恢复，确认继续吗？',
    title: '删除活动分类',
    type: 'warning',
    action: () => deleteCategoryApi(id),
    successMessage: '活动分类已删除',
    afterSuccess: load
  })
}

async function batchRemove() {
  if (!selectedIds.value.length) {
    return
  }

  await runConfirmedAction({
    message: `确认批量删除 ${selectedIds.value.length} 个活动分类吗？`,
    title: '批量删除活动分类',
    type: 'warning',
    action: () => batchDeleteCategoriesApi(selectedIds.value),
    successMessage: '活动分类已批量删除',
    afterSuccess: async () => {
      clearSelection()
      await load()
    }
  })
}

onMounted(load)
</script>

<style scoped>
.action-cell {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
