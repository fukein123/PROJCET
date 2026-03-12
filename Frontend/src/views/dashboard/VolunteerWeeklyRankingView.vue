<template>
  <div class="fade-up">
    <el-card class="module" shadow="never">
      <template #header>
        <div class="head">
          <div class="head-left">
            <span>志愿者周统计排行</span>
            <span class="sub">统计区间：{{ weekRangeText }}</span>
            <span class="sub">最近生成：{{ generatedText }}</span>
          </div>
          <div class="head-right">
            <el-date-picker
              v-model="weekDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择周内任意日期"
              style="width: 180px"
            />
            <el-button @click="load">查询</el-button>
            <el-button type="primary" @click="rebuild">重建统计</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" border v-loading="loading" empty-text="该周暂无有效服务记录">
        <el-table-column prop="rankNo" label="排名" width="80" />
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="realName" label="姓名" min-width="140" />
        <el-table-column prop="completedCount" label="完成场次" width="100" />
        <el-table-column prop="signInCount" label="签到次数" width="100" />
        <el-table-column prop="signOutCount" label="签退次数" width="100" />
        <el-table-column prop="serviceMinutes" label="服务分钟" width="110" />
        <el-table-column label="服务小时" width="110">
          <template #default="{ row }">
            {{ row.serviceHours }}h
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import {
  rebuildWeeklyVolunteerRankingApi,
  weeklyVolunteerRankingApi,
  type VolunteerWeeklyRankingItem
} from '@/api/dashboard'

const loading = ref(false)
const list = ref<VolunteerWeeklyRankingItem[]>([])
const weekDate = ref(dayjs().format('YYYY-MM-DD'))
const weekStart = ref('')
const weekEnd = ref('')
const generatedTime = ref('')

const weekRangeText = computed(() => {
  if (!weekStart.value || !weekEnd.value) return '-'
  return `${weekStart.value} ~ ${weekEnd.value}`
})

const generatedText = computed(() => {
  if (!generatedTime.value) return '-'
  return dayjs(generatedTime.value).format('YYYY-MM-DD HH:mm:ss')
})

async function load() {
  loading.value = true
  try {
    const res = await weeklyVolunteerRankingApi({
      weekStart: weekDate.value,
      size: 100
    })
    list.value = res.records
    weekStart.value = res.weekStart
    weekEnd.value = res.weekEnd
    generatedTime.value = res.generatedTime || ''
  } finally {
    loading.value = false
  }
}

async function rebuild() {
  await rebuildWeeklyVolunteerRankingApi({ weekStart: weekDate.value })
  ElMessage.success('周统计已重建')
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
  gap: 12px;
  flex-wrap: wrap;
}

.head-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.head-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sub {
  color: var(--cvs-text-sub);
  font-size: 13px;
}
</style>
