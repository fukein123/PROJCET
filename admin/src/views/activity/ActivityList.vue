<template>
  <div class="cvs-page">
    <div class="toolbar cvs-card">
      <div class="left">
        <el-input v-model="q.keyword" placeholder="按活动名称搜索" style="max-width: 320px" />
        <el-select v-model="q.category" placeholder="活动类型" style="width: 160px">
          <el-option label="全部" value="" />
          <el-option label="文化娱乐" value="1" />
          <el-option label="环境保护" value="2" />
          <el-option label="医疗服务" value="3" />
          <el-option label="教育助学" value="4" />
        </el-select>
        <el-button type="primary" @click="onSearch">查询</el-button>
      </div>
      <div class="right">
        <el-button v-if="isAdmin" type="primary" plain>发布活动</el-button>
      </div>
    </div>

    <div class="list cvs-card">
      <el-table :data="rows" stripe>
        <el-table-column prop="title" label="活动名称" min-width="180" />
        <el-table-column prop="category" label="类型" width="120" />
        <el-table-column prop="time" label="时间" min-width="200" />
        <el-table-column prop="address" label="地址" min-width="220" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column label="操作" width="180">
          <template #default>
            <el-button link type="primary">详情</el-button>
            <el-button v-if="!isAdmin" link type="primary">报名</el-button>
            <el-button v-if="isAdmin" link type="primary">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useUserStore } from '@/stores/userStore'

const user = useUserStore()
const isAdmin = computed(() => user.role === 'ADMIN' || user.role === 'COMMUNITY_ADMIN')

const q = reactive({ keyword: '', category: '' })
const rows = ref([
  {
    title: '青少年红色文学传承',
    category: '文化娱乐',
    time: '2026-03-12 09:00 ~ 2026-03-12 12:00',
    address: 'XX社区活动中心',
    status: 'PUBLISHED'
  }
])

function onSearch() {
  // TODO: 接入后端分页查询
}
</script>

<style scoped>
.toolbar {
  padding: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.list {
  margin-top: 14px;
  padding: 10px;
}
</style>

