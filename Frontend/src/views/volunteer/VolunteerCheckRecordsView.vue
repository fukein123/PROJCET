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
            <el-tag :type="statusTag(row.status)">{{ statusLabel(row.status) }}</el-tag>
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
import { onMounted, reactive, ref } from 'vue'
import { myCheckRecordsApi, type CheckRecordModel } from '@/api/activity'

const list = ref<CheckRecordModel[]>([])
const total = ref(0)
const query = reactive({
  current: 1,
  size: 10
})

function statusLabel(status: string) {
  if (status === 'SIGNED_IN') return '已签到'
  if (status === 'FINISHED') return '已完成'
  return status || '未知'
}

function statusTag(status: string) {
  if (status === 'SIGNED_IN') return 'warning'
  if (status === 'FINISHED') return 'success'
  return 'info'
}

function toServiceHours(minutes?: number) {
  const value = minutes || 0
  if (!value) return '0 分钟'
  const hours = Math.floor(value / 60)
  const mins = value % 60
  if (!hours) return `${mins} 分钟`
  return `${hours} 小时 ${mins} 分钟`
}

async function load() {
  const res = await myCheckRecordsApi(query)
  list.value = res.records
  total.value = res.total
}

function handlePage(page: number) {
  query.current = page
  load()
}

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
