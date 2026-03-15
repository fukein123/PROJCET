<template>
  <div class="page-shell">
    <WorkspaceHero
      compact
      eyebrow="个人中心"
      title="统一管理个人资料、实名认证和账户安全"
      description="这里不再出现只能看不能改的空壳信息。资料、实名认证和密码修改都集中在当前页面处理。"
    >
      <template #actions>
        <el-button type="primary" @click="router.push(PORTAL_PATHS.selfServiceApplications)">查看报名记录</el-button>
        <el-button @click="router.push(PORTAL_PATHS.selfServiceCheckRecords)">查看服务记录</el-button>
      </template>

      <template #aside>
        <div class="hero-stat-grid">
          <article class="hero-stat">
            <h3>认证状态</h3>
            <strong>{{ certificationLabel }}</strong>
            <span>{{ certificationHint }}</span>
          </article>
          <article class="hero-stat">
            <h3>当前积分</h3>
            <strong>{{ profile.points ?? 0 }}</strong>
            <span>活动完成后发放积分，兑换商品时会实时扣减。</span>
          </article>
          <article class="hero-stat">
            <h3>联系方式</h3>
            <strong>{{ profile.phone || '待补充' }}</strong>
            <span>{{ profile.email || '建议补充邮箱用于接收平台通知' }}</span>
          </article>
        </div>
      </template>
    </WorkspaceHero>

    <div ref="profileSectionRef">
      <VolunteerPageSection
        eyebrow="基础资料"
        title="资料维护"
        description="点击“编辑资料”后再修改用户名、姓名、邮箱和手机号。积分与认证状态为系统信息，只读展示。"
      >
        <template #actions>
          <div class="section-actions-bar">
            <el-button v-if="!profileEditing" type="primary" @click="beginProfileEdit">编辑资料</el-button>
            <template v-else>
              <el-button @click="cancelProfileEdit">取消</el-button>
              <el-button type="primary" :loading="profileSaving" @click="saveProfile">保存资料</el-button>
            </template>
          </div>
        </template>

        <div class="section-tip">
          <strong>{{ profileEditing ? '正在编辑资料' : '当前为只读展示' }}</strong>
          <span>
            {{ profileEditing ? '保存后会立即同步当前账号资料。' : '未进入编辑态时不显示无效输入框，避免误以为可以直接修改。' }}
          </span>
        </div>

        <el-form :model="profileDraft" label-width="100px" class="profile-form">
          <el-form-item label="用户名">
            <el-input v-model="profileDraft.username" :disabled="!profileEditing || profileSaving" />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="profileDraft.realName" :disabled="!profileEditing || profileSaving" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="profileDraft.email" :disabled="!profileEditing || profileSaving" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="profileDraft.phone" :disabled="!profileEditing || profileSaving" />
          </el-form-item>
          <el-form-item label="当前积分">
            <el-input :model-value="String(profile.points ?? 0)" disabled />
          </el-form-item>
          <el-form-item label="认证状态">
            <div class="status-row">
              <el-tag :type="certificationTag">{{ certificationLabel }}</el-tag>
              <span class="tip">{{ certificationHint }}</span>
            </div>
          </el-form-item>
        </el-form>
      </VolunteerPageSection>
    </div>

    <div ref="certificationSectionRef">
      <VolunteerPageSection
        eyebrow="实名认证"
        title="志愿者实名认证"
        description="实名认证信息在这里统一管理。已通过认证后仍允许补充或更新资料，提交后会重新进入审核。"
      >
        <div class="certification-panel">
          <div class="certification-status">
            <el-tag :type="certificationTag" size="large">{{ certificationLabel }}</el-tag>
            <p>{{ certificationHint }}</p>
            <p v-if="certification.rejectReason" class="danger-text">驳回原因：{{ certification.rejectReason }}</p>
          </div>

          <dl class="certification-grid">
            <div>
              <dt>认证状态</dt>
              <dd>{{ certificationLabel }}</dd>
            </div>
            <div>
              <dt>提交时间</dt>
              <dd>{{ formatDateTime(certification.submitTime) }}</dd>
            </div>
            <div>
              <dt>审核时间</dt>
              <dd>{{ formatDateTime(certification.auditTime) }}</dd>
            </div>
            <div>
              <dt>说明</dt>
              <dd>{{ certificationSubmitTip }}</dd>
            </div>
          </dl>

          <div class="section-tip certification-tip">
            <strong>{{ certification.status === 'APPROVED' ? '允许重新提交' : '可直接提交认证资料' }}</strong>
            <span>{{ certificationSubmitTip }}</span>
          </div>

          <el-form :model="certification" label-width="110px" class="profile-form certification-form">
            <el-form-item label="真实姓名">
              <el-input v-model="certification.realName" :disabled="certificationSubmitting" />
            </el-form-item>
            <el-form-item label="身份证号">
              <el-input v-model="certification.idCardNo" :disabled="certificationSubmitting" />
            </el-form-item>
            <el-form-item label="身份证正面">
              <div class="upload-panel">
                <div class="image-preview">
                  <img v-if="certification.idCardFrontUrl" :src="certification.idCardFrontUrl" alt="身份证正面" />
                  <div v-else class="image-empty">暂无图片</div>
                </div>
                <input
                  ref="frontInputRef"
                  class="hidden-file-input"
                  type="file"
                  accept="image/png,image/jpeg,image/jpg,image/webp"
                  @change="handleCertificationFileChange('idCardFrontUrl', $event)"
                />
                <div class="upload-actions">
                  <el-button :loading="uploadingField === 'idCardFrontUrl'" @click="openCertificationPicker('idCardFrontUrl')">
                    上传身份证正面
                  </el-button>
                  <el-button text :disabled="!certification.idCardFrontUrl" @click="certification.idCardFrontUrl = ''">
                    清空图片
                  </el-button>
                </div>
              </div>
            </el-form-item>
            <el-form-item label="身份证反面">
              <div class="upload-panel">
                <div class="image-preview">
                  <img v-if="certification.idCardBackUrl" :src="certification.idCardBackUrl" alt="身份证反面" />
                  <div v-else class="image-empty">暂无图片</div>
                </div>
                <input
                  ref="backInputRef"
                  class="hidden-file-input"
                  type="file"
                  accept="image/png,image/jpeg,image/jpg,image/webp"
                  @change="handleCertificationFileChange('idCardBackUrl', $event)"
                />
                <div class="upload-actions">
                  <el-button :loading="uploadingField === 'idCardBackUrl'" @click="openCertificationPicker('idCardBackUrl')">
                    上传身份证反面
                  </el-button>
                  <el-button text :disabled="!certification.idCardBackUrl" @click="certification.idCardBackUrl = ''">
                    清空图片
                  </el-button>
                </div>
              </div>
            </el-form-item>
            <el-form-item class="form-actions">
              <el-button type="primary" :loading="certificationSubmitting" @click="submitCertification">
                {{ certificationActionText }}
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </VolunteerPageSection>
    </div>

    <div ref="securitySectionRef">
      <VolunteerPageSection
        eyebrow="账户安全"
        title="密码更新"
        description="密码修改同样在个人中心统一处理，不再作为单独入口分散出去。"
      >
        <el-form :model="passwordForm" label-width="100px" class="profile-form">
          <el-form-item label="旧密码">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="passwordForm.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item class="form-actions">
            <el-button type="warning" @click="savePassword">更新密码</el-button>
          </el-form-item>
        </el-form>
      </VolunteerPageSection>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { uploadImageApi } from '@/api/common'
import {
  getMyCertificationApi,
  getMyProfileApi,
  submitMyCertificationApi,
  updateMyProfileApi,
  updatePasswordApi,
  type CertificationStatus,
  type VolunteerCertificationModel
} from '@/api/user'
import VolunteerPageSection from '@/components/volunteer/VolunteerPageSection.vue'
import WorkspaceHero from '@/components/shared/WorkspaceHero.vue'
import { PORTAL_PATHS } from '@/constants/portal-routes'
import { formatDateTime, getCertificationStatusLabel, getCertificationStatusTag } from '@/utils/display'
import { validateImageFile } from '@/utils/upload'

interface ProfileDraftModel {
  username: string
  realName: string
  email: string
  phone: string
  gender: string
  avatar: string
}

type CertificationUploadField = 'idCardFrontUrl' | 'idCardBackUrl'

const router = useRouter()
const route = useRoute()

const profile = reactive({
  username: '',
  realName: '',
  email: '',
  phone: '',
  gender: 'UNKNOWN',
  avatar: '',
  points: 0,
  certified: 0
})

const profileDraft = reactive<ProfileDraftModel>({
  username: '',
  realName: '',
  email: '',
  phone: '',
  gender: 'UNKNOWN',
  avatar: ''
})

const certification = reactive<VolunteerCertificationModel>({
  userId: 0,
  realName: '',
  idCardNo: '',
  idCardFrontUrl: '',
  idCardBackUrl: '',
  status: 'NOT_SUBMITTED'
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: ''
})

const frontInputRef = ref<HTMLInputElement>()
const backInputRef = ref<HTMLInputElement>()
const profileEditing = ref(false)
const profileSaving = ref(false)
const certificationSubmitting = ref(false)
const uploadingField = ref<CertificationUploadField | null>(null)
const profileSectionRef = ref<HTMLElement>()
const certificationSectionRef = ref<HTMLElement>()
const securitySectionRef = ref<HTMLElement>()

const certificationTag = computed(() => getCertificationStatusTag(certification.status))
const certificationLabel = computed(() => getCertificationStatusLabel(certification.status))
const certificationHint = computed(() => {
  const hints: Record<CertificationStatus, string> = {
    NOT_SUBMITTED: '尚未提交实名认证，活动报名会受到限制。',
    PENDING: '认证资料已提交，等待管理员审核。',
    APPROVED: '实名认证已通过，可以正常报名活动。',
    REJECTED: '认证被驳回，请根据原因修正后重新提交。'
  }
  return hints[certification.status]
})
const certificationSubmitTip = computed(() => {
  if (certification.status === 'APPROVED') {
    return '如需补充或调整认证信息，可以重新提交，系统会重新进入审核流程。'
  }
  if (certification.status === 'PENDING') {
    return '你仍可修改当前信息并重新提交，以最新资料为准。'
  }
  return '请完整填写真实姓名、身份证号及两张证件照片。'
})
const certificationActionText = computed(() => {
  if (certification.status === 'APPROVED') {
    return '重新提交认证'
  }
  if (certification.status === 'REJECTED') {
    return '修改后重新提交'
  }
  return '提交认证'
})

function syncProfileDraft() {
  Object.assign(profileDraft, {
    username: profile.username || '',
    realName: profile.realName || '',
    email: profile.email || '',
    phone: profile.phone || '',
    gender: profile.gender || 'UNKNOWN',
    avatar: profile.avatar || ''
  })
}

async function load() {
  const [profileRes, certificationRes] = await Promise.all([getMyProfileApi(), getMyCertificationApi()])
  Object.assign(profile, profileRes)
  Object.assign(certification, certificationRes)
  syncProfileDraft()

  if (!certification.realName) {
    certification.realName = profile.realName || ''
  }
  if (!certification.status) {
    certification.status = profile.certified === 1 ? 'APPROVED' : 'NOT_SUBMITTED'
  }
}

function beginProfileEdit() {
  syncProfileDraft()
  profileEditing.value = true
}

function cancelProfileEdit() {
  syncProfileDraft()
  profileEditing.value = false
}

async function saveProfile() {
  const username = profileDraft.username.trim()
  if (!username) {
    ElMessage.warning('请输入用户名')
    return
  }

  profileSaving.value = true
  try {
    await updateMyProfileApi({
      username,
      realName: profileDraft.realName.trim(),
      email: profileDraft.email.trim(),
      phone: profileDraft.phone.trim(),
      gender: profileDraft.gender,
      avatar: profileDraft.avatar
    })
    ElMessage.success('资料已更新')
    profileEditing.value = false
    await load()
  } finally {
    profileSaving.value = false
  }
}

async function savePassword() {
  await updatePasswordApi(passwordForm)
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  ElMessage.success('密码已更新')
}

function openCertificationPicker(field: CertificationUploadField) {
  if (certificationSubmitting.value || uploadingField.value) {
    return
  }

  if (field === 'idCardFrontUrl') {
    frontInputRef.value?.click()
    return
  }

  backInputRef.value?.click()
}

async function uploadCertificationFile(field: CertificationUploadField, file: File) {
  if (!validateImageFile(file, 5)) {
    return
  }

  uploadingField.value = field
  try {
    const res = await uploadImageApi(file)
    certification[field] = res.url
    ElMessage.success('证件图片上传成功')
  } finally {
    uploadingField.value = null
  }
}

async function handleCertificationFileChange(field: CertificationUploadField, event: Event) {
  const target = event.target as HTMLInputElement | null
  const file = target?.files?.[0]
  if (target) {
    target.value = ''
  }
  if (!file) {
    return
  }

  await uploadCertificationFile(field, file)
}

async function submitCertification() {
  const realName = (certification.realName || '').trim()
  const idCardNo = (certification.idCardNo || '').trim()
  const idCardFrontUrl = (certification.idCardFrontUrl || '').trim()
  const idCardBackUrl = (certification.idCardBackUrl || '').trim()

  if (!realName) {
    ElMessage.warning('请输入真实姓名')
    return
  }
  if (!idCardNo) {
    ElMessage.warning('请输入身份证号')
    return
  }
  if (!idCardFrontUrl) {
    ElMessage.warning('请上传身份证正面照片')
    return
  }
  if (!idCardBackUrl) {
    ElMessage.warning('请上传身份证反面照片')
    return
  }

  certificationSubmitting.value = true
  try {
    await submitMyCertificationApi({
      realName,
      idCardNo,
      idCardFrontUrl,
      idCardBackUrl
    })
    ElMessage.success('认证资料已提交')
    await load()
  } finally {
    certificationSubmitting.value = false
  }
}

async function scrollToSection(section?: string) {
  await nextTick()
  if (section === 'certification') {
    certificationSectionRef.value?.scrollIntoView?.({ behavior: 'smooth', block: 'start' })
    return
  }
  if (section === 'security') {
    securitySectionRef.value?.scrollIntoView?.({ behavior: 'smooth', block: 'start' })
    return
  }
  profileSectionRef.value?.scrollIntoView?.({ behavior: 'smooth', block: 'start' })
}

watch(
  () => route.query.section,
  (section) => {
    void scrollToSection(typeof section === 'string' ? section : undefined)
  }
)

onMounted(async () => {
  await load()
  await scrollToSection(typeof route.query.section === 'string' ? route.query.section : undefined)
})
</script>

<style scoped>
.page-shell {
  display: grid;
  gap: 14px;
}

.section-actions-bar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.section-tip {
  display: grid;
  gap: 6px;
  margin-bottom: 16px;
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(245, 249, 247, 0.92);
  border: 1px solid var(--cvs-border);
  color: #4e6259;
}

.section-tip strong {
  color: #1f523b;
}

.certification-tip {
  background: rgba(247, 244, 236, 0.96);
}

.profile-form {
  max-width: 760px;
}

.certification-form {
  max-width: 860px;
}

.status-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.form-actions :deep(.el-form-item__content) {
  justify-content: flex-start;
}

.tip {
  color: var(--cvs-text-sub);
}

.certification-panel {
  display: grid;
  gap: 18px;
}

.certification-status {
  display: grid;
  gap: 10px;
}

.certification-status p {
  margin: 0;
  color: var(--cvs-text-sub);
}

.danger-text {
  color: var(--el-color-danger);
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

.upload-panel {
  display: grid;
  gap: 10px;
}

.hidden-file-input {
  display: none;
}

.upload-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.image-preview {
  width: min(320px, 100%);
  min-height: 180px;
  overflow: hidden;
  border-radius: 16px;
  border: 1px solid var(--cvs-border);
  background: #f8fbf9;
  display: grid;
  place-items: center;
}

.image-preview img {
  display: block;
  width: 100%;
  height: auto;
  object-fit: cover;
}

.image-empty {
  color: #7a8b84;
  font-size: 14px;
}

@media (max-width: 760px) {
  .certification-grid {
    grid-template-columns: 1fr;
  }
}
</style>
