<template>
  <AdminListScaffold
    title="论坛治理"
    description="集中管理帖子分类、帖子审核和评论信息，避免论坛治理与内容公告、商城订单继续混用。"
  >
    <el-tabs v-model="tab" class="domain-tabs">
      <el-tab-pane label="帖子分类" name="forumCategory">
        <ForumCategoryManageSection v-if="shouldRender('forumCategory')" />
      </el-tab-pane>
      <el-tab-pane label="帖子审核" name="post">
        <PostModerationSection v-if="shouldRender('post')" />
      </el-tab-pane>
      <el-tab-pane label="评论管理" name="comment">
        <CommentManageSection v-if="shouldRender('comment')" />
      </el-tab-pane>
    </el-tabs>
  </AdminListScaffold>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminListScaffold from '@/components/admin/AdminListScaffold.vue'
import { normalizeForumTab, type ForumTabKey } from '@/constants/admin-navigation'
import CommentManageSection from './components/CommentManageSection.vue'
import ForumCategoryManageSection from './components/ForumCategoryManageSection.vue'
import PostModerationSection from './components/PostModerationSection.vue'

const route = useRoute()
const router = useRouter()
const tab = ref<ForumTabKey>('forumCategory')
const loadedTabs = reactive<Record<ForumTabKey, boolean>>({
  forumCategory: true,
  post: false,
  comment: false
})

function shouldRender(name: ForumTabKey) {
  return tab.value === name || loadedTabs[name]
}

watch(
  () => route.query.tab,
  (value) => {
    const nextTab = normalizeForumTab(value)
    tab.value = nextTab
    loadedTabs[nextTab] = true
  },
  { immediate: true }
)

watch(tab, (name) => {
  loadedTabs[name] = true
  if (route.query.tab === name) {
    return
  }
  router.replace({
    path: route.path,
    query: {
      ...route.query,
      tab: name
    }
  })
})
</script>

<style scoped>
.domain-tabs :deep(.el-tabs__header) {
  margin-bottom: var(--cvs-space-3);
}
</style>
