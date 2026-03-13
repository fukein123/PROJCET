<template>
  <div class="page-shell">
    <WorkspaceHero
      compact
      eyebrow="报名申请"
      title="统一查看报名审核与签到动作"
      description="在同一页面跟踪活动报名进度，并在审核通过后直接完成签到与签退操作。"
    >
      <template #actions>
        <el-button type="primary" @click="load">刷新记录</el-button>
        <el-button @click="router.push('/volunteer/check-records')">查看打卡记录</el-button>
        <el-button @click="router.push('/volunteer/activity-center')">返回活动中心</el-button>
      </template>
    </WorkspaceHero>

    <VolunteerPageSection eyebrow="报名与审核" title="我的报名记录" description="统一查看报名审核状态、拒绝原因和打卡操作入口。">
      <StatePanel
        v-if="loading"
        state="loading"
        title="正在同步报名记录"
        description="正在加载你的活动申请、审核结果与打卡入口。"
      />

      <template v-else-if="list.length">
        <el-table :data="list" border>
          <el-table-column prop="id" label="申请 ID" width="90" />
          <el-table-column prop="activityTitle" label="活动名称" min-width="180" />
          <el-table-column prop="realName" label="志愿者姓名" width="120" />
          <el-table-column prop="status" label="审核状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getApplicationStatusTag(row.status)">{{ getApplicationStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="rejectReason" label="拒绝理由" min-width="180" />
          <el-table-column label="申请时间" min-width="170">
            <template #default="{ row }">{{ formatDateTime(row.applyTime) }}</template>
          </el-table-column>
          <el-table-column label="打卡操作" width="220">
            <template #default="{ row }">
              <el-button link type="success" :disabled="row.status !== 'APPROVED'" @click="signIn(row.id)">签到</el-button>
              <el-button link type="warning" :disabled="row.status !== 'APPROVED'" @click="signOut(row.id)">签退</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <StatePanel
        v-else
        title="暂无报名记录"
        description="前往活动中心报名后，这里会展示审核状态、拒绝原因与签到入口。"
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
import { ElMessage } from 'element-plus'
import { myApplicationsApi, signInApi, signOutApi, type ApplicationModel } from '@/api/activity'
import StatePanel from '@/components/shared/StatePanel.vue'
import WorkspaceHero from '@/components/shared/WorkspaceHero.vue'
import VolunteerPageSection from '@/components/volunteer/VolunteerPageSection.vue'
import { useTable } from '@/composables/useTable'
import { formatDateTime, getApplicationStatusLabel, getApplicationStatusTag } from '@/utils/display'

interface MyApplicationQuery {
  current: number
  size: number
}

const router = useRouter()

const { loading, records: list, total, query, load, handlePage } = useTable<ApplicationModel, MyApplicationQuery>({
  initialQuery: {
    current: 1,
    size: 10
  },
  fetcher: (params) => myApplicationsApi({ current: params.current, size: params.size })
})

async function signIn(applicationId: number) {
  await signInApi({ applicationId })
  ElMessage.success('签到成功')
  await load()
}

async function signOut(applicationId: number) {
  await signOutApi({ applicationId })
  ElMessage.success('签退成功')
  await load()
}

onMounted(load)
</script>

<style scoped>
.page-shell {
  display: grid;
  gap: 14px;
}
</style>
