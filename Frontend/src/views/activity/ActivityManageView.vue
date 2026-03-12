<template>
  <div class="fade-up">
    <SearchForm @search="load" @reset="resetQuery">
      <el-form-item label="关键词">
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
          <div class="head-actions">
            <el-button type="danger" plain :disabled="!selectedIds.length" @click="batchRemove">批量删除</el-button>
            <el-button type="primary" @click="openCreate">新增活动</el-button>
          </div>
        </div>
      </template>
      <el-table :data="list" border v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="48" />
        <el-table-column label="封面" width="96">
          <template #default="{ row }">
            <img class="cover-mini" :src="row.coverImage || defaultCover" alt="活动封面" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="活动名称" min-width="180" />
        <el-table-column prop="content" label="活动内容" min-width="180" show-overflow-tooltip />
        <el-table-column prop="address" label="活动地点" min-width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="volunteerQuota" label="志愿者人数" width="110" />
        <el-table-column prop="targetCount" label="目标人数" width="100" />
        <el-table-column label="时间" min-width="220">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="190">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="removeOne(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="footer">
        <el-pagination
          layout="total, sizes, prev, pager, next"
          :current-page="query.current"
          :page-size="query.size"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          @current-change="handlePage"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-drawer
      v-model="drawerVisible"
      :title="editingId ? '编辑活动' : '新增活动'"
      size="min(92vw, 920px)"
      append-to-body
      class="activity-drawer"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="drawer-form">
        <div class="form-grid">
          <el-form-item label="活动名称" prop="title" class="span-2">
            <el-input v-model="form.title" />
          </el-form-item>

          <el-form-item label="活动分类" prop="categoryId">
            <el-select v-model="form.categoryId">
              <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status">
              <el-option label="已发布" value="PUBLISHED" />
              <el-option label="进行中" value="ONGOING" />
              <el-option label="已结束" value="ENDED" />
            </el-select>
          </el-form-item>

          <el-form-item label="活动时间（范围）" class="span-2">
            <el-date-picker
              v-model="activityRange"
              type="datetimerange"
              value-format="YYYY-MM-DD HH:mm:ss"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              @change="syncRangeToTime"
            />
          </el-form-item>

          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
          </el-form-item>
          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
          </el-form-item>

          <el-form-item label="活动内容" prop="content" class="span-2">
            <el-input
              v-model="form.content"
              maxlength="200"
              show-word-limit
              placeholder="例如：河道垃圾清理、分类回收与环保宣导"
            />
          </el-form-item>

          <el-form-item label="活动地点" prop="address" class="span-2">
            <el-input v-model="form.address" />
          </el-form-item>

          <el-form-item label="志愿者人数" prop="volunteerQuota">
            <el-input-number v-model="form.volunteerQuota" :min="1" />
          </el-form-item>
          <el-form-item label="目标人数" prop="targetCount">
            <el-input-number v-model="form.targetCount" :min="1" />
          </el-form-item>

          <el-form-item label="活动封面" class="span-2">
            <div class="uploader">
              <img :src="form.coverImage || defaultCover" class="cover-preview" alt="活动封面" />
              <div class="uploader-actions">
                <el-upload
                  :show-file-list="false"
                  :http-request="handleCoverUpload"
                  :before-upload="beforeCoverUpload"
                  accept="image/png,image/jpeg,image/jpg,image/webp,image/gif"
                >
                  <el-button :loading="coverUploading">上传封面</el-button>
                </el-upload>
                <el-button text @click="form.coverImage = ''">清空封面</el-button>
              </div>
            </div>
          </el-form-item>

          <el-form-item label="活动详细描述" prop="description" class="span-2">
            <el-input
              v-model="form.description"
              type="textarea"
              rows="9"
              placeholder="建议包含：活动背景、服务流程、注意事项、联系人、物资准备与应急预案"
            />
          </el-form-item>
        </div>
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
import type { FormInstance, FormRules, UploadProps, UploadRequestOptions } from 'element-plus'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import SearchForm from '@/components/SearchForm.vue'
import { uploadImageApi } from '@/api/common'
import {
  batchDeleteActivitiesApi,
  createActivityApi,
  deleteActivityApi,
  listCategoriesApi,
  pageActivitiesApi,
  updateActivityApi,
  type ActivityCategory,
  type ActivityModel
} from '@/api/activity'

const defaultCover =
  'https://images.unsplash.com/photo-1509062522246-3755977927d7?auto=format&fit=crop&w=700&q=80'

const loading = ref(false)
const list = ref<ActivityModel[]>([])
const total = ref(0)
const selectedIds = ref<number[]>([])
const query = reactive({
  current: 1,
  size: 10,
  keyword: '',
  status: ''
})

const drawerVisible = ref(false)
const editingId = ref<number>()
const categories = ref<ActivityCategory[]>([])
const coverUploading = ref(false)
const formRef = ref<FormInstance>()
const activityRange = ref<[string, string] | []>([])

const form = reactive<Partial<ActivityModel>>({
  title: '',
  categoryId: undefined,
  startTime: '',
  endTime: '',
  address: '',
  status: 'PUBLISHED',
  targetCount: 20,
  volunteerQuota: 20,
  content: '',
  description: '',
  coverImage: ''
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择活动分类', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  content: [{ required: true, message: '请输入活动内容', trigger: 'blur' }],
  address: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
  volunteerQuota: [{ required: true, message: '请输入志愿者人数', trigger: 'change' }],
  targetCount: [{ required: true, message: '请输入目标人数', trigger: 'change' }],
  description: [{ required: true, message: '请填写活动详细描述', trigger: 'blur' }]
}

function syncRangeToTime(value: [string, string] | null) {
  if (!value) return
  form.startTime = value[0]
  form.endTime = value[1]
}

function handleSelectionChange(rows: ActivityModel[]) {
  selectedIds.value = rows.map((row) => row.id)
}

function formatTime(value: string) {
  return dayjs(value).format('YYYY-MM-DD HH:mm')
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

const beforeCoverUpload: UploadProps['beforeUpload'] = (rawFile) => {
  if (!rawFile.type.startsWith('image/')) {
    ElMessage.warning('仅支持上传图片文件')
    return false
  }
  if (rawFile.size / 1024 / 1024 > 5) {
    ElMessage.warning('图片大小不能超过 5MB')
    return false
  }
  return true
}

async function handleCoverUpload(option: UploadRequestOptions) {
  coverUploading.value = true
  try {
    const file = option.file as File
    const res = await uploadImageApi(file)
    form.coverImage = res.url
    ElMessage.success('封面上传成功')
    option.onSuccess?.(res)
  } catch (error) {
    option.onError?.(error as any)
  } finally {
    coverUploading.value = false
  }
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

function handleSizeChange(size: number) {
  query.size = size
  query.current = 1
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
    volunteerQuota: 20,
    content: '',
    description: '',
    coverImage: ''
  })
  activityRange.value = []
  drawerVisible.value = true
}

function openEdit(row: ActivityModel) {
  editingId.value = row.id
  Object.assign(form, row)
  activityRange.value = [row.startTime, row.endTime]
  drawerVisible.value = true
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate()
  if (dayjs(form.endTime as string).isBefore(dayjs(form.startTime as string))) {
    ElMessage.warning('结束时间必须晚于开始时间')
    return
  }
  if (editingId.value) {
    await updateActivityApi(editingId.value, form)
  } else {
    await createActivityApi(form)
  }
  drawerVisible.value = false
  ElMessage.success(editingId.value ? '活动更新成功' : '活动创建成功')
  await load()
}

async function removeOne(id: number) {
  await ElMessageBox.confirm('删除后将同步清理报名、打卡与收藏记录，确认继续？', '删除活动', {
    type: 'warning'
  })
  await deleteActivityApi(id)
  ElMessage.success('活动已删除')
  await load()
}

async function batchRemove() {
  if (!selectedIds.value.length) return
  await ElMessageBox.confirm(`确认批量删除 ${selectedIds.value.length} 个活动？`, '批量删除活动', {
    type: 'warning'
  })
  await batchDeleteActivitiesApi(selectedIds.value)
  ElMessage.success('批量删除成功')
  selectedIds.value = []
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
  align-items: center;
}

.head-actions {
  display: flex;
  gap: 8px;
}

.footer {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.cover-mini {
  width: 62px;
  height: 42px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid var(--cvs-border);
}

.drawer-form {
  padding-right: 4px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.span-2 {
  grid-column: span 2;
}

.uploader {
  display: grid;
  grid-template-columns: 160px 1fr;
  gap: 12px;
  align-items: center;
}

.cover-preview {
  width: 160px;
  height: 100px;
  border-radius: 12px;
  border: 1px solid var(--cvs-border);
  object-fit: cover;
}

.uploader-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

:deep(.activity-drawer .el-drawer__body) {
  overflow: auto;
}

@media (max-width: 900px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .span-2 {
    grid-column: span 1;
  }

  .uploader {
    grid-template-columns: 1fr;
  }
}
</style>
