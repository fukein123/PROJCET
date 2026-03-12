<template>
  <div class="fade-up">
    <el-card class="module" shadow="never">
      <el-table :data="list" border>
        <el-table-column prop="activityTitle" label="活动名称" min-width="180" />
        <el-table-column prop="activityAddress" label="活动地点" min-width="180" />
        <el-table-column prop="signInTime" label="签到时间" min-width="170" />
        <el-table-column prop="signOutTime" label="签退时间" min-width="170" />
        <el-table-column label="服务时长" width="110">
          <template #default="{ row }">{{ toServiceHours(row.serviceMinutes) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getCheckRecordStatusTag(row.status)">{{ getCheckRecordStatusLabel(row.status) }}</el-tag>
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
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { myCheckRecordsApi, type CheckRecordModel } from '@/api/activity'
import { useTable } from '@/composables/useTable'
import { getCheckRecordStatusLabel, getCheckRecordStatusTag, toServiceHours } from '@/utils/display'

interface CheckRecordQuery {
  current: number
  size: number
}

const { records: list, total, query, load, handlePage } = useTable<CheckRecordModel, CheckRecordQuery>({
  initialQuery: {
    current: 1,
    size: 10
  },
  fetcher: (params) => myCheckRecordsApi({ current: params.current, size: params.size })
})

onMounted(load)
</script>

<style scoped>
.module {
  border: 1px solid var(--cvs-border);
  border-radius: 16px;
}

.footer {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
