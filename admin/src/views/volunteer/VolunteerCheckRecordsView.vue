<template>
  <div class="fade-up">
    <el-card class="module" shadow="never">
      <el-table :data="list" border>
        <el-table-column prop="activityId" label="活动ID" width="90" />
        <el-table-column prop="signInTime" label="签到时间" min-width="180" />
        <el-table-column prop="signOutTime" label="签退时间" min-width="180" />
        <el-table-column prop="status" label="状态" width="120" />
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

