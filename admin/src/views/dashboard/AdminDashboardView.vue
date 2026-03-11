<template>
  <div class="dash-root fade-up">
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
        <div class="metric-title">兑换订单数量</div>
        <div class="metric-value">{{ metrics.orderCount }}</div>
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
import { onMounted, onBeforeUnmount, ref } from 'vue'
import { use, init, type ECharts } from 'echarts/core'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { adminDashboardApi, type DashboardPayload } from '@/api/dashboard'

use([LineChart, BarChart, PieChart, TooltipComponent, GridComponent, LegendComponent, CanvasRenderer])

const lineRef = ref<HTMLDivElement>()
const barRef = ref<HTMLDivElement>()
const pieRef = ref<HTMLDivElement>()
const metrics = ref<DashboardPayload>({
  activityCount: 0,
  postCount: 0,
  orderCount: 0,
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
      color: ['#1f7a54']
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
      color: ['#efc264']
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
      color: ['#1f7a54', '#4da783', '#efc264', '#ca8f42', '#3f5f52']
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
  .chart-grid {
    grid-template-columns: 1fr;
  }
  .chart-card.wide {
    grid-column: span 1;
  }
}
</style>
