<template>
  <div class="user-manage-section">
    <SearchForm @search="load" @reset="resetQuery">
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable style="width: 140px">
          <el-option label="启用中" :value="1" />
          <el-option label="已停用" :value="0" />
        </el-select>
      </el-form-item>

      <el-form-item v-if="isVolunteer" label="认证审核">
        <el-select v-model="query.certificationStatus" clearable style="width: 160px">
          <el-option label="未提交" value="NOT_SUBMITTED" />
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
      </el-form-item>

      <el-form-item label="关键词">
        <el-input v-model="query.keyword" :placeholder="keywordPlaceholder" />
      </el-form-item>
    </SearchForm>

    <div class="toolbar">
      <div class="toolbar-info">
        <h3>{{ panelTitle }}</h3>
        <p>{{ panelDescription }}</p>
      </div>

      <div class="toolbar-actions">
        <template v-if="isVolunteer">
          <el-button type="danger" plain :disabled="!selectedIds.length" @click="batchDelete">批量删除</el-button>
        </template>
        <template v-else>
          <el-button type="warning" plain :disabled="!selectedIds.length" @click="batchDisable">批量停用</el-button>
          <el-button plain :disabled="!selectedIds.length" @click="batchEnable">批量启用</el-button>
        </template>
        <el-button type="primary" @click="openCreate">新增{{ roleLabel }}</el-button>
      </div>
    </div>

    <el-table :data="list" border v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" />
      <el-table-column prop="avatar" label="头像" width="88">
        <template #default="{ row }">
          <el-avatar :size="38" :src="row.avatar" />
        </template>
      </el-table-column>
      <el-table-column prop="username" label="账号" min-width="150" />
      <el-table-column prop="realName" label="姓名" min-width="120" />
      <el-table-column prop="phone" label="电话" min-width="140" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column v-if="isVolunteer" prop="points" label="积分" width="100" />
      <el-table-column v-if="isVolunteer" label="认证状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getCertificationStatusTag(row.certificationStatus)">
            {{ getCertificationStatusLabel(row.certificationStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="isVolunteer" prop="certificationIdCardNo" label="身份证号" min-width="170" />
      <el-table-column v-if="isVolunteer" prop="certificationRejectReason" label="拒绝理由" min-width="200" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="180" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <div class="action-cell">
            <el-tooltip content="编辑信息" placement="top">
              <el-button link type="primary" :icon="EditPen" @click="openEdit(row)" />
            </el-tooltip>

            <el-tooltip v-if="isVolunteer" content="查看 / 审核认证" placement="top">
              <el-button link type="warning" :icon="DocumentChecked" @click="openCertification(row)" />
            </el-tooltip>

            <template v-if="isVolunteer">
              <el-tooltip :content="deleteTooltip(row)" placement="top">
                <span>
                  <el-button
                    link
                    type="danger"
                    :icon="Delete"
                    :disabled="isCurrentUser(row)"
                    @click="deleteOne(row)"
                  />
                </span>
              </el-tooltip>
            </template>
            <template v-else>
              <el-tooltip v-if="row.status === 1" :content="disableTooltip(row)" placement="top">
                <span>
                  <el-button
                    link
                    type="warning"
                    :icon="CircleClose"
                    :disabled="isCurrentUser(row)"
                    @click="disableOne(row)"
                  />
                </span>
              </el-tooltip>
              <el-tooltip v-else content="恢复启用" placement="top">
                <el-button link type="success" :icon="RefreshRight" @click="enableOne(row)" />
              </el-tooltip>
            </template>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        layout="total, sizes, prev, pager, next"
        :current-page="query.current"
        :page-size="query.size"
        :page-sizes="[10, 20, 30, 50]"
        :total="total"
        @current-change="onPageChange"
        @size-change="onSizeChange"
      />
    </div>
  </div>

  <el-dialog
    v-model="visible"
    :title="editingId ? `编辑${roleLabel}` : `新增${roleLabel}`"
    width="min(92vw, 720px)"
    append-to-body
    top="6vh"
    class="manage-dialog"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <div class="dialog-grid">
        <el-form-item label="头像">
          <div class="avatar-uploader">
            <el-avatar :size="68" :src="form.avatar || defaultAvatar" />
            <el-upload
              :show-file-list="false"
              :http-request="handleAvatarUpload"
              :before-upload="beforeAvatarUpload"
              accept="image/png,image/jpeg,image/jpg,image/webp"
            >
              <el-button :loading="avatarUploading">上传头像</el-button>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item label="账号" prop="username">
          <el-input v-model.trim="form.username" />
        </el-form-item>

        <el-form-item v-if="!editingId" label="初始密码" prop="password">
          <el-input v-model.trim="form.password" type="password" show-password />
        </el-form-item>

        <el-form-item label="姓名" prop="realName">
          <el-input v-model.trim="form.realName" />
        </el-form-item>

        <el-form-item label="电话" prop="phone">
          <el-input v-model.trim="form.phone" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model.trim="form.email" />
        </el-form-item>

        <el-form-item label="状态">
          <el-switch v-model="statusSwitch" />
        </el-form-item>

        <el-form-item v-if="editingId && isVolunteer" label="当前积分">
          <el-input :model-value="String(form.points ?? 0)" disabled />
        </el-form-item>
      </div>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submit">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog
    v-model="certificationVisible"
    title="实名认证审核"
    width="min(92vw, 760px)"
    append-to-body
    top="6vh"
    class="manage-dialog"
  >
    <div class="certification-dialog">
      <div class="certification-overview">
        <el-tag :type="getCertificationStatusTag(certificationDetail.status)" size="large">
          {{ getCertificationStatusLabel(certificationDetail.status) }}
        </el-tag>
        <p>{{ certificationStatusDescription }}</p>
      </div>

      <dl class="certification-grid">
        <div>
          <dt>真实姓名</dt>
          <dd>{{ certificationDetail.realName || '-' }}</dd>
        </div>
        <div>
          <dt>身份证号</dt>
          <dd>{{ certificationDetail.idCardNo || '-' }}</dd>
        </div>
        <div>
          <dt>提交时间</dt>
          <dd>{{ formatDateTime(certificationDetail.submitTime) }}</dd>
        </div>
        <div>
          <dt>审核时间</dt>
          <dd>{{ formatDateTime(certificationDetail.auditTime) }}</dd>
        </div>
      </dl>

      <div class="certification-images">
        <div class="image-card">
          <strong>身份证正面</strong>
          <div class="image-frame" v-if="certificationDetail.idCardFrontUrl">
            <img :src="certificationDetail.idCardFrontUrl" alt="身份证正面" />
          </div>
          <span v-else>暂无图片</span>
        </div>
        <div class="image-card">
          <strong>身份证反面</strong>
          <div class="image-frame" v-if="certificationDetail.idCardBackUrl">
            <img :src="certificationDetail.idCardBackUrl" alt="身份证反面" />
          </div>
          <span v-else>暂无图片</span>
        </div>
      </div>

      <el-form v-if="canAuditCertification" label-position="top" class="audit-form">
        <el-form-item label="驳回说明">
          <el-input
            v-model.trim="auditRejectReason"
            type="textarea"
            :rows="3"
            placeholder="驳回时必须填写原因，便于志愿者修改后重新提交。"
          />
        </el-form-item>
      </el-form>

      <div v-else-if="certificationDetail.rejectReason" class="audit-result">
        <strong>驳回原因</strong>
        <p>{{ certificationDetail.rejectReason }}</p>
      </div>
    </div>

    <template #footer>
      <el-button @click="closeCertification">关闭</el-button>
      <el-button v-if="canAuditCertification" type="danger" plain @click="submitCertificationAudit('REJECTED')">
        驳回
      </el-button>
      <el-button v-if="canAuditCertification" type="success" @click="submitCertificationAudit('APPROVED')">
        通过
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules, UploadProps, UploadRequestOptions } from 'element-plus'
import { ElMessage } from 'element-plus'
import { CircleClose, Delete, DocumentChecked, EditPen, RefreshRight } from '@element-plus/icons-vue'
import { uploadImageApi } from '@/api/common'
import {
  adminUpdateUserApi,
  auditUserCertificationApi,
  batchDeleteUsersApi,
  batchDisableUsersApi,
  batchEnableUsersApi,
  createUserApi,
  deleteUserApi,
  disableUserApi,
  enableUserApi,
  getUserCertificationDetailApi,
  getUserDetailApi,
  pageUsersApi,
  type CertificationStatus,
  type UserModel,
  type VolunteerCertificationModel
} from '@/api/user'
import SearchForm from '@/components/SearchForm.vue'
import { useSelectionIds } from '@/composables/useSelectionIds'
import { useTable } from '@/composables/useTable'
import { useUserStore } from '@/stores/userStore'
import { runConfirmedAction } from '@/utils/confirmed-action'
import { validateElementForm } from '@/utils/form'
import { formatDateTime, getCertificationStatusLabel, getCertificationStatusTag } from '@/utils/display'
import { validateImageFile } from '@/utils/upload'

type UserRole = 'ADMIN' | 'VOLUNTEER'

interface UserQuery {
  current: number
  size: number
  keyword: string
  status?: number
  certificationStatus?: CertificationStatus
}

const props = defineProps<{
  role: UserRole
}>()

const defaultAvatar = 'https://cdn.jsdelivr.net/gh/fukexin123/assets/default-avatar.png'
const userStore = useUserStore()
const isVolunteer = computed(() => props.role === 'VOLUNTEER')
const roleLabel = computed(() => (isVolunteer.value ? '志愿者' : '管理员'))
const panelTitle = computed(() => `${roleLabel.value}信息`)
const panelDescription = computed(() =>
  isVolunteer.value
    ? '对志愿者账号进行新增、编辑、查询和删除，并管理积分、实名认证和基础资料。'
    : '对管理员账号进行新增、编辑、查询、启用和停用管理。'
)
const keywordPlaceholder = computed(() => `${roleLabel.value}账号 / 姓名 / 电话`)

const {
  loading,
  records: list,
  total,
  query,
  load,
  reset,
  handlePage,
  handleSizeChange
} = useTable<UserModel, UserQuery>({
  initialQuery: {
    current: 1,
    size: 10,
    keyword: '',
    status: undefined,
    certificationStatus: undefined
  },
  fetcher: (params) =>
    pageUsersApi({
      current: params.current,
      size: params.size,
      role: props.role,
      keyword: params.keyword || undefined,
      status: typeof params.status === 'number' ? params.status : undefined,
      certificationStatus: isVolunteer.value ? params.certificationStatus : undefined
    })
})

const { selectedIds, handleSelectionChange, clearSelection } = useSelectionIds<UserModel>()
const visible = ref(false)
const editingId = ref<number>()
const avatarUploading = ref(false)
const formRef = ref<FormInstance>()
const certificationVisible = ref(false)
const certificationUserId = ref<number>()
const auditRejectReason = ref('')
const certificationDetail = reactive<VolunteerCertificationModel>({
  userId: 0,
  realName: '',
  idCardNo: '',
  idCardFrontUrl: '',
  idCardBackUrl: '',
  status: 'NOT_SUBMITTED',
  rejectReason: '',
  submitTime: '',
  auditTime: ''
})

const form = reactive<Partial<UserModel> & { password?: string }>({
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  avatar: '',
  status: 1,
  points: 0
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

const statusSwitch = computed({
  get: () => form.status === 1,
  set: (value: boolean) => {
    form.status = value ? 1 : 0
  }
})

const beforeAvatarUpload: UploadProps['beforeUpload'] = (rawFile) => validateImageFile(rawFile, 3)
const canAuditCertification = computed(() => certificationDetail.status === 'PENDING')
const certificationStatusDescription = computed(() => {
  const descriptionMap: Record<CertificationStatus, string> = {
    NOT_SUBMITTED: '该志愿者尚未提交实名认证资料。',
    PENDING: '资料已提交，管理员可在此审核通过或驳回。',
    APPROVED: '实名认证已通过，志愿者资料可正常用于报名等流程。',
    REJECTED: '实名认证已驳回，志愿者可修改资料后重新提交。'
  }
  return descriptionMap[certificationDetail.status]
})

function resetQuery() {
  reset({
    status: undefined,
    certificationStatus: undefined
  })
}

function resetForm() {
  Object.assign(form, {
    username: '',
    password: '',
    realName: '',
    email: '',
    phone: '',
    avatar: '',
    status: 1,
    points: 0
  })
}

function resetCertificationDetail() {
  Object.assign(certificationDetail, {
    userId: 0,
    realName: '',
    idCardNo: '',
    idCardFrontUrl: '',
    idCardBackUrl: '',
    status: 'NOT_SUBMITTED',
    rejectReason: '',
    submitTime: '',
    auditTime: ''
  })
  certificationUserId.value = undefined
  auditRejectReason.value = ''
}

function openCreate() {
  editingId.value = undefined
  resetForm()
  visible.value = true
}

async function openEdit(row: UserModel) {
  const detail = await getUserDetailApi(row.id)
  editingId.value = row.id
  Object.assign(form, {
    username: detail.username,
    password: '',
    realName: detail.realName || '',
    email: detail.email || '',
    phone: detail.phone || '',
    avatar: detail.avatar || '',
    status: detail.status,
    points: detail.points ?? 0
  })
  visible.value = true
}

async function openCertification(row: UserModel) {
  if (!isVolunteer.value) {
    return
  }
  const detail = await getUserCertificationDetailApi(row.id)
  certificationUserId.value = row.id
  Object.assign(certificationDetail, detail)
  auditRejectReason.value = detail.rejectReason || ''
  certificationVisible.value = true
}

function closeCertification() {
  certificationVisible.value = false
  resetCertificationDetail()
}

async function handleAvatarUpload(option: UploadRequestOptions) {
  avatarUploading.value = true
  try {
    const file = option.file as File
    const res = await uploadImageApi(file)
    form.avatar = res.url
    ElMessage.success('头像上传成功')
    option.onSuccess?.(res)
  } catch (error) {
    option.onError?.(error as never)
  } finally {
    avatarUploading.value = false
  }
}

async function submit() {
  if (!(await validateElementForm(formRef.value))) {
    return
  }

  const payload = {
    username: (form.username || '').trim(),
    password: (form.password || '').trim(),
    realName: (form.realName || '').trim(),
    email: (form.email || '').trim() || undefined,
    phone: (form.phone || '').trim(),
    avatar: (form.avatar || '').trim() || undefined,
    role: props.role,
    status: form.status ?? 1
  }

  if (editingId.value) {
    await adminUpdateUserApi(editingId.value, payload)
    ElMessage.success(`${roleLabel.value}信息已更新`)
  } else {
    await createUserApi(payload as Parameters<typeof createUserApi>[0])
    ElMessage.success(`${roleLabel.value}已创建`)
  }

  visible.value = false
  await load()
}

async function submitCertificationAudit(status: Extract<CertificationStatus, 'APPROVED' | 'REJECTED'>) {
  if (!certificationUserId.value) {
    return
  }
  if (status === 'REJECTED' && !auditRejectReason.value.trim()) {
    ElMessage.warning('驳回时必须填写原因')
    return
  }

  await auditUserCertificationApi(certificationUserId.value, {
    status,
    rejectReason: status === 'REJECTED' ? auditRejectReason.value.trim() : undefined
  })
  ElMessage.success(status === 'APPROVED' ? '认证已通过' : '认证已驳回')
  closeCertification()
  await load()
}

function isCurrentUser(row: UserModel) {
  return userStore.profile?.id === row.id
}

function sanitizeSelectedIdsForDisable() {
  return selectedIds.value.filter((id) => id !== userStore.profile?.id)
}

function disableTooltip(row: UserModel) {
  return isCurrentUser(row) ? '不能停用当前登录账号' : '停用账号'
}

function deleteTooltip(row: UserModel) {
  return isCurrentUser(row) ? '不能删除当前登录账号' : '删除志愿者'
}

async function disableOne(row: UserModel) {
  if (isCurrentUser(row)) {
    return
  }
  await runConfirmedAction({
    message: `确认停用账号“${row.username}”吗？停用后该账号将不能继续登录。`,
    title: '停用账号',
    type: 'warning',
    action: () => disableUserApi(row.id),
    successMessage: '账号已停用',
    afterSuccess: () => load()
  })
}

async function enableOne(row: UserModel) {
  await runConfirmedAction({
    message: `确认恢复账号“${row.username}”吗？`,
    title: '恢复账号',
    type: 'success',
    action: () => enableUserApi(row.id),
    successMessage: '账号已恢复启用',
    afterSuccess: () => load()
  })
}

async function deleteOne(row: UserModel) {
  if (isCurrentUser(row)) {
    return
  }
  await runConfirmedAction({
    message: `确认删除志愿者“${row.username}”吗？删除后该志愿者的可清理资料将一并删除。`,
    title: '删除志愿者',
    type: 'warning',
    action: () => deleteUserApi(row.id),
    successMessage: '志愿者已删除',
    afterSuccess: () => load()
  })
}

async function batchDisable() {
  const effectiveIds = sanitizeSelectedIdsForDisable()
  if (!effectiveIds.length) {
    ElMessage.warning('当前选择中没有可停用的账号')
    return
  }
  await runConfirmedAction({
    message: `确认批量停用 ${effectiveIds.length} 个管理员账号吗？`,
    title: '批量停用账号',
    type: 'warning',
    action: () => batchDisableUsersApi(effectiveIds),
    successMessage: '批量停用成功',
    afterSuccess: async () => {
      clearSelection()
      await load()
    }
  })
}

async function batchEnable() {
  if (!selectedIds.value.length) {
    return
  }
  await runConfirmedAction({
    message: `确认批量恢复 ${selectedIds.value.length} 个管理员账号吗？`,
    title: '批量恢复账号',
    type: 'success',
    action: () => batchEnableUsersApi(selectedIds.value),
    successMessage: '批量恢复成功',
    afterSuccess: async () => {
      clearSelection()
      await load()
    }
  })
}

async function batchDelete() {
  if (!selectedIds.value.length) {
    return
  }
  await runConfirmedAction({
    message: `确认批量删除 ${selectedIds.value.length} 个志愿者账号吗？`,
    title: '批量删除志愿者',
    type: 'warning',
    action: () => batchDeleteUsersApi(selectedIds.value),
    successMessage: '批量删除成功',
    afterSuccess: async () => {
      clearSelection()
      await load()
    }
  })
}

async function onPageChange(page: number) {
  clearSelection()
  await handlePage(page)
}

async function onSizeChange(size: number) {
  clearSelection()
  await handleSizeChange(size)
}

onMounted(async () => {
  if (!userStore.profile) {
    try {
      await userStore.fetchProfile()
    } catch {
      // Ignore profile refresh failure and keep page usable.
    }
  }
  await load()
})
</script>

<style scoped>
.user-manage-section {
  display: grid;
  gap: 14px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 14px;
}

.toolbar-info h3 {
  margin: 0;
  font-size: var(--cvs-font-size-lg);
}

.toolbar-info p {
  margin: 8px 0 0;
  color: var(--cvs-text-sub);
  line-height: 1.7;
}

.toolbar-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.action-cell {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
}

.dialog-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 14px;
}

.avatar-uploader {
  display: flex;
  gap: 12px;
  align-items: center;
}

.certification-dialog {
  display: grid;
  gap: 18px;
}

.certification-overview {
  display: grid;
  gap: 10px;
}

.certification-overview p {
  margin: 0;
  color: var(--cvs-text-sub);
}

.certification-grid {
  margin: 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.certification-grid div {
  border: 1px solid var(--cvs-border);
  border-radius: 14px;
  padding: 14px;
  background: rgba(247, 250, 248, 0.9);
}

.certification-grid dt {
  color: #70817b;
  font-size: 12px;
  margin-bottom: 6px;
}

.certification-grid dd {
  margin: 0;
  font-size: 16px;
  color: #2f403a;
}

.certification-images {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.image-card {
  display: grid;
  gap: 10px;
}

.image-frame {
  overflow: hidden;
  border-radius: 16px;
  border: 1px solid var(--cvs-border);
  background: #f8fbf9;
}

.image-frame img {
  display: block;
  width: 100%;
  height: auto;
  object-fit: cover;
}

.audit-result {
  display: grid;
  gap: 6px;
  padding: 14px;
  border-radius: 14px;
  background: rgba(255, 247, 236, 0.85);
  border: 1px solid rgba(232, 157, 59, 0.18);
}

.audit-result strong {
  color: #8b4b16;
}

.audit-result p {
  margin: 0;
  color: #694326;
}

:deep(.manage-dialog .el-dialog__body) {
  max-height: calc(100vh - 240px);
  overflow: auto;
}

@media (max-width: 900px) {
  .toolbar {
    flex-direction: column;
  }

  .toolbar-actions,
  .pagination {
    justify-content: flex-start;
  }

  .dialog-grid,
  .certification-grid,
  .certification-images {
    grid-template-columns: 1fr;
  }
}
</style>
