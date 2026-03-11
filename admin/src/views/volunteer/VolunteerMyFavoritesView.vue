<template>
  <div class="fade-up">
    <el-card class="module" shadow="never">
      <el-table :data="list" border>
        <el-table-column prop="activityId" label="活动ID" width="120" />
        <el-table-column prop="createTime" label="收藏时间" min-width="180" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="danger" @click="remove(row.activityId)">取消收藏</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { myFavoritesApi, removeFavoriteApi, type FavoriteModel } from '@/api/content'

const list = ref<FavoriteModel[]>([])

async function load() {
  list.value = await myFavoritesApi()
}

async function remove(activityId: number) {
  await removeFavoriteApi(activityId)
  await load()
}

onMounted(load)
</script>

<style scoped>
.module {
  border: 1px solid var(--cvs-border);
  border-radius: 16px;
}
</style>
