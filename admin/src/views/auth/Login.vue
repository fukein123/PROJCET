<template>
  <div class="wrap">
    <div class="panel cvs-card">
      <div class="head">
        <div class="h1">社区志愿服务管理系统</div>
        <div class="h2 cvs-muted">登录</div>
      </div>

      <el-form :model="form" label-position="top" size="large" @submit.prevent>
        <el-form-item label="登录类型">
          <el-segmented v-model="form.loginType" :options="loginTypeOptions" />
        </el-form-item>
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="验证码（预留）">
          <el-input v-model="form.captcha" placeholder="后续接入图形验证码/短信验证码" />
        </el-form-item>

        <div class="actions">
          <el-button type="primary" :loading="loading" style="width: 100%" @click="onLogin">登录</el-button>
          <div class="links">
            <el-link type="primary" :underline="false" @click="goRegister">注册账号</el-link>
            <el-link :underline="false" class="cvs-muted">忘记密码</el-link>
          </div>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/userStore'
import type { LoginType } from '@/api/auth'

const router = useRouter()
const user = useUserStore()
const loading = ref(false)

const loginTypeOptions = [
  { label: '管理员', value: 'ADMIN' },
  { label: '志愿者', value: 'VOLUNTEER' }
]

const form = reactive({
  loginType: 'VOLUNTEER' as LoginType,
  username: '',
  password: '',
  captcha: ''
})

async function onLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请填写账号与密码')
    return
  }
  loading.value = true
  try {
    await user.login(form.username, form.password, form.loginType)
    if (user.role === 'VOLUNTEER') await router.push('/app/home')
    else await router.push('/app/dashboard')
  } finally {
    loading.value = false
  }
}

function goRegister() {
  router.push('/register')
}
</script>

<style scoped>
.wrap {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background: radial-gradient(1200px 600px at 20% 10%, rgba(216, 30, 43, 0.18), transparent 60%),
    radial-gradient(900px 500px at 90% 40%, rgba(0, 102, 255, 0.14), transparent 55%),
    var(--cvs-bg);
}
.panel {
  width: min(420px, 100%);
  padding: 18px 18px 16px;
}
.head {
  margin-bottom: 12px;
}
.h1 {
  font-size: 18px;
  font-weight: 800;
  letter-spacing: 0.5px;
}
.h2 {
  margin-top: 4px;
  font-size: 13px;
}
.actions {
  margin-top: 6px;
}
.links {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
}
</style>

