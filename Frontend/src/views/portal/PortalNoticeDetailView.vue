<template>
  <div>
    <PortalNavBar />
    <main class="portal-article-wrap">
      <WorkspaceHero
        tone="portal"
        eyebrow="系统公告详情"
        :title="notice?.title || '查看平台公告详情'"
        description="统一查看平台公告、服务提醒与社区志愿服务说明。"
      >
        <template #actions>
          <el-button type="primary" @click="router.push(PORTAL_PATHS.notices)">返回公告列表</el-button>
          <el-button @click="router.push(PORTAL_PATHS.home)">返回门户首页</el-button>
        </template>
      </WorkspaceHero>

      <StatePanel
        v-if="loading"
        state="loading"
        tone="portal"
        title="正在加载公告详情"
        description="正在同步公告正文与发布时间。"
      />

      <template v-else-if="notice">
        <article class="article-card fade-up">
          <div class="meta-row">
            <span>平台公告</span>
            <span>{{ formatDateTime(notice.publishTime) }}</span>
          </div>
          <h1>{{ notice.title }}</h1>
          <RichTextRenderer class="article-body" :value="notice?.content" empty-html="<p>当前公告暂无正文内容。</p>" />
        </article>
      </template>

      <StatePanel
        v-else
        tone="portal"
        title="公告不存在或已下线"
        description="可以返回公告列表查看其他平台通知。"
      >
        <template #actions>
          <el-button type="primary" @click="router.push(PORTAL_PATHS.notices)">返回公告列表</el-button>
        </template>
      </StatePanel>
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { noticeDetailApi, type NoticeModel } from '@/api/content'
import RichTextRenderer from '@/components/shared/RichTextRenderer.vue'
import StatePanel from '@/components/shared/StatePanel.vue'
import WorkspaceHero from '@/components/shared/WorkspaceHero.vue'
import { PORTAL_PATHS } from '@/constants/portal-routes'
import { formatDateTime } from '@/utils/display'
import PortalNavBar from './PortalNavBar.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const notice = ref<NoticeModel>()

onMounted(async () => {
  loading.value = true
  try {
    notice.value = await noticeDetailApi(Number(route.params.id))
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.portal-article-wrap {
  width: min(980px, calc(100% - 24px));
  margin: 14px auto 40px;
  display: grid;
  gap: 14px;
}

.article-card {
  border: 1px solid var(--cvs-border);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.96);
  padding: 18px;
  box-shadow: var(--cvs-shadow-soft);
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: #667771;
  font-size: 13px;
}

.article-card h1 {
  margin: 14px 0;
  font-size: clamp(26px, 4vw, 38px);
  line-height: 1.2;
}

.article-body {
  margin: 0;
  line-height: 1.95;
  color: #3f4f4a;
}

.rich-text-content :deep(p),
.rich-text-content :deep(ul),
.rich-text-content :deep(ol),
.rich-text-content :deep(blockquote) {
  margin: 0 0 14px;
}

.rich-text-content :deep(ul),
.rich-text-content :deep(ol) {
  padding-left: 22px;
}
</style>
