<template>
  <div class="register-scene">
    <section class="register-hero fade-up">
      <p class="hero-tag">社区志愿服务平台</p>
      <h1>创建志愿者账户，参与社区服务、论坛互动与活动打卡</h1>
      <ul class="hero-list">
        <li>实名认证后可报名活动，形成完整服务记录</li>
        <li>支持上传头像，打造更可信的志愿者主页</li>
        <li>全流程中文提示，降低上手门槛</li>
      </ul>
    </section>

    <section class="register-shell fade-up">
      <el-card class="register-card" shadow="never">
        <header class="card-head">
          <h2>志愿者注册</h2>
          <p>请完善以下信息，提交后即可跳转登录</p>
        </header>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="register-form">
          <div class="form-grid">
            <el-form-item label="用户名" prop="username">
              <el-input v-model.trim="form.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model.trim="form.phone" placeholder="请输入手机号" />
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input v-model.trim="form.email" placeholder="请输入邮箱地址" />
            </el-form-item>
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择性别">
                <el-option label="男" value="MALE" />
                <el-option label="女" value="FEMALE" />
                <el-option label="保密" value="UNKNOWN" />
              </el-select>
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input
                v-model="form.password"
                type="password"
                show-password
                placeholder="至少6位，包含字母和数字"
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入密码" />
            </el-form-item>

            <el-form-item class="span-2" label="头像（可选）">
              <div class="avatar-uploader">
                <img class="avatar-preview" :src="avatarPreview" alt="头像预览" />
                <div class="avatar-actions">
                  <el-upload
                    class="upload-btn"
                    :show-file-list="false"
                    :http-request="handleAvatarUpload"
                    :before-upload="beforeAvatarUpload"
                    accept="image/png,image/jpeg,image/jpg,image/webp,image/gif"
                  >
                    <el-button :loading="avatarUploading">上传头像</el-button>
                  </el-upload>
                  <el-button text @click="form.avatar = ''">清空头像</el-button>
                  <span class="tip">支持 JPG/PNG/WEBP/GIF，大小不超过 5MB</span>
                </div>
              </div>
            </el-form-item>
          </div>
        </el-form>

        <div class="action-row">
          <el-button type="primary" :loading="loading" @click="submitRegister">立即注册</el-button>
          <el-button @click="router.push('/login')">已有账号，去登录</el-button>
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
import { isPhone, isStrongPassword } from '@/utils/validate'

interface RegisterForm {
  username: string
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
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    {
      validator: (_rule, value: string, callback) => {
        if (!isPhone(value)) callback(new Error('手机号格式不正确'))
        callback()
      },
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    {
      validator: (_rule, value: string, callback) => {
        if (!isStrongPassword(value)) callback(new Error('密码至少6位，且需要包含字母和数字'))
        callback()
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule, value: string, callback) => {
        if (value !== form.password) callback(new Error('两次密码输入不一致'))
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const beforeAvatarUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const isImage = rawFile.type.startsWith('image/')
  if (!isImage) {
    ElMessage.warning('仅支持上传图片文件')
    return false
  }
  const isLt5M = rawFile.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.warning('图片大小不能超过 5MB')
    return false
  }
  return true
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
    option.onError?.(error as any)
  } finally {
    avatarUploading.value = false
  }
}

async function submitRegister() {
  if (!formRef.value) return
  await formRef.value.validate()
  loading.value = true
  try {
    await registerApi({
      username: form.username,
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
.register-scene {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  background:
    radial-gradient(circle at 14% 12%, rgba(24, 113, 79, 0.22), transparent 40%),
    radial-gradient(circle at 88% 85%, rgba(225, 170, 63, 0.2), transparent 36%),
    linear-gradient(160deg, #f7fbf8 0%, #edf3ee 100%);
}

.register-hero {
  padding: 72px 64px;
}

.hero-tag {
  display: inline-flex;
  margin: 0 0 14px;
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid rgba(21, 101, 71, 0.24);
  color: #1a5d43;
  font-size: 12px;
  letter-spacing: 0.08em;
}

.register-hero h1 {
  margin: 0;
  line-height: 1.25;
  font-size: clamp(28px, 4vw, 46px);
  color: #173a2c;
}

.hero-list {
  margin: 22px 0 0;
  padding-left: 20px;
  color: #405750;
  line-height: 1.9;
}

.register-shell {
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.register-card {
  width: min(640px, 100%);
  border-radius: 18px;
  border: 1px solid var(--cvs-border);
  box-shadow: 0 18px 40px rgba(28, 54, 44, 0.1);
  background: rgba(255, 255, 255, 0.93);
}

.card-head h2 {
  margin: 0;
  font-size: 30px;
}

.card-head p {
  margin: 8px 0 0;
  color: var(--cvs-text-sub);
}

.register-form {
  margin-top: 18px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 12px;
}

.span-2 {
  grid-column: span 2;
}

.avatar-uploader {
  display: grid;
  grid-template-columns: 82px 1fr;
  gap: 12px;
  align-items: center;
}

.avatar-preview {
  width: 82px;
  height: 82px;
  border-radius: 16px;
  object-fit: cover;
  border: 1px solid var(--cvs-border);
}

.avatar-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.tip {
  color: #65756f;
  font-size: 12px;
}

.action-row {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

@media (max-width: 1020px) {
  .register-scene {
    grid-template-columns: 1fr;
  }

  .register-hero {
    padding: 30px 22px 8px;
  }

  .register-shell {
    padding: 8px 16px 30px;
  }
}

@media (max-width: 760px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .span-2 {
    grid-column: span 1;
  }

  .avatar-uploader {
    grid-template-columns: 1fr;
  }
}
</style>
