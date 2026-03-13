<template>
  <div class="page-shell">
    <WorkspaceHero
      compact
      eyebrow="个人中心"
      title="统一维护个人资料与账户安全"
      description="在工作台内集中维护基础资料、联系方式与账户密码，让志愿服务报名、通知触达与个人信息更新保持一致。"
    >
      <template #actions>
        <el-button type="primary" @click="router.push('/volunteer/apply-records')">查看报名记录</el-button>
        <el-button @click="router.push('/volunteer/check-records')">查看服务记录</el-button>
      </template>
      <template #aside>
        <div class="hero-stat-grid">
          <article class="hero-stat">
            <h3>认证状态</h3>
            <strong>{{ profile.certified === 1 ? '已认证' : '待认证' }}</strong>
            <span>{{ profile.certified === 1 ? '已满足活动报名条件' : '完成认证后可报名活动' }}</span>
          </article>
          <article class="hero-stat">
            <h3>联系方式</h3>
            <strong>{{ profile.phone || '待补充' }}</strong>
            <span>{{ profile.email || '建议补充邮箱用于接收平台通知' }}</span>
          </article>
        </div>
      </template>
    </WorkspaceHero>

    <VolunteerPageSection eyebrow="基础资料" title="资料维护" description="更新姓名、邮箱、手机号等资料，保持报名与通知信息一致。">
      <el-form :model="profile" label-width="100px" class="profile-form">
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
        <el-form-item label="认证状态">
          <el-tag :type="profile.certified === 1 ? 'success' : 'warning'">
            {{ profile.certified === 1 ? '已认证' : '待认证' }}
          </el-tag>
          <span class="tip">仅认证通过后可报名活动</span>
        </el-form-item>
        <el-form-item class="form-actions">
          <el-button type="primary" @click="saveProfile">保存资料</el-button>
        </el-form-item>
      </el-form>
    </VolunteerPageSection>

    <VolunteerPageSection eyebrow="账户安全" title="密码更新" description="修改登录密码后会继续保留当前账户角色边界与工作台访问范围。">
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
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyProfileApi, updateMyProfileApi, updatePasswordApi } from '@/api/user'
import VolunteerPageSection from '@/components/volunteer/VolunteerPageSection.vue'
import WorkspaceHero from '@/components/shared/WorkspaceHero.vue'

const router = useRouter()

const profile = reactive({
  username: '',
  realName: '',
  email: '',
  phone: '',
  gender: 'UNKNOWN',
  avatar: '',
  certified: 0
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
  ElMessage.success('资料已更新')
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
.page-shell {
  display: grid;
  gap: 14px;
}

.profile-form {
  max-width: 760px;
}

.form-actions :deep(.el-form-item__content) {
  justify-content: flex-start;
}

.tip {
  margin-left: 8px;
  color: var(--cvs-text-sub);
}
</style>
