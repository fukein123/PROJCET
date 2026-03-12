<template>
  <el-card shadow="never" class="table-card">
    <el-table :data="data" v-loading="loading" border>
      <slot />
    </el-table>
    <div class="table-footer">
      <el-pagination
        layout="total, prev, pager, next"
        :total="total"
        :current-page="current"
        :page-size="size"
        @current-change="onPageChange"
      />
    </div>
  </el-card>
</template>

<script setup lang="ts">
defineProps<{
  data: unknown[]
  loading: boolean
  total: number
  current: number
  size: number
}>()

const emit = defineEmits<{
  (e: 'change-page', page: number): void
}>()

function onPageChange(page: number) {
  emit('change-page', page)
}
</script>

<style scoped>
.table-card {
  border-radius: 16px;
  border: 1px solid var(--cvs-border);
}

.table-footer {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
