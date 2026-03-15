<template>
  <AdminListScaffold
    title="活动打卡记录管理"
    description="统一查看签到、签退、服务时长与志愿者信息，支持按状态和关键词筛选记录。"
    @search="load"
    @reset="resetQuery"
  >
    <template #filters>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" clearable placeholder="活动名称 / 账号 / 姓名" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable style="width: 160px">
          <el-option label="已签到" value="SIGNED_IN" />
          <el-option label="已完成" value="FINISHED" />
        </el-select>
      </el-form-item>
    </template>

    <AdminContentSection
      title="打卡记录列表"
      description="支持回看活动信息、志愿者身份、签到签退时间与本次服务时长，便于后台统一核查。"
      :loading="loading && !list.length"
      :empty="!loading && !list.length"
      loading-title="正在加载打卡记录"
      loading-description="系统正在同步活动签到、签退与服务时长数据。"
      empty-title="暂无打卡记录"
      empty-description="当前筛选条件下没有匹配的打卡记录，可以调整关键词或状态后重试。"
    >
      <template #actions>
        <el-button type="primary" @click="load()">刷新记录</el-button>
      </template>

      <el-table :data="list" border v-loading="loading">
        <el-table-column prop="id" label="记录 ID" width="90" />
        <el-table-column label="活动信息" min-width="260">
          <template #default="{ row }">
            <div class="activity-cell">
              <strong>{{ row.activityTitle || '-' }}</strong>
              <span>{{ row.activityAddress || '未填写地点' }}</span>
              <small>{{ formatDateTime(row.activityStartTime) }} - {{ formatDateTime(row.activityEndTime) }}</small>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="志愿者" min-width="170">
          <template #default="{ row }">
            <div class="user-cell">
              <strong>{{ row.realName || '未实名' }}</strong>
              <span>{{ row.username || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="签到时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.signInTime) }}</template>
        </el-table-column>
        <el-table-column label="签退时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.signOutTime) }}</template>
        </el-table-column>
        <el-table-column label="服务时长" width="130">
          <template #default="{ row }">{{ toServiceHours(row.serviceMinutes) }}</template>
        </el-table-column>
        <el-table-column label="签到 / 签退距离" min-width="150">
          <template #default="{ row }">
            {{ formatDistance(row.signInDistance) }} / {{ formatDistance(row.signOutDistance) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getCheckRecordStatusTag(row.status)">{{ getCheckRecordStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <template #pagination>
        <el-pagination
          layout="total, sizes, prev, pager, next"
          :current-page="query.current"
          :page-size="query.size"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          @current-change="handlePage"
          @size-change="handleSizeChange"
        />
      </template>
    </AdminContentSection>
  </AdminListScaffold>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import AdminContentSection from '@/components/admin/AdminContentSection.vue'
import AdminListScaffold from '@/components/admin/AdminListScaffold.vue'
import { pageCheckRecordsApi, type CheckRecordModel } from '@/api/activity'
import { useTable } from '@/composables/useTable'
import { formatDateTime, getCheckRecordStatusLabel, getCheckRecordStatusTag, toServiceHours } from '@/utils/display'

interface CheckRecordQuery {
  current: number
  size: number
  keyword: string
  status: string
}

const { loading, records: list, total, query, load, reset, handlePage, handleSizeChange } = useTable<
  CheckRecordModel,
  CheckRecordQuery
>({
  initialQuery: {
    current: 1,
    size: 10,
    keyword: '',
    status: ''
  },
  fetcher: (params) =>
    pageCheckRecordsApi({
      current: params.current,
      size: params.size,
      keyword: params.keyword || undefined,
      status: params.status || undefined
    })
})

function resetQuery() {
  reset()
}

function formatDistance(value?: number) {
  if (typeof value !== 'number' || Number.isNaN(value)) {
    return '-'
  }
  return `${value.toFixed(1)} m`
}

onMounted(load)
</script>

<style scoped>
.activity-cell,
.user-cell {
  display: grid;
  gap: 4px;
}

.activity-cell strong,
.user-cell strong {
  color: var(--cvs-text-main);
  font-weight: var(--cvs-font-weight-heavy);
}

.activity-cell span,
.user-cell span,
.activity-cell small {
  color: var(--cvs-text-sub);
}

.activity-cell small {
  font-size: 12px;
}
</style>
