<template>
  <div class="wrap">
    <div class="panel cvs-card">
      <div class="head">
        <div class="h1">社区志愿服务管理系统</div>
        <div class="h2 cvs-muted">志愿者注册</div>
      </div>

      <el-form label-position="top" size="large" @submit.prevent>
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="3-32 位" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="至少 8 位" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="form.password2" type="password" show-password placeholder="再次输入密码" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="可选" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="可选" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender" placeholder="请选择" style="width: 100%">
            <el-option label="未知" :value="0" />
            <el-option label="男" :value="1" />
            <el-option label="女" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="头像（可选）">
          <el-input v-model="form.avatarUrl" placeholder="先以 URL 占位，后续接入上传组件" />
        </el-form-item>

        <div class="actions">
          <el-button type="primary" :loading="loading" style="width: 100%" @click="onRegister">注册</el-button>
          <div class="links">
            <el-link type="primary" :underline="false" @click="goLogin">已有账号？去登录</el-link>
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
import { apiRegister } from '@/api/auth'

const router = useRouter()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  password2: '',
  email: '',
  phone: '',
  gender: 0,
  avatarUrl: ''
})

async function onRegister() {
  if (!form.username || !form.password) {
    ElMessage.warning('请填写用户名与密码')
    return
  }
  if (form.password !== form.password2) {
    ElMessage.warning('两次密码不一致')
    return
  }
  loading.value = true
  try {
    await apiRegister({
      username: form.username,
      password: form.password,
      email: form.email || undefined,
      phone: form.phone || undefined,
      gender: form.gender,
      avatarUrl: form.avatarUrl || undefined
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}

function goLogin() {
  router.push('/login')
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
  width: min(520px, 100%);
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
  justify-content: flex-start;
}
</style>

