<template>
  <AdminContentSection
    title="兑换订单"
    description="统一查看订单快照、履约状态和收货信息。列表默认脱敏展示电话与地址，详情弹窗可查看完整信息。"
    :loading="loading && !records.length"
    :empty="!loading && !records.length"
    loading-title="正在加载兑换订单"
    loading-description="请稍候，系统正在同步订单状态和履约快照。"
    empty-title="当前暂无兑换订单"
    empty-description="志愿者完成积分兑换后，订单会在这里展示。"
  >
    <SearchForm @search="load" @reset="resetQuery">
      <el-form-item label="订单号">
        <el-input v-model.trim="query.orderNo" placeholder="订单号" />
      </el-form-item>
      <el-form-item label="商品名称">
        <el-input v-model.trim="query.productKeyword" placeholder="商品名称" />
      </el-form-item>
      <el-form-item label="志愿者">
        <el-input v-model.trim="query.userKeyword" placeholder="账号 / 姓名" />
      </el-form-item>
      <el-form-item label="订单状态">
        <el-select v-model="query.status" clearable style="width: 160px">
          <el-option label="待处理" value="CREATED" />
          <el-option label="待签收" value="SHIPPED" />
          <el-option label="已签收" value="RECEIVED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
      </el-form-item>
    </SearchForm>

    <el-table :data="records" border v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" min-width="190" />
      <el-table-column prop="productName" label="商品名称" min-width="180" />
      <el-table-column label="志愿者" min-width="150">
        <template #default="{ row }">
          {{ row.realName || row.userName || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" width="80" />
      <el-table-column prop="totalPoints" label="消耗积分" width="110" />
      <el-table-column prop="receiverName" label="收货人" width="110" />
      <el-table-column prop="receiverPhone" label="联系电话" min-width="140" />
      <el-table-column prop="receiverAddress" label="收货地址" min-width="180" show-overflow-tooltip />
      <el-table-column label="订单状态" width="110">
        <template #default="{ row }">
          <el-tag :type="getOrderStatusTag(row.status)">{{ getOrderStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <div class="action-cell">
            <el-tooltip content="查看详情" placement="top">
              <el-button link type="primary" :icon="View" @click="openDetail(row.id)" />
            </el-tooltip>
            <el-tooltip v-if="row.status === 'CREATED'" content="标记发货" placement="top">
              <el-button link type="primary" :icon="Van" @click="updateStatus(row.id, 'SHIPPED')" />
            </el-tooltip>
            <el-tooltip v-if="row.status === 'CREATED'" content="取消订单" placement="top">
              <el-button link type="danger" :icon="CircleClose" @click="updateStatus(row.id, 'CANCELLED')" />
            </el-tooltip>
            <el-tooltip v-if="row.status === 'SHIPPED'" content="标记签收" placement="top">
              <el-button link type="success" :icon="CircleCheck" @click="updateStatus(row.id, 'RECEIVED')" />
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
        @current-change="handlePage"
        @size-change="handleSizeChange"
      />
    </template>
  </AdminContentSection>

  <el-dialog
    v-model="visible"
    title="订单详情"
    width="min(92vw, 760px)"
    append-to-body
    top="6vh"
    class="detail-dialog"
  >
    <div v-if="detail" class="detail-grid">
      <div class="detail-card">
        <img class="detail-image" :src="detail.productImage" :alt="detail.productName" />
        <div>
          <h3>{{ detail.productName }}</h3>
          <p>{{ detail.productSummary || '订单已固化商品摘要快照' }}</p>
        </div>
      </div>

      <dl class="detail-list">
        <div>
          <dt>订单号</dt>
          <dd>{{ detail.orderNo }}</dd>
        </div>
        <div>
          <dt>志愿者</dt>
          <dd>{{ detail.realName || detail.userName || '-' }}</dd>
        </div>
        <div>
          <dt>兑换数量</dt>
          <dd>{{ detail.quantity }}</dd>
        </div>
        <div>
          <dt>消耗积分</dt>
          <dd>{{ detail.totalPoints }}</dd>
        </div>
        <div>
          <dt>收货人</dt>
          <dd>{{ detail.receiverName }}</dd>
        </div>
        <div>
          <dt>联系电话</dt>
          <dd>{{ detail.receiverPhone }}</dd>
        </div>
        <div class="wide">
          <dt>收货地址</dt>
          <dd>{{ detail.receiverAddress }}</dd>
        </div>
        <div>
          <dt>订单状态</dt>
          <dd>
            <el-tag :type="getOrderStatusTag(detail.status)">{{ getOrderStatusLabel(detail.status) }}</el-tag>
          </dd>
        </div>
        <div>
          <dt>创建时间</dt>
          <dd>{{ formatDateTime(detail.createTime) }}</dd>
        </div>
        <div v-if="detail.shippedTime">
          <dt>发货时间</dt>
          <dd>{{ formatDateTime(detail.shippedTime) }}</dd>
        </div>
        <div v-if="detail.receivedTime">
          <dt>签收时间</dt>
          <dd>{{ formatDateTime(detail.receivedTime) }}</dd>
        </div>
        <div v-if="detail.cancelledTime">
          <dt>取消时间</dt>
          <dd>{{ formatDateTime(detail.cancelledTime) }}</dd>
        </div>
        <div v-if="detail.statusReason" class="wide">
          <dt>状态说明</dt>
          <dd>{{ detail.statusReason }}</dd>
        </div>
      </dl>
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { CircleCheck, CircleClose, Van, View } from '@element-plus/icons-vue'
import {
  adminExchangeOrderDetailApi,
  adminPageExchangeOrdersApi,
  updateExchangeOrderStatusApi,
  type ExchangeOrderModel
} from '@/api/content'
import SearchForm from '@/components/SearchForm.vue'
import AdminContentSection from '@/components/admin/AdminContentSection.vue'
import { useTable } from '@/composables/useTable'
import { runConfirmedAction } from '@/utils/confirmed-action'
import { formatDateTime, getOrderStatusLabel, getOrderStatusTag } from '@/utils/display'

interface OrderQuery {
  current: number
  size: number
  orderNo: string
  productKeyword: string
  userKeyword: string
  status?: string
}

const pageSizes = [10, 20, 30, 50]
const visible = ref(false)
const detail = ref<ExchangeOrderModel>()

const { loading, records, total, query, load, handlePage, handleSizeChange, reset } = useTable<
  ExchangeOrderModel,
  OrderQuery
>({
  initialQuery: {
    current: 1,
    size: 10,
    orderNo: '',
    productKeyword: '',
    userKeyword: '',
    status: undefined
  },
  fetcher: (params) =>
    adminPageExchangeOrdersApi({
      current: params.current,
      size: params.size,
      orderNo: params.orderNo || undefined,
      productKeyword: params.productKeyword || undefined,
      userKeyword: params.userKeyword || undefined,
      status: params.status || undefined
    })
})

function resetQuery() {
  reset({
    orderNo: '',
    productKeyword: '',
    userKeyword: '',
    status: undefined
  })
}

async function openDetail(id: number) {
  detail.value = await adminExchangeOrderDetailApi(id)
  visible.value = true
}

async function updateStatus(id: number, status: 'SHIPPED' | 'RECEIVED' | 'CANCELLED') {
  const config = {
    SHIPPED: {
      title: '订单发货',
      message: '确认将该订单标记为已发货吗？',
      success: '订单已标记为待签收',
      type: 'success' as const
    },
    RECEIVED: {
      title: '订单签收',
      message: '确认将该订单标记为已签收吗？',
      success: '订单已标记为已签收',
      type: 'success' as const
    },
    CANCELLED: {
      title: '取消订单',
      message: '确认取消该订单吗？取消后会自动回退积分并恢复库存。',
      success: '订单已取消并完成积分回退',
      type: 'warning' as const
    }
  }[status]

  await runConfirmedAction({
    message: config.message,
    title: config.title,
    type: config.type,
    action: () => updateExchangeOrderStatusApi(id, { status }),
    successMessage: config.success,
    afterSuccess: async () => {
      await load()
      if (visible.value && detail.value?.id === id) {
        detail.value = await adminExchangeOrderDetailApi(id)
      }
    }
  })
}

void load()
</script>

<style scoped>
.action-cell {
  display: flex;
  align-items: center;
  gap: 4px;
}

.detail-grid {
  display: grid;
  gap: 14px;
}

.detail-card {
  display: grid;
  grid-template-columns: 140px 1fr;
  gap: 14px;
  align-items: start;
}

.detail-card h3,
.detail-card p {
  margin: 0;
}

.detail-card p {
  margin-top: 6px;
  color: var(--cvs-text-sub);
  line-height: 1.7;
}

.detail-image {
  width: 140px;
  height: 104px;
  object-fit: cover;
  border-radius: 12px;
  border: 1px solid var(--cvs-border);
}

.detail-list {
  margin: 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.detail-list div {
  padding: 12px 14px;
  border: 1px solid var(--cvs-border);
  border-radius: 14px;
  background: rgba(247, 250, 248, 0.92);
}

.detail-list dt {
  margin-bottom: 6px;
  color: #72837c;
  font-size: 12px;
}

.detail-list dd {
  margin: 0;
  color: #2f403a;
  line-height: 1.7;
}

.wide {
  grid-column: 1 / -1;
}

:deep(.detail-dialog .el-dialog__body) {
  max-height: calc(100vh - 220px);
  overflow: auto;
}

@media (max-width: 760px) {
  .detail-card,
  .detail-list {
    grid-template-columns: 1fr;
  }
}
</style>
