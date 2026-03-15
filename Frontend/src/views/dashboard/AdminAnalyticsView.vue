<template>
  <div class="analytics-root fade-up">
    <section class="metric-grid">
      <article class="metric-card">
        <span class="metric-label">志愿活动</span>
        <strong class="metric-value">{{ metrics.activityCount }}</strong>
        <p class="metric-hint">默认不含已归档活动</p>
      </article>
      <article class="metric-card">
        <span class="metric-label">论坛帖子</span>
        <strong class="metric-value">{{ metrics.postCount }}</strong>
        <p class="metric-hint">仅统计当前可展示的论坛帖子</p>
      </article>
      <article class="metric-card">
        <span class="metric-label">评论数量</span>
        <strong class="metric-value">{{ metrics.commentCount }}</strong>
        <p class="metric-hint">用于观察社区互动活跃度</p>
      </article>
      <article class="metric-card">
        <span class="metric-label">兑换订单</span>
        <strong class="metric-value">{{ metrics.orderCount }}</strong>
        <p class="metric-hint">默认不含已取消订单</p>
      </article>
    </section>

    <section class="chart-grid">
      <el-card class="section-card chart-card" shadow="never">
        <template #header>每日报名趋势图</template>
        <div ref="lineRef" class="chart"></div>
      </el-card>

      <el-card class="section-card chart-card" shadow="never">
        <template #header>活动分类分布图</template>
        <div ref="barRef" class="chart"></div>
      </el-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { use, init, type ECharts } from 'echarts/core'
import { BarChart, LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { adminDashboardApi, type DashboardPayload } from '@/api/dashboard'

use([LineChart, BarChart, TooltipComponent, GridComponent, CanvasRenderer])

const lineRef = ref<HTMLDivElement>()
const barRef = ref<HTMLDivElement>()
const metrics = ref<DashboardPayload>({
  activityCount: 0,
  postCount: 0,
  commentCount: 0,
  volunteerCount: 0,
  orderCount: 0,
  pendingApplicationCount: 0,
  pendingPostCount: 0,
  weeklyApplicationTrend: [],
  activityTypeBar: [],
  postTypePie: [],
  recentOperationLogs: []
})

let lineChart: ECharts | undefined
let barChart: ECharts | undefined

function ensureCharts() {
  if (lineRef.value && !lineChart) {
    lineChart = init(lineRef.value)
  }
  if (barRef.value && !barChart) {
    barChart = init(barRef.value)
  }
}

function renderCharts() {
  ensureCharts()
  const trendData = metrics.value.weeklyApplicationTrend.length
    ? metrics.value.weeklyApplicationTrend
    : [{ day: '暂无数据', value: 0 }]
  const activityTypeData = metrics.value.activityTypeBar.length
    ? metrics.value.activityTypeBar
    : [{ name: '暂无分类', value: 0 }]

  lineChart?.setOption({
    color: ['#1f7a54'],
    grid: { top: 24, right: 16, bottom: 24, left: 36 },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: trendData.map((item) => item.day.slice(5)),
      boundaryGap: false
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        name: '报名申请',
        type: 'line',
        smooth: true,
        data: trendData.map((item) => item.value),
        areaStyle: {
          color: 'rgba(31, 122, 84, 0.12)'
        }
      }
    ]
  })

  barChart?.setOption({
    color: ['#2d8a61'],
    grid: { top: 24, right: 16, bottom: 24, left: 36 },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: activityTypeData.map((item) => item.name),
      axisLabel: {
        interval: 0,
        rotate: activityTypeData.length > 4 ? 20 : 0
      }
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        name: '活动数量',
        type: 'bar',
        barMaxWidth: 28,
        data: activityTypeData.map((item) => item.value)
      }
    ]
  })
}

async function load() {
  try {
    metrics.value = await adminDashboardApi()
  } catch {
    ElMessage.warning('统计分析数据加载失败，请检查后端服务是否已启动')
  } finally {
    await nextTick()
    renderCharts()
  }
}

function resizeCharts() {
  lineChart?.resize()
  barChart?.resize()
}

onMounted(() => {
  void load()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  lineChart?.dispose()
  barChart?.dispose()
})
</script>

<style scoped>
.analytics-root {
  display: grid;
  gap: 16px;
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

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
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

.chart-card :deep(.el-card__body) {
  padding: 14px 20px 18px;
}

.chart {
  height: 320px;
}

@media (max-width: 1080px) {
  .chart-grid {
    grid-template-columns: 1fr;
  }
}
</style>
