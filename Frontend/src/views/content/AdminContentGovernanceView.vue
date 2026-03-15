<template>
  <AdminListScaffold
    title="内容治理"
    description="按业务域统一维护信息动态、系统公告和轮播图，不再与论坛治理和商城治理混放在同一入口。"
  >
    <el-tabs v-model="tab" class="domain-tabs">
      <el-tab-pane label="信息动态" name="dynamic">
        <DynamicManageSection v-if="shouldRender('dynamic')" />
      </el-tab-pane>
      <el-tab-pane label="系统公告" name="notice">
        <NoticeManageSection v-if="shouldRender('notice')" />
      </el-tab-pane>
      <el-tab-pane label="轮播图" name="banner">
        <BannerManageSection v-if="shouldRender('banner')" />
      </el-tab-pane>
    </el-tabs>
  </AdminListScaffold>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminListScaffold from '@/components/admin/AdminListScaffold.vue'
import { normalizeContentTab, type ContentTabKey } from '@/constants/admin-navigation'
import BannerManageSection from './components/BannerManageSection.vue'
import DynamicManageSection from './components/DynamicManageSection.vue'
import NoticeManageSection from './components/NoticeManageSection.vue'

const route = useRoute()
const router = useRouter()
const tab = ref<ContentTabKey>('dynamic')
const loadedTabs = reactive<Record<ContentTabKey, boolean>>({
  dynamic: true,
  notice: false,
  banner: false
})

function shouldRender(name: ContentTabKey) {
  return tab.value === name || loadedTabs[name]
}

watch(
  () => route.query.tab,
  (value) => {
    const nextTab = normalizeContentTab(value)
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
