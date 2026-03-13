<template>
  <AdminListScaffold title="活动报名审核" @search="load" @reset="resetQuery">
    <template #filters>
      <el-form-item label="审核状态">
        <el-select v-model="query.status" clearable style="width: 160px">
          <el-option label="待审核" value="PENDING" />
          <el-option label="通过" value="APPROVED" />
          <el-option label="拒绝" value="REJECTED" />
        </el-select>
      </el-form-item>
    </template>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="id" label="申请 ID" width="90" />
      <el-table-column prop="activityTitle" label="活动名称" min-width="170" />
      <el-table-column prop="username" label="账号" min-width="120" />
      <el-table-column prop="realName" label="姓名" min-width="120" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getApplicationStatusTag(row.status)">{{ getApplicationStatusLabel(row.status) }}</el-tag>
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

    <template #pagination>
      <el-pagination
        layout="total, sizes, prev, pager, next"
        :current-page="query.current"
        :page-size="query.size"
        :page-sizes="[10, 20, 30, 50]"
        :total="total"
        @current-change="handlePage"
        @size-change="handleSizeChange"
      />
    </template>
  </AdminListScaffold>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import AdminListScaffold from '@/components/admin/AdminListScaffold.vue'
import { auditApplicationApi, pageApplicationsApi, type ApplicationModel } from '@/api/activity'
import { useTable } from '@/composables/useTable'
import { getApplicationStatusLabel, getApplicationStatusTag } from '@/utils/display'
import { runConfirmedAction } from '@/utils/confirmed-action'

interface ApplicationQuery {
  current: number
  size: number
  status: string
}

const { loading, records: list, total, query, load, reset, handlePage, handleSizeChange } = useTable<
  ApplicationModel,
  ApplicationQuery
>({
  initialQuery: {
    current: 1,
    size: 10,
    status: ''
  },
  fetcher: (params) =>
    pageApplicationsApi({
      current: params.current,
      size: params.size,
      status: params.status || undefined
    })
})

function resetQuery() {
  reset()
}

async function audit(id: number, status: string) {
  await runConfirmedAction({
    message: status === 'APPROVED' ? '确认通过该报名申请吗？' : '请输入拒绝理由',
    title: status === 'APPROVED' ? '通过申请' : '拒绝申请',
    confirmButtonText: status === 'APPROVED' ? '确认通过' : '确认拒绝',
    type: status === 'APPROVED' ? 'success' : 'warning',
    prompt:
      status === 'REJECTED'
        ? {
            message: '请输入拒绝理由',
            inputPlaceholder: '拒绝理由必填',
            inputType: 'textarea',
            inputValidator: (value) => (value.trim() ? true : '请输入拒绝理由')
          }
        : undefined,
    action: (reason) =>
      auditApplicationApi(id, {
        status,
        rejectReason: status === 'REJECTED' ? (reason || '').trim() : undefined
      }),
    successMessage: status === 'APPROVED' ? '已通过申请' : '已拒绝申请',
    afterSuccess: load
  })
}

async function reject(id: number) {
  await audit(id, 'REJECTED')
}

onMounted(load)
</script>

<style scoped>
.audit-actions {
  display: flex;
  gap: 8px;
}
</style>
