<template>
  <div class="auth-page">
    <div class="auth-left">
      <h1>社区志愿服务平台</h1>
      <p>连接社区力量，让每一次善意都被看见与记录。</p>
      <div class="chip-row">
        <span>活动管理</span>
        <span>报名审核</span>
        <span>签到签退</span>
        <span>论坛互动</span>
      </div>
    </div>
    <div class="auth-card fade-up">
      <h2>登录账号</h2>
      <p class="role-hint">系统将自动识别账号角色并进入对应工作台</p>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item prop="username" label="账号">
          <el-input v-model="form.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item prop="password" label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item prop="captchaInput" label="验证码">
          <div class="captcha-row">
            <el-input v-model="form.captchaInput" placeholder="输入右侧验证码" />
            <button class="captcha" type="button" @click="refreshCaptcha">{{ captchaCode }}</button>
          </div>
        </el-form-item>
      </el-form>

      <div class="action-row">
        <el-button type="primary" :loading="loading" @click="submitLogin">登录</el-button>
        <el-button @click="router.push('/register')">注册</el-button>
        <el-button text @click="ElMessage.info('请联系管理员重置密码')">忘记密码</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/userStore'

interface LoginForm {
  username: string
  password: string
  captchaInput: string
}

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const formRef = ref<FormInstance>()
const captchaCode = ref('')

const form = reactive<LoginForm>({
  username: '',
  password: '',
  captchaInput: ''
})

const rules: FormRules<LoginForm> = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaInput: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

function refreshCaptcha() {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
  captchaCode.value = Array.from({ length: 4 })
    .map(() => chars[Math.floor(Math.random() * chars.length)])
    .join('')
}

async function submitLogin() {
  if (!formRef.value) return
  await formRef.value.validate()
  if (form.captchaInput.toUpperCase() !== captchaCode.value) {
    ElMessage.error('验证码错误')
    refreshCaptcha()
    return
  }
  loading.value = true
  try {
    await userStore.login({
      username: form.username,
      password: form.password
    })
    const redirect = (route.query.redirect as string) || ''
    if (redirect) {
      router.push(redirect)
      return
    }
    if (userStore.role === 'ADMIN') {
      router.push('/admin/dashboard')
    } else {
      router.push('/volunteer/home')
    }
  } finally {
    loading.value = false
  }
}

refreshCaptcha()
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.15fr 1fr;
}

.auth-left {
  background:
    linear-gradient(140deg, rgba(25, 70, 53, 0.88), rgba(15, 50, 38, 0.9)),
    url('https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?auto=format&fit=crop&w=1800&q=80')
      center/cover no-repeat;
  color: #ecfff4;
  padding: 84px 72px;
}

.auth-left h1 {
  margin: 0;
  font-size: 46px;
  letter-spacing: 0.02em;
}

.auth-left p {
  margin: 16px 0 0;
  max-width: 450px;
  line-height: 1.7;
  color: #c3ebd6;
}

.chip-row {
  margin-top: 30px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.chip-row span {
  border: 1px solid rgba(167, 255, 209, 0.3);
  padding: 8px 12px;
  border-radius: 999px;
  font-size: 13px;
}

.auth-card {
  max-width: 460px;
  margin: auto;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid var(--cvs-border);
  border-radius: 18px;
  padding: 28px 28px 22px;
  box-shadow: 0 16px 40px rgba(29, 44, 36, 0.08);
}

.auth-card h2 {
  margin: 0 0 12px;
  font-size: 24px;
}

.role-hint {
  margin: 0 0 14px;
  color: var(--cvs-text-sub);
  font-size: 13px;
}

.captcha-row {
  display: grid;
  grid-template-columns: 1fr 110px;
  gap: 8px;
}

.captcha {
  border: 1px solid var(--cvs-border);
  border-radius: 8px;
  background: #f2f6f2;
  cursor: pointer;
  font-weight: 800;
  letter-spacing: 0.12em;
}

.action-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

@media (max-width: 1000px) {
  .auth-page {
    grid-template-columns: 1fr;
  }
  .auth-left {
    display: none;
  }
  .auth-card {
    margin: 24px;
  }
}
</style>
