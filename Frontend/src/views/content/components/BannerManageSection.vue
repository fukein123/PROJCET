<template>
  <AdminContentSection
    title="轮播图信息"
    description="维护门户首页轮播图、关联活动、排序和启停状态，支持单条删除与批量删除。"
    :loading="loading && !records.length"
    :empty="!loading && !records.length"
    loading-title="正在加载轮播图信息"
    loading-description="请稍候，系统正在同步首页轮播配置。"
    empty-title="当前暂无轮播图"
    empty-description="新增轮播图后，门户首页会展示这里配置的内容。"
  >
    <template #actions>
      <el-button type="danger" plain :disabled="!selectedIds.length" @click="batchDelete">批量删除</el-button>
      <el-button type="primary" @click="openCreate">新增轮播图</el-button>
    </template>

    <template #emptyActions>
      <el-button type="primary" @click="openCreate">立即新增轮播图</el-button>
    </template>

    <SearchForm @search="load" @reset="resetQuery">
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable style="width: 160px">
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model.trim="query.keyword" placeholder="标题 / 关联活动" />
      </el-form-item>
    </SearchForm>

    <el-table :data="filteredRecords" border v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" />
      <el-table-column label="轮播图" width="100">
        <template #default="{ row }">
          <img class="thumb" :src="row.imageUrl || bannerFallback" alt="轮播图" />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="活动名称" min-width="220" />
      <el-table-column label="关联活动" min-width="220">
        <template #default="{ row }">
          {{ activityLabel(row.activityId) }}
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="170" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <div class="action-cell">
            <el-tooltip content="编辑轮播图" placement="top">
              <el-button link type="primary" :icon="EditPen" @click="openEdit(row)" />
            </el-tooltip>
            <el-tooltip content="删除轮播图" placement="top">
              <el-button link type="danger" :icon="Delete" @click="remove(row.id)" />
            </el-tooltip>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </AdminContentSection>

  <el-dialog
    v-model="visible"
    :title="form.id ? '编辑轮播图' : '新增轮播图'"
    width="min(92vw, 760px)"
    top="6vh"
    append-to-body
    class="manage-dialog"
  >
    <el-form :model="form" label-position="top">
      <el-form-item label="活动名称" required>
        <el-input
          v-model.trim="form.title"
          maxlength="128"
          show-word-limit
          placeholder="请输入轮播图标题或活动名称"
        />
      </el-form-item>
      <el-form-item label="关联活动">
        <el-select
          :model-value="form.activityId"
          clearable
          filterable
          placeholder="可选，关联后门户首页可直接跳转活动详情"
          @update:model-value="applyActivity"
        >
          <el-option v-for="item in activityOptions" :key="item.id" :label="item.title" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sort" :min="0" />
      </el-form-item>
      <el-form-item label="状态">
        <el-switch v-model="statusSwitch" />
      </el-form-item>
      <el-form-item label="轮播图图片" required>
        <div class="uploader">
          <img class="thumb-lg" :src="form.imageUrl || bannerFallback" alt="轮播图预览" />
          <el-upload
            :show-file-list="false"
            :http-request="handleImageUpload"
            :before-upload="beforeImageUpload"
            accept="image/png,image/jpeg,image/jpg,image/webp,image/gif"
          >
            <el-button :loading="imageUploading">上传图片</el-button>
          </el-upload>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import type { UploadProps, UploadRequestOptions } from 'element-plus'
import { ElMessage } from 'element-plus'
import { Delete, EditPen } from '@element-plus/icons-vue'
import { pageActivitiesApi, type ActivityModel } from '@/api/activity'
import { uploadImageApi } from '@/api/common'
import {
  batchDeleteBannersApi,
  deleteBannerApi,
  listManageBannersApi,
  saveBannerApi,
  updateBannerApi,
  type BannerModel
} from '@/api/content'
import SearchForm from '@/components/SearchForm.vue'
import AdminContentSection from '@/components/admin/AdminContentSection.vue'
import { useSelectionIds } from '@/composables/useSelectionIds'
import { runConfirmedAction } from '@/utils/confirmed-action'
import { validateImageFile } from '@/utils/upload'

interface BannerQuery {
  keyword: string
  status?: number
}

const bannerFallback =
  'https://images.unsplash.com/photo-1469571486292-b53601020f53?auto=format&fit=crop&w=1200&q=80'

const loading = ref(false)
const records = ref<BannerModel[]>([])
const activityOptions = ref<ActivityModel[]>([])
const { selectedIds, handleSelectionChange, clearSelection } = useSelectionIds<BannerModel>()
const visible = ref(false)
const imageUploading = ref(false)
const query = reactive<BannerQuery>({
  keyword: '',
  status: undefined
})

const form = reactive<Partial<BannerModel>>({
  id: undefined,
  title: '',
  imageUrl: '',
  activityId: undefined,
  sort: 0,
  status: 1
})

const statusSwitch = computed({
  get: () => form.status === 1,
  set: (value: boolean) => {
    form.status = value ? 1 : 0
  }
})

const filteredRecords = computed(() => {
  const keyword = query.keyword.trim().toLowerCase()

  return records.value.filter((item) => {
    if (typeof query.status === 'number' && item.status !== query.status) {
      return false
    }

    if (!keyword) {
      return true
    }

    const activityName = activityLabel(item.activityId).toLowerCase()
    return item.title.toLowerCase().includes(keyword) || activityName.includes(keyword)
  })
})

const beforeImageUpload: UploadProps['beforeUpload'] = (rawFile) => validateImageFile(rawFile)

function resetQuery() {
  clearSelection()
  query.keyword = ''
  query.status = undefined
}

async function loadActivities(force = false) {
  if (!force && activityOptions.value.length) {
    return
  }

  const res = await pageActivitiesApi({
    current: 1,
    size: 200
  })
  activityOptions.value = res.records
}

async function load() {
  clearSelection()
  loading.value = true
  try {
    await loadActivities()
    records.value = await listManageBannersApi()
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, {
    id: undefined,
    title: '',
    imageUrl: '',
    activityId: undefined,
    sort: 0,
    status: 1
  })
}

function openCreate() {
  resetForm()
  visible.value = true
  void loadActivities()
}

function openEdit(row: BannerModel) {
  Object.assign(form, row)
  visible.value = true
  void loadActivities()
}

function activityLabel(activityId?: number | null) {
  if (!activityId) {
    return '未关联活动'
  }

  return activityOptions.value.find((item) => item.id === activityId)?.title || `活动#${activityId}`
}

function applyActivity(activityId?: number | null) {
  form.activityId = activityId

  if (!activityId || (form.title || '').trim()) {
    return
  }

  form.title = activityLabel(activityId)
}

async function handleImageUpload(option: UploadRequestOptions) {
  imageUploading.value = true
  try {
    const file = option.file as File
    const res = await uploadImageApi(file)
    form.imageUrl = res.url
    ElMessage.success('轮播图上传成功')
    option.onSuccess?.(res)
  } catch (error) {
    option.onError?.(error as never)
  } finally {
    imageUploading.value = false
  }
}

async function submit() {
  form.title = (form.title || '').trim()
  form.imageUrl = (form.imageUrl || '').trim()
  form.sort = Math.max(0, Number(form.sort || 0))

  if (!form.title) {
    ElMessage.warning('请输入轮播图标题')
    return
  }

  if (!form.imageUrl) {
    ElMessage.warning('请先上传轮播图图片')
    return
  }

  const payload = {
    ...form,
    activityId: form.activityId || null,
    sort: Number(form.sort || 0)
  }

  if (form.id) {
    await updateBannerApi(form.id, payload)
  } else {
    await saveBannerApi(payload)
  }

  visible.value = false
  ElMessage.success('轮播图保存成功')
  await load()
}

async function remove(id: number) {
  await runConfirmedAction({
    message: '确认删除这张轮播图吗？',
    title: '删除轮播图',
    type: 'warning',
    action: () => deleteBannerApi(id),
    successMessage: '轮播图已删除',
    afterSuccess: () => load()
  })
}

async function batchDelete() {
  if (!selectedIds.value.length) {
    return
  }

  await runConfirmedAction({
    message: `确认批量删除 ${selectedIds.value.length} 张轮播图吗？`,
    title: '批量删除轮播图',
    type: 'warning',
    action: () => batchDeleteBannersApi(selectedIds.value),
    successMessage: '轮播图已批量删除',
    afterSuccess: async () => {
      clearSelection()
      await load()
    }
  })
}

void load()
</script>

<style scoped>
.action-cell {
  display: flex;
  align-items: center;
  gap: 4px;
}

.thumb {
  width: 62px;
  height: 42px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid var(--cvs-border);
}

.uploader {
  display: flex;
  gap: 12px;
  align-items: center;
}

.thumb-lg {
  width: 160px;
  height: 94px;
  border-radius: 10px;
  object-fit: cover;
  border: 1px solid var(--cvs-border);
}

:deep(.manage-dialog .el-dialog__body) {
  max-height: calc(100vh - 260px);
  overflow: auto;
}
</style>
