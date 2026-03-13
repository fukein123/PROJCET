<template>
  <div>
    <PortalNavBar />
    <main class="portal-wrap">
      <WorkspaceHero
        tone="portal"
        compact
        eyebrow="系统公告"
        title="统一查看平台公告与社区服务提醒"
        description="公告页采用与门户端、志愿者端一致的页头层级与空态表达，方便居民与志愿者快速获取官方通知。"
      >
        <template #actions>
          <el-button type="primary" @click="load">刷新公告</el-button>
        </template>
      </WorkspaceHero>

      <section class="module-card fade-up">
        <div class="module-head">
          <div>
            <p class="module-eyebrow">公告列表</p>
            <h2 class="section-title">系统公告</h2>
          </div>
        </div>

        <StatePanel
          v-if="loading"
          state="loading"
          tone="portal"
          title="正在加载公告"
          description="正在同步平台最新公告内容。"
        />
        <el-collapse v-else-if="list.length">
          <el-collapse-item v-for="item in list" :key="item.id" :title="item.title" :name="item.id">
            <p>{{ item.content }}</p>
            <small>{{ formatTime(item.publishTime) }}</small>
          </el-collapse-item>
        </el-collapse>
        <StatePanel
          v-else
          tone="portal"
          title="暂无系统公告"
          description="新的平台通知发布后会展示在这里。"
        />
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { pageNoticesApi, type NoticeModel } from '@/api/content'
import StatePanel from '@/components/shared/StatePanel.vue'
import WorkspaceHero from '@/components/shared/WorkspaceHero.vue'
import PortalNavBar from './PortalNavBar.vue'

const loading = ref(false)
const list = ref<NoticeModel[]>([])

function formatTime(value: string) {
  return dayjs(value).format('YYYY-MM-DD HH:mm')
}

async function load() {
  loading.value = true
  try {
    const res = await pageNoticesApi({ current: 1, size: 30, onlyPublished: true })
    list.value = res.records
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.portal-wrap {
  width: min(1100px, calc(100% - 24px));
  margin: 14px auto 40px;
  display: grid;
  gap: 14px;
}

.module-card {
  border-radius: 18px;
  border: 1px solid var(--cvs-border);
  background: rgba(255, 255, 255, 0.94);
  padding: 18px;
  box-shadow: var(--cvs-shadow-soft);
}

.module-head {
  margin-bottom: 14px;
}

.module-eyebrow {
  margin: 0 0 6px;
  color: #2a7a5f;
  font-size: var(--cvs-font-size-xs);
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.module-head :deep(.section-title) {
  margin-bottom: 0;
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
