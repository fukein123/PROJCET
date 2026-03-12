<template>
  <div>
    <PortalNavBar />
    <main class="portal-wrap">
      <el-card class="module" shadow="never">
        <h2 class="section-title">系统公告</h2>
        <el-collapse>
          <el-collapse-item v-for="item in list" :key="item.id" :title="item.title" :name="item.id">
            <p>{{ item.content }}</p>
            <small>{{ formatTime(item.publishTime) }}</small>
          </el-collapse-item>
        </el-collapse>
      </el-card>
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { pageNoticesApi, type NoticeModel } from '@/api/content'
import PortalNavBar from './PortalNavBar.vue'

const list = ref<NoticeModel[]>([])

function formatTime(value: string) {
  return dayjs(value).format('YYYY-MM-DD HH:mm')
}

onMounted(async () => {
  const res = await pageNoticesApi({ current: 1, size: 30, onlyPublished: true })
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
  margin-top: 0;
  color: var(--cvs-text-sub);
  line-height: 1.8;
}

small {
  color: #8d9a94;
}
</style>
