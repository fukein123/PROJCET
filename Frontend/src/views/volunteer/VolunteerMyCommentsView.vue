<template>
  <div class="fade-up">
    <el-card class="module" shadow="never">
      <el-table :data="list" border>
        <el-table-column prop="targetType" label="评论类型" width="140" />
        <el-table-column prop="targetId" label="关联ID" width="100" />
        <el-table-column prop="content" label="内容" min-width="240" />
        <el-table-column prop="createTime" label="时间" min-width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { pageCommentsApi, type CommentModel } from '@/api/content'

const list = ref<CommentModel[]>([])

onMounted(async () => {
  const res = await pageCommentsApi({
    current: 1,
    size: 50,
    onlyMine: true
  })
  list.value = res.records
})
</script>

<style scoped>
.module {
  border: 1px solid var(--cvs-border);
  border-radius: 16px;
}
</style>

