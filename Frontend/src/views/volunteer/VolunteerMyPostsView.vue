<template>
  <div class="fade-up">
    <el-card class="module" shadow="never">
      <template #header>
        <div class="head">
          <span>我的帖子</span>
          <el-button type="primary" @click="openCreate">发布帖子</el-button>
        </div>
      </template>
      <el-table :data="list" border>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column label="分类" min-width="130">
          <template #default="{ row }">
            {{ categoryName(row.categoryId) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="auditReason" label="审核说明" min-width="200" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="visible"
      :title="form.id ? '编辑帖子' : '新增帖子'"
      width="min(92vw, 760px)"
      top="5vh"
      append-to-body
      class="post-dialog"
    >
      <el-form :model="form" label-position="top">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" style="width: 100%">
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" rows="8" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">提交（进入待审核）</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import {
  listForumCategoriesApi,
  pageForumPostsApi,
  saveMyPostApi,
  type ForumCategoryModel,
  type PostModel
} from '@/api/content'

const list = ref<PostModel[]>([])
const categories = ref<ForumCategoryModel[]>([])
const visible = ref(false)
const form = reactive<Partial<PostModel>>({
  id: undefined,
  title: '',
  categoryId: undefined,
  content: ''
})

async function load() {
  const res = await pageForumPostsApi({
    current: 1,
    size: 50,
    onlyMine: true,
    onlyApproved: false
  })
  list.value = res.records
}

function openCreate() {
  Object.assign(form, { id: undefined, title: '', categoryId: categories.value[0]?.id, content: '' })
  visible.value = true
}

function openEdit(row: PostModel) {
  Object.assign(form, row)
  visible.value = true
}

async function submit() {
  await saveMyPostApi(form)
  visible.value = false
  await load()
}

function categoryName(categoryId: number) {
  const category = categories.value.find((item) => item.id === categoryId)
  return category?.name || `分类#${categoryId}`
}

onMounted(async () => {
  categories.value = await listForumCategoriesApi()
  const firstCategory = categories.value[0]
  if (!form.categoryId && firstCategory !== undefined) {
    form.categoryId = firstCategory.id
  }
  await load()
})
</script>

<style scoped>
.module {
  border: 1px solid var(--cvs-border);
  border-radius: 16px;
}

.head {
  display: flex;
  justify-content: space-between;
}

:deep(.post-dialog .el-dialog__body) {
  max-height: calc(100vh - 260px);
  overflow: auto;
}
</style>
