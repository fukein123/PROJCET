<template>
  <AdminListScaffold
    title="志愿活动管理"
    description="统一维护社区志愿活动的基础信息、封面素材与志愿者容量，列表支持分页浏览与批量治理。"
    @search="load"
    @reset="resetQuery"
  >
    <template #filters>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="活动名称" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable style="width: 140px">
          <el-option label="已发布" value="PUBLISHED" />
          <el-option label="进行中" value="ONGOING" />
          <el-option label="已结束" value="ENDED" />
          <el-option label="已归档" value="ARCHIVED" />
        </el-select>
      </el-form-item>
    </template>

    <AdminContentSection
      title="活动列表"
      description="支持查看封面、人数、时间与状态信息，创建、编辑、归档与恢复发布互不影响列表筛选条件。"
      :loading="loading && !list.length"
      :empty="!loading && !list.length"
      loading-title="正在加载活动列表"
      loading-description="请稍候，系统正在同步最新的志愿活动数据。"
      empty-title="当前暂无活动"
      empty-description="可以先创建一场新的社区志愿活动，创建后会在这里按分页展示。"
    >
      <template #actions>
        <el-button type="warning" plain :disabled="!selectedIds.length" @click="batchArchive">批量归档</el-button>
        <el-button plain :disabled="!selectedIds.length" @click="batchRestore">恢复发布</el-button>
        <el-button type="primary" @click="openCreate">新增活动</el-button>
      </template>

      <template #emptyActions>
        <el-button type="primary" @click="openCreate">立即新增活动</el-button>
      </template>

      <el-table :data="list" border v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="48" />
        <el-table-column label="封面" width="96">
          <template #default="{ row }">
            <img class="cover-mini" :src="row.coverImage || DEFAULT_ACTIVITY_COVER" alt="活动封面" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="活动名称" min-width="180" />
        <el-table-column prop="content" label="活动内容" min-width="180" show-overflow-tooltip />
        <el-table-column prop="address" label="活动地点" min-width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getActivityStatusTag(row.status)">{{ getActivityStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="volunteerQuota" label="志愿者人数" width="110" />
        <el-table-column prop="pointReward" label="活动积分" width="100" />
        <el-table-column prop="targetCount" label="目标人数" width="100" />
        <el-table-column label="时间" min-width="220">
          <template #default="{ row }">
            {{ formatDateTime(row.startTime) }} - {{ formatDateTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="156" fixed="right">
          <template #default="{ row }">
            <div class="action-cell">
              <el-tooltip content="编辑活动" placement="top">
                <el-button link type="primary" :icon="EditPen" @click="openEdit(row)" />
              </el-tooltip>
              <el-tooltip v-if="row.status === 'ARCHIVED'" content="恢复发布" placement="top">
                <el-button link type="success" :icon="RefreshRight" @click="restoreOne(row.id)" />
              </el-tooltip>
              <el-tooltip v-else content="归档活动" placement="top">
                <el-button link type="warning" :icon="Delete" @click="archiveOne(row.id)" />
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <template #pagination>
        <el-pagination
          layout="total, sizes, prev, pager, next"
          :current-page="query.current"
          :page-size="query.size"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          @current-change="handlePage"
          @size-change="handleSizeChange"
        />
      </template>
    </AdminContentSection>

    <el-dialog
      v-model="drawerVisible"
      :title="editingId ? '编辑活动' : '新增活动'"
      width="min(92vw, 920px)"
      top="5vh"
      append-to-body
      class="activity-dialog"
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
              <el-option label="已归档" value="ARCHIVED" />
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

          <el-form-item label="活动积分" prop="pointReward">
            <el-input-number v-model="form.pointReward" :min="0" />
          </el-form-item>

          <el-form-item label="活动封面" class="span-2">
            <div class="uploader">
              <img :src="form.coverImage || DEFAULT_ACTIVITY_COVER" class="cover-preview" alt="活动封面" />
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

          <el-form-item label="活动详情" prop="description" class="span-2">
            <RichTextEditor v-model="form.description" placeholder="请按标准模板补充活动背景、服务流程、注意事项等内容" />
            <span class="field-tip">建议按“活动背景 / 服务流程 / 注意事项 / 联系方式”结构补充详情。</span>
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="drawerVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </AdminListScaffold>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules, UploadProps, UploadRequestOptions } from 'element-plus'
import { ElMessage } from 'element-plus'
import { Delete, EditPen, RefreshRight } from '@element-plus/icons-vue'
import AdminContentSection from '@/components/admin/AdminContentSection.vue'
import AdminListScaffold from '@/components/admin/AdminListScaffold.vue'
import RichTextEditor from '@/components/shared/RichTextEditor.vue'
import { uploadImageApi } from '@/api/common'
import {
  batchArchiveActivitiesApi,
  batchRestoreActivitiesApi,
  createActivityApi,
  deleteActivityApi,
  listCategoriesApi,
  pageActivitiesApi,
  updateActivityApi,
  type ActivityCategory,
  type ActivityModel
} from '@/api/activity'
import { useSelectionIds } from '@/composables/useSelectionIds'
import { useTable } from '@/composables/useTable'
import {
  DEFAULT_ACTIVITY_COVER,
  formatDateTime,
  getActivityStatusLabel,
  getActivityStatusTag
} from '@/utils/display'
import { runConfirmedAction } from '@/utils/confirmed-action'
import { validateElementForm } from '@/utils/form'
import { richTextToPlainText, sanitizeRichText } from '@/utils/rich-text'
import { validateImageFile } from '@/utils/upload'

interface ActivityQuery {
  current: number
  size: number
  keyword: string
  status: string
}

const DEFAULT_ACTIVITY_DETAIL_TEMPLATE = `
<h2>活动背景</h2>
<p>请补充活动发起背景、服务目标与服务对象。</p>
<h2>服务流程</h2>
<ul>
  <li>集合签到时间与地点</li>
  <li>现场分工安排</li>
  <li>服务结束后的签退方式</li>
</ul>
<h2>注意事项</h2>
<ul>
  <li>请按时到场并服从现场安排</li>
  <li>如需自备物资请提前说明</li>
  <li>涉及特殊天气时请关注活动通知</li>
</ul>
<h2>联系方式</h2>
<p>请补充活动联系人、联系电话或对接方式。</p>
`.trim()

const { loading, records: list, total, query, load, reset, handlePage, handleSizeChange } = useTable<
  ActivityModel,
  ActivityQuery
>({
  initialQuery: {
    current: 1,
    size: 10,
    keyword: '',
    status: ''
  },
  fetcher: (params) =>
    pageActivitiesApi({
      current: params.current,
      size: params.size,
      keyword: params.keyword || undefined,
      status: params.status || undefined,
      includeArchived: true
    })
})

const { selectedIds, handleSelectionChange, clearSelection } = useSelectionIds<ActivityModel>()
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
  pointReward: 0,
  content: '',
  description: '',
  coverImage: ''
})

function validatePositiveCount(_: unknown, value: number, callback: (error?: Error) => void) {
  if (value == null || Number.isNaN(Number(value)) || Number(value) <= 0) {
    callback(new Error('请输入大于 0 的人数'))
    return
  }
  callback()
}

function validateNonNegativeNumber(_: unknown, value: number, callback: (error?: Error) => void) {
  if (value == null || Number.isNaN(Number(value)) || Number(value) < 0) {
    callback(new Error('请输入大于等于 0 的数值'))
    return
  }
  callback()
}

const rules: FormRules = {
  title: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择活动分类', trigger: 'change' }],
  status: [{ required: true, message: '请选择活动状态', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  content: [{ required: true, message: '请输入活动内容', trigger: 'blur' }],
  address: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
  volunteerQuota: [{ required: true, validator: validatePositiveCount, trigger: 'change' }],
  targetCount: [{ required: true, validator: validatePositiveCount, trigger: 'change' }],
  pointReward: [{ required: true, validator: validateNonNegativeNumber, trigger: 'change' }],
  description: [{ required: true, message: '请填写活动详细描述', trigger: 'blur' }]
}

function syncRangeToTime(value: [string, string] | null) {
  if (!value) {
    form.startTime = ''
    form.endTime = ''
    return
  }

  form.startTime = value[0]
  form.endTime = value[1]
}

const beforeCoverUpload: UploadProps['beforeUpload'] = (rawFile) => validateImageFile(rawFile)

async function handleCoverUpload(option: UploadRequestOptions) {
  coverUploading.value = true
  try {
    const file = option.file as File
    const res = await uploadImageApi(file)
    form.coverImage = res.url
    ElMessage.success('封面上传成功')
    option.onSuccess?.(res)
  } catch (error) {
    option.onError?.(error as never)
  } finally {
    coverUploading.value = false
  }
}

function resetQuery() {
  reset()
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
    pointReward: 0,
    content: '',
    description: DEFAULT_ACTIVITY_DETAIL_TEMPLATE,
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
  if (!(await validateElementForm(formRef.value))) {
    return
  }
  const start = new Date(form.startTime as string).getTime()
  const end = new Date(form.endTime as string).getTime()
  if (Number.isFinite(start) && Number.isFinite(end) && end <= start) {
    ElMessage.warning('结束时间必须晚于开始时间')
    return
  }

  const payload = {
    ...form,
    title: String(form.title || '').trim(),
    content: String(form.content || '').trim(),
    address: String(form.address || '').trim(),
    description: sanitizeRichText(form.description)
  }

  if (!payload.content) {
    ElMessage.warning('请输入活动简介')
    return
  }

  if (!richTextToPlainText(payload.description)) {
    ElMessage.warning('请完善活动详情内容')
    return
  }

  if (editingId.value) {
    await updateActivityApi(editingId.value, payload)
  } else {
    await createActivityApi(payload)
  }

  drawerVisible.value = false
  ElMessage.success(editingId.value ? '活动更新成功' : '活动创建成功')
  await load()
}

async function archiveOne(id: number) {
  await runConfirmedAction({
    message: '确认归档该活动吗？归档后活动会从前台下线，但历史报名、打卡与收藏记录会保留。',
    title: '归档活动',
    type: 'warning',
    confirmButtonText: '确认归档',
    action: () => deleteActivityApi(id),
    successMessage: '活动已归档',
    afterSuccess: load
  })
}

async function restoreOne(id: number) {
  await runConfirmedAction({
    message: '确认恢复发布该活动吗？恢复后志愿者侧可再次查看和报名。',
    title: '恢复发布活动',
    action: () => batchRestoreActivitiesApi([id]),
    successMessage: '活动已恢复发布',
    afterSuccess: load
  })
}

async function batchArchive() {
  if (!selectedIds.value.length) {
    return
  }

  await runConfirmedAction({
    message: `确认将选中的 ${selectedIds.value.length} 个活动归档吗？归档后前台不再展示，但历史报名与打卡记录会保留。`,
    title: '批量归档活动',
    type: 'warning',
    confirmButtonText: '确认归档',
    action: () => batchArchiveActivitiesApi(selectedIds.value),
    successMessage: '批量归档成功',
    afterSuccess: async () => {
      clearSelection()
      await load()
    }
  })
}

async function batchRestore() {
  if (!selectedIds.value.length) {
    return
  }

  await runConfirmedAction({
    message: `确认将选中的 ${selectedIds.value.length} 个活动恢复为已发布状态？`,
    title: '恢复发布活动',
    confirmButtonText: '确认恢复',
    action: () => batchRestoreActivitiesApi(selectedIds.value),
    successMessage: '活动已批量恢复发布',
    afterSuccess: async () => {
      clearSelection()
      await load()
    }
  })
}

onMounted(async () => {
  categories.value = await listCategoriesApi()
  await load()
})

defineExpose({
  batchArchive
})
</script>

<style scoped>
.cover-mini {
  width: 62px;
  height: 42px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid var(--cvs-border);
}

.action-cell {
  display: flex;
  align-items: center;
  gap: 4px;
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

.field-tip {
  display: block;
  margin-top: 8px;
  color: var(--cvs-text-sub);
  font-size: 12px;
  line-height: 1.6;
}

:deep(.activity-dialog .el-dialog__body) {
  overflow: auto;
  max-height: calc(100vh - 240px);
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
