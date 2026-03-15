<template>
  <div class="page-shell">
    <WorkspaceHero
      compact
      eyebrow="我的评论"
      title="统一查看个人评论与关联对象"
      description="集中回看你在论坛和活动中的发言记录，快速定位评论对象与发表时间。"
    >
      <template #actions>
        <el-button type="primary" @click="load">刷新评论</el-button>
        <el-button @click="router.push(PORTAL_PATHS.selfServicePosts)">查看我的帖子</el-button>
        <el-button @click="router.push(PORTAL_PATHS.activities)">前往活动中心</el-button>
      </template>
    </WorkspaceHero>

    <VolunteerPageSection eyebrow="评论记录" title="我的评论列表" description="统一查看评论类型、关联对象、内容摘要与发表时间。">
      <StatePanel
        v-if="loading"
        state="loading"
        title="正在同步评论记录"
        description="正在加载你在论坛与活动中的评论内容。"
      />

      <template v-else-if="list.length">
        <el-table :data="list" border>
          <el-table-column label="评论类型" width="140">
            <template #default="{ row }">{{ getCommentTargetLabel(row.targetType) }}</template>
          </el-table-column>
          <el-table-column prop="targetId" label="关联 ID" width="100" />
          <el-table-column label="内容" min-width="260">
            <template #default="{ row }">{{ commentPreview(row.content) }}</template>
          </el-table-column>
          <el-table-column label="时间" min-width="180">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
        </el-table>
      </template>

      <StatePanel
        v-else
        title="暂无评论记录"
        description="在论坛发帖、回帖或参与活动评价后，这里会沉淀你的个人评论。"
      />

      <template #footer>
        <el-pagination
          v-if="!loading && total > 0"
          layout="total, prev, pager, next"
          :total="total"
          :current-page="query.current"
          :page-size="query.size"
          @current-change="handlePage"
        />
      </template>
    </VolunteerPageSection>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { pageCommentsApi, type CommentModel } from '@/api/content'
import StatePanel from '@/components/shared/StatePanel.vue'
import WorkspaceHero from '@/components/shared/WorkspaceHero.vue'
import VolunteerPageSection from '@/components/volunteer/VolunteerPageSection.vue'
import { useTable } from '@/composables/useTable'
import { PORTAL_PATHS } from '@/constants/portal-routes'
import { formatDateTime, getCommentTargetLabel } from '@/utils/display'
import { richTextToPlainText } from '@/utils/rich-text'

interface MyCommentQuery {
  current: number
  size: number
}

const router = useRouter()

const { loading, records: list, total, query, load, handlePage } = useTable<CommentModel, MyCommentQuery>({
  initialQuery: {
    current: 1,
    size: 10
  },
  fetcher: (params) =>
    pageCommentsApi({
      current: params.current,
      size: params.size,
      onlyMine: true
    })
})

function commentPreview(content?: string) {
  return richTextToPlainText(content) || '图片或富文本评论'
}

onMounted(load)
</script>

<style scoped>
.page-shell {
  display: grid;
  gap: 14px;
}
</style>
