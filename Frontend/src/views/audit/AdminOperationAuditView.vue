<template>
  <AdminListScaffold
    title="审计中心"
    description="集中查看管理员关键操作留痕，支持按动作、对象、结果和关键字进行筛选。"
  >
    <SearchForm @search="load" @reset="resetQuery">
      <el-form-item label="动作类型">
        <el-select v-model="query.actionType" clearable style="width: 180px">
          <el-option label="审核报名申请" value="AUDIT_APPLICATION" />
          <el-option label="审核论坛帖子" value="AUDIT_POST" />
          <el-option label="审核认证信息" value="AUDIT_CERTIFICATION" />
          <el-option label="修改订单状态" value="UPDATE_ORDER_STATUS" />
          <el-option label="归档活动" value="ARCHIVE_ACTIVITY_BATCH" />
        </el-select>
      </el-form-item>
      <el-form-item label="对象类型">
        <el-select v-model="query.targetType" clearable style="width: 180px">
          <el-option label="志愿活动" value="ACTIVITY" />
          <el-option label="报名申请" value="APPLICATION" />
          <el-option label="论坛帖子" value="POST" />
          <el-option label="认证审核" value="CERTIFICATION" />
          <el-option label="兑换订单" value="ORDER" />
        </el-select>
      </el-form-item>
      <el-form-item label="结果">
        <el-select v-model="query.result" clearable style="width: 160px">
          <el-option label="成功" value="SUCCESS" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作人">
        <el-input v-model.trim="query.operatorKeyword" placeholder="管理员账号" />
      </el-form-item>
      <el-form-item label="对象关键字">
        <el-input v-model.trim="query.targetKeyword" placeholder="对象名称 / 详情" />
      </el-form-item>
    </SearchForm>

    <el-table :data="records" border v-loading="loading">
      <el-table-column label="时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column prop="operatorUsername" label="操作人" min-width="120" />
      <el-table-column label="动作" min-width="160">
        <template #default="{ row }">{{ getAdminActionLabel(row.actionType) }}</template>
      </el-table-column>
      <el-table-column label="对象" min-width="130">
        <template #default="{ row }">{{ getAdminTargetLabel(row.targetType) }}</template>
      </el-table-column>
      <el-table-column prop="targetName" label="对象名称" min-width="180" show-overflow-tooltip />
      <el-table-column label="结果" width="100">
        <template #default="{ row }">
          <el-tag :type="row.result === 'SUCCESS' ? 'success' : 'danger'" effect="light">
            {{ row.result === 'SUCCESS' ? '成功' : row.result }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="detail" label="详情" min-width="240" show-overflow-tooltip />
    </el-table>

    <template #pagination>
      <el-pagination
        layout="total, sizes, prev, pager, next"
        :current-page="query.current"
        :page-size="query.size"
        :page-sizes="pageSizes"
        :total="total"
        @current-change="handlePage"
        @size-change="handleSizeChange"
      />
    </template>
  </AdminListScaffold>
</template>

<script setup lang="ts">
import SearchForm from '@/components/SearchForm.vue'
import AdminListScaffold from '@/components/admin/AdminListScaffold.vue'
import { pageAdminOperationLogsApi, type AdminOperationLogItem } from '@/api/audit'
import { useTable } from '@/composables/useTable'
import { formatDateTime, getAdminActionLabel, getAdminTargetLabel } from '@/utils/display'

interface AuditQuery {
  current: number
  size: number
  actionType?: string
  targetType?: string
  result?: string
  operatorKeyword: string
  targetKeyword: string
}

const pageSizes = [10, 20, 30, 50]

const { loading, records, total, query, load, handlePage, handleSizeChange, reset } = useTable<
  AdminOperationLogItem,
  AuditQuery
>({
  initialQuery: {
    current: 1,
    size: 10,
    actionType: undefined,
    targetType: undefined,
    result: undefined,
    operatorKeyword: '',
    targetKeyword: ''
  },
  fetcher: (params) =>
    pageAdminOperationLogsApi({
      current: params.current,
      size: params.size,
      actionType: params.actionType || undefined,
      targetType: params.targetType || undefined,
      result: params.result || undefined,
      operatorKeyword: params.operatorKeyword || undefined,
      targetKeyword: params.targetKeyword || undefined
    })
})

function resetQuery() {
  reset({
    actionType: undefined,
    targetType: undefined,
    result: undefined,
    operatorKeyword: '',
    targetKeyword: ''
  })
}

void load()
</script>
