<template>
  <AdminListScaffold title="活动主题分类管理">
    <template #actions>
      <el-button type="danger" plain :disabled="!selectedIds.length" @click="batchRemove">批量删除</el-button>
      <el-button type="primary" @click="openCreate">新增分类</el-button>
    </template>

    <el-table :data="list" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" />
      <el-table-column prop="name" label="分类名称" min-width="180" />
      <el-table-column prop="description" label="描述" min-width="220" />
      <el-table-column prop="sort" label="排序" width="90" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="170">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="removeOne(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </AdminListScaffold>

  <el-dialog v-model="visible" :title="editingId ? '编辑分类' : '新增分类'" width="520px" append-to-body>
    <el-form :model="form" label-width="88px">
      <el-form-item label="名称">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" />
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

function openCreate() {
  editingId.value = undefined
  form.name = ''
  form.description = ''
  form.sort = 0
  form.status = 1
  visible.value = true
}

function openEdit(row: ActivityCategory) {
  editingId.value = row.id
  Object.assign(form, row)
  visible.value = true
}

async function submit() {
  if (editingId.value) {
    await updateCategoryApi(editingId.value, form)
    ElMessage.success('分类更新成功')
  } else {
    await createCategoryApi(form)
    ElMessage.success('分类创建成功')
  }
  visible.value = false
  await load()
}

async function removeOne(id: number) {
  await runConfirmedAction({
    message: '删除后不可恢复，确认继续？',
    title: '删除分类',
    action: () => deleteCategoryApi(id),
    successMessage: '删除成功',
    afterSuccess: load
  })
}

async function batchRemove() {
  if (!selectedIds.value.length) return

  await runConfirmedAction({
    message: `确认批量删除 ${selectedIds.value.length} 个分类？`,
    title: '批量删除分类',
    action: () => batchDeleteCategoriesApi(selectedIds.value),
    successMessage: '批量删除成功',
    afterSuccess: async () => {
      clearSelection()
      await load()
    }
  })
}

onMounted(load)
</script>