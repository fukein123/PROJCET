<template>
  <div class="register-scene">
    <section class="register-hero fade-up">
      <p class="hero-tag">社区志愿服务平台</p>
      <h1>成为社区志愿者，让每一份善意都能被看见</h1>
      <ul class="hero-list">
        <li>坚持公益导向，围绕“服务社区、关爱邻里、共建共享”开展志愿行动。</li>
        <li>建立可追溯的服务档案，完整记录报名、打卡、反馈与成长轨迹。</li>
        <li>倡导长期参与与互助精神，让志愿服务从“活动”走向“常态”。</li>
      </ul>
    </section>

    <section class="register-shell fade-up">
      <el-card class="register-card" shadow="never">
        <header class="card-head">
          <h2>志愿者注册</h2>
          <p>请按顺序完善账号信息，提交后将跳转到登录页面。</p>
        </header>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="register-form">
          <div class="form-grid">
            <el-form-item label="志愿者账号" prop="username">
              <el-input v-model.trim="form.username" placeholder="请输入志愿者账号" />
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input
                v-model="form.password"
                type="password"
                show-password
                placeholder="至少 6 位，包含字母和数字"
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入密码" />
            </el-form-item>

            <el-form-item label="志愿者姓名" prop="realName">
              <el-input v-model.trim="form.realName" placeholder="请输入真实姓名" />
            </el-form-item>

            <el-form-item class="span-2" label="头像" prop="avatar">
              <div class="avatar-panel">
                <el-upload
                  class="avatar-upload"
                  :show-file-list="false"
                  :http-request="handleAvatarUpload"
                  :before-upload="beforeAvatarUpload"
                  accept="image/png,image/jpeg,image/jpg,image/webp,image/gif"
                >
                  <div class="upload-box">
                    <img v-if="form.avatar" class="avatar-preview" :src="form.avatar" alt="头像预览" />
                    <template v-else>
                      <span class="plus">+</span>
                      <span class="upload-text">点击上传头像</span>
                    </template>
                  </div>
                </el-upload>

                <div class="avatar-actions">
                  <el-button size="small" :loading="avatarUploading" @click="triggerUpload">选择图片</el-button>
                  <el-button size="small" text @click="clearAvatar">清空头像</el-button>
                  <span class="tip">支持 JPG/PNG/WEBP/GIF，大小不超过 5MB</span>
                </div>
              </div>
            </el-form-item>

            <el-form-item label="联系电话" prop="phone">
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
import { reactive, ref } from 'vue'
import type { FormInstance, FormRules, UploadProps, UploadRequestOptions } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { registerApi } from '@/api/auth'
import { uploadImageApi } from '@/api/common'
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

const rules: FormRules<RegisterForm> = {
  username: [{ required: true, message: '请输入志愿者账号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入志愿者姓名', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
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

function triggerUpload() {
  const trigger = document.querySelector('.avatar-upload input[type=file]') as HTMLInputElement | null
  trigger?.click()
}

function clearAvatar() {
  form.avatar = ''
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

async function submitRegister() {
  if (!formRef.value) {
    return
  }

  await formRef.value.validate()
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
.register-scene {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr 1.15fr;
  background:
    radial-gradient(circle at 14% 12%, rgba(24, 113, 79, 0.22), transparent 40%),
    radial-gradient(circle at 88% 85%, rgba(225, 170, 63, 0.2), transparent 36%),
    linear-gradient(160deg, #f7fbf8 0%, #edf3ee 100%);
}

.register-hero {
  padding: 68px 64px;
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
  line-height: 1.3;
  font-size: clamp(30px, 4vw, 44px);
  color: #173a2c;
}

.hero-list {
  margin: 24px 0 0;
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
  width: min(700px, 100%);
  border-radius: 18px;
  border: 1px solid var(--cvs-border);
  box-shadow: 0 18px 40px rgba(28, 54, 44, 0.1);
  background: rgba(255, 255, 255, 0.95);
}

.card-head h2 {
  margin: 0;
  font-size: 28px;
}

.card-head p {
  margin: 8px 0 0;
  color: var(--cvs-text-sub);
}

.register-form {
  margin-top: 16px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 12px;
}

.span-2 {
  grid-column: span 2;
}

.avatar-panel {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.upload-box {
  width: 168px;
  height: 86px;
  border-radius: 24px;
  border: 1px dashed #cfdad2;
  background: #f4f6f5;
  display: grid;
  place-items: center;
  cursor: pointer;
  overflow: hidden;
}

.plus {
  font-size: 44px;
  line-height: 1;
  color: #6a706d;
}

.upload-text {
  margin-top: -4px;
  font-size: 13px;
  color: #6a706d;
}

.avatar-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
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
  margin-top: 10px;
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
}
</style>
