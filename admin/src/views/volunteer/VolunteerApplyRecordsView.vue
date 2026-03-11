<template>
  <div class="fade-up">
    <el-card class="module" shadow="never">
      <el-table :data="list" border>
        <el-table-column prop="id" label="申请ID" width="90" />
        <el-table-column prop="activityId" label="活动ID" width="90" />
        <el-table-column prop="status" label="审核状态" width="120" />
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { myApplicationsApi, signInApi, signOutApi, type ApplicationModel } from '@/api/activity'

const list = ref<ApplicationModel[]>([])
const total = ref(0)
const query = reactive({
  current: 1,
  size: 10
})

async function load() {
  const res = await myApplicationsApi({ current: query.current, size: query.size })
  list.value = res.records
  total.value = res.total
}

function handlePage(page: number) {
  query.current = page
  load()
}

async function getLocation() {
  return new Promise<{ latitude: number; longitude: number }>((resolve) => {
    if (!navigator.geolocation) {
      resolve({ latitude: 31.2304, longitude: 121.4737 })
      return
    }
    navigator.geolocation.getCurrentPosition(
      (position) => {
        resolve({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude
        })
      },
      () => resolve({ latitude: 31.2304, longitude: 121.4737 }),
      { enableHighAccuracy: true, timeout: 5000 }
    )
  })
}

async function signIn(applicationId: number) {
  const loc = await getLocation()
  await signInApi({ applicationId, ...loc })
  ElMessage.success('签到成功')
}

async function signOut(applicationId: number) {
  const loc = await getLocation()
  await signOutApi({ applicationId, ...loc })
  ElMessage.success('签退成功')
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

