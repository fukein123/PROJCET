<template>
  <div class="fade-up">
    <SearchForm @search="load" @reset="resetQuery">
      <el-form-item label="审核状态">
        <el-select v-model="query.status" clearable style="width: 160px">
          <el-option label="待审核" value="PENDING" />
          <el-option label="通过" value="APPROVED" />
          <el-option label="拒绝" value="REJECTED" />
        </el-select>
      </el-form-item>
    </SearchForm>

    <el-card class="module" shadow="never">
      <el-table :data="list" border v-loading="loading">
        <el-table-column prop="id" label="申请ID" width="90" />
        <el-table-column prop="activityId" label="活动ID" width="90" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="tagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="拒绝理由" min-width="200" />
        <el-table-column prop="applyTime" label="申请时间" min-width="180" />
        <el-table-column label="操作" width="170">
          <template #default="{ row }">
            <el-button link type="success" :disabled="row.status !== 'PENDING'" @click="audit(row.id, 'APPROVED')">
              通过
            </el-button>
            <el-button link type="danger" :disabled="row.status !== 'PENDING'" @click="reject(row.id)">
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="footer">
        <el-pagination
          layout="total, prev, pager, next"
          :current-page="query.current"
          :page-size="query.size"
          :total="total"
          @current-change="handlePage"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import SearchForm from '@/components/SearchForm.vue'
import { auditApplicationApi, pageApplicationsApi, type ApplicationModel } from '@/api/activity'

const loading = ref(false)
const list = ref<ApplicationModel[]>([])
const total = ref(0)
const query = reactive({
  current: 1,
  size: 10,
  status: ''
})

function tagType(status: string) {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'warning'
}

function statusLabel(status: string) {
  if (status === 'APPROVED') return '已通过'
  if (status === 'REJECTED') return '已拒绝'
  if (status === 'PENDING') return '待审核'
  return status
}

async function load() {
  loading.value = true
  try {
    const res = await pageApplicationsApi({
      current: query.current,
      size: query.size,
      status: query.status || undefined
    })
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function handlePage(page: number) {
  query.current = page
  load()
}

function resetQuery() {
  query.current = 1
  query.status = ''
  load()
}

async function audit(id: number, status: string, rejectReason?: string) {
  await auditApplicationApi(id, { status, rejectReason })
  await load()
}

async function reject(id: number) {
  const reason = await ElMessageBox.prompt('请输入拒绝理由', '审核拒绝', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '拒绝理由必填'
  })
  await audit(id, 'REJECTED', reason.value)
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
