<template>
  <AdminListScaffold
    title="商城治理"
    description="集中管理积分商品与兑换订单履约，不再与动态、公告和论坛审核共用同一管理入口。"
  >
    <el-tabs v-model="tab" class="domain-tabs">
      <el-tab-pane label="商城商品" name="mallProduct">
        <MallProductManageSection v-if="shouldRender('mallProduct')" />
      </el-tab-pane>
      <el-tab-pane label="兑换订单" name="exchangeOrder">
        <ExchangeOrderManageSection v-if="shouldRender('exchangeOrder')" />
      </el-tab-pane>
    </el-tabs>
  </AdminListScaffold>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminListScaffold from '@/components/admin/AdminListScaffold.vue'
import { normalizeMallTab, type MallTabKey } from '@/constants/admin-navigation'
import ExchangeOrderManageSection from './components/ExchangeOrderManageSection.vue'
import MallProductManageSection from './components/MallProductManageSection.vue'

const route = useRoute()
const router = useRouter()
const tab = ref<MallTabKey>('mallProduct')
const loadedTabs = reactive<Record<MallTabKey, boolean>>({
  mallProduct: true,
  exchangeOrder: false
})

function shouldRender(name: MallTabKey) {
  return tab.value === name || loadedTabs[name]
}

watch(
  () => route.query.tab,
  (value) => {
    const nextTab = normalizeMallTab(value)
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
