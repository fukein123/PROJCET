<template>
  <div class="fade-up">
    <el-card class="module" shadow="never">
      <template #header>
        <div class="head">
          <span>我的收藏</span>
          <div class="actions">
            <el-button type="danger" plain :disabled="!selectedIds.length" @click="batchRemove">批量删除</el-button>
            <el-button type="primary" @click="openCreate">新增收藏</el-button>
          </div>
        </div>
      </template>
      <el-table :data="list" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="48" />
        <el-table-column prop="activityId" label="活动ID" width="100" />
        <el-table-column label="活动名称" min-width="180">
          <template #default="{ row }">
            {{ activityLabel(row.activityId) }}
          </template>
        </el-table-column>
        <el-table-column prop="tag" label="标签" width="120" />
        <el-table-column prop="priority" label="优先级" width="100" />
        <el-table-column prop="note" label="备注" min-width="220" />
        <el-table-column prop="createTime" label="收藏时间" min-width="180" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="removeOne(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="footer">
        <el-pagination
          layout="total, prev, pager, next"
          :total="total"
          :current-page="query.current"
          :page-size="query.size"
          @current-change="handlePage"
        />
      </div>
    </el-card>

    <el-dialog v-model="visible" :title="editingId ? '编辑收藏' : '新增收藏'" width="560px" append-to-body>
      <el-form :model="form" label-position="top">
        <el-form-item label="活动">
          <el-select v-model="form.activityId" :disabled="Boolean(editingId)" filterable>
            <el-option v-for="item in activities" :key="item.id" :label="item.title" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.tag" placeholder="例如：周末、亲子、环保" />
        </el-form-item>
        <el-form-item label="优先级（0-5）">
          <el-input-number v-model="form.priority" :min="0" :max="5" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.note" type="textarea" rows="4" placeholder="记录你收藏该活动的原因" />
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  batchDeleteFavoritesApi,
  createFavoriteApi,
  pageFavoritesApi,
  removeFavoriteByIdApi,
  updateFavoriteApi,
  type FavoriteModel
} from '@/api/content'
import { pageActivitiesApi, type ActivityModel } from '@/api/activity'

const list = ref<FavoriteModel[]>([])
const total = ref(0)
const selectedIds = ref<number[]>([])
const activities = ref<ActivityModel[]>([])
const query = reactive({
  current: 1,
  size: 10
})

const visible = ref(false)
const editingId = ref<number>()
const form = reactive<{
  activityId?: number
  note: string
  tag: string
  priority: number
}>({
  activityId: undefined,
  note: '',
  tag: '',
  priority: 0
})

function handleSelectionChange(rows: FavoriteModel[]) {
  selectedIds.value = rows.map((item) => item.id)
}

function activityLabel(activityId: number) {
  return activities.value.find((item) => item.id === activityId)?.title || `活动#${activityId}`
}

async function loadActivities() {
  const res = await pageActivitiesApi({ current: 1, size: 200 })
  activities.value = res.records
}

async function load() {
  const res = await pageFavoritesApi(query)
  list.value = res.records
  total.value = res.total
}

function handlePage(page: number) {
  query.current = page
  load()
}

function openCreate() {
  editingId.value = undefined
  form.activityId = undefined
  form.note = ''
  form.tag = ''
  form.priority = 0
  visible.value = true
}

function openEdit(row: FavoriteModel) {
  editingId.value = row.id
  form.activityId = row.activityId
  form.note = row.note || ''
  form.tag = row.tag || ''
  form.priority = row.priority || 0
  visible.value = true
}

async function submit() {
  if (!form.activityId) {
    ElMessage.warning('请先选择活动')
    return
  }

  if (editingId.value) {
    await updateFavoriteApi(editingId.value, {
      note: form.note,
      tag: form.tag,
      priority: form.priority
    })
    ElMessage.success('收藏更新成功')
  } else {
    await createFavoriteApi({
      activityId: form.activityId,
      note: form.note,
      tag: form.tag,
      priority: form.priority
    })
    ElMessage.success('收藏新增成功')
  }
  visible.value = false
  await load()
}

async function removeOne(id: number) {
  await ElMessageBox.confirm('确认删除该收藏记录？', '删除收藏', { type: 'warning' })
  await removeFavoriteByIdApi(id)
  ElMessage.success('删除成功')
  await load()
}

async function batchRemove() {
  if (!selectedIds.value.length) return
  await ElMessageBox.confirm(`确认批量删除 ${selectedIds.value.length} 条收藏记录？`, '批量删除收藏', {
    type: 'warning'
  })
  await batchDeleteFavoritesApi(selectedIds.value)
  selectedIds.value = []
  ElMessage.success('批量删除成功')
  await load()
}

onMounted(async () => {
  await Promise.all([loadActivities(), load()])
})
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

.actions {
  display: flex;
  gap: 8px;
}

.footer {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
