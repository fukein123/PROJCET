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
        <el-table-column prop="activityTitle" label="活动名称" min-width="170" />
        <el-table-column prop="username" label="账号" min-width="120" />
        <el-table-column prop="realName" label="姓名" min-width="120" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="tagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="拒绝理由" min-width="180" />
        <el-table-column prop="applyTime" label="申请时间" min-width="180" />
        <el-table-column label="审核操作" width="220">
          <template #default="{ row }">
            <div class="audit-actions">
              <el-button
                type="success"
                size="small"
                :disabled="row.status !== 'PENDING'"
                @click="audit(row.id, 'APPROVED')"
              >
                通过申请
              </el-button>
              <el-button type="danger" size="small" :disabled="row.status !== 'PENDING'" @click="reject(row.id)">
                拒绝申请
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="footer">
        <el-pagination
          layout="total, sizes, prev, pager, next"
          :current-page="query.current"
          :page-size="query.size"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          @current-change="handlePage"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
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

function handleSizeChange(size: number) {
  query.size = size
  query.current = 1
  load()
}

function resetQuery() {
  query.current = 1
  query.status = ''
  load()
}

async function audit(id: number, status: string, rejectReason?: string) {
  await auditApplicationApi(id, { status, rejectReason })
  ElMessage.success(status === 'APPROVED' ? '已通过申请' : '已拒绝申请')
  await load()
}

async function reject(id: number) {
  const reason = await ElMessageBox.prompt('请输入拒绝理由', '拒绝申请', {
    confirmButtonText: '确认拒绝',
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

.audit-actions {
  display: flex;
  gap: 8px;
}

.footer {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
