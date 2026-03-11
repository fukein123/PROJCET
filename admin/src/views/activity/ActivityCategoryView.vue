<template>
  <div class="fade-up">
    <el-card class="module" shadow="never">
      <template #header>
        <div class="head">
          <span>活动分类管理</span>
          <el-button type="primary" @click="openCreate">新增分类</el-button>
        </div>
      </template>
      <el-table :data="list" border>
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="sort" label="排序" width="90" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

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
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { createCategoryApi, listCategoriesApi, updateCategoryApi, type ActivityCategory } from '@/api/activity'

const list = ref<ActivityCategory[]>([])
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
  } else {
    await createCategoryApi(form)
  }
  visible.value = false
  await load()
}

onMounted(load)
</script>

<style scoped>
.module {
  border: 1px solid var(--cvs-border);
  border-radius: 16px;
}

.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
