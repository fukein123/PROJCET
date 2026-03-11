<template>
  <div>
    <PortalNavBar />
    <main class="portal-wrap">
      <el-card class="module" shadow="never">
        <h2 class="section-title">社区新闻 / 信息动态</h2>
        <el-timeline>
          <el-timeline-item v-for="item in list" :key="item.id" :timestamp="formatTime(item.publishTime)">
            <strong>{{ item.title }}</strong>
            <p>{{ item.content }}</p>
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { pageDynamicsApi, type DynamicModel } from '@/api/content'
import PortalNavBar from './PortalNavBar.vue'

const list = ref<DynamicModel[]>([])

function formatTime(value: string) {
  return dayjs(value).format('YYYY-MM-DD HH:mm')
}

onMounted(async () => {
  const res = await pageDynamicsApi({ current: 1, size: 30, onlyPublished: true })
  list.value = res.records
})
</script>

<style scoped>
.portal-wrap {
  width: min(1100px, calc(100% - 24px));
  margin: 14px auto 40px;
}

.module {
  border-radius: 16px;
  border: 1px solid var(--cvs-border);
}

p {
  margin: 8px 0 0;
  color: var(--cvs-text-sub);
}
</style>

