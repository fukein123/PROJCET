<template>
  <div class="auth-scene">
    <section class="story-panel fade-up">
      <p class="panel-tag">社区志愿服务</p>
      <h1>让每一次善意都被记录、被看见、被回应</h1>
      <p class="panel-desc">
        从活动发布、报名审核，到签到签退与论坛互动，统一协同，帮助社区志愿服务更高效地持续运行。
      </p>
      <div class="impact-grid">
        <article>
          <h3>活动组织</h3>
          <p>线上发布、分类管理、状态追踪一体化。</p>
        </article>
        <article>
          <h3>服务闭环</h3>
          <p>报名、审核、签到、评价形成可追踪流程。</p>
        </article>
        <article>
          <h3>社区共建</h3>
          <p>论坛互动与公告同步，信息透明高效。</p>
        </article>
      </div>
    </section>

    <section class="auth-shell">
      <el-card class="auth-card fade-up" shadow="never">
        <header class="card-head">
          <h2>账号登录</h2>
          <p>请选择登录入口，系统将按角色进入对应工作台。</p>
        </header>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="auth-form">
          <el-form-item prop="role" label="登录入口">
            <el-select v-model="form.role" placeholder="请选择登录入口">
              <el-option label="管理员登录" value="ADMIN" />
              <el-option label="志愿者登录" value="VOLUNTEER" />
            </el-select>
          </el-form-item>

          <el-form-item prop="username" label="账号">
            <el-input v-model.trim="form.username" placeholder="请输入账号" />
          </el-form-item>

          <el-form-item prop="password" label="密码">
            <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
          </el-form-item>

          <el-form-item prop="captchaInput" label="验证码">
            <div class="captcha-row">
              <el-input v-model.trim="form.captchaInput" placeholder="输入右侧验证码" />
              <button class="captcha" type="button" @click="refreshCaptcha">{{ captchaCode }}</button>
            </div>
          </el-form-item>
        </el-form>

        <div class="action-row">
          <el-button type="primary" :loading="loading" @click="submitLogin">登录</el-button>
          <el-button @click="router.push('/register')">注册</el-button>
          <el-button text @click="ElMessage.info('请联系管理员重置密码')">忘记密码</el-button>
        </div>
        <p class="login-tip">默认账号：管理员 admin / 123456，志愿者 volunteer / 123456</p>
      </el-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'

interface LoginForm {
  role: 'ADMIN' | 'VOLUNTEER'
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
const lastRoleStorageKey = 'cvs:last-login-role'
const lastRole = localStorage.getItem(lastRoleStorageKey)
const defaultRole: LoginForm['role'] = lastRole === 'VOLUNTEER' ? 'VOLUNTEER' : 'ADMIN'

const form = reactive<LoginForm>({
  role: defaultRole,
  username: '',
  password: '',
  captchaInput: ''
})

const rules: FormRules<LoginForm> = {
  role: [{ required: true, message: '请选择登录入口', trigger: 'change' }],
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
    ElMessage.error('验证码错误，请重新输入')
    form.captchaInput = ''
    refreshCaptcha()
    return
  }

  loading.value = true
  try {
    await userStore.login({
      username: form.username,
      password: form.password,
      role: form.role
    })
    localStorage.setItem(lastRoleStorageKey, form.role)

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
  } catch (error: any) {
    const message = String(error?.message || '')
    if (message.includes('角色') && form.username.trim().toLowerCase() === 'admin') {
      form.role = 'ADMIN'
      ElMessage.info('检测到管理员账号，已切换为管理员入口，请重新登录')
    }
    form.captchaInput = ''
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

watch(
  () => form.username,
  (value) => {
    if (value.trim().toLowerCase() === 'admin') {
      form.role = 'ADMIN'
    }
  }
)

refreshCaptcha()
</script>

<style scoped>
.auth-scene {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.15fr 1fr;
  background:
    radial-gradient(circle at 8% 18%, rgba(29, 98, 72, 0.26), transparent 40%),
    radial-gradient(circle at 90% 85%, rgba(235, 176, 61, 0.22), transparent 35%),
    linear-gradient(160deg, #edf5ef 0%, #f8fbf9 50%, #f3f7f3 100%);
}

.story-panel {
  padding: 74px 68px;
  position: relative;
}

.panel-tag {
  display: inline-flex;
  border: 1px solid rgba(16, 105, 74, 0.28);
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 12px;
  letter-spacing: 0.08em;
  color: #1a6b4a;
  margin: 0 0 14px;
  background: rgba(255, 255, 255, 0.52);
}

.story-panel h1 {
  margin: 0;
  font-size: clamp(30px, 4vw, 52px);
  line-height: 1.16;
  color: #123327;
}

.panel-desc {
  margin: 18px 0 0;
  max-width: 560px;
  line-height: 1.78;
  color: #3d5950;
  font-size: 15px;
}

.impact-grid {
  margin-top: 28px;
  display: grid;
  gap: 12px;
}

.impact-grid article {
  border: 1px solid rgba(20, 83, 62, 0.18);
  border-radius: 14px;
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(3px);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.impact-grid article:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(26, 52, 42, 0.08);
}

.impact-grid h3 {
  margin: 0;
  font-size: 16px;
}

.impact-grid p {
  margin: 8px 0 0;
  color: #51665e;
  line-height: 1.7;
}

.auth-shell {
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.auth-card {
  width: min(480px, 100%);
  border-radius: 20px;
  border: 1px solid var(--cvs-border);
  box-shadow: 0 18px 42px rgba(33, 57, 47, 0.1);
  background: rgba(255, 255, 255, 0.9);
}

.card-head h2 {
  margin: 0;
  font-size: 28px;
  color: #1d2a25;
}

.card-head p {
  margin: 10px 0 0;
  color: var(--cvs-text-sub);
  line-height: 1.6;
}

.auth-form {
  margin-top: 16px;
}

.auth-form :deep(.el-select) {
  width: 100%;
}

.captcha-row {
  display: grid;
  grid-template-columns: 1fr 130px;
  gap: 8px;
}

.captcha {
  border: 1px solid var(--cvs-border);
  border-radius: 8px;
  background: #f2f7f2;
  cursor: pointer;
  font-weight: 800;
  color: #244639;
  letter-spacing: 0.1em;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.captcha:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 14px rgba(28, 58, 46, 0.12);
}

.action-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 4px;
}

.login-tip {
  margin: 10px 0 0;
  color: #5f706a;
  font-size: 12px;
}

@media (max-width: 1000px) {
  .auth-scene {
    grid-template-columns: 1fr;
  }

  .story-panel {
    padding: 28px 22px 12px;
  }

  .auth-shell {
    padding: 8px 16px 28px;
  }
}
</style>
