<template>
  <div class="fade-up">
    <SearchForm @search="load" @reset="resetQuery">
      <el-form-item label="角色">
        <el-select v-model="query.role" clearable style="width: 140px">
          <el-option label="管理员" value="ADMIN" />
          <el-option label="志愿者" value="VOLUNTEER" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键字">
        <el-input v-model="query.keyword" placeholder="用户名/姓名/手机号" />
      </el-form-item>
    </SearchForm>

    <el-card class="module" shadow="never">
      <el-table :data="list" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" min-width="130" />
        <el-table-column prop="realName" label="姓名" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column prop="role" label="角色" width="110" />
        <el-table-column prop="certified" label="认证" width="100">
          <template #default="{ row }">
            <el-tag :type="row.certified === 1 ? 'success' : 'warning'">
              {{ row.certified === 1 ? '已认证' : '未认证' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="footer">
        <el-pagination
          layout="total, prev, pager, next"
          :current-page="query.current"
          :page-size="query.size"
          :total="total"
          @current-change="handlePage"
        />
      </div>
    </el-card>

    <el-dialog v-model="visible" title="编辑用户" width="560px" append-to-body>
      <el-form :model="form" label-width="90px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="志愿者" value="VOLUNTEER" />
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
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import SearchForm from '@/components/SearchForm.vue'
import { adminUpdateUserApi, pageUsersApi, type UserModel } from '@/api/user'

const loading = ref(false)
const list = ref<UserModel[]>([])
const total = ref(0)
const query = reactive({
  current: 1,
  size: 10,
  role: '',
  keyword: ''
})

const visible = ref(false)
const editingId = ref<number>()
const form = reactive<Partial<UserModel>>({
  username: '',
  realName: '',
  email: '',
  phone: '',
  role: 'VOLUNTEER',
  status: 1,
  certified: 0
})

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

async function load() {
  loading.value = true
  try {
    const res = await pageUsersApi({
      current: query.current,
      size: query.size,
      role: query.role || undefined,
      keyword: query.keyword || undefined
    })
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function handlePage(page: number) {
  query.current = page
  load()
}

function resetQuery() {
  query.current = 1
  query.keyword = ''
  query.role = ''
  load()
}

function openEdit(row: UserModel) {
  editingId.value = row.id
  Object.assign(form, row)
  visible.value = true
}

async function submit() {
  if (!editingId.value) return
  await adminUpdateUserApi(editingId.value, form)
  visible.value = false
  await load()
}

onMounted(load)
</script>

<style scoped>
.module {
  border: 1px solid var(--cvs-border);
  border-radius: 16px;
}

.footer {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
