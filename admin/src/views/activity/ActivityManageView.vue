<template>
  <div class="fade-up">
    <SearchForm @search="load" @reset="resetQuery">
      <el-form-item label="关键字">
        <el-input v-model="query.keyword" placeholder="活动名称" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable style="width: 140px">
          <el-option label="已发布" value="PUBLISHED" />
          <el-option label="进行中" value="ONGOING" />
          <el-option label="已结束" value="ENDED" />
        </el-select>
      </el-form-item>
    </SearchForm>

    <el-card class="module" shadow="never">
      <template #header>
        <div class="head">
          <span>志愿活动管理</span>
          <el-button type="primary" @click="openCreate">新增活动</el-button>
        </div>
      </template>
      <el-table :data="list" border v-loading="loading">
        <el-table-column prop="title" label="活动名称" min-width="180" />
        <el-table-column prop="address" label="活动地址" min-width="180" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="targetCount" label="目标人数" width="90" />
        <el-table-column label="时间" min-width="220">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="footer">
        <el-pagination
          layout="total, prev, pager, next"
          :current-page="query.current"
          :page-size="query.size"
          :total="total"
          @current-change="handlePage"
        />
      </div>
    </el-card>

    <el-drawer v-model="drawerVisible" :title="editingId ? '编辑活动' : '新增活动'" size="560px">
      <el-form :model="form" label-width="98px">
        <el-form-item label="活动名称">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="活动分类">
          <el-select v-model="form.categoryId">
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="活动地址">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="目标人数">
          <el-input-number v-model="form.targetCount" :min="1" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="进行中" value="ONGOING" />
            <el-option label="已结束" value="ENDED" />
          </el-select>
        </el-form-item>
        <el-form-item label="纬度">
          <el-input-number v-model="form.latitude" :precision="6" :step="0.000001" />
        </el-form-item>
        <el-form-item label="经度">
          <el-input-number v-model="form.longitude" :precision="6" :step="0.000001" />
        </el-form-item>
        <el-form-item label="活动描述">
          <el-input v-model="form.description" type="textarea" rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="drawerVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import SearchForm from '@/components/SearchForm.vue'
import {
  createActivityApi,
  listCategoriesApi,
  pageActivitiesApi,
  updateActivityApi,
  type ActivityCategory,
  type ActivityModel
} from '@/api/activity'

const loading = ref(false)
const list = ref<ActivityModel[]>([])
const total = ref(0)
const query = reactive({
  current: 1,
  size: 10,
  keyword: '',
  status: ''
})

const drawerVisible = ref(false)
const editingId = ref<number>()
const categories = ref<ActivityCategory[]>([])
const form = reactive<Partial<ActivityModel>>({
  title: '',
  categoryId: undefined,
  startTime: '',
  endTime: '',
  address: '',
  status: 'PUBLISHED',
  targetCount: 20,
  description: '',
  latitude: 31.2304,
  longitude: 121.4737
})

function formatTime(value: string) {
  return dayjs(value).format('YYYY-MM-DD HH:mm')
}

async function load() {
  loading.value = true
  try {
    const res = await pageActivitiesApi({
      current: query.current,
      size: query.size,
      keyword: query.keyword || undefined,
      status: query.status || undefined
    })
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function handlePage(page: number) {
  query.current = page
  load()
}

function resetQuery() {
  query.current = 1
  query.keyword = ''
  query.status = ''
  load()
}

function openCreate() {
  editingId.value = undefined
  Object.assign(form, {
    title: '',
    categoryId: categories.value[0]?.id,
    startTime: '',
    endTime: '',
    address: '',
    status: 'PUBLISHED',
    targetCount: 20,
    description: '',
    latitude: 31.2304,
    longitude: 121.4737
  })
  drawerVisible.value = true
}

function openEdit(row: ActivityModel) {
  editingId.value = row.id
  Object.assign(form, row)
  drawerVisible.value = true
}

async function submit() {
  if (editingId.value) {
    await updateActivityApi(editingId.value, form)
  } else {
    await createActivityApi(form)
  }
  drawerVisible.value = false
  await load()
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

.head {
  display: flex;
  justify-content: space-between;
}

.footer {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>

