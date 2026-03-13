<template>
  <div class="page-shell">
    <WorkspaceHero
      compact
      eyebrow="打卡记录"
      title="统一查看签到签退与服务时长"
      description="集中查看每一次服务打卡结果，快速确认签到状态、签退时间与累计服务时长。"
    >
      <template #actions>
        <el-button type="primary" @click="load">刷新记录</el-button>
        <el-button @click="router.push('/volunteer/apply-records')">查看报名申请</el-button>
        <el-button @click="router.push('/volunteer/profile')">前往个人中心</el-button>
      </template>
    </WorkspaceHero>

    <VolunteerPageSection eyebrow="服务记录" title="我的打卡记录" description="统一查看活动地点、签到签退时间与本次服务时长。">
      <StatePanel
        v-if="loading"
        state="loading"
        title="正在同步打卡记录"
        description="正在加载你的签到、签退与服务时长数据。"
      />

      <template v-else-if="list.length">
        <el-table :data="list" border>
          <el-table-column prop="activityTitle" label="活动名称" min-width="180" />
          <el-table-column prop="activityAddress" label="活动地点" min-width="180" />
          <el-table-column label="签到时间" min-width="170">
            <template #default="{ row }">{{ formatDateTime(row.signInTime) }}</template>
          </el-table-column>
          <el-table-column label="签退时间" min-width="170">
            <template #default="{ row }">{{ formatDateTime(row.signOutTime) }}</template>
          </el-table-column>
          <el-table-column label="服务时长" width="120">
            <template #default="{ row }">{{ toServiceHours(row.serviceMinutes) }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getCheckRecordStatusTag(row.status)">{{ getCheckRecordStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <StatePanel
        v-else
        title="暂无打卡记录"
        description="报名活动并完成签到后，这里会沉淀你的服务记录与时长。"
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
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { myCheckRecordsApi, type CheckRecordModel } from '@/api/activity'
import StatePanel from '@/components/shared/StatePanel.vue'
import WorkspaceHero from '@/components/shared/WorkspaceHero.vue'
import VolunteerPageSection from '@/components/volunteer/VolunteerPageSection.vue'
import { useTable } from '@/composables/useTable'
import { formatDateTime, getCheckRecordStatusLabel, getCheckRecordStatusTag, toServiceHours } from '@/utils/display'

interface CheckRecordQuery {
  current: number
  size: number
}

const router = useRouter()

const { loading, records: list, total, query, load, handlePage } = useTable<CheckRecordModel, CheckRecordQuery>({
  initialQuery: {
    current: 1,
    size: 10
  },
  fetcher: (params) => myCheckRecordsApi({ current: params.current, size: params.size })
})

onMounted(load)
</script>

<style scoped>
.page-shell {
  display: grid;
  gap: 14px;
}
</style>
