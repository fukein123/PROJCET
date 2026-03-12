<template>
  <div class="fade-up">
    <SearchForm @search="load" @reset="reset">
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
            <img class="cover-mini" :src="row.coverImage || defaultCover" alt="活动封面" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="活动名称" min-width="180" />
        <el-table-column prop="address" label="活动地址" min-width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetCount" label="目标人数" width="90" />
        <el-table-column prop="description" label="活动说明" min-width="240" show-overflow-tooltip />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" link @click="apply(row.id)">报名</el-button>
            <el-button type="warning" link @click="favorite(row.id)">收藏</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="footer">
        <el-pagination
          layout="total, prev, pager, next"
          :total="total"
          :current-page="query.current"
          :page-size="query.size"
          @current-change="handlePage"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import SearchForm from '@/components/SearchForm.vue'
import {
  applyActivityApi,
  listCategoriesApi,
  pageActivitiesApi,
  type ActivityCategory,
  type ActivityModel
} from '@/api/activity'
import { createFavoriteApi } from '@/api/content'

const defaultCover =
  'https://images.unsplash.com/photo-1509062522246-3755977927d7?auto=format&fit=crop&w=700&q=80'

const categories = ref<ActivityCategory[]>([])
const list = ref<ActivityModel[]>([])
const total = ref(0)
const query = reactive({
  current: 1,
  size: 10,
  keyword: '',
  categoryId: undefined as number | undefined
})

async function load() {
  const res = await pageActivitiesApi({
    current: query.current,
    size: query.size,
    keyword: query.keyword || undefined,
    categoryId: query.categoryId
  })
  list.value = res.records
  total.value = res.total
}

function handlePage(page: number) {
  query.current = page
  load()
}

function reset() {
  query.current = 1
  query.keyword = ''
  query.categoryId = undefined
  load()
}

function statusLabel(status: string) {
  if (status === 'PUBLISHED') return '报名中'
  if (status === 'ONGOING') return '进行中'
  if (status === 'ENDED') return '已结束'
  return status
}

function statusTag(status: string) {
  if (status === 'PUBLISHED') return 'success'
  if (status === 'ONGOING') return 'warning'
  if (status === 'ENDED') return 'info'
  return undefined
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
