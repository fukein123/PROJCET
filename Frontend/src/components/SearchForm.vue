<template>
  <el-card shadow="never" class="search-card">
    <el-form :inline="true" class="search-form" @submit.prevent="$emit('search')">
      <div class="search-fields">
        <slot />
      </div>
      <el-form-item class="search-actions">
        <el-button type="primary" @click="$emit('search')">查询</el-button>
        <el-button @click="$emit('reset')">重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
defineEmits<{
  (e: 'search'): void
  (e: 'reset'): void
}>()
</script>

<style scoped>
.search-card {
  border-radius: var(--cvs-radius-lg);
  border: 1px solid var(--cvs-border);
  margin-bottom: var(--cvs-space-3);
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  gap: var(--cvs-space-3);
}

.search-fields {
  display: flex;
  flex: 1 1 320px;
  flex-wrap: wrap;
  gap: 0 var(--cvs-space-2);
}

.search-fields :deep(.el-form-item) {
  margin-right: 0;
  margin-bottom: 0;
}

.search-actions {
  margin-right: 0;
  margin-bottom: 0;
  margin-left: auto;
}

.search-actions :deep(.el-button) {
  min-width: 88px;
}

.search-actions :deep(.el-button + .el-button) {
  margin-left: var(--cvs-space-2);
}

@media (max-width: 960px) {
  .search-fields,
  .search-actions {
    flex: 1 1 100%;
  }

  .search-actions {
    margin-left: 0;
  }
}
</style>