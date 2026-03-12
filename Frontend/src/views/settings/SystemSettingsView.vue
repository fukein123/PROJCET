<template>
  <div class="settings-root fade-up">
    <el-card class="module" shadow="never">
      <template #header>管理员个人信息</template>
      <el-form :model="profile" label-width="100px" class="form">
        <el-form-item label="用户名">
          <el-input v-model="profile.username" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="profile.realName" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="profile.email" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="profile.phone" />
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="saveProfile">保存资料</el-button>
    </el-card>

    <el-card class="module" shadow="never">
      <template #header>修改密码</template>
      <el-form :model="passwordForm" label-width="100px" class="form">
        <el-form-item label="旧密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <el-button type="warning" @click="savePassword">更新密码</el-button>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyProfileApi, updateMyProfileApi, updatePasswordApi } from '@/api/user'

const profile = reactive({
  username: '',
  realName: '',
  email: '',
  phone: '',
  gender: 'UNKNOWN',
  avatar: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: ''
})

async function load() {
  Object.assign(profile, await getMyProfileApi())
}

async function saveProfile() {
  await updateMyProfileApi(profile)
  ElMessage.success('保存成功')
}

async function savePassword() {
  await updatePasswordApi(passwordForm)
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  ElMessage.success('密码已更新')
}

onMounted(load)
</script>

<style scoped>
.settings-root {
  display: grid;
  gap: 14px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.module {
  border: 1px solid var(--cvs-border);
  border-radius: 16px;
}

.form {
  margin-bottom: 10px;
}

@media (max-width: 900px) {
  .settings-root {
    grid-template-columns: 1fr;
  }
}
</style>

