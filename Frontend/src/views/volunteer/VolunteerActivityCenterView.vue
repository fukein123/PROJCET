<template>
  <div class="fade-up">
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

    <el-card class="module" shadow="never">
      <el-table :data="list" border>
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

      <div class="footer">
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
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import SearchForm from '@/components/SearchForm.vue'
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

interface VolunteerActivityQuery {
  current: number
  size: number
  keyword: string
  categoryId?: number
}

const router = useRouter()
const categories = ref<ActivityCategory[]>([])

const { records: list, total, query, load, reset, handlePage, handleSizeChange } = useTable<
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
  await applyActivityApi(activityId)
  ElMessage.success('报名申请已提交，请等待审核')
}

async function favorite(activityId: number) {
  await createFavoriteApi({ activityId })
  ElMessage.success('已加入收藏')
}

onMounted(async () => {
  categories.value = await listCategoriesApi()
  await load()
})
</script>

<style scoped>
.module {
  border: 1px solid var(--cvs-border);
  border-radius: 16px;
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
