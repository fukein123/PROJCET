<template>
  <div class="register-page">
    <el-card class="register-card fade-up" shadow="never">
      <h2>志愿者注册</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="double-grid">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" />
          </el-form-item>
          <el-form-item label="性别" prop="gender">
            <el-select v-model="form.gender">
              <el-option label="男" value="MALE" />
              <el-option label="女" value="FEMALE" />
              <el-option label="保密" value="UNKNOWN" />
            </el-select>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" show-password />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" show-password />
          </el-form-item>
        </div>
        <el-form-item label="头像（可选）">
          <el-input v-model="form.avatar" placeholder="留空则使用系统默认头像" />
        </el-form-item>
      </el-form>

      <div class="action-row">
        <el-button type="primary" :loading="loading" @click="submitRegister">注册</el-button>
        <el-button @click="router.push('/login')">已有账号？去登录</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { registerApi } from '@/api/auth'
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

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<RegisterForm>({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  gender: 'UNKNOWN',
  avatar: ''
})

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
        if (!isStrongPassword(value)) callback(new Error('密码至少6位，需包含字母和数字'))
        callback()
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule, value: string, callback) => {
        if (value !== form.password) callback(new Error('两次密码不一致'))
        callback()
      },
      trigger: 'blur'
    }
  ]
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
.register-page {
  min-height: 100vh;
  padding: 40px 16px;
}

.register-card {
  width: min(860px, 100%);
  margin: 0 auto;
  border: 1px solid var(--cvs-border);
  border-radius: 20px;
}

.register-card h2 {
  margin-top: 0;
  font-size: 28px;
}

.double-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 12px;
}

.action-row {
  display: flex;
  gap: 8px;
}

@media (max-width: 760px) {
  .double-grid {
    grid-template-columns: 1fr;
  }
}
</style>

