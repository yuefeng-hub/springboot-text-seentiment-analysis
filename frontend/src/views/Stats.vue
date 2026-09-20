<template>
  <div class="stats-page">
    <!-- 页面标题 -->
    <div class="cyber-page-head">
      <div>
        <div class="cyber-page-title">数据统计中心</div>
        <div class="cyber-page-sub">DATA ANALYTICS · 情感分布与趋势实时可视化</div>
      </div>
    </div>

    <!-- 数据总览卡片 -->
    <div class="stat-grid">
      <div class="stat-card c-blue">
        <div class="stat-icon"><el-icon><DataAnalysis /></el-icon></div>
        <div>
          <div class="stat-num">{{ overview.total }}</div>
          <div class="stat-lbl">分析总量</div>
        </div>
        <span class="stat-code">TOTAL</span>
      </div>
      <div class="stat-card c-green">
        <div class="stat-icon"><el-icon><TrendCharts /></el-icon></div>
        <div>
          <div class="stat-num">{{ overview.positiveRate }}<span class="unit">%</span></div>
          <div class="stat-lbl">正向占比</div>
        </div>
        <span class="stat-code">POS</span>
      </div>
      <div class="stat-card c-amber">
        <div class="stat-icon"><el-icon><Calendar /></el-icon></div>
        <div>
          <div class="stat-num">{{ totalDays }}</div>
          <div class="stat-lbl">近7日统计天数</div>
        </div>
        <span class="stat-code">DAYS</span>
      </div>
      <div class="stat-card c-violet">
        <div class="stat-icon"><el-icon><Files /></el-icon></div>
        <div>
          <div class="stat-num">{{ totalNegative }}</div>
          <div class="stat-lbl">负向数量</div>
        </div>
        <span class="stat-code">NEG</span>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="chart-grid">
      <el-card class="chart-card">
        <div class="chart-title"><span class="dot"></span>情感分布占比</div>
        <div ref="pieRef" class="chart"></div>
      </el-card>
      <el-card class="chart-card">
        <div class="chart-title"><span class="dot"></span>近 7 日情感趋势</div>
        <div ref="lineRef" class="chart"></div>
      </el-card>
    </div>
  </div>
</template>
<script setup>
import { ref, computed, onMounted, nextTick, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import request from '../utils/request'
const overview = ref({ total: 0, positiveRate: 0, distribution: {}, trend: [] })
const pieRef = ref(null)
const lineRef = ref(null)
let pieChart = null
let lineChart = null
const totalDays = computed(() => new Set((overview.value.trend || []).map(r => r.d)).size)
const totalNegative = computed(() => Number((overview.value.distribution || {})['-1'] || 0))
const AXIS_TEXT = '#8fd6ec'
const SPLIT = 'rgba(0,240,255,0.1)'
async function load() {
  const res = await request.get('/stats/overview')
  if (res.code === 200) {
    overview.value = res.data
    await nextTick()
    renderPie()
    renderLine()
  }
}
function renderPie() {
  if (!pieRef.value) return
  pieChart = pieChart || echarts.init(pieRef.value)
  const d = overview.value.distribution || {}
  pieChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
      backgroundColor: 'rgba(8,12,28,0.95)',
      borderColor: 'rgba(0,240,255,0.4)',
      textStyle: { color: '#d9f8ff' }
    },
    legend: { bottom: 0, icon: 'circle', textStyle: { color: '#8fd6ec' } },
    series: [{
      type: 'pie',
      radius: ['45%', '68%'],
      center: ['50%', '46%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 8, borderColor: '#0a0f2a', borderWidth: 3 },
      label: { show: true, formatter: '{b}\n{d}%', fontSize: 12, color: '#cfeffb' },
      labelLine: { lineStyle: { color: 'rgba(0,240,255,0.4)' } },
      data: [
        { name: '正向', value: Number(d['1'] || 0), itemStyle: { color: { type: 'linear', x: 0, y: 0, x2: 1, y2: 1, colorStops: [{ offset: 0, color: '#00f0a0' }, { offset: 1, color: '#00c2a0' }] } } },
        { name: '中性', value: Number(d['0'] || 0), itemStyle: { color: { type: 'linear', x: 0, y: 0, x2: 1, y2: 1, colorStops: [{ offset: 0, color: '#ffe14d' }, { offset: 1, color: '#ffb800' }] } } },
        { name: '负向', value: Number(d['-1'] || 0), itemStyle: { color: { type: 'linear', x: 0, y: 0, x2: 1, y2: 1, colorStops: [{ offset: 0, color: '#ff4d8a' }, { offset: 1, color: '#ff2d6e' }] } } }
      ]
    }]
  })
}
function renderLine() {
  if (!lineRef.value) return
  lineChart = lineChart || echarts.init(lineRef.value)
  const trend = overview.value.trend || []
  const days = [...new Set(trend.map(r => String(r.d)))].sort()
  const colors = { '1': '#00f0a0', '0': '#ffc94d', '-1': '#ff4d8a' }
  const typeMap = { '1': '正向', '0': '中性', '-1': '负向' }
  const series = ['1', '0', '-1'].map(t => ({
    name: typeMap[t],
    type: 'line',
    smooth: true,
    symbolSize: 7,
    lineStyle: { width: 3, color: colors[t], shadowColor: colors[t], shadowBlur: 8 },
    itemStyle: { color: colors[t] },
    areaStyle: {
      color: {
        type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: colors[t] + '55' },
          { offset: 1, color: colors[t] + '08' }
        ]
      }
    },
    data: days.map(d => {
      const row = trend.find(r => String(r.d) === d && String(r.type) === t)
      return row ? Number(row.cnt) : 0
    })
  }))
  lineChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(8,12,28,0.95)',
      borderColor: 'rgba(0,240,255,0.4)',
      textStyle: { color: '#d9f8ff' }
    },
    legend: { bottom: 0, icon: 'circle', textStyle: { color: '#8fd6ec' } },
    grid: { left: 44, right: 20, top: 30, bottom: 44 },
    xAxis: { type: 'category', data: days, axisLine: { lineStyle: { color: 'rgba(0,240,255,0.25)' } }, axisLabel: { color: AXIS_TEXT } },
    yAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { color: SPLIT } }, axisLabel: { color: AXIS_TEXT } },
    series
  })
}
function onResize() {
  if (pieChart) pieChart.resize()
  if (lineChart) lineChart.resize()
}
onMounted(() => {
  load()
  window.addEventListener('resize', onResize)
})
onBeforeUnmount(() => window.removeEventListener('resize', onResize))
</script>
<style scoped>
.stats-page { max-width: 1100px; margin: 0 auto; }

/* 数据卡片 */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
  margin-bottom: 4px;
}
.stat-card {
  position: relative;
  display: flex; align-items: center; gap: 16px;
  border-radius: 16px;
  padding: 20px 22px;
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.18);
  overflow: hidden;
}
.stat-card::before {
  content: '';
  position: absolute; top: 0; left: 0; right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.9), transparent);
}
.stat-code {
  position: absolute; top: 12px; right: 14px;
  font-size: 9px; letter-spacing: 2px;
  opacity: 0.6;
}
.stat-icon {
  width: 50px; height: 50px; border-radius: 12px;
  background: rgba(255,255,255,0.18);
  display: flex; align-items: center; justify-content: center;
  font-size: 26px;
  box-shadow: inset 0 0 12px rgba(255,255,255,0.15);
}
.stat-num { font-size: 27px; font-weight: 800; line-height: 1.2; text-shadow: 0 0 14px rgba(255,255,255,0.35); }
.stat-lbl { font-size: 13px; opacity: 0.9; margin-top: 2px; letter-spacing: 0.5px; }
.unit { font-size: 15px; font-weight: 500; }
.c-blue { background: linear-gradient(135deg, #0e63f0, #00b8e8); box-shadow: 0 8px 24px rgba(0, 184, 232, 0.25); }
.c-green { background: linear-gradient(135deg, #00c2a0, #00e0a6); box-shadow: 0 8px 24px rgba(0, 224, 166, 0.25); }
.c-amber { background: linear-gradient(135deg, #f0a800, #ffd23d); box-shadow: 0 8px 24px rgba(255, 201, 77, 0.25); }
.c-violet { background: linear-gradient(135deg, #7b2dff, #b45cff); box-shadow: 0 8px 24px rgba(123, 45, 255, 0.3); }

/* 图表 */
.chart-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(420px, 1fr));
  gap: 16px;
  margin-top: 16px;
}
.chart-card { padding: 4px 8px 10px; }
.chart-title {
  display: flex; align-items: center; gap: 8px;
  font-size: 15px; font-weight: 700; color: #d9f8ff;
  padding: 10px 6px 4px;
}
.dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: #00f0ff;
  box-shadow: 0 0 10px #00f0ff;
}
.chart { height: 320px; }
@media (max-width: 900px) {
  .chart-grid { grid-template-columns: 1fr; }
}
</style>
