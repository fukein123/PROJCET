<template>
  <div class="page-shell">
    <WorkspaceHero
      compact
      eyebrow="我的收藏"
      title="统一整理活动收藏、活动快照与个人备注"
      description="收藏记录会保留当时的活动标题、地点和时间快照，方便后续回看与再次报名。"
    >
      <template #actions>
        <el-button type="primary" @click="load">刷新收藏</el-button>
        <el-button @click="router.push('/volunteer/activity-center')">前往活动中心</el-button>
        <el-button @click="router.push('/volunteer/my-posts')">查看我的帖子</el-button>
      </template>
    </WorkspaceHero>

    <VolunteerPageSection eyebrow="收藏管理" title="我的收藏清单" description="统一查看收藏活动的历史快照、补充备注并执行批量清理。">
      <template #actions>
        <el-button type="danger" plain :disabled="!selectedIds.length" @click="batchRemove">批量删除</el-button>
        <el-button type="primary" @click="openCreate">新增收藏</el-button>
      </template>

      <StatePanel
        v-if="loading"
        state="loading"
        title="正在同步收藏记录"
        description="正在加载收藏活动、优先级和备注信息。"
      />

      <template v-else-if="list.length">
        <el-table :data="list" border @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="48" />
          <el-table-column prop="activityId" label="活动 ID" width="100" />
          <el-table-column label="活动名称" min-width="180">
            <template #default="{ row }">
              {{ favoriteTitleLabel(row) }}
            </template>
          </el-table-column>
          <el-table-column label="活动地点" min-width="180">
            <template #default="{ row }">
              {{ favoriteAddressLabel(row) }}
            </template>
          </el-table-column>
          <el-table-column label="活动时间" min-width="220">
            <template #default="{ row }">
              {{ favoriteScheduleLabel(row) }}
            </template>
          </el-table-column>
          <el-table-column prop="tag" label="标签" width="120" />
          <el-table-column prop="priority" label="优先级" width="100" />
          <el-table-column prop="note" label="备注" min-width="220" />
          <el-table-column label="收藏时间" min-width="180">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button link type="danger" @click="removeOne(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <StatePanel
        v-else
        title="暂无收藏活动"
        description="可以在活动中心把感兴趣的活动加入收藏，便于后续持续跟进。"
      />

      <template #footer>
        <el-pagination
          v-if="!loading && total > 0"
          layout="total, prev, pager, next"
          :total="total"
          :current-page="query.current"
          :page-size="query.size"
          @current-change="handlePage"
        />
      </template>
    </VolunteerPageSection>

    <el-dialog v-model="visible" :title="editingId ? '编辑收藏' : '新增收藏'" width="560px" append-to-body>
      <el-form :model="form" label-position="top" class="dialog-form">
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  batchDeleteFavoritesApi,
  createFavoriteApi,
  pageFavoritesApi,
  removeFavoriteByIdApi,
  updateFavoriteApi,
  type FavoriteModel
} from '@/api/content'
import { pageActivitiesApi, type ActivityModel } from '@/api/activity'
import StatePanel from '@/components/shared/StatePanel.vue'
import WorkspaceHero from '@/components/shared/WorkspaceHero.vue'
import VolunteerPageSection from '@/components/volunteer/VolunteerPageSection.vue'
import { useSelectionIds } from '@/composables/useSelectionIds'
import { useTable } from '@/composables/useTable'
import { formatDateTime } from '@/utils/display'
import { runConfirmedAction } from '@/utils/confirmed-action'

interface FavoriteQuery {
  current: number
  size: number
}

const router = useRouter()

const { loading, records: list, total, query, load, handlePage } = useTable<FavoriteModel, FavoriteQuery>({
  initialQuery: {
    current: 1,
    size: 10
  },
  fetcher: (params) => pageFavoritesApi(params)
})

const { selectedIds, handleSelectionChange, clearSelection } = useSelectionIds<FavoriteModel>()
const activities = ref<ActivityModel[]>([])

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

function activityLabel(activityId: number) {
  return activities.value.find((item) => item.id === activityId)?.title || `活动#${activityId}`
}

function currentActivity(activityId: number) {
  return activities.value.find((item) => item.id === activityId)
}

function favoriteTitleLabel(row: FavoriteModel) {
  return row.activityTitle || activityLabel(row.activityId)
}

function favoriteAddressLabel(row: FavoriteModel) {
  return row.activityAddress || currentActivity(row.activityId)?.address || '-'
}

function favoriteScheduleLabel(row: FavoriteModel) {
  const current = currentActivity(row.activityId)
  const startTime = row.activityStartTime || current?.startTime
  const endTime = row.activityEndTime || current?.endTime
  if (!startTime && !endTime) {
    return '-'
  }
  return `${formatDateTime(startTime)} - ${formatDateTime(endTime)}`
}

async function loadActivities() {
  const res = await pageActivitiesApi({ current: 1, size: 200 })
  activities.value = res.records
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
  await runConfirmedAction({
    message: '确认删除该收藏记录？',
    title: '删除收藏',
    action: () => removeFavoriteByIdApi(id),
    successMessage: '删除成功',
    afterSuccess: load
  })
}

async function batchRemove() {
  if (!selectedIds.value.length) return

  await runConfirmedAction({
    message: `确认批量删除 ${selectedIds.value.length} 条收藏记录？`,
    title: '批量删除收藏',
    action: () => batchDeleteFavoritesApi(selectedIds.value),
    successMessage: '批量删除成功',
    afterSuccess: async () => {
      clearSelection()
      await load()
    }
  })
}

onMounted(async () => {
  await Promise.all([loadActivities(), load()])
})
</script>

<style scoped>
.page-shell {
  display: grid;
  gap: 14px;
}

.dialog-form :deep(.el-select) {
  width: 100%;
}
</style>
