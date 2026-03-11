<template>
  <div class="cvs-page">
    <div class="grid">
      <div class="stat cvs-card" v-for="s in stats" :key="s.label">
        <div class="label cvs-muted">{{ s.label }}</div>
        <div class="value">{{ s.value }}</div>
      </div>
    </div>

    <div class="charts">
      <div class="chart cvs-card">
        <div class="title">近一周每日报名人数（折线图）</div>
        <div ref="lineRef" class="canvas" />
      </div>
      <div class="chart cvs-card">
        <div class="title">不同类型活动数量（柱状图）</div>
        <div ref="barRef" class="canvas" />
      </div>
      <div class="chart cvs-card">
        <div class="title">不同类型帖子数量（饼图）</div>
        <div ref="pieRef" class="canvas" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'

const stats = [
  { label: '志愿活动数量', value: 11 },
  { label: '社区帖子数量', value: 11 },
  { label: '兑换订单数量', value: 7 },
  { label: '志愿者数量', value: 4 }
]

const lineRef = ref<HTMLDivElement | null>(null)
const barRef = ref<HTMLDivElement | null>(null)
const pieRef = ref<HTMLDivElement | null>(null)

let lineChart: echarts.ECharts | null = null
let barChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

function build() {
  const days = Array.from({ length: 7 }).map((_, i) => dayjs().subtract(6 - i, 'day').format('MM-DD'))
  const signups = [2, 4, 1, 6, 3, 5, 2]

  lineChart = echarts.init(lineRef.value!)
  lineChart.setOption({
    grid: { left: 30, right: 20, top: 30, bottom: 24 },
    xAxis: { type: 'category', data: days },
    yAxis: { type: 'value' },
    series: [
      {
        type: 'line',
        data: signups,
        smooth: true,
        areaStyle: { opacity: 0.12 },
        lineStyle: { width: 3, color: '#d81e2b' },
        itemStyle: { color: '#d81e2b' }
      }
    ],
    tooltip: { trigger: 'axis' }
  })

  barChart = echarts.init(barRef.value!)
  barChart.setOption({
    grid: { left: 30, right: 20, top: 30, bottom: 24 },
    xAxis: { type: 'category', data: ['文化娱乐', '环境保护', '医疗服务', '教育助学'] },
    yAxis: { type: 'value' },
    series: [
      {
        type: 'bar',
        data: [3, 4, 2, 2],
        itemStyle: { borderRadius: [8, 8, 0, 0], color: '#d81e2b' }
      }
    ],
    tooltip: { trigger: 'axis' }
  })

  pieChart = echarts.init(pieRef.value!)
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    series: [
      {
        type: 'pie',
        radius: ['45%', '72%'],
        label: { color: '#1f2329' },
        data: [
          { value: 5, name: '志愿点滴' },
          { value: 3, name: '心路历程' },
          { value: 2, name: '服务心得' },
          { value: 1, name: '志愿故事' }
        ]
      }
    ]
  })
}

function resize() {
  lineChart?.resize()
  barChart?.resize()
  pieChart?.resize()
}

onMounted(() => {
  build()
  window.addEventListener('resize', resize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  lineChart?.dispose()
  barChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}
.stat {
  padding: 14px 14px 16px;
}
.label {
  font-size: 12px;
}
.value {
  margin-top: 6px;
  font-size: 26px;
  font-weight: 800;
}
.charts {
  margin-top: 14px;
  display: grid;
  grid-template-columns: 2fr 1.2fr;
  gap: 14px;
}
.chart {
  padding: 14px;
}
.chart:nth-child(3) {
  grid-column: 1 / -1;
}
.title {
  font-weight: 800;
  margin-bottom: 10px;
}
.canvas {
  width: 100%;
  height: 320px;
}
@media (max-width: 1100px) {
  .grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .charts {
    grid-template-columns: 1fr;
  }
  .chart:nth-child(3) {
    grid-column: auto;
  }
}
</style>

