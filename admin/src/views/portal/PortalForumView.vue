<template>
  <div>
    <PortalNavBar />
    <main class="portal-wrap">
      <el-card class="module" shadow="never">
        <template #header>
          <div class="head">
            <span>社区论坛（仅展示审核通过帖子）</span>
            <el-button type="primary" @click="router.push('/login')">登录后发帖</el-button>
          </div>
        </template>
        <el-table :data="posts" border>
          <el-table-column prop="title" label="标题" min-width="200" />
          <el-table-column prop="views" label="浏览量" width="100" />
          <el-table-column label="内容" min-width="260">
            <template #default="{ row }">
              {{ row.content }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { pageForumPostsApi, type PostModel } from '@/api/content'
import PortalNavBar from './PortalNavBar.vue'

const router = useRouter()
const posts = ref<PostModel[]>([])

onMounted(async () => {
  const res = await pageForumPostsApi({
    current: 1,
    size: 30,
    onlyApproved: true
  })
  posts.value = res.records
})
</script>

<style scoped>
.portal-wrap {
  width: min(1200px, calc(100% - 24px));
  margin: 14px auto 40px;
}

.module {
  border-radius: 16px;
  border: 1px solid var(--cvs-border);
}

.head {
  display: flex;
  justify-content: space-between;
}
</style>

