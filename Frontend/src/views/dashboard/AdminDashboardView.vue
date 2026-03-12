<template>
  <div class="dash-root fade-up">
    <el-card class="hero-card" shadow="never">
      <div class="hero-head">
        <div>
          <p class="hero-tag">管理员运营看板</p>
          <h2>社区志愿服务总览与治理入口</h2>
          <p class="hero-desc">集中查看活动、论坛、评论与报名审核趋势，并快速进入核心管理页处理任务。</p>
        </div>
        <div class="hero-actions">
          <el-button type="primary" @click="router.push('/admin/application-audit')">去审核报名</el-button>
          <el-button @click="router.push('/admin/content-manage')">去审帖与公告</el-button>
          <el-button @click="router.push('/admin/activity-manage')">去维护活动</el-button>
        </div>
      </div>
    </el-card>

    <div class="card-grid">
      <article class="metric-card">
        <div class="metric-title">志愿活动数量</div>
        <div class="metric-value">{{ metrics.activityCount }}</div>
      </article>
      <article class="metric-card">
        <div class="metric-title">社区帖子数量</div>
        <div class="metric-value">{{ metrics.postCount }}</div>
      </article>
      <article class="metric-card">
        <div class="metric-title">评论数量</div>
        <div class="metric-value">{{ metrics.commentCount }}</div>
      </article>
      <article class="metric-card">
        <div class="metric-title">志愿者数量</div>
        <div class="metric-value">{{ metrics.volunteerCount }}</div>
      </article>
    </div>

    <div class="chart-grid">
      <el-card class="chart-card" shadow="never">
        <template #header>近一周活动报名趋势</template>
        <div ref="lineRef" class="chart"></div>
      </el-card>
      <el-card class="chart-card" shadow="never">
        <template #header>活动类型数量柱状图</template>
        <div ref="barRef" class="chart"></div>
      </el-card>
      <el-card class="chart-card wide" shadow="never">
        <template #header>论坛帖子类型分布饼图</template>
        <div ref="pieRef" class="chart"></div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { use, init, type ECharts } from 'echarts/core'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { useRouter } from 'vue-router'
import { adminDashboardApi, type DashboardPayload } from '@/api/dashboard'

use([LineChart, BarChart, PieChart, TooltipComponent, GridComponent, LegendComponent, CanvasRenderer])

const router = useRouter()
const lineRef = ref<HTMLDivElement>()
const barRef = ref<HTMLDivElement>()
const pieRef = ref<HTMLDivElement>()
const metrics = ref<DashboardPayload>({
  activityCount: 0,
  postCount: 0,
  commentCount: 0,
  volunteerCount: 0,
  weeklyApplicationTrend: [],
  activityTypeBar: [],
  postTypePie: []
})

let lineChart: ECharts | null = null
let barChart: ECharts | null = null
let pieChart: ECharts | null = null

function renderCharts() {
  if (lineRef.value) {
    lineChart = init(lineRef.value)
    lineChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: metrics.value.weeklyApplicationTrend.map((item) => item.day)
      },
      yAxis: { type: 'value' },
      series: [
        {
          data: metrics.value.weeklyApplicationTrend.map((item) => item.value),
          type: 'line',
          smooth: true,
          symbol: 'circle',
          areaStyle: { opacity: 0.14 }
        }
      ],
      color: ['#d12a53']
    })
  }

  if (barRef.value) {
    barChart = init(barRef.value)
    barChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: metrics.value.activityTypeBar.map((item) => item.name)
      },
      yAxis: { type: 'value' },
      series: [
        {
          data: metrics.value.activityTypeBar.map((item) => item.value),
          type: 'bar',
          barWidth: 28,
          itemStyle: { borderRadius: [6, 6, 0, 0] }
        }
      ],
      color: ['#f09c45']
    })
  }

  if (pieRef.value) {
    pieChart = init(pieRef.value)
    pieChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [
        {
          type: 'pie',
          radius: ['32%', '62%'],
          data: metrics.value.postTypePie
        }
      ],
      color: ['#c5204c', '#f05b7a', '#f09c45', '#f0be61', '#5f7484']
    })
  }
}

onMounted(async () => {
  metrics.value = await adminDashboardApi()
  renderCharts()
  window.addEventListener('resize', handleResize)
})

function handleResize() {
  lineChart?.resize()
  barChart?.resize()
  pieChart?.resize()
}

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  lineChart?.dispose()
  barChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped>
.dash-root {
  display: grid;
  gap: 14px;
}

.hero-card {
  border: 1px solid #efd6dc;
  border-radius: 16px;
  background: linear-gradient(120deg, rgba(210, 40, 86, 0.08), rgba(244, 150, 79, 0.14));
}

.hero-head {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: flex-start;
}

.hero-tag {
  margin: 0;
  color: #9f2d4b;
  letter-spacing: 0.08em;
  font-size: 12px;
  font-weight: 700;
}

.hero-head h2 {
  margin: 8px 0 0;
}

.hero-desc {
  margin: 10px 0 0;
  color: #655760;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.chart-card :deep(.el-card__header) {
  font-weight: 700;
  color: #9f2d4b;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.chart-card {
  border: 1px solid var(--cvs-border);
  border-radius: 16px;
}

.chart-card.wide {
  grid-column: span 2;
}

.chart {
  height: 320px;
}

@media (max-width: 900px) {
  .hero-head {
    flex-direction: column;
  }

  .hero-actions {
    justify-content: flex-start;
  }

  .chart-grid {
    grid-template-columns: 1fr;
  }

  .chart-card.wide {
    grid-column: span 1;
  }
}
</style>
