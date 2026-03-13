<template>
  <div class="page-shell">
    <WorkspaceHero
      compact
      eyebrow="我的帖子"
      title="统一管理个人发帖与审核状态"
      description="在工作台内持续维护论坛内容，跟踪审核结果、驳回原因与再次编辑入口。"
    >
      <template #actions>
        <el-button type="primary" @click="openCreate">发布帖子</el-button>
        <el-button @click="router.push('/portal/forum')">进入社区论坛</el-button>
        <el-button @click="load">刷新列表</el-button>
      </template>
    </WorkspaceHero>

    <VolunteerPageSection eyebrow="论坛内容" title="我的帖子列表" description="统一查看帖子标题、分类、审核状态与原因说明。">
      <StatePanel
        v-if="loading"
        state="loading"
        title="正在同步帖子列表"
        description="正在加载你的发帖记录与审核结果。"
      />

      <template v-else-if="list.length">
        <el-table :data="list" border>
          <el-table-column prop="title" label="标题" min-width="220" />
          <el-table-column label="分类" min-width="140">
            <template #default="{ row }">
              {{ categoryName(row.categoryId) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="审核状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getForumPostStatusTag(row.status)">{{ getForumPostStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="auditReason" label="审核说明" min-width="220" />
          <el-table-column label="提交时间" min-width="180">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <StatePanel
        v-else
        title="暂无个人帖子"
        description="可以直接发布帖子，统一进入待审核队列。"
      />

      <template #footer>
        <el-pagination
          v-if="!loading && total > 0"
          layout="total, prev, pager, next"
          :total="total"
          :current-page="query.current"
          :page-size="query.size"
          @current-change="handlePage"
        />
      </template>
    </VolunteerPageSection>

    <el-dialog
      v-model="visible"
      :title="form.id ? '编辑帖子' : '新增帖子'"
      width="min(92vw, 760px)"
      top="5vh"
      append-to-body
      class="post-dialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" style="width: 100%">
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" rows="8" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">提交审核</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import {
  listForumCategoriesApi,
  pageForumPostsApi,
  saveMyPostApi,
  type ForumCategoryModel,
  type PostModel
} from '@/api/content'
import StatePanel from '@/components/shared/StatePanel.vue'
import WorkspaceHero from '@/components/shared/WorkspaceHero.vue'
import VolunteerPageSection from '@/components/volunteer/VolunteerPageSection.vue'
import { useTable } from '@/composables/useTable'
import { formatDateTime, getForumPostStatusLabel, getForumPostStatusTag } from '@/utils/display'

interface MyPostQuery {
  current: number
  size: number
}

const router = useRouter()
const categories = ref<ForumCategoryModel[]>([])
const visible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<Partial<PostModel>>({
  id: undefined,
  title: '',
  categoryId: undefined,
  content: ''
})
const rules: FormRules = {
  title: [{ required: true, message: '请输入帖子标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择帖子分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入帖子内容', trigger: 'blur' }]
}

const { loading, records: list, total, query, load, handlePage } = useTable<PostModel, MyPostQuery>({
  initialQuery: {
    current: 1,
    size: 10
  },
  fetcher: (params) =>
    pageForumPostsApi({
      current: params.current,
      size: params.size,
      onlyMine: true,
      onlyApproved: false
    })
})

function openCreate() {
  Object.assign(form, { id: undefined, title: '', categoryId: categories.value[0]?.id, content: '' })
  visible.value = true
}

function openEdit(row: PostModel) {
  Object.assign(form, row)
  visible.value = true
}

async function submit() {
  if (!formRef.value) {
    return
  }
  form.title = (form.title || '').trim()
  form.content = (form.content || '').trim()
  await formRef.value.validate()
  await saveMyPostApi(form)
  visible.value = false
  ElMessage.success('帖子已提交，等待审核')
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
.page-shell {
  display: grid;
  gap: 14px;
}

:deep(.post-dialog .el-dialog__body) {
  max-height: calc(100vh - 260px);
  overflow: auto;
}
</style>
