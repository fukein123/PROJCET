<template>
  <AdminListScaffold
    title="内容运营管理"
    description="按模块管理门户动态、系统公告、论坛审核与评论清理，各标签页独立分页、独立刷新，避免重载整页。"
  >
    <el-tabs v-model="tab" class="content-tabs">
      <el-tab-pane label="信息动态" name="dynamic">
        <AdminContentSection
          title="信息动态"
          description="统一管理社区新闻与活动动态，支持封面上传、状态控制、批量归档和批量清理。"
          :loading="dynamicLoading && !dynamics.length"
          :empty="!dynamicLoading && !dynamics.length"
          loading-title="正在加载动态列表"
          loading-description="请稍候，系统正在拉取最新动态内容。"
          empty-title="当前暂无动态"
          empty-description="可以先发布一条社区新闻或活动动态，后续会按分页展示。"
        >
          <template #actions>
            <el-button type="warning" plain :disabled="!selectedDynamics.length" @click="batchArchiveDynamics">
              批量归档
            </el-button>
            <el-button plain :disabled="!selectedDynamics.length" @click="batchRestoreDynamics">恢复发布</el-button>
            <el-button type="danger" plain :disabled="!selectedDynamics.length" @click="batchDeleteDynamics">
              批量删除
            </el-button>
            <el-button type="primary" @click="openDynamicCreate">新增动态</el-button>
          </template>

          <template #emptyActions>
            <el-button type="primary" @click="openDynamicCreate">立即新增动态</el-button>
          </template>

          <el-table :data="dynamics" border v-loading="dynamicLoading" @selection-change="onDynamicSelection">
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

          <template #pagination>
            <el-pagination
              layout="total, sizes, prev, pager, next"
              :current-page="dynamicQuery.current"
              :page-size="dynamicQuery.size"
              :page-sizes="pageSizes"
              :total="dynamicTotal"
              @current-change="handleDynamicPageChange"
              @size-change="handleDynamicSizeChange"
            />
          </template>
        </AdminContentSection>
      </el-tab-pane>

      <el-tab-pane label="系统公告" name="notice">
        <AdminContentSection
          title="系统公告"
          description="用于维护门户公告栏信息，支持状态切换、编辑、批量归档与批量清理。"
          :loading="noticeLoading && !notices.length"
          :empty="!noticeLoading && !notices.length"
          loading-title="正在加载公告列表"
          loading-description="请稍候，系统正在同步最新公告。"
          empty-title="当前暂无公告"
          empty-description="可以先创建一条新的系统公告，便于门户端统一展示。"
        >
          <template #actions>
            <el-button type="warning" plain :disabled="!selectedNotices.length" @click="batchArchiveNotices">
              批量归档
            </el-button>
            <el-button plain :disabled="!selectedNotices.length" @click="batchRestoreNotices">恢复发布</el-button>
            <el-button type="danger" plain :disabled="!selectedNotices.length" @click="batchDeleteNotices">
              批量删除
            </el-button>
            <el-button type="primary" @click="openNoticeCreate">新增公告</el-button>
          </template>

          <template #emptyActions>
            <el-button type="primary" @click="openNoticeCreate">立即新增公告</el-button>
          </template>

          <el-table :data="notices" border v-loading="noticeLoading" @selection-change="onNoticeSelection">
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

          <template #pagination>
            <el-pagination
              layout="total, sizes, prev, pager, next"
              :current-page="noticeQuery.current"
              :page-size="noticeQuery.size"
              :page-sizes="pageSizes"
              :total="noticeTotal"
              @current-change="handleNoticePageChange"
              @size-change="handleNoticeSizeChange"
            />
          </template>
        </AdminContentSection>
      </el-tab-pane>

      <el-tab-pane label="论坛帖子审核" name="post">
        <AdminContentSection
          title="论坛帖子审核"
          description="仅刷新当前审核标签页，支持通过、拒绝与批量删除历史帖子。"
          :loading="postLoading && !posts.length"
          :empty="!postLoading && !posts.length"
          loading-title="正在加载帖子审核列表"
          loading-description="请稍候，系统正在同步论坛审核数据。"
          empty-title="当前暂无待管理帖子"
          empty-description="帖子审核和历史帖子清理结果会在这里按分页显示。"
        >
          <template #actions>
            <el-button type="danger" plain :disabled="!selectedPosts.length" @click="batchDeletePosts">批量删除</el-button>
          </template>

          <el-table :data="posts" border v-loading="postLoading" @selection-change="onPostSelection">
            <el-table-column type="selection" width="48" />
            <el-table-column prop="title" label="标题" min-width="200" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getForumPostStatusTag(row.status)">{{ getForumPostStatusLabel(row.status) }}</el-tag>
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

          <template #pagination>
            <el-pagination
              layout="total, sizes, prev, pager, next"
              :current-page="postQuery.current"
              :page-size="postQuery.size"
              :page-sizes="pageSizes"
              :total="postTotal"
              @current-change="handlePostPageChange"
              @size-change="handlePostSizeChange"
            />
          </template>
        </AdminContentSection>
      </el-tab-pane>

      <el-tab-pane label="评论管理" name="comment">
        <AdminContentSection
          title="评论管理"
          description="默认隔离测试标记评论，管理端可显式查看并清理自动化残留，避免业务评论视图被测试数据污染。"
          :loading="commentLoading && !comments.length"
          :empty="!commentLoading && !comments.length"
          loading-title="正在加载评论列表"
          loading-description="请稍候，系统正在拉取最新评论数据。"
          empty-title="当前暂无评论数据"
          empty-description="评论清理结果会在这里按分页展示，便于逐页处理。"
        >
          <template #actions>
            <el-button type="danger" plain :disabled="!selectedComments.length" @click="batchDeleteComments">
              批量删除
            </el-button>
          </template>

          <el-table :data="comments" border v-loading="commentLoading" @selection-change="onCommentSelection">
            <el-table-column type="selection" width="48" />
            <el-table-column label="目标类型" width="120">
              <template #default="{ row }">
                {{ getCommentTargetLabel(row.targetType) }}
              </template>
            </el-table-column>
            <el-table-column prop="targetId" label="目标ID" width="120" />
            <el-table-column prop="userId" label="用户ID" width="120" />
            <el-table-column label="数据标记" width="140">
              <template #default="{ row }">
                <el-tag v-if="row.testDataTag" type="warning">测试数据</el-tag>
                <span v-else>业务数据</span>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="评论内容" min-width="260" />
            <el-table-column label="操作" width="110">
              <template #default="{ row }">
                <el-button link type="danger" @click="deleteComment(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <template #pagination>
            <el-pagination
              layout="total, sizes, prev, pager, next"
              :current-page="commentQuery.current"
              :page-size="commentQuery.size"
              :page-sizes="pageSizes"
              :total="commentTotal"
              @current-change="handleCommentPageChange"
              @size-change="handleCommentSizeChange"
            />
          </template>
        </AdminContentSection>
      </el-tab-pane>
    </el-tabs>

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
  </AdminListScaffold>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import type { UploadProps, UploadRequestOptions } from 'element-plus'
import { ElMessage } from 'element-plus'
import AdminContentSection from '@/components/admin/AdminContentSection.vue'
import AdminListScaffold from '@/components/admin/AdminListScaffold.vue'
import { uploadImageApi } from '@/api/common'
import { useSelectionIds } from '@/composables/useSelectionIds'
import { useTable, type TableQuery } from '@/composables/useTable'
import { runConfirmedAction } from '@/utils/confirmed-action'
import { getCommentTargetLabel, getForumPostStatusLabel, getForumPostStatusTag } from '@/utils/display'
import { validateImageFile } from '@/utils/upload'
import {
  auditPostApi,
  batchArchiveDynamicsApi,
  batchArchiveNoticesApi,
  batchDeleteCommentsApi,
  batchDeleteDynamicsApi,
  batchDeleteForumPostsApi,
  batchDeleteNoticesApi,
  batchRestoreDynamicsApi,
  batchRestoreNoticesApi,
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
const pageSizes = [10, 20, 30, 50]

type ContentTabKey = 'dynamic' | 'notice' | 'post' | 'comment'

function createPagedTable<TRecord>(fetcher: (params: TableQuery) => Promise<{ total: number; records: TRecord[] }>) {
  return useTable<TRecord, TableQuery>({
    initialQuery: {
      current: 1,
      size: 10
    },
    fetcher
  })
}

const tab = ref<ContentTabKey>('dynamic')
const {
  loading: dynamicLoading,
  records: dynamics,
  total: dynamicTotal,
  query: dynamicQuery,
  load: loadDynamics,
  handlePage: handleDynamicPage,
  handleSizeChange: handleDynamicPageSize
} = createPagedTable<DynamicModel>((params) =>
  pageDynamicsApi({
    current: params.current,
    size: params.size,
    onlyPublished: false
  })
)
const {
  loading: noticeLoading,
  records: notices,
  total: noticeTotal,
  query: noticeQuery,
  load: loadNotices,
  handlePage: handleNoticePage,
  handleSizeChange: handleNoticePageSize
} = createPagedTable<NoticeModel>((params) =>
  pageNoticesApi({
    current: params.current,
    size: params.size,
    onlyPublished: false
  })
)
const {
  loading: postLoading,
  records: posts,
  total: postTotal,
  query: postQuery,
  load: loadPosts,
  handlePage: handlePostPage,
  handleSizeChange: handlePostPageSize
} = createPagedTable<PostModel>((params) =>
  pageForumPostsApi({
    current: params.current,
    size: params.size,
    onlyApproved: false
  })
)
const {
  loading: commentLoading,
  records: comments,
  total: commentTotal,
  query: commentQuery,
  load: loadComments,
  handlePage: handleCommentPage,
  handleSizeChange: handleCommentPageSize
} = createPagedTable<CommentModel>((params) =>
  pageCommentsApi({
    current: params.current,
    size: params.size,
    includeTestData: true
  })
)

const { selectedIds: selectedDynamics, handleSelectionChange: onDynamicSelection, clearSelection: clearDynamicSelection } =
  useSelectionIds<DynamicModel>()
const { selectedIds: selectedNotices, handleSelectionChange: onNoticeSelection, clearSelection: clearNoticeSelection } =
  useSelectionIds<NoticeModel>()
const { selectedIds: selectedPosts, handleSelectionChange: onPostSelection, clearSelection: clearPostSelection } =
  useSelectionIds<PostModel>()
const {
  selectedIds: selectedComments,
  handleSelectionChange: onCommentSelection,
  clearSelection: clearCommentSelection
} = useSelectionIds<CommentModel>()

const dynamicVisible = ref(false)
const noticeVisible = ref(false)
const dynamicImageUploading = ref(false)
const loadedTabs = reactive<Record<ContentTabKey, boolean>>({
  dynamic: false,
  notice: false,
  post: false,
  comment: false
})

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

const beforeImageUpload: UploadProps['beforeUpload'] = (rawFile) => validateImageFile(rawFile)

async function handleDynamicImageUpload(option: UploadRequestOptions) {
  dynamicImageUploading.value = true
  try {
    const file = option.file as File
    const res = await uploadImageApi(file)
    dynamicForm.imageUrl = res.url
    ElMessage.success('图片上传成功')
    option.onSuccess?.(res)
  } catch (error) {
    option.onError?.(error as never)
  } finally {
    dynamicImageUploading.value = false
  }
}

const tabLoaders: Record<ContentTabKey, () => Promise<void>> = {
  dynamic: () => loadDynamics(),
  notice: () => loadNotices(),
  post: () => loadPosts(),
  comment: () => loadComments()
}

async function loadTab(name: ContentTabKey = tab.value) {
  await tabLoaders[name]()
  loadedTabs[name] = true
}

async function refreshTab(name: ContentTabKey) {
  await loadTab(name)
}

async function handleDynamicPageChange(page: number) {
  clearDynamicSelection()
  await handleDynamicPage(page)
}

async function handleDynamicSizeChange(size: number) {
  clearDynamicSelection()
  await handleDynamicPageSize(size)
}

async function handleNoticePageChange(page: number) {
  clearNoticeSelection()
  await handleNoticePage(page)
}

async function handleNoticeSizeChange(size: number) {
  clearNoticeSelection()
  await handleNoticePageSize(size)
}

async function handlePostPageChange(page: number) {
  clearPostSelection()
  await handlePostPage(page)
}

async function handlePostSizeChange(size: number) {
  clearPostSelection()
  await handlePostPageSize(size)
}

async function handleCommentPageChange(page: number) {
  clearCommentSelection()
  await handleCommentPage(page)
}

async function handleCommentSizeChange(size: number) {
  clearCommentSelection()
  await handleCommentPageSize(size)
}

async function audit(id: number, status: string) {
  await runConfirmedAction({
    message: status === 'APPROVED' ? '确认通过该帖子审核吗？' : '请输入拒绝原因',
    title: '帖子审核',
    type: status === 'APPROVED' ? 'success' : 'warning',
    confirmButtonText: status === 'APPROVED' ? '确认通过' : '确认拒绝',
    prompt:
      status === 'REJECTED'
        ? {
            message: '请输入拒绝原因',
            inputPlaceholder: '拒绝原因必填',
            inputType: 'textarea',
            inputValidator: (value) => (value.trim() ? true : '请输入拒绝原因')
          }
        : undefined,
    action: (reason) =>
      auditPostApi(id, {
        status,
        reason: status === 'REJECTED' ? (reason || '').trim() : ''
      }),
    successMessage: status === 'APPROVED' ? '帖子已通过审核' : '帖子已拒绝',
    afterSuccess: () => refreshTab('post')
  })
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
  dynamicForm.title = (dynamicForm.title || '').trim()
  dynamicForm.content = (dynamicForm.content || '').trim()
  if (!dynamicForm.title) {
    ElMessage.warning('请输入动态标题')
    return
  }
  if (!dynamicForm.content) {
    ElMessage.warning('请输入动态内容')
    return
  }
  if (dynamicForm.id) {
    await updateDynamicApi(dynamicForm.id, dynamicForm)
  } else {
    await saveDynamicApi(dynamicForm)
  }
  dynamicVisible.value = false
  ElMessage.success('动态保存成功')
  await refreshTab('dynamic')
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
  noticeForm.title = (noticeForm.title || '').trim()
  noticeForm.content = (noticeForm.content || '').trim()
  if (!noticeForm.title) {
    ElMessage.warning('请输入公告标题')
    return
  }
  if (!noticeForm.content) {
    ElMessage.warning('请输入公告内容')
    return
  }
  if (noticeForm.id) {
    await updateNoticeApi(noticeForm.id, noticeForm)
  } else {
    await saveNoticeApi(noticeForm)
  }
  noticeVisible.value = false
  ElMessage.success('公告保存成功')
  await refreshTab('notice')
}

async function deleteDynamic(id: number) {
  await runConfirmedAction({
    message: '确认删除该动态？',
    title: '删除动态',
    action: () => deleteDynamicApi(id),
    successMessage: '删除成功',
    afterSuccess: () => refreshTab('dynamic')
  })
}

async function batchDeleteDynamics() {
  if (!selectedDynamics.value.length) {
    return
  }

  await runConfirmedAction({
    message: `确认批量删除 ${selectedDynamics.value.length} 条动态？`,
    title: '批量删除动态',
    action: () => batchDeleteDynamicsApi(selectedDynamics.value),
    successMessage: '批量删除成功',
    afterSuccess: async () => {
      clearDynamicSelection()
      await refreshTab('dynamic')
    }
  })
}

async function batchArchiveDynamics() {
  if (!selectedDynamics.value.length) {
    return
  }

  await runConfirmedAction({
    message: `确认批量归档 ${selectedDynamics.value.length} 条动态？`,
    title: '批量归档动态',
    type: 'warning',
    action: () => batchArchiveDynamicsApi(selectedDynamics.value),
    successMessage: '动态已批量归档',
    afterSuccess: async () => {
      clearDynamicSelection()
      await refreshTab('dynamic')
    }
  })
}

async function batchRestoreDynamics() {
  if (!selectedDynamics.value.length) {
    return
  }

  await runConfirmedAction({
    message: `确认恢复发布 ${selectedDynamics.value.length} 条动态？`,
    title: '恢复发布动态',
    action: () => batchRestoreDynamicsApi(selectedDynamics.value),
    successMessage: '动态已恢复发布',
    afterSuccess: async () => {
      clearDynamicSelection()
      await refreshTab('dynamic')
    }
  })
}

async function deleteNotice(id: number) {
  await runConfirmedAction({
    message: '确认删除该公告？',
    title: '删除公告',
    action: () => deleteNoticeApi(id),
    successMessage: '删除成功',
    afterSuccess: () => refreshTab('notice')
  })
}

async function batchDeleteNotices() {
  if (!selectedNotices.value.length) {
    return
  }

  await runConfirmedAction({
    message: `确认批量删除 ${selectedNotices.value.length} 条公告？`,
    title: '批量删除公告',
    action: () => batchDeleteNoticesApi(selectedNotices.value),
    successMessage: '批量删除成功',
    afterSuccess: async () => {
      clearNoticeSelection()
      await refreshTab('notice')
    }
  })
}

async function batchArchiveNotices() {
  if (!selectedNotices.value.length) {
    return
  }

  await runConfirmedAction({
    message: `确认批量归档 ${selectedNotices.value.length} 条公告？`,
    title: '批量归档公告',
    type: 'warning',
    action: () => batchArchiveNoticesApi(selectedNotices.value),
    successMessage: '公告已批量归档',
    afterSuccess: async () => {
      clearNoticeSelection()
      await refreshTab('notice')
    }
  })
}

async function batchRestoreNotices() {
  if (!selectedNotices.value.length) {
    return
  }

  await runConfirmedAction({
    message: `确认恢复发布 ${selectedNotices.value.length} 条公告？`,
    title: '恢复发布公告',
    action: () => batchRestoreNoticesApi(selectedNotices.value),
    successMessage: '公告已恢复发布',
    afterSuccess: async () => {
      clearNoticeSelection()
      await refreshTab('notice')
    }
  })
}

async function deletePost(id: number) {
  await runConfirmedAction({
    message: '确认删除该帖子？',
    title: '删除帖子',
    action: () => deleteForumPostApi(id),
    successMessage: '删除成功',
    afterSuccess: () => refreshTab('post')
  })
}

async function batchDeletePosts() {
  if (!selectedPosts.value.length) {
    return
  }

  await runConfirmedAction({
    message: `确认批量删除 ${selectedPosts.value.length} 条帖子？`,
    title: '批量删除帖子',
    action: () => batchDeleteForumPostsApi(selectedPosts.value),
    successMessage: '批量删除成功',
    afterSuccess: async () => {
      clearPostSelection()
      await refreshTab('post')
    }
  })
}

async function deleteComment(id: number) {
  await runConfirmedAction({
    message: '确认删除该评论？',
    title: '删除评论',
    action: () => deleteCommentApi(id),
    successMessage: '删除成功',
    afterSuccess: () => refreshTab('comment')
  })
}

async function batchDeleteComments() {
  if (!selectedComments.value.length) {
    return
  }

  await runConfirmedAction({
    message: `确认批量删除 ${selectedComments.value.length} 条评论？`,
    title: '批量删除评论',
    action: () => batchDeleteCommentsApi(selectedComments.value),
    successMessage: '批量删除成功',
    afterSuccess: async () => {
      clearCommentSelection()
      await refreshTab('comment')
    }
  })
}

watch(tab, async (name) => {
  if (!loadedTabs[name]) {
    await loadTab(name)
  }
})

onMounted(async () => {
  await loadTab()
})
</script>

<style scoped>
.content-tabs :deep(.el-tabs__header) {
  margin-bottom: var(--cvs-space-3);
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
