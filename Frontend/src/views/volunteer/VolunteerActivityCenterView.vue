<template>
  <div class="activity-center fade-up">
    <WorkspaceHero
      compact
      eyebrow="活动中心"
      title="统一查看、报名与收藏社区志愿活动"
      description="保持与门户端一致的页头层级和状态反馈，在工作台内完成活动筛选、报名与收藏操作。"
    >
      <template #actions>
        <el-button type="primary" @click="load">刷新活动</el-button>
        <el-button @click="router.push('/volunteer/apply-records')">报名记录</el-button>
        <el-button @click="router.push('/volunteer/my-favorites')">我的收藏</el-button>
      </template>
    </WorkspaceHero>

    <section class="module-card">
      <div class="module-head">
        <div>
          <p class="module-eyebrow">筛选与操作</p>
          <h2 class="section-title">活动列表</h2>
        </div>
      </div>

      <SearchForm @search="load" @reset="resetQuery">
        <el-form-item label="活动名称">
          <el-input v-model="query.keyword" placeholder="活动名称" />
        </el-form-item>

        <el-form-item label="活动分类">
          <el-select v-model="query.categoryId" clearable style="width: 160px">
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
      </SearchForm>

      <StatePanel
        v-if="loading"
        state="loading"
        title="正在加载活动"
        description="正在同步符合筛选条件的社区志愿活动。"
      />
      <el-table v-else-if="list.length" :data="list" border>
        <el-table-column label="封面" width="100">
          <template #default="{ row }">
            <img class="cover-mini" :src="row.coverImage || DEFAULT_ACTIVITY_COVER" alt="活动封面" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="活动名称" min-width="180" />
        <el-table-column prop="content" label="活动内容" min-width="180" show-overflow-tooltip />
        <el-table-column prop="address" label="活动地点" min-width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getActivityStatusTag(row.status)">{{ getActivityStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="volunteerQuota" label="志愿者人数" width="110" />
        <el-table-column prop="targetCount" label="目标人数" width="100" />
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button type="primary" link @click="toDetail(row.id)">查看详情</el-button>
            <el-button type="success" link @click="apply(row.id)">报名</el-button>
            <el-button type="warning" link @click="favorite(row.id)">收藏</el-button>
          </template>
        </el-table-column>
      </el-table>
      <StatePanel
        v-else
        title="暂无匹配活动"
        description="可以调整筛选条件，或稍后再查看新的活动发布。"
      />

      <div v-if="!loading && total > 0" class="footer">
        <el-pagination
          layout="total, sizes, prev, pager, next"
          :total="total"
          :current-page="query.current"
          :page-size="query.size"
          :page-sizes="[10, 20, 30, 50]"
          @current-change="handlePage"
          @size-change="handleSizeChange"
        />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import SearchForm from '@/components/SearchForm.vue'
import StatePanel from '@/components/shared/StatePanel.vue'
import WorkspaceHero from '@/components/shared/WorkspaceHero.vue'
import {
  applyActivityApi,
  listCategoriesApi,
  pageActivitiesApi,
  type ActivityCategory,
  type ActivityModel
} from '@/api/activity'
import { createFavoriteApi } from '@/api/content'
import { useTable } from '@/composables/useTable'
import { DEFAULT_ACTIVITY_COVER, getActivityStatusLabel, getActivityStatusTag } from '@/utils/display'
import { runConfirmedAction } from '@/utils/confirmed-action'

interface VolunteerActivityQuery {
  current: number
  size: number
  keyword: string
  categoryId?: number
}

const router = useRouter()
const categories = ref<ActivityCategory[]>([])

const { loading, records: list, total, query, load, reset, handlePage, handleSizeChange } = useTable<
  ActivityModel,
  VolunteerActivityQuery
>({
  initialQuery: {
    current: 1,
    size: 10,
    keyword: '',
    categoryId: undefined
  },
  fetcher: (params) =>
    pageActivitiesApi({
      current: params.current,
      size: params.size,
      keyword: params.keyword || undefined,
      categoryId: params.categoryId
    })
})

function resetQuery() {
  reset({
    categoryId: undefined
  })
}

function toDetail(activityId: number) {
  router.push(`/volunteer/activity-detail/${activityId}`)
}

async function apply(activityId: number) {
  await runConfirmedAction({
    message: '确认提交该活动的报名申请吗？',
    title: '报名活动',
    type: 'info',
    confirmButtonText: '确认报名',
    action: () => applyActivityApi(activityId),
    successMessage: '报名申请已提交，请等待审核',
    afterSuccess: load
  })
}

async function favorite(activityId: number) {
  await runConfirmedAction({
    message: '确认将该活动加入收藏吗？',
    title: '加入收藏',
    type: 'info',
    confirmButtonText: '确认收藏',
    action: () => createFavoriteApi({ activityId }),
    successMessage: '已加入收藏',
    afterSuccess: load
  })
}

onMounted(async () => {
  categories.value = await listCategoriesApi()
  await load()
})
</script>

<style scoped>
.activity-center {
  display: grid;
  gap: 14px;
}

.module-card {
  border: 1px solid var(--cvs-border);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.94);
  padding: 18px;
  box-shadow: var(--cvs-shadow-soft);
}

.module-head {
  margin-bottom: 14px;
}

.module-eyebrow {
  margin: 0 0 6px;
  color: #1f7a54;
  font-size: var(--cvs-font-size-xs);
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.module-head :deep(.section-title) {
  margin-bottom: 0;
}

.cover-mini {
  width: 62px;
  height: 42px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid var(--cvs-border);
}

.footer {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
