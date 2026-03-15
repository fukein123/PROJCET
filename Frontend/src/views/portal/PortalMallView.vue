<template>
  <div>
    <PortalNavBar />
    <main class="portal-wrap">
      <WorkspaceHero
        tone="portal"
        compact
        eyebrow="积分商城"
        title="用服务积分兑换社区物资与纪念礼品"
        description="登录后可直接提交兑换申请。系统会在一次事务内完成积分校验、库存扣减和订单生成，避免出现重复扣分或库存穿透。"
      >
        <template #actions>
          <el-button type="primary" @click="load">刷新商品</el-button>
          <el-button @click="router.push(PORTAL_PATHS.selfServiceOrders)">查看订单</el-button>
        </template>
        <template #aside>
          <div class="hero-stat-grid">
            <article class="hero-stat">
              <h3>可兑换商品</h3>
              <strong>{{ total }}</strong>
              <span>当前上架且可浏览的积分商品数量</span>
            </article>
            <article class="hero-stat">
              <h3>当前积分</h3>
              <strong>{{ userStore.profile?.points ?? 0 }}</strong>
              <span>兑换时会实时校验你的积分余额</span>
            </article>
            <article class="hero-stat">
              <h3>我的订单</h3>
              <strong>{{ userStore.isLogin ? '已开放' : '登录后可见' }}</strong>
              <span>订单状态会同步到头像下拉菜单中的兑换订单</span>
            </article>
          </div>
        </template>
      </WorkspaceHero>

      <section class="module-card fade-up">
        <div class="module-head">
          <div>
            <p class="module-eyebrow">筛选商品</p>
            <h2 class="section-title">商品列表</h2>
          </div>
        </div>

        <div class="toolbar">
          <el-input v-model.trim="keyword" placeholder="请输入商品名称搜索" clearable @keyup.enter="load" />
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </div>

        <StatePanel
          v-if="loading"
          state="loading"
          tone="portal"
          title="正在同步商品列表"
          description="正在加载积分商城商品与库存信息。"
        />
        <StatePanel
          v-else-if="!products.length"
          tone="portal"
          title="当前暂无可兑换商品"
          description="管理员上架新的积分商品后，这里会自动展示。"
        />
        <section v-else class="product-grid">
          <article v-for="item in products" :key="item.id" class="product-card">
            <img class="product-image" :src="item.imageUrl" :alt="item.name" />
            <div class="product-main">
              <div class="product-top">
                <h3>{{ item.name }}</h3>
                <el-tag :type="item.stock > 0 ? 'success' : 'info'">
                  {{ item.stock > 0 ? `库存 ${item.stock}` : '已售罄' }}
                </el-tag>
              </div>
              <p class="product-summary">{{ item.summary }}</p>
              <div class="meta-row">
                <span>单件积分</span>
                <strong>{{ item.pointsCost }}</strong>
              </div>
              <div class="meta-row">
                <span>当前积分</span>
                <strong>{{ userStore.profile?.points ?? 0 }}</strong>
              </div>
              <div class="actions">
                <el-button
                  type="primary"
                  :disabled="item.stock <= 0"
                  @click="openExchange(item)"
                >
                  立即兑换
                </el-button>
                <el-button plain @click="router.push(PORTAL_PATHS.selfServiceOrders)">查看订单</el-button>
              </div>
            </div>
          </article>
        </section>

        <div v-if="!loading && total > 0" class="pager">
          <el-pagination
            layout="total, prev, pager, next"
            :total="total"
            :current-page="query.current"
            :page-size="query.size"
            @current-change="handlePage"
          />
        </div>
      </section>
    </main>
  </div>

  <el-dialog
    v-model="visible"
    title="提交兑换申请"
    width="min(92vw, 720px)"
    append-to-body
    top="6vh"
    class="exchange-dialog"
  >
    <div v-if="currentProduct" class="exchange-shell">
      <div class="exchange-product">
        <img :src="currentProduct.imageUrl" :alt="currentProduct.name" />
        <div>
          <h3>{{ currentProduct.name }}</h3>
          <p>{{ currentProduct.summary }}</p>
          <div class="exchange-meta">
            <span>单件积分：{{ currentProduct.pointsCost }}</span>
            <span>剩余库存：{{ currentProduct.stock }}</span>
          </div>
        </div>
      </div>

      <el-alert
        type="info"
        :closable="false"
        show-icon
        :title="`本次预计消耗 ${estimatedPoints} 积分，当前可用 ${userStore.profile?.points ?? 0} 积分`"
      />

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="兑换数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" :max="currentProduct.stock || 1" />
        </el-form-item>
        <el-form-item label="收货人名称" prop="receiverName">
          <el-input v-model.trim="form.receiverName" maxlength="64" show-word-limit />
        </el-form-item>
        <el-form-item label="收货人电话" prop="receiverPhone">
          <el-input v-model.trim="form.receiverPhone" maxlength="32" />
        </el-form-item>
        <el-form-item label="收货地址" prop="receiverAddress">
          <el-input
            v-model.trim="form.receiverAddress"
            type="textarea"
            :rows="3"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submitExchange">确认兑换</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  createExchangeOrderApi,
  pageMallProductsApi,
  type MallProductModel
} from '@/api/content'
import StatePanel from '@/components/shared/StatePanel.vue'
import WorkspaceHero from '@/components/shared/WorkspaceHero.vue'
import { PORTAL_PATHS } from '@/constants/portal-routes'
import { usePortalNavigation } from '@/composables/usePortalNavigation'
import { useUserStore } from '@/stores/userStore'
import { validateElementForm } from '@/utils/form'
import PortalNavBar from './PortalNavBar.vue'

const router = useRouter()
const portalNav = usePortalNavigation()
const userStore = useUserStore()

const loading = ref(false)
const visible = ref(false)
const submitting = ref(false)
const keyword = ref('')
const total = ref(0)
const products = ref<MallProductModel[]>([])
const currentProduct = ref<MallProductModel>()
const formRef = ref<FormInstance>()
const query = reactive({
  current: 1,
  size: 9
})
const form = reactive({
  quantity: 1,
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  requestKey: ''
})

const rules: FormRules = {
  quantity: [{ required: true, message: '请输入兑换数量', trigger: 'change' }],
  receiverName: [{ required: true, message: '请输入收货人名称', trigger: 'blur' }],
  receiverPhone: [{ required: true, message: '请输入收货人电话', trigger: 'blur' }],
  receiverAddress: [{ required: true, message: '请输入收货地址', trigger: 'blur' }]
}

const estimatedPoints = computed(() => (currentProduct.value ? currentProduct.value.pointsCost * form.quantity : 0))

function createRequestKey(productId: number) {
  return `mall-${productId}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
}

async function ensureProfileLoaded() {
  if (!userStore.isLogin) {
    return
  }
  if (!userStore.profile) {
    await userStore.fetchProfile()
  }
}

async function load() {
  loading.value = true
  try {
    const res = await pageMallProductsApi({
      current: query.current,
      size: query.size,
      keyword: keyword.value || undefined,
      onlyEnabled: true
    })
    products.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function reset() {
  query.current = 1
  keyword.value = ''
  void load()
}

function handlePage(page: number) {
  query.current = page
  void load()
}

async function openExchange(product: MallProductModel) {
  await portalNav.requireLogin(async () => {
    if (userStore.role !== 'VOLUNTEER') {
      ElMessage.warning('请使用志愿者账号进行积分兑换')
      return
    }
    await ensureProfileLoaded()
    currentProduct.value = product
    form.quantity = 1
    form.receiverName = userStore.profile?.realName || userStore.username || ''
    form.receiverPhone = userStore.profile?.phone || ''
    form.receiverAddress = ''
    form.requestKey = createRequestKey(product.id)
    visible.value = true
  }, PORTAL_PATHS.mall)
}

async function submitExchange() {
  if (!currentProduct.value || !(await validateElementForm(formRef.value))) {
    return
  }
  if (estimatedPoints.value > (userStore.profile?.points ?? 0)) {
    ElMessage.warning('当前积分不足，无法完成兑换')
    return
  }

  submitting.value = true
  try {
    const order = await createExchangeOrderApi({
      productId: currentProduct.value.id,
      quantity: form.quantity,
      receiverName: form.receiverName,
      receiverPhone: form.receiverPhone,
      receiverAddress: form.receiverAddress,
      requestKey: form.requestKey
    })
    visible.value = false
    await Promise.all([load(), userStore.fetchProfile().catch(() => undefined)])
    ElMessage.success(`兑换成功，订单号 ${order.orderNo}`)
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await Promise.all([load(), ensureProfileLoaded().catch(() => undefined)])
})
</script>

<style scoped>
.portal-wrap {
  width: min(1220px, calc(100% - 24px));
  margin: 14px auto 40px;
  display: grid;
  gap: 14px;
}

.module-card {
  border: 1px solid var(--cvs-border);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.94);
  padding: 18px;
  box-shadow: var(--cvs-shadow-soft);
}

.module-head {
  margin-bottom: 14px;
}

.module-eyebrow {
  margin: 0 0 6px;
  color: #2a7a5f;
  font-size: var(--cvs-font-size-xs);
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.module-head :deep(.section-title) {
  margin-bottom: 0;
}

.toolbar {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 8px;
  margin-bottom: 16px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 14px;
}

.product-card {
  display: grid;
  grid-template-rows: auto 1fr;
  border: 1px solid var(--cvs-border);
  border-radius: 16px;
  overflow: hidden;
  background: #fff;
  transition:
    transform var(--cvs-motion-fast) var(--cvs-ease-standard),
    box-shadow var(--cvs-motion-fast) var(--cvs-ease-standard);
}

.product-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--cvs-shadow-card-hover);
}

.product-image {
  width: 100%;
  height: 190px;
  object-fit: cover;
  display: block;
}

.product-main {
  padding: 14px;
  display: grid;
  gap: 10px;
}

.product-top {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: flex-start;
}

.product-top h3 {
  margin: 0;
}

.product-summary {
  margin: 0;
  color: var(--cvs-text-sub);
  line-height: 1.7;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  color: #5b6964;
}

.meta-row strong {
  color: #1e312a;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pager {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.exchange-shell {
  display: grid;
  gap: 14px;
}

.exchange-product {
  display: grid;
  grid-template-columns: 140px 1fr;
  gap: 14px;
  align-items: start;
}

.exchange-product img {
  width: 140px;
  height: 104px;
  object-fit: cover;
  border-radius: 12px;
  border: 1px solid var(--cvs-border);
}

.exchange-product h3,
.exchange-product p {
  margin: 0;
}

.exchange-product p {
  color: var(--cvs-text-sub);
  line-height: 1.7;
}

.exchange-meta {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: #5d7268;
}

:deep(.exchange-dialog .el-dialog__body) {
  max-height: calc(100vh - 220px);
  overflow: auto;
}

@media (max-width: 860px) {
  .toolbar {
    grid-template-columns: 1fr;
  }

  .pager {
    justify-content: flex-start;
  }

  .exchange-product {
    grid-template-columns: 1fr;
  }
}
</style>
