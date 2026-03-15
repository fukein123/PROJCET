<template>
  <AdminListScaffold
    title="用户管理"
    description="按角色拆分维护志愿者与管理员账号，列表默认脱敏显示联系方式，详情与审核动作在标准弹窗内完成。"
  >
    <el-tabs v-model="tab" class="user-tabs">
      <el-tab-pane label="志愿者信息" name="volunteer">
        <UserManageSection v-if="shouldRender('volunteer')" role="VOLUNTEER" />
      </el-tab-pane>
      <el-tab-pane label="管理员信息" name="admin">
        <UserManageSection v-if="shouldRender('admin')" role="ADMIN" />
      </el-tab-pane>
    </el-tabs>
  </AdminListScaffold>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminListScaffold from '@/components/admin/AdminListScaffold.vue'
import { normalizeUserTab, type UserTabKey } from '@/constants/admin-navigation'
import UserManageSection from './components/UserManageSection.vue'

const route = useRoute()
const router = useRouter()
const tab = ref<UserTabKey>('volunteer')
const loadedTabs = reactive<Record<UserTabKey, boolean>>({
  volunteer: true,
  admin: false
})

function shouldRender(name: UserTabKey) {
  return tab.value === name || loadedTabs[name]
}

watch(
  () => route.query.tab,
  (value) => {
    const nextTab = normalizeUserTab(value)
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
.user-tabs :deep(.el-tabs__header) {
  margin-bottom: var(--cvs-space-3);
}
</style>
