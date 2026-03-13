<template>
  <AdminListScaffold title="志愿者用户管理" @search="load" @reset="resetQuery">
    <template #filters>
      <el-form-item label="角色">
        <el-select v-model="query.role" clearable style="width: 160px">
          <el-option label="志愿者" value="VOLUNTEER" />
          <el-option label="管理员" value="ADMIN" />
        </el-select>
      </el-form-item>

      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="用户名 / 姓名 / 手机号" />
      </el-form-item>
    </template>

    <template #actions>
      <el-button type="danger" plain :disabled="!selectedIds.length" @click="batchRemove">批量删除</el-button>
      <el-button type="primary" @click="openCreate">新增志愿者</el-button>
    </template>

    <el-table :data="list" border v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="头像" width="90">
        <template #default="{ row }">
          <el-avatar :size="36" :src="row.avatar" />
        </template>
      </el-table-column>
      <el-table-column prop="username" label="账号" min-width="120" />
      <el-table-column prop="realName" label="姓名" min-width="120" />
      <el-table-column prop="phone" label="手机号" min-width="130" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="role" label="角色" width="100" />
      <el-table-column prop="certified" label="认证" width="100">
        <template #default="{ row }">
          <el-tag :type="row.certified === 1 ? 'success' : 'warning'">
            {{ row.certified === 1 ? '已认证' : '未认证' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="190">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="removeOne(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <template #pagination>
      <el-pagination
        layout="total, sizes, prev, pager, next"
        :current-page="query.current"
        :page-size="query.size"
        :page-sizes="[10, 20, 30, 50]"
        :total="total"
        @current-change="handlePage"
        @size-change="handleSizeChange"
      />
    </template>
  </AdminListScaffold>

  <el-dialog v-model="visible" :title="editingId ? '编辑用户' : '新增志愿者'" width="620px" append-to-body>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
      <el-form-item label="用户名" prop="username">
        <el-input v-model.trim="form.username" />
      </el-form-item>

      <el-form-item v-if="!editingId" label="初始密码" prop="password">
        <el-input v-model.trim="form.password" type="password" show-password placeholder="至少 6 位，包含字母和数字" />
      </el-form-item>

      <el-form-item label="姓名" prop="realName">
        <el-input v-model.trim="form.realName" />
      </el-form-item>

      <el-form-item label="邮箱" prop="email">
        <el-input v-model.trim="form.email" />
      </el-form-item>

      <el-form-item label="手机号" prop="phone">
        <el-input v-model.trim="form.phone" />
      </el-form-item>

      <el-form-item label="角色">
        <el-select v-model="form.role" :disabled="!editingId">
          <el-option label="志愿者" value="VOLUNTEER" />
          <el-option label="管理员" value="ADMIN" />
        </el-select>
      </el-form-item>

      <el-form-item label="状态">
        <el-switch v-model="statusSwitch" />
      </el-form-item>

      <el-form-item label="认证">
        <el-switch v-model="certifiedSwitch" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import AdminListScaffold from '@/components/admin/AdminListScaffold.vue'
import {
  adminUpdateUserApi,
  batchDeleteVolunteerUsersApi,
  createVolunteerUserApi,
  deleteVolunteerUserApi,
  pageUsersApi,
  type UserModel
} from '@/api/user'
import { useSelectionIds } from '@/composables/useSelectionIds'
import { useTable } from '@/composables/useTable'
import { runConfirmedAction } from '@/utils/confirmed-action'

interface UserQuery {
  current: number
  size: number
  role: string
  keyword: string
}

const {
  loading,
  records: list,
  total,
  query,
  load,
  reset,
  handlePage,
  handleSizeChange
} = useTable<UserModel, UserQuery>({
  initialQuery: {
    current: 1,
    size: 10,
    role: 'VOLUNTEER',
    keyword: ''
  },
  fetcher: (params) =>
    pageUsersApi({
      current: params.current,
      size: params.size,
      role: params.role || undefined,
      keyword: params.keyword || undefined
    })
})

const { selectedIds, handleSelectionChange, clearSelection } = useSelectionIds<UserModel>()
const visible = ref(false)
const editingId = ref<number>()
const formRef = ref<FormInstance>()
const form = reactive<{
  username: string
  password?: string
  realName: string
  email: string
  phone: string
  role: 'ADMIN' | 'VOLUNTEER'
  status: number
  certified: number
}>({
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  role: 'VOLUNTEER',
  status: 1,
  certified: 0
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }]
}

const statusSwitch = computed({
  get: () => form.status === 1,
  set: (value: boolean) => {
    form.status = value ? 1 : 0
  }
})

const certifiedSwitch = computed({
  get: () => form.certified === 1,
  set: (value: boolean) => {
    form.certified = value ? 1 : 0
  }
})

function resetQuery() {
  reset()
}

function openCreate() {
  editingId.value = undefined
  Object.assign(form, {
    username: '',
    password: '',
    realName: '',
    email: '',
    phone: '',
    role: 'VOLUNTEER',
    status: 1,
    certified: 0
  })
  visible.value = true
}

function openEdit(row: UserModel) {
  editingId.value = row.id
  Object.assign(form, {
    username: row.username,
    password: '',
    realName: row.realName || '',
    email: row.email || '',
    phone: row.phone || '',
    role: row.role,
    status: row.status,
    certified: row.certified
  })
  visible.value = true
}

async function submit() {
  if (!formRef.value) {
    return
  }

  await formRef.value.validate()
  if (editingId.value) {
    await adminUpdateUserApi(editingId.value, {
      username: form.username,
      realName: form.realName,
      email: form.email,
      phone: form.phone,
      role: form.role,
      status: form.status,
      certified: form.certified
    })
    ElMessage.success('用户信息已更新')
  } else {
    await createVolunteerUserApi({
      username: form.username,
      password: form.password || '',
      realName: form.realName,
      email: form.email,
      phone: form.phone,
      status: form.status,
      certified: form.certified
    })
    ElMessage.success('志愿者已创建')
  }

  visible.value = false
  await load()
}

async function removeOne(row: UserModel) {
  await runConfirmedAction({
    message: `确认删除用户“${row.username}”吗？`,
    title: '删除用户',
    action: () => deleteVolunteerUserApi(row.id),
    successMessage: '用户已删除',
    afterSuccess: load
  })
}

async function batchRemove() {
  if (!selectedIds.value.length) {
    return
  }

  await runConfirmedAction({
    message: `确认批量删除 ${selectedIds.value.length} 个用户？`,
    title: '批量删除用户',
    action: () => batchDeleteVolunteerUsersApi(selectedIds.value),
    successMessage: '批量删除成功',
    afterSuccess: async () => {
      clearSelection()
      await load()
    }
  })
}

onMounted(load)
</script>