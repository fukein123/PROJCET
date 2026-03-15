<template>
  <AdminContentSection
    title="积分商城"
    description="管理商品名称、图片、简介、积分和库存。删除语义冻结为停用 / 恢复，不做物理删除。"
    :loading="loading && !records.length"
    :empty="!loading && !records.length"
    loading-title="正在加载商城商品"
    loading-description="请稍候，系统正在同步积分商城商品与库存数据。"
    empty-title="当前暂无商城商品"
    empty-description="新建商品后，这里会显示上架状态、库存和积分消耗。"
  >
    <template #actions>
      <el-button type="warning" plain :disabled="!selectedIds.length" @click="batchDisable">批量停用</el-button>
      <el-button plain :disabled="!selectedIds.length" @click="batchEnable">批量恢复</el-button>
      <el-button type="primary" @click="openCreate">新增商品</el-button>
    </template>

    <SearchForm @search="load" @reset="resetQuery">
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable style="width: 160px">
          <el-option label="上架中" :value="1" />
          <el-option label="已停用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model.trim="query.keyword" placeholder="商品名称" />
      </el-form-item>
    </SearchForm>

    <el-table :data="records" border v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" />
      <el-table-column label="商品图片" width="100">
        <template #default="{ row }">
          <img class="thumb" :src="row.imageUrl" :alt="row.name" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名称" min-width="180" />
      <el-table-column prop="summary" label="商品简介" min-width="220" show-overflow-tooltip />
      <el-table-column prop="pointsCost" label="消耗积分" width="110" />
      <el-table-column prop="stock" label="库存" width="90" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '上架中' : '已停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="180" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <div class="action-cell">
            <el-tooltip content="编辑商品" placement="top">
              <el-button link type="primary" :icon="EditPen" @click="openEdit(row)" />
            </el-tooltip>
            <el-tooltip v-if="row.status === 1" content="停用商品" placement="top">
              <el-button link type="danger" :icon="Delete" @click="disableOne(row.id)" />
            </el-tooltip>
            <el-tooltip v-else content="恢复上架" placement="top">
              <el-button link type="success" :icon="RefreshRight" @click="enableOne(row.id)" />
            </el-tooltip>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <template #pagination>
      <el-pagination
        layout="total, sizes, prev, pager, next"
        :current-page="query.current"
        :page-size="query.size"
        :page-sizes="pageSizes"
        :total="total"
        @current-change="onPageChange"
        @size-change="onSizeChange"
      />
    </template>
  </AdminContentSection>

  <el-dialog
    v-model="visible"
    :title="form.id ? '编辑商品' : '新增商品'"
    width="min(92vw, 760px)"
    append-to-body
    top="6vh"
    class="manage-dialog"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="商品名称" prop="name">
        <el-input v-model.trim="form.name" maxlength="128" show-word-limit />
      </el-form-item>
      <el-form-item label="商品图片" prop="imageUrl">
        <div class="uploader">
          <img class="thumb-lg" :src="form.imageUrl || productFallback" alt="商品预览" />
          <el-upload
            :show-file-list="false"
            :http-request="handleImageUpload"
            :before-upload="beforeImageUpload"
            accept="image/png,image/jpeg,image/jpg,image/webp"
          >
            <el-button :loading="imageUploading">上传图片</el-button>
          </el-upload>
        </div>
      </el-form-item>
      <div class="dialog-grid">
        <el-form-item label="消耗积分" prop="pointsCost">
          <el-input-number v-model="form.pointsCost" :min="0" />
        </el-form-item>
        <el-form-item label="商品库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" />
        </el-form-item>
      </div>
      <el-form-item label="商品状态">
        <el-switch v-model="statusSwitch" />
      </el-form-item>
      <el-form-item label="商品简介" prop="summary">
        <el-input
          v-model.trim="form.summary"
          type="textarea"
          :rows="4"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import type { FormInstance, FormRules, UploadProps, UploadRequestOptions } from 'element-plus'
import { ElMessage } from 'element-plus'
import { Delete, EditPen, RefreshRight } from '@element-plus/icons-vue'
import { uploadImageApi } from '@/api/common'
import {
  adminPageMallProductsApi,
  batchDisableMallProductsApi,
  batchEnableMallProductsApi,
  disableMallProductApi,
  enableMallProductApi,
  saveMallProductApi,
  updateMallProductApi,
  type MallProductModel
} from '@/api/content'
import SearchForm from '@/components/SearchForm.vue'
import AdminContentSection from '@/components/admin/AdminContentSection.vue'
import { useSelectionIds } from '@/composables/useSelectionIds'
import { useTable } from '@/composables/useTable'
import { runConfirmedAction } from '@/utils/confirmed-action'
import { validateElementForm } from '@/utils/form'
import { validateImageFile } from '@/utils/upload'

interface ProductQuery {
  current: number
  size: number
  keyword: string
  status?: number
}

const productFallback =
  'https://images.unsplash.com/photo-1488459716781-31db52582fe9?auto=format&fit=crop&w=900&q=80'
const pageSizes = [10, 20, 30, 50]

const { loading, records, total, query, load, handlePage, handleSizeChange, reset } = useTable<
  MallProductModel,
  ProductQuery
>({
  initialQuery: {
    current: 1,
    size: 10,
    keyword: '',
    status: undefined
  },
  fetcher: (params) =>
    adminPageMallProductsApi({
      current: params.current,
      size: params.size,
      keyword: params.keyword || undefined,
      status: typeof params.status === 'number' ? params.status : undefined
    })
})

const { selectedIds, handleSelectionChange, clearSelection } = useSelectionIds<MallProductModel>()
const visible = ref(false)
const imageUploading = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<Partial<MallProductModel>>({
  id: undefined,
  name: '',
  imageUrl: '',
  summary: '',
  pointsCost: 0,
  stock: 0,
  status: 1
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  imageUrl: [{ required: true, message: '请先上传商品图片', trigger: 'change' }],
  summary: [{ required: true, message: '请输入商品简介', trigger: 'blur' }]
}

const statusSwitch = computed({
  get: () => form.status === 1,
  set: (value: boolean) => {
    form.status = value ? 1 : 0
  }
})

const beforeImageUpload: UploadProps['beforeUpload'] = (rawFile) => validateImageFile(rawFile)

function resetQuery() {
  reset({
    keyword: '',
    status: undefined
  })
}

function resetForm() {
  Object.assign(form, {
    id: undefined,
    name: '',
    imageUrl: '',
    summary: '',
    pointsCost: 0,
    stock: 0,
    status: 1
  })
}

function openCreate() {
  resetForm()
  visible.value = true
}

function openEdit(row: MallProductModel) {
  Object.assign(form, row)
  visible.value = true
}

async function handleImageUpload(option: UploadRequestOptions) {
  imageUploading.value = true
  try {
    const file = option.file as File
    const res = await uploadImageApi(file)
    form.imageUrl = res.url
    ElMessage.success('商品图片上传成功')
    option.onSuccess?.(res)
  } catch (error) {
    option.onError?.(error as never)
  } finally {
    imageUploading.value = false
  }
}

async function submit() {
  if (!(await validateElementForm(formRef.value))) {
    return
  }
  const payload = {
    name: (form.name || '').trim(),
    imageUrl: (form.imageUrl || '').trim(),
    summary: (form.summary || '').trim(),
    pointsCost: Number(form.pointsCost || 0),
    stock: Number(form.stock || 0),
    status: form.status ?? 1
  }
  if (form.id) {
    await updateMallProductApi(form.id, payload)
  } else {
    await saveMallProductApi(payload)
  }
  visible.value = false
  ElMessage.success('商品保存成功')
  await load()
}

async function disableOne(id: number) {
  await runConfirmedAction({
    message: '确认停用该商品吗？停用后不会影响历史订单快照。',
    title: '停用商品',
    type: 'warning',
    action: () => disableMallProductApi(id),
    successMessage: '商品已停用',
    afterSuccess: () => load()
  })
}

async function enableOne(id: number) {
  await runConfirmedAction({
    message: '确认恢复该商品上架吗？',
    title: '恢复商品',
    type: 'success',
    action: () => enableMallProductApi(id),
    successMessage: '商品已恢复上架',
    afterSuccess: () => load()
  })
}

async function batchDisable() {
  if (!selectedIds.value.length) {
    return
  }
  await runConfirmedAction({
    message: `确认批量停用 ${selectedIds.value.length} 件商品吗？`,
    title: '批量停用商品',
    type: 'warning',
    action: () => batchDisableMallProductsApi(selectedIds.value),
    successMessage: '批量停用成功',
    afterSuccess: async () => {
      clearSelection()
      await load()
    }
  })
}

async function batchEnable() {
  if (!selectedIds.value.length) {
    return
  }
  await runConfirmedAction({
    message: `确认批量恢复 ${selectedIds.value.length} 件商品吗？`,
    title: '批量恢复商品',
    type: 'success',
    action: () => batchEnableMallProductsApi(selectedIds.value),
    successMessage: '批量恢复成功',
    afterSuccess: async () => {
      clearSelection()
      await load()
    }
  })
}

async function onPageChange(page: number) {
  clearSelection()
  await handlePage(page)
}

async function onSizeChange(size: number) {
  clearSelection()
  await handleSizeChange(size)
}

void load()
</script>

<style scoped>
.action-cell {
  display: flex;
  align-items: center;
  gap: 4px;
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
  width: 168px;
  height: 112px;
  border-radius: 12px;
  object-fit: cover;
  border: 1px solid var(--cvs-border);
}

.dialog-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 14px;
}

:deep(.manage-dialog .el-dialog__body) {
  max-height: calc(100vh - 240px);
  overflow: auto;
}

@media (max-width: 760px) {
  .dialog-grid {
    grid-template-columns: 1fr;
  }
}
</style>
