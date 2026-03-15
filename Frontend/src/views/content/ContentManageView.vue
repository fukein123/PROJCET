<template>
  <DynamicManageSection v-if="currentTab === 'dynamic'" />
  <NoticeManageSection v-else-if="currentTab === 'notice'" />
  <BannerManageSection v-else-if="currentTab === 'banner'" />
  <ForumCategoryManageSection v-else-if="currentTab === 'forumCategory'" />
  <PostModerationSection v-else-if="currentTab === 'post'" />
  <CommentManageSection v-else-if="currentTab === 'comment'" />
  <MallProductManageSection v-else-if="currentTab === 'mallProduct'" />
  <ExchangeOrderManageSection v-else />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { normalizeContentTab, type ContentTabKey } from '@/constants/admin-navigation'
import BannerManageSection from './components/BannerManageSection.vue'
import CommentManageSection from './components/CommentManageSection.vue'
import DynamicManageSection from './components/DynamicManageSection.vue'
import ExchangeOrderManageSection from './components/ExchangeOrderManageSection.vue'
import ForumCategoryManageSection from './components/ForumCategoryManageSection.vue'
import MallProductManageSection from './components/MallProductManageSection.vue'
import NoticeManageSection from './components/NoticeManageSection.vue'
import PostModerationSection from './components/PostModerationSection.vue'

const route = useRoute()
const currentTab = computed<ContentTabKey>(() => normalizeContentTab(route.query.tab))
</script>
