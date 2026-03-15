<template>
  <div>
    <PortalNavBar />
    <main class="portal-article-wrap">
      <WorkspaceHero
        tone="portal"
        eyebrow="信息动态详情"
        :title="article?.title || '查看社区动态详情'"
        :description="article?.source || '统一查看社区新闻、活动动态与热点信息的完整内容。'"
      >
        <template #actions>
          <el-button type="primary" @click="router.push(PORTAL_PATHS.news)">返回动态列表</el-button>
          <el-button @click="router.push(PORTAL_PATHS.activities)">查看志愿活动</el-button>
        </template>
        <template #aside>
          <div v-if="article" class="hero-stat-grid">
            <article class="hero-stat">
              <h3>信息来源</h3>
              <strong>{{ article.source || '平台发布' }}</strong>
              <span>来源信息与内容正文同步展示</span>
            </article>
            <article class="hero-stat">
              <h3>浏览量</h3>
              <strong>{{ article.views || 0 }}</strong>
              <span>详情页打开后会累计最新浏览量</span>
            </article>
          </div>
        </template>
      </WorkspaceHero>

      <StatePanel
        v-if="loading"
        state="loading"
        tone="portal"
        title="正在加载动态详情"
        description="正在同步信息正文、来源和发布时间。"
      />

      <template v-else-if="article">
        <article class="article-card fade-up">
          <img v-if="article.imageUrl" class="hero-image" :src="article.imageUrl" alt="动态配图" />
          <div class="meta-row">
            <span>{{ typeLabel(article.type) }}</span>
            <span>{{ article.source || '平台发布' }}</span>
            <span>浏览 {{ article.views || 0 }}</span>
            <span>{{ formatDateTime(article.publishTime) }}</span>
          </div>
          <h1>{{ article.title }}</h1>
          <RichTextRenderer class="article-body" :value="article?.content" empty-html="<p>当前动态暂无正文内容。</p>" />
        </article>
      </template>

      <StatePanel
        v-else
        tone="portal"
        title="动态不存在或已下线"
        description="可以返回动态列表查看其他社区新闻与活动更新。"
      >
        <template #actions>
          <el-button type="primary" @click="router.push(PORTAL_PATHS.news)">返回动态列表</el-button>
        </template>
      </StatePanel>
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { dynamicDetailApi, type DynamicModel } from '@/api/content'
import RichTextRenderer from '@/components/shared/RichTextRenderer.vue'
import StatePanel from '@/components/shared/StatePanel.vue'
import WorkspaceHero from '@/components/shared/WorkspaceHero.vue'
import { PORTAL_PATHS } from '@/constants/portal-routes'
import { formatDateTime } from '@/utils/display'
import PortalNavBar from './PortalNavBar.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const article = ref<DynamicModel>()

function typeLabel(type?: string) {
  return type === 'DYNAMIC' ? '活动动态' : '社区新闻'
}

onMounted(async () => {
  loading.value = true
  try {
    article.value = await dynamicDetailApi(Number(route.params.id))
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.portal-article-wrap {
  width: min(1080px, calc(100% - 24px));
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

.hero-image {
  width: 100%;
  max-height: 360px;
  object-fit: cover;
  border-radius: 16px;
  margin-bottom: 14px;
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
  font-size: clamp(28px, 4vw, 42px);
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
