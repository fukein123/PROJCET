<template>
  <div class="dashboard-root fade-up">
    <section class="dashboard-head">
      <div class="head-copy">
        <p class="head-eyebrow">管理员工作台</p>
        <h2>系统首页</h2>
        <p class="head-desc">
          后台首页只保留核心指标、待处理事项和治理分流。趋势图、审计留痕与深度分析全部进入独立页面处理。
        </p>
      </div>

      <div class="head-actions">
        <el-button type="primary" @click="openRoute({ path: '/admin/application-audit' })">审核报名申请</el-button>
        <el-button @click="openRoute({ path: '/admin/forum', query: { tab: 'post' } })">处理帖子审核</el-button>
        <el-button @click="openRoute({ path: '/admin/analytics' })">进入统计分析</el-button>
      </div>
    </section>

    <section class="metric-grid">
      <article v-for="item in focusMetricCards" :key="item.key" class="metric-card">
        <span class="metric-label">{{ item.label }}</span>
        <strong class="metric-value">{{ item.value }}</strong>
        <p class="metric-hint">{{ item.hint }}</p>
      </article>
    </section>

    <section class="detail-grid">
      <el-card class="section-card" shadow="never">
        <template #header>
          <div class="section-head">
            <div>
              <strong>待处理事项</strong>
              <span>只保留会直接影响志愿活动执行、内容发布和兑换履约的关键动作。</span>
            </div>
          </div>
        </template>

        <div class="todo-list">
          <article v-for="item in todoCards" :key="item.key" class="todo-item">
            <div class="todo-copy">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
              <p>{{ item.hint }}</p>
            </div>
            <el-button size="small" :type="item.type" plain @click="openRoute(item.to)">
              {{ item.actionLabel }}
            </el-button>
          </article>
        </div>
      </el-card>

      <el-card class="section-card" shadow="never">
        <template #header>
          <div class="section-head">
            <div>
              <strong>独立页面分流</strong>
              <span>首页不再展开图表和审计表格，只保留继续工作的直达入口。</span>
            </div>
          </div>
        </template>

        <div class="handoff-list">
          <article v-for="item in handoffCards" :key="item.key" class="handoff-item">
            <div class="handoff-copy">
              <span class="handoff-label">{{ item.label }}</span>
              <strong class="handoff-title">{{ item.title }}</strong>
              <p class="handoff-hint">{{ item.hint }}</p>
            </div>
            <el-button size="small" :type="item.type" plain @click="openRoute(item.to)">
              {{ item.actionLabel }}
            </el-button>
          </article>
        </div>
      </el-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter, type RouteLocationRaw } from 'vue-router'
import { adminDashboardApi, type DashboardPayload } from '@/api/dashboard'

interface DashboardSummary {
  activityCount: number
  volunteerCount: number
  orderCount: number
  pendingApplicationCount: number
  pendingPostCount: number
}

function createEmptySummary(): DashboardSummary {
  return {
    activityCount: 0,
    volunteerCount: 0,
    orderCount: 0,
    pendingApplicationCount: 0,
    pendingPostCount: 0
  }
}

function mapDashboardSummary(payload: DashboardPayload): DashboardSummary {
  return {
    activityCount: payload.activityCount,
    volunteerCount: payload.volunteerCount,
    orderCount: payload.orderCount,
    pendingApplicationCount: payload.pendingApplicationCount,
    pendingPostCount: payload.pendingPostCount
  }
}

const router = useRouter()
const metrics = ref<DashboardSummary>(createEmptySummary())

const focusMetricCards = computed(() => [
  {
    key: 'activityCount',
    label: '志愿活动数量',
    value: metrics.value.activityCount,
    hint: '默认不含已归档活动'
  },
  {
    key: 'volunteerCount',
    label: '志愿者数量',
    value: metrics.value.volunteerCount,
    hint: '仅统计当前启用中的志愿者账号'
  },
  {
    key: 'pendingApplicationCount',
    label: '待审核报名',
    value: metrics.value.pendingApplicationCount,
    hint: '优先处理报名审核，避免影响志愿者参与活动'
  },
  {
    key: 'pendingPostCount',
    label: '待审核帖子',
    value: metrics.value.pendingPostCount,
    hint: '帖子审核结果会直接影响前台社区论坛展示'
  }
])

const todoCards = computed(() => [
  {
    key: 'application',
    label: '活动报名申请',
    value: metrics.value.pendingApplicationCount,
    hint: '报名审核直接影响志愿者能否参与活动。',
    actionLabel: '进入审核',
    type: 'primary' as const,
    to: { path: '/admin/application-audit' }
  },
  {
    key: 'post',
    label: '论坛帖子审核',
    value: metrics.value.pendingPostCount,
    hint: '帖子审核影响论坛帖子是否对前台可见。',
    actionLabel: '进入帖子',
    type: 'warning' as const,
    to: { path: '/admin/forum', query: { tab: 'post' } }
  },
  {
    key: 'order',
    label: '兑换订单履约',
    value: metrics.value.orderCount,
    hint: '优先关注待签收订单，保证兑换链路完整。',
    actionLabel: '查看订单',
    type: 'success' as const,
    to: { path: '/admin/mall', query: { tab: 'exchangeOrder' } }
  }
])

const handoffCards = [
  {
    key: 'analytics',
    label: '统计分析',
    title: '趋势图与分类图表',
    hint: '报名趋势、活动分类分布和运营指标统一进入统计分析页。',
    actionLabel: '查看分析',
    type: 'primary' as const,
    to: { path: '/admin/analytics' }
  },
  {
    key: 'audit',
    label: '审计中心',
    title: '关键操作留痕',
    hint: '查看报名审核、帖子审核、认证审核和订单状态修改的审计记录。',
    actionLabel: '查看审计',
    type: 'warning' as const,
    to: { path: '/admin/audit' }
  },
  {
    key: 'content',
    label: '内容治理',
    title: '公告、动态与轮播图',
    hint: '统一维护门户内容发布，不再与论坛和商城链路混放。',
    actionLabel: '进入内容',
    type: 'success' as const,
    to: { path: '/admin/content' }
  },
  {
    key: 'forum',
    label: '论坛治理',
    title: '帖子与评论审核',
    hint: '集中处理论坛分类、帖子审核和评论治理。',
    actionLabel: '进入论坛',
    type: 'info' as const,
    to: { path: '/admin/forum' }
  }
]

function openRoute(target: RouteLocationRaw) {
  void router.push(target)
}

async function load() {
  try {
    metrics.value = mapDashboardSummary(await adminDashboardApi())
  } catch {
    ElMessage.warning('后台统计数据加载失败，请检查后端服务是否已启动')
  }
}

onMounted(() => {
  void load()
})
</script>

<style scoped>
.dashboard-root {
  display: grid;
  gap: 16px;
}

.dashboard-head {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 18px;
  padding: 22px 24px;
  border-radius: 22px;
  border: 1px solid rgba(24, 102, 71, 0.12);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(248, 251, 249, 0.98));
  box-shadow: 0 16px 36px rgba(24, 53, 40, 0.06);
}

.head-copy {
  display: grid;
  gap: 10px;
}

.head-eyebrow {
  margin: 0;
  color: #1e7250;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.head-copy h2 {
  margin: 0;
  color: #183224;
  font-size: clamp(30px, 4vw, 40px);
  line-height: 1.16;
}

.head-desc {
  margin: 0;
  max-width: 760px;
  color: #5c7066;
  line-height: 1.8;
}

.head-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-content: flex-start;
  justify-content: flex-end;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.metric-card {
  display: grid;
  gap: 10px;
  min-height: 122px;
  padding: 18px;
  border-radius: 18px;
  border: 1px solid rgba(24, 102, 71, 0.08);
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 12px 24px rgba(24, 53, 40, 0.05);
}

.metric-label {
  color: #61756c;
  font-size: 13px;
}

.metric-value {
  color: #223128;
  font-size: 32px;
  line-height: 1;
}

.metric-hint {
  margin: 0;
  color: #74867f;
  line-height: 1.6;
  font-size: 12px;
}

.detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(320px, 0.95fr);
  gap: 14px;
}

.section-card {
  border: 1px solid var(--cvs-border);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.96);
}

.section-card :deep(.el-card__header) {
  border-bottom: 1px solid var(--cvs-border);
  padding: 16px 20px;
}

.section-card :deep(.el-card__body) {
  padding: 18px 20px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.section-head strong {
  display: block;
  color: #183224;
  font-size: 16px;
}

.section-head span {
  display: block;
  margin-top: 4px;
  color: var(--cvs-text-sub);
  font-size: 13px;
  line-height: 1.6;
}

.todo-list,
.handoff-list {
  display: grid;
  gap: 12px;
}

.todo-item,
.handoff-item {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid rgba(24, 102, 71, 0.08);
  background: rgba(255, 255, 255, 0.94);
}

.handoff-item {
  background:
    linear-gradient(180deg, rgba(249, 252, 250, 0.98), rgba(244, 248, 246, 0.96)),
    radial-gradient(circle at top right, rgba(31, 122, 84, 0.08), transparent 48%);
}

.todo-copy,
.handoff-copy {
  display: grid;
  gap: 6px;
}

.todo-copy span,
.handoff-label {
  color: var(--cvs-text-sub);
  font-size: 13px;
}

.todo-copy strong {
  color: #183224;
  font-size: 24px;
  line-height: 1;
}

.todo-copy p,
.handoff-hint {
  margin: 0;
  color: #72847c;
  line-height: 1.7;
  font-size: 12px;
}

.handoff-label {
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #5e766b;
}

.handoff-title {
  color: #223128;
  font-size: 18px;
  line-height: 1.3;
}

@media (max-width: 1280px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1100px) {
  .dashboard-head {
    grid-template-columns: 1fr;
  }

  .head-actions {
    justify-content: flex-start;
  }
}

@media (max-width: 820px) {
  .todo-item,
  .handoff-item {
    grid-template-columns: 1fr;
  }
}
</style>
