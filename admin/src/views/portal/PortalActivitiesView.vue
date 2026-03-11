<template>
  <div>
    <PortalNavBar />
    <main class="portal-wrap">
      <el-card shadow="never" class="module">
        <template #header>
          <div class="head">
            <span>志愿活动列表</span>
            <el-input v-model="keyword" placeholder="按活动名称搜索" clearable style="width: 260px" @change="load" />
          </div>
        </template>
        <el-table :data="activities" border>
          <el-table-column prop="title" label="活动名称" min-width="180" />
          <el-table-column prop="address" label="活动地点" min-width="180" />
          <el-table-column prop="status" label="状态" width="110" />
          <el-table-column label="时间" min-width="220">
            <template #default="{ row }">
              {{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { pageActivitiesApi, type ActivityModel } from '@/api/activity'
import PortalNavBar from './PortalNavBar.vue'

const keyword = ref('')
const activities = ref<ActivityModel[]>([])

function formatTime(value: string) {
  return dayjs(value).format('YYYY-MM-DD HH:mm')
}

async function load() {
  const res = await pageActivitiesApi({
    current: 1,
    size: 50,
    keyword: keyword.value || undefined
  })
  activities.value = res.records
}

onMounted(load)
</script>

<style scoped>
.portal-wrap {
  width: min(1200px, calc(100% - 24px));
  margin: 14px auto 40px;
}

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

