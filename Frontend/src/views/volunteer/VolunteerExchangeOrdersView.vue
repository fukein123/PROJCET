<template>
  <div class="page-shell">
    <WorkspaceHero
      compact
      eyebrow="兑换订单"
      title="统一查看积分兑换后的履约状态"
      description="这里会展示商品快照、消耗积分、收货信息和订单状态，商品后续下架或改名也不会影响历史订单阅读。"
    >
      <template #actions>
        <el-button type="primary" @click="load">刷新订单</el-button>
        <el-button @click="router.push(PORTAL_PATHS.mall)">继续兑换</el-button>
      </template>
    </WorkspaceHero>

    <VolunteerPageSection eyebrow="历史订单" title="我的兑换订单" description="统一查看订单号、商品快照、积分消耗和签收状态。">
      <SearchForm @search="load" @reset="resetQuery">
        <el-form-item label="订单状态">
          <el-select v-model="query.status" clearable style="width: 160px">
            <el-option label="待处理" value="CREATED" />
            <el-option label="待签收" value="SHIPPED" />
            <el-option label="已签收" value="RECEIVED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
      </SearchForm>

      <StatePanel
        v-if="loading"
        state="loading"
        title="正在同步兑换订单"
        description="正在加载你的积分兑换记录和履约状态。"
      />

      <template v-else-if="list.length">
        <el-table :data="list" border>
          <el-table-column prop="orderNo" label="订单号" min-width="190" />
          <el-table-column label="商品信息" min-width="240">
            <template #default="{ row }">
              <div class="product-cell">
                <img :src="row.productImage" :alt="row.productName" />
                <div>
                  <strong>{{ row.productName }}</strong>
                  <p>{{ row.productSummary || '商品摘要已固化到订单快照中' }}</p>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="90" />
          <el-table-column prop="totalPoints" label="消耗积分" width="110" />
          <el-table-column prop="receiverName" label="收货人" width="120" />
          <el-table-column prop="receiverPhone" label="电话" min-width="150" />
          <el-table-column prop="receiverAddress" label="地址" min-width="220" show-overflow-tooltip />
          <el-table-column label="订单状态" width="110">
            <template #default="{ row }">
              <el-tag :type="getOrderStatusTag(row.status)">{{ getOrderStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" min-width="170">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
        </el-table>
      </template>

      <StatePanel
        v-else
        title="暂无兑换订单"
        description="前往积分商城完成一次兑换后，这里会展示订单快照和履约状态。"
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
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { pageMyExchangeOrdersApi, type ExchangeOrderModel } from '@/api/content'
import SearchForm from '@/components/SearchForm.vue'
import StatePanel from '@/components/shared/StatePanel.vue'
import WorkspaceHero from '@/components/shared/WorkspaceHero.vue'
import VolunteerPageSection from '@/components/volunteer/VolunteerPageSection.vue'
import { useTable } from '@/composables/useTable'
import { PORTAL_PATHS } from '@/constants/portal-routes'
import { formatDateTime, getOrderStatusLabel, getOrderStatusTag } from '@/utils/display'

interface OrderQuery {
  current: number
  size: number
  status?: string
}

const router = useRouter()

const { loading, records: list, total, query, load, handlePage, reset } = useTable<ExchangeOrderModel, OrderQuery>({
  initialQuery: {
    current: 1,
    size: 10,
    status: undefined
  },
  fetcher: (params) =>
    pageMyExchangeOrdersApi({
      current: params.current,
      size: params.size,
      status: params.status || undefined
    })
})

function resetQuery() {
  reset({
    status: undefined
  })
}

onMounted(load)
</script>

<style scoped>
.page-shell {
  display: grid;
  gap: 14px;
}

.product-cell {
  display: grid;
  grid-template-columns: 64px 1fr;
  gap: 10px;
  align-items: start;
}

.product-cell img {
  width: 64px;
  height: 48px;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid var(--cvs-border);
}

.product-cell strong,
.product-cell p {
  margin: 0;
}

.product-cell p {
  margin-top: 4px;
  color: var(--cvs-text-sub);
  line-height: 1.6;
}
</style>
