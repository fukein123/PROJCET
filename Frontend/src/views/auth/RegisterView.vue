<template>
  <div class="register-page">
    <section class="register-hero fade-up">
      <p class="hero-tag">社区志愿服务平台</p>
      <h1>注册志愿者账号，进入社区志愿服务主页面</h1>
      <p class="hero-copy">
        注册后可直接在主页面完成活动报名、打卡记录、论坛互动、积分兑换和个人资料维护。
      </p>
    </section>

    <section class="register-shell fade-up">
      <el-card class="register-card" shadow="never">
        <header class="card-head">
          <h2>志愿者注册</h2>
          <p>请按顺序填写注册信息。</p>
        </header>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          class="register-form"
          @keydown.enter.prevent="submitRegister"
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model.trim="form.username" placeholder="请输入登录用账号" />
          </el-form-item>

          <el-form-item label="姓名" prop="realName">
            <el-input v-model.trim="form.realName" placeholder="请输入真实姓名" />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="至少 6 位，需包含字母和数字"
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入密码" />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model.trim="form.email" placeholder="请输入邮箱地址" />
          </el-form-item>

          <el-form-item label="头像（可选）">
            <div class="avatar-panel">
              <div class="avatar-preview-shell">
                <el-avatar class="avatar-preview" :size="88" :src="avatarPreview" />
              </div>

              <div class="avatar-info">
                <div class="avatar-action-group">
                  <el-upload
                    class="avatar-upload"
                    :show-file-list="false"
                    :http-request="handleAvatarUpload"
                    :before-upload="beforeAvatarUpload"
                    accept="image/png,image/jpeg,image/jpg,image/webp,image/gif"
                  >
                    <el-button class="avatar-button" type="primary" plain :loading="avatarUploading">选择头像</el-button>
                  </el-upload>
                </div>

                <span class="tip">支持 JPG / PNG / WEBP / GIF，单张不超过 5MB。</span>
              </div>
            </div>
          </el-form-item>

          <el-form-item label="性别" prop="gender">
            <el-select v-model="form.gender" placeholder="请选择性别">
              <el-option label="男" value="MALE" />
              <el-option label="女" value="FEMALE" />
              <el-option label="保密" value="UNKNOWN" />
            </el-select>
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input v-model.trim="form.phone" placeholder="请输入手机号" />
          </el-form-item>
        </el-form>

        <div class="action-row">
          <el-button @click="router.push('/login')">已有账号？去登录</el-button>
          <el-button type="primary" :loading="loading" @click="submitRegister">注册</el-button>
        </div>
      </el-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import type { FormInstance, FormRules, UploadProps, UploadRequestOptions } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { registerApi } from '@/api/auth'
import { uploadImageApi } from '@/api/common'
import { validateElementForm } from '@/utils/form'
import { validateImageFile } from '@/utils/upload'
import { isPhone, isStrongPassword } from '@/utils/validate'

interface RegisterForm {
  username: string
  realName: string
  password: string
  confirmPassword: string
  email: string
  phone: string
  gender: string
  avatar: string
}

const defaultAvatar = 'https://cdn.jsdelivr.net/gh/fukexin123/assets/default-avatar.png'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const avatarUploading = ref(false)

const form = reactive<RegisterForm>({
  username: '',
  realName: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  gender: 'UNKNOWN',
  avatar: ''
})

const avatarPreview = computed(() => form.avatar || defaultAvatar)

const rules: FormRules<RegisterForm> = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    {
      validator: (_rule, value: string, callback) => {
        if (!isPhone(value)) {
          callback(new Error('手机号格式不正确'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    {
      validator: (_rule, value: string, callback) => {
        if (!isStrongPassword(value)) {
          callback(new Error('密码至少 6 位，且需要包含字母和数字'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule, value: string, callback) => {
        if (value !== form.password) {
          callback(new Error('两次密码输入不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const beforeAvatarUpload: UploadProps['beforeUpload'] = (rawFile) => validateImageFile(rawFile)

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

async function submitRegister() {
  if (!(await validateElementForm(formRef.value))) {
    return
  }

  loading.value = true
  try {
    await registerApi({
      username: form.username,
      realName: form.realName,
      password: form.password,
      confirmPassword: form.confirmPassword,
      email: form.email,
      phone: form.phone,
      gender: form.gender,
      avatar: form.avatar || undefined
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  padding: 36px 16px 48px;
  display: grid;
  justify-items: center;
  gap: 18px;
  background:
    radial-gradient(circle at 12% 12%, rgba(16, 100, 69, 0.18), transparent 34%),
    radial-gradient(circle at 88% 10%, rgba(231, 167, 63, 0.14), transparent 28%),
    linear-gradient(180deg, #f7fbf8 0%, #eef3ef 100%);
}

.register-hero {
  width: min(560px, 100%);
  text-align: center;
  display: grid;
  gap: 12px;
}

.hero-tag {
  margin: 0 auto;
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid rgba(21, 101, 71, 0.24);
  color: #1a5d43;
  font-size: 12px;
  letter-spacing: 0.08em;
}

.register-hero h1 {
  margin: 0;
  font-size: clamp(30px, 5vw, 42px);
  line-height: 1.28;
  color: #173a2c;
}

.hero-copy {
  margin: 0;
  color: #536660;
  line-height: 1.8;
}

.register-shell {
  width: min(560px, 100%);
}

.register-card {
  border-radius: 20px;
  border: 1px solid var(--cvs-border);
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 18px 40px rgba(28, 54, 44, 0.1);
}

.card-head h2 {
  margin: 0;
  font-size: 28px;
}

.card-head p {
  margin: 8px 0 0;
  color: var(--cvs-text-sub);
  line-height: 1.7;
}

.register-form {
  margin-top: 18px;
}

.register-form :deep(.el-select) {
  width: 100%;
}

.avatar-panel {
  display: grid;
  grid-template-columns: 100px minmax(0, 1fr);
  gap: 14px;
  align-items: center;
  padding: 14px 16px;
  border: 1px solid rgba(207, 217, 211, 0.92);
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(248, 251, 249, 0.98), rgba(255, 255, 255, 0.98));
}

.avatar-preview-shell {
  width: 100px;
  height: 100px;
  display: grid;
  place-items: center;
  border-radius: 22px;
  background: linear-gradient(180deg, #f5f8f6, #eef4f1);
  border: 1px solid rgba(31, 122, 84, 0.1);
}

.avatar-preview {
  border: 4px solid rgba(255, 255, 255, 0.94);
  box-shadow:
    0 0 0 1px rgba(31, 122, 84, 0.12),
    0 10px 22px rgba(27, 56, 43, 0.08);
  background: #f3f5f4;
}

.avatar-info {
  display: grid;
  gap: 8px;
  min-width: 0;
  align-content: center;
  justify-items: start;
}

.avatar-action-group {
  display: flex;
  gap: 0;
  align-items: center;
}

.avatar-upload :deep(.el-upload) {
  display: flex;
}

.avatar-button {
  min-width: 112px;
}

.tip {
  color: #65756f;
  font-size: 12px;
  line-height: 1.6;
}

.action-row {
  margin-top: 16px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

@media (max-width: 640px) {
  .register-page {
    padding-inline: 12px;
  }

  .avatar-panel {
    grid-template-columns: 1fr;
    justify-items: start;
  }

  .avatar-preview-shell {
    width: 92px;
    height: 92px;
  }

  .avatar-action-group,
  .action-row {
    flex-direction: column;
  }

  .avatar-action-group :deep(.el-button),
  .action-row :deep(.el-button) {
    width: 100%;
  }
}
</style>
