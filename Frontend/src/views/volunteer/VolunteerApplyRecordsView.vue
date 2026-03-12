<template>
  <div class="fade-up">
    <el-card class="module" shadow="never">
      <el-table :data="list" border>
        <el-table-column prop="id" label="申请 ID" width="90" />
        <el-table-column prop="activityTitle" label="活动名称" min-width="180" />
        <el-table-column prop="realName" label="志愿者姓名" width="120" />
        <el-table-column prop="status" label="审核状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getApplicationStatusTag(row.status)">{{ getApplicationStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="拒绝理由" min-width="160" />
        <el-table-column prop="applyTime" label="申请时间" min-width="170" />
        <el-table-column label="打卡操作" width="220">
          <template #default="{ row }">
            <el-button link type="success" :disabled="row.status !== 'APPROVED'" @click="signIn(row.id)">签到</el-button>
            <el-button link type="warning" :disabled="row.status !== 'APPROVED'" @click="signOut(row.id)">签退</el-button>
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
import { ElMessage } from 'element-plus'
import { myApplicationsApi, signInApi, signOutApi, type ApplicationModel } from '@/api/activity'
import { useTable } from '@/composables/useTable'
import { getApplicationStatusLabel, getApplicationStatusTag } from '@/utils/display'

interface MyApplicationQuery {
  current: number
  size: number
}

const { records: list, total, query, load, handlePage } = useTable<ApplicationModel, MyApplicationQuery>({
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
