<template>
  <div class="fade-up">
    <el-card class="module" shadow="never">
      <el-tabs v-model="tab">
        <el-tab-pane label="信息动态" name="dynamic">
          <div class="tab-head">
            <div class="left-actions">
              <el-button type="danger" plain :disabled="!selectedDynamics.length" @click="batchDeleteDynamics">
                批量删除
              </el-button>
            </div>
            <el-button type="primary" @click="openDynamicCreate">新增动态</el-button>
          </div>
          <el-table :data="dynamics" border @selection-change="onDynamicSelection">
            <el-table-column type="selection" width="48" />
            <el-table-column label="图片" width="90">
              <template #default="{ row }">
                <img class="thumb" :src="row.imageUrl || dynamicFallback" alt="动态配图" />
              </template>
            </el-table-column>
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
            <el-table-column label="操作" width="170">
              <template #default="{ row }">
                <el-button link type="primary" @click="openDynamicEdit(row)">编辑</el-button>
                <el-button link type="danger" @click="deleteDynamic(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="系统公告" name="notice">
          <div class="tab-head">
            <el-button type="danger" plain :disabled="!selectedNotices.length" @click="batchDeleteNotices">批量删除</el-button>
            <el-button type="primary" @click="openNoticeCreate">新增公告</el-button>
          </div>
          <el-table :data="notices" border @selection-change="onNoticeSelection">
            <el-table-column type="selection" width="48" />
            <el-table-column prop="title" label="标题" min-width="220" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'">
                  {{ row.status === 1 ? '发布' : '下线' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="publishTime" label="发布时间" min-width="180" />
            <el-table-column label="操作" width="170">
              <template #default="{ row }">
                <el-button link type="primary" @click="openNoticeEdit(row)">编辑</el-button>
                <el-button link type="danger" @click="deleteNotice(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="论坛帖子审核" name="post">
          <div class="tab-head">
            <el-button type="danger" plain :disabled="!selectedPosts.length" @click="batchDeletePosts">批量删除</el-button>
            <span class="tip">支持审核与批量清理历史帖子</span>
          </div>
          <el-table :data="posts" border @selection-change="onPostSelection">
            <el-table-column type="selection" width="48" />
            <el-table-column prop="title" label="标题" min-width="200" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="postStatusTag(row.status)">{{ postStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="auditReason" label="审核说明" min-width="220" />
            <el-table-column label="操作" width="220">
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
                <el-button link type="danger" @click="deletePost(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="评论管理" name="comment">
          <div class="tab-head">
            <el-button type="danger" plain :disabled="!selectedComments.length" @click="batchDeleteComments">
              批量删除
            </el-button>
            <span class="tip">自动过滤“自动化冒烟评论”展示内容</span>
          </div>
          <el-table :data="comments" border @selection-change="onCommentSelection">
            <el-table-column type="selection" width="48" />
            <el-table-column prop="targetType" label="目标类型" width="120" />
            <el-table-column prop="targetId" label="目标ID" width="120" />
            <el-table-column prop="userId" label="用户ID" width="120" />
            <el-table-column prop="content" label="评论内容" min-width="260" />
            <el-table-column label="操作" width="110">
              <template #default="{ row }">
                <el-button link type="danger" @click="deleteComment(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog
      v-model="dynamicVisible"
      :title="dynamicForm.id ? '编辑动态' : '新增动态'"
      width="min(92vw, 760px)"
      top="5vh"
      append-to-body
      class="manage-dialog"
    >
      <el-form :model="dynamicForm" label-position="top">
        <el-form-item label="标题">
          <el-input v-model="dynamicForm.title" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="dynamicForm.type">
            <el-option label="社区新闻" value="NEWS" />
            <el-option label="活动动态" value="DYNAMIC" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="dynamicStatusSwitch" />
        </el-form-item>
        <el-form-item label="图片">
          <div class="uploader">
            <img class="thumb-lg" :src="dynamicForm.imageUrl || dynamicFallback" alt="动态配图" />
            <el-upload
              :show-file-list="false"
              :http-request="handleDynamicImageUpload"
              :before-upload="beforeImageUpload"
              accept="image/png,image/jpeg,image/jpg,image/webp,image/gif"
            >
              <el-button :loading="dynamicImageUploading">上传图片</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="dynamicForm.content" type="textarea" rows="8" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dynamicVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDynamic">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="noticeVisible"
      :title="noticeForm.id ? '编辑公告' : '新增公告'"
      width="min(92vw, 720px)"
      top="5vh"
      append-to-body
      class="manage-dialog"
    >
      <el-form :model="noticeForm" label-position="top">
        <el-form-item label="标题">
          <el-input v-model="noticeForm.title" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="noticeStatusSwitch" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="noticeForm.content" type="textarea" rows="8" />
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
import type { UploadProps, UploadRequestOptions } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { uploadImageApi } from '@/api/common'
import { validateImageFile } from '@/utils/upload'
import {
  auditPostApi,
  batchDeleteCommentsApi,
  batchDeleteDynamicsApi,
  batchDeleteForumPostsApi,
  batchDeleteNoticesApi,
  deleteCommentApi,
  deleteDynamicApi,
  deleteForumPostApi,
  deleteNoticeApi,
  pageCommentsApi,
  pageDynamicsApi,
  pageForumPostsApi,
  pageNoticesApi,
  saveDynamicApi,
  saveNoticeApi,
  updateDynamicApi,
  updateNoticeApi,
  type CommentModel,
  type DynamicModel,
  type NoticeModel,
  type PostModel
} from '@/api/content'

const dynamicFallback =
  'https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?auto=format&fit=crop&w=900&q=80'

const tab = ref('dynamic')
const dynamics = ref<DynamicModel[]>([])
const notices = ref<NoticeModel[]>([])
const posts = ref<PostModel[]>([])
const comments = ref<CommentModel[]>([])
const selectedDynamics = ref<number[]>([])
const selectedNotices = ref<number[]>([])
const selectedPosts = ref<number[]>([])
const selectedComments = ref<number[]>([])
const dynamicVisible = ref(false)
const noticeVisible = ref(false)
const dynamicImageUploading = ref(false)

const dynamicForm = reactive<Partial<DynamicModel>>({
  id: undefined,
  title: '',
  content: '',
  imageUrl: '',
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

const beforeImageUpload: UploadProps['beforeUpload'] = (rawFile) => {
  return validateImageFile(rawFile)
}

async function handleDynamicImageUpload(option: UploadRequestOptions) {
  dynamicImageUploading.value = true
  try {
    const file = option.file as File
    const res = await uploadImageApi(file)
    dynamicForm.imageUrl = res.url
    ElMessage.success('图片上传成功')
    option.onSuccess?.(res)
  } catch (error) {
    option.onError?.(error as any)
  } finally {
    dynamicImageUploading.value = false
  }
}

function onDynamicSelection(rows: DynamicModel[]) {
  selectedDynamics.value = rows.map((item) => item.id)
}

function onNoticeSelection(rows: NoticeModel[]) {
  selectedNotices.value = rows.map((item) => item.id)
}

function onPostSelection(rows: PostModel[]) {
  selectedPosts.value = rows.map((item) => item.id)
}

function onCommentSelection(rows: CommentModel[]) {
  selectedComments.value = rows.map((item) => item.id)
}

async function load() {
  const [dynamicRes, noticeRes, postRes, commentRes] = await Promise.all([
    pageDynamicsApi({ current: 1, size: 50, onlyPublished: false }),
    pageNoticesApi({ current: 1, size: 50, onlyPublished: false }),
    pageForumPostsApi({ current: 1, size: 50, onlyApproved: false }),
    pageCommentsApi({ current: 1, size: 200 })
  ])
  dynamics.value = dynamicRes.records
  notices.value = noticeRes.records
  posts.value = postRes.records
  comments.value = commentRes.records
}

async function audit(id: number, status: string) {
  let reason = ''
  if (status === 'REJECTED') {
    const result = await ElMessageBox.prompt('请输入拒绝原因', '帖子审核')
    reason = result.value
  }
  await auditPostApi(id, { status, reason })
  ElMessage.success(status === 'APPROVED' ? '帖子已通过审核' : '帖子已拒绝')
  await load()
}

function postStatusLabel(status: string) {
  if (status === 'PENDING') return '待审核'
  if (status === 'APPROVED') return '已通过'
  if (status === 'REJECTED') return '已拒绝'
  return status
}

function postStatusTag(status: string) {
  if (status === 'PENDING') return 'warning'
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'info'
}

function openDynamicCreate() {
  Object.assign(dynamicForm, {
    id: undefined,
    title: '',
    content: '',
    imageUrl: '',
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

async function deleteDynamic(id: number) {
  await ElMessageBox.confirm('确认删除该动态？', '删除动态', { type: 'warning' })
  await deleteDynamicApi(id)
  ElMessage.success('删除成功')
  await load()
}

async function batchDeleteDynamics() {
  await ElMessageBox.confirm(`确认批量删除 ${selectedDynamics.value.length} 条动态？`, '批量删除动态', {
    type: 'warning'
  })
  await batchDeleteDynamicsApi(selectedDynamics.value)
  selectedDynamics.value = []
  ElMessage.success('批量删除成功')
  await load()
}

async function deleteNotice(id: number) {
  await ElMessageBox.confirm('确认删除该公告？', '删除公告', { type: 'warning' })
  await deleteNoticeApi(id)
  ElMessage.success('删除成功')
  await load()
}

async function batchDeleteNotices() {
  await ElMessageBox.confirm(`确认批量删除 ${selectedNotices.value.length} 条公告？`, '批量删除公告', {
    type: 'warning'
  })
  await batchDeleteNoticesApi(selectedNotices.value)
  selectedNotices.value = []
  ElMessage.success('批量删除成功')
  await load()
}

async function deletePost(id: number) {
  await ElMessageBox.confirm('确认删除该帖子？', '删除帖子', { type: 'warning' })
  await deleteForumPostApi(id)
  ElMessage.success('删除成功')
  await load()
}

async function batchDeletePosts() {
  await ElMessageBox.confirm(`确认批量删除 ${selectedPosts.value.length} 条帖子？`, '批量删除帖子', {
    type: 'warning'
  })
  await batchDeleteForumPostsApi(selectedPosts.value)
  selectedPosts.value = []
  ElMessage.success('批量删除成功')
  await load()
}

async function deleteComment(id: number) {
  await ElMessageBox.confirm('确认删除该评论？', '删除评论', { type: 'warning' })
  await deleteCommentApi(id)
  ElMessage.success('删除成功')
  await load()
}

async function batchDeleteComments() {
  await ElMessageBox.confirm(`确认批量删除 ${selectedComments.value.length} 条评论？`, '批量删除评论', {
    type: 'warning'
  })
  await batchDeleteCommentsApi(selectedComments.value)
  selectedComments.value = []
  ElMessage.success('批量删除成功')
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

.left-actions {
  display: flex;
  gap: 8px;
}

.tip {
  color: #6f7d78;
  font-size: 13px;
}

.thumb {
  width: 62px;
  height: 42px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid var(--cvs-border);
}

.uploader {
  display: flex;
  gap: 12px;
  align-items: center;
}

.thumb-lg {
  width: 160px;
  height: 94px;
  border-radius: 10px;
  object-fit: cover;
  border: 1px solid var(--cvs-border);
}

:deep(.manage-dialog .el-dialog__body) {
  max-height: calc(100vh - 260px);
  overflow: auto;
}
</style>
