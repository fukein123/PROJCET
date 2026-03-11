<template>
  <el-card shadow="never" class="search-card">
    <el-form :inline="true" @submit.prevent>
      <slot />
      <el-form-item>
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
  border-radius: 16px;
  border: 1px solid var(--cvs-border);
  margin-bottom: 12px;
}
</style>

