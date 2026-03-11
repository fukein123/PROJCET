<template>
  <div class="fade-up">
    <el-card class="module" shadow="never">
      <el-tabs v-model="tab">
        <el-tab-pane label="信息动态" name="dynamic">
          <div class="tab-head">
            <span>信息动态列表</span>
            <el-button type="primary" @click="openDynamicCreate">新增动态</el-button>
          </div>
          <el-table :data="dynamics" border>
            <el-table-column prop="title" label="标题" min-width="180" />
            <el-table-column prop="type" label="类型" width="120" />
            <el-table-column prop="views" label="浏览量" width="100" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'">
                  {{ row.status === 1 ? '发布' : '下线' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="publishTime" label="发布时间" min-width="180" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button link type="primary" @click="openDynamicEdit(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="公告信息" name="notice">
          <div class="tab-head">
            <span>公告列表</span>
            <el-button type="primary" @click="openNoticeCreate">新增公告</el-button>
          </div>
          <el-table :data="notices" border>
            <el-table-column prop="title" label="标题" min-width="220" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'">
                  {{ row.status === 1 ? '发布' : '下线' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="publishTime" label="发布时间" min-width="180" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button link type="primary" @click="openNoticeEdit(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="论坛帖子审核" name="post">
          <el-table :data="posts" border>
            <el-table-column prop="title" label="标题" min-width="200" />
            <el-table-column prop="status" label="状态" width="120" />
            <el-table-column prop="auditReason" label="审核说明" min-width="220" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button
                  link
                  type="success"
                  :disabled="row.status !== 'PENDING'"
                  @click="audit(row.id, 'APPROVED')"
                >
                  通过
                </el-button>
                <el-button link type="danger" :disabled="row.status !== 'PENDING'" @click="audit(row.id, 'REJECTED')">
                  拒绝
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="dynamicVisible" :title="dynamicForm.id ? '编辑动态' : '新增动态'" width="620px" append-to-body>
      <el-form :model="dynamicForm" label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="dynamicForm.title" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="dynamicForm.type">
            <el-option label="社区新闻" value="NEWS" />
            <el-option label="动态资讯" value="DYNAMIC" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="dynamicStatusSwitch" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="dynamicForm.content" type="textarea" rows="6" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dynamicVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDynamic">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="noticeVisible" :title="noticeForm.id ? '编辑公告' : '新增公告'" width="620px" append-to-body>
      <el-form :model="noticeForm" label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="noticeForm.title" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="noticeStatusSwitch" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="noticeForm.content" type="textarea" rows="6" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="noticeVisible = false">取消</el-button>
        <el-button type="primary" @click="submitNotice">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  auditPostApi,
  pageDynamicsApi,
  pageForumPostsApi,
  pageNoticesApi,
  saveDynamicApi,
  saveNoticeApi,
  updateDynamicApi,
  updateNoticeApi,
  type DynamicModel,
  type NoticeModel,
  type PostModel
} from '@/api/content'

const tab = ref('dynamic')
const dynamics = ref<DynamicModel[]>([])
const notices = ref<NoticeModel[]>([])
const posts = ref<PostModel[]>([])
const dynamicVisible = ref(false)
const noticeVisible = ref(false)

const dynamicForm = reactive<Partial<DynamicModel>>({
  id: undefined,
  title: '',
  content: '',
  type: 'NEWS',
  status: 1
})

const noticeForm = reactive<Partial<NoticeModel>>({
  id: undefined,
  title: '',
  content: '',
  status: 1
})

const dynamicStatusSwitch = computed({
  get: () => dynamicForm.status === 1,
  set: (value: boolean) => {
    dynamicForm.status = value ? 1 : 0
  }
})

const noticeStatusSwitch = computed({
  get: () => noticeForm.status === 1,
  set: (value: boolean) => {
    noticeForm.status = value ? 1 : 0
  }
})

async function load() {
  const [dynamicRes, noticeRes, postRes] = await Promise.all([
    pageDynamicsApi({ current: 1, size: 20, onlyPublished: false }),
    pageNoticesApi({ current: 1, size: 20, onlyPublished: false }),
    pageForumPostsApi({ current: 1, size: 20, onlyApproved: false })
  ])
  dynamics.value = dynamicRes.records
  notices.value = noticeRes.records
  posts.value = postRes.records
}

async function audit(id: number, status: string) {
  let reason = ''
  if (status === 'REJECTED') {
    const result = await ElMessageBox.prompt('请输入拒绝原因', '帖子审核')
    reason = result.value
  }
  await auditPostApi(id, { status, reason })
  await load()
}

function openDynamicCreate() {
  Object.assign(dynamicForm, {
    id: undefined,
    title: '',
    content: '',
    type: 'NEWS',
    status: 1
  })
  dynamicVisible.value = true
}

function openDynamicEdit(row: DynamicModel) {
  Object.assign(dynamicForm, row)
  dynamicVisible.value = true
}

async function submitDynamic() {
  if (dynamicForm.id) {
    await updateDynamicApi(dynamicForm.id, dynamicForm)
  } else {
    await saveDynamicApi(dynamicForm)
  }
  dynamicVisible.value = false
  ElMessage.success('动态保存成功')
  await load()
}

function openNoticeCreate() {
  Object.assign(noticeForm, {
    id: undefined,
    title: '',
    content: '',
    status: 1
  })
  noticeVisible.value = true
}

function openNoticeEdit(row: NoticeModel) {
  Object.assign(noticeForm, row)
  noticeVisible.value = true
}

async function submitNotice() {
  if (noticeForm.id) {
    await updateNoticeApi(noticeForm.id, noticeForm)
  } else {
    await saveNoticeApi(noticeForm)
  }
  noticeVisible.value = false
  ElMessage.success('公告保存成功')
  await load()
}

onMounted(load)
</script>

<style scoped>
.module {
  border: 1px solid var(--cvs-border);
  border-radius: 16px;
}

.tab-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
</style>
