<template>
  <div class="analyze-page">
    <!-- 页面标题 -->
    <div class="cyber-page-head">
      <div>
        <div class="cyber-page-title">情感分析引擎</div>
        <div class="cyber-page-sub">SENTIMENT ANALYSIS ENGINE · 词典引擎实时判定</div>
      </div>
      <span class="page-status">
        <span class="page-status-dot"></span> ENGINE READY
      </span>
    </div>
    <!-- 分屏控制台 -->
    <div class="console-grid">
      <!-- 左：输入终端 -->
      <div class="cyber-panel input-panel">
        <div class="panel-head">
          <div class="head-icon">
            <el-icon><EditPen /></el-icon>
          </div>
          <div class="head-text">
            <div class="head-title">文本输入终端</div>
            <div class="head-sub">INPUT TERMINAL · 输入中文文本以判定情感倾向</div>
          </div>
          <span class="panel-tag">INPUT</span>
        </div>
        <div class="term-body">
          <div class="term-line">
            <span class="term-prompt">&gt;</span>
            <span class="term-label">TEXT_BUFFER</span>
          </div>
          <el-input v-model="text" type="textarea" :rows="6" maxlength="2000" show-word-limit
                    resize="none" class="text-input"
                    placeholder="请输入要分析的中文文本，例如：这个商品质量非常好，强烈推荐！" />
          <div class="term-scanline"></div>
        </div>
        <div class="samples">
          <span class="samples-label">// 试试这些样本</span>
          <el-tag v-for="s in samples" :key="s" class="sample-tag" @click="text = s">
            {{ s }}
          </el-tag>
        </div>
        <div class="actions">
          <el-button type="primary" size="large" class="analyze-btn" :loading="loading" @click="handleAnalyze">
            <el-icon style="margin-right:6px;"><MagicStick /></el-icon>开始分析
          </el-button>
          <el-button size="large" @click="reset">清空</el-button>
          <span class="engine-readout">
            <span class="er-label">ENGINE</span>
            <span class="er-val">词典法 v1.0</span>
          </span>
        </div>
      </div>
      <!-- 右：结果全息投影台 -->
      <div class="holo-panel" :class="{ 'has-result': !!result }">
        <div v-if="result" class="result-body" :class="'r-' + result.sentimentType">
          <div class="holo-top">
            <span class="holo-tag">RESULT</span>
            <span class="holo-id">#{{ resultId }}</span>
          </div>
          <div class="result-icon-ring">
            <svg v-if="result.sentimentType === 1" viewBox="0 0 48 48" width="52" height="52" fill="none">
              <path d="M6 26l8-11 8 0 0-6c0-4 2-5 6-5l4 0 2 5 8 5 2 0c2 0 4 2 4 4l0 10-12 0-3 9c-1 2-3 4-6 4l-9 0c-2 0-4-2-4-4l0-11z" fill="#00e6a6"/>
            </svg>
            <svg v-else-if="result.sentimentType === -1" viewBox="0 0 48 48" width="52" height="52" fill="none">
              <path d="M6 22l8 11 8 0 0 6c0 4 2 5 6 5l4 0 2-5 8-5 2 0c2 0 4-2 4-4l0-10-12 0-3-9c-1-2-3-4-6-4l-9 0c-2 0-4 2-4 4l0 11z" fill="#ff3d6e"/>
            </svg>
            <svg v-else viewBox="0 0 48 48" width="52" height="52" fill="none">
              <rect x="6" y="12" width="36" height="24" rx="8" fill="#ffc94d"/>
              <circle cx="17" cy="24" r="3" fill="#0a0f2a"/>
              <circle cx="31" cy="24" r="3" fill="#0a0f2a"/>
            </svg>
          </div>
          <div class="result-label" :style="{ color: metaColor, textShadow: '0 0 24px ' + metaColor }">
            {{ result.sentimentLabel }}
          </div>
          <div class="result-meta">
            <span class="meta-item"><i>得分</i><b>{{ result.score }}</b></span>
            <span class="meta-item"><i>置信度</i><b>{{ Math.round(result.confidence * 100) }}%</b></span>
            <span class="engine-badge">{{ result.engineType === 'dictionary' ? '词典引擎' : result.engineType }}</span>
          </div>
          <div class="score-bar">
            <div class="score-fill" :style="{ width: barWidth + '%', background: barColor, boxShadow: '0 0 14px ' + barColor }"></div>
          </div>
          <div v-if="result.keywords && result.keywords.length" class="keywords">
            <span class="kw-label">命中情感词</span>
            <el-tag v-for="k in result.keywords" :key="k" size="small" class="kw-tag" effect="light">
              {{ k }}
            </el-tag>
          </div>
          <div class="result-text">“{{ result.text }}”</div>
        </div>
        <!-- 空状态：全息占位 -->
        <div v-else class="empty">
          <div class="empty-cross">
            <span class="cross-h"></span>
            <span class="cross-v"></span>
            <el-icon class="empty-icon"><ChatLineSquare /></el-icon>
          </div>
          <p class="empty-main">AWAITING INPUT</p>
          <p class="empty-sub">在左侧输入文本，点击「开始分析」，全息结果将在此投影</p>
          <div class="empty-scan"></div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
const text = ref('')
const loading = ref(false)
const result = ref(null)
const resultId = computed(() => result.value ? String(Date.now()).slice(-6) : '----')
const samples = [
  '这个商品质量非常好，物流也很快，强烈推荐！',
  '服务态度太差了，等了很久没人理，垃圾体验。',
  '东西一般吧，说不上好也说不上坏。'
]
const meta = {
  1: { color: '#00e6a6', border: 'rgba(0,230,166,0.45)' },
  0: { color: '#ffc94d', border: 'rgba(255,201,77,0.45)' },
  '-1': { color: '#ff3d6e', border: 'rgba(255,61,110,0.45)' }
}
const metaColor = computed(() => (meta[result.value ? result.value.sentimentType : 0] || meta[0]).color)
const barWidth = computed(() => {
  if (!result.value) return 0
  return Math.min(100, Math.abs(result.value.score) * 30)
})
const barColor = computed(() => meta[result.value.sentimentType]?.color || meta[0].color)
async function handleAnalyze() {
  if (!text.value.trim()) {
    ElMessage.warning('请输入要分析的文本')
    return
  }
  loading.value = true
  try {
    const res = await request.post('/analysis/analyze', { content: text.value })
    if (res.code === 200) {
      result.value = res.data
    } else {
      ElMessage.error(res.message)
    }
  } finally {
    loading.value = false
  }
}
function reset() {
  text.value = ''
  result.value = null
}
</script>
<style scoped>
.analyze-page { max-width: 1160px; margin: 0 auto; }
.cyber-page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.page-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: Consolas, monospace;
  font-size: 11px;
  letter-spacing: 2px;
  color: rgba(0, 230, 166, 0.85);
  border: 1px solid rgba(0, 230, 166, 0.3);
  border-radius: 20px;
  padding: 5px 14px;
  background: rgba(0, 230, 166, 0.06);
}
.page-status-dot {
  width: 7px; height: 7px; border-radius: 50%;
  background: #00e6a6;
  box-shadow: 0 0 8px #00e6a6;
  animation: dotPulse 1.5s ease-in-out infinite;
}
@keyframes dotPulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.3; } }
/* ===== 分屏 ===== */
.console-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 22px;
  align-items: start;
}
/* ===== 左：输入面板 ===== */
.input-panel { padding: 22px 24px; position: relative; }
.panel-head { display: flex; align-items: center; gap: 14px; margin-bottom: 16px; }
.head-icon {
  width: 46px; height: 46px; border-radius: 13px;
  background: linear-gradient(135deg, #00c2d6, #7b2dff);
  color: #fff; font-size: 22px;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 0 18px rgba(0, 240, 255, 0.45);
  animation: headGlow 2.6s ease-in-out infinite;
}
@keyframes headGlow {
  0%, 100% { box-shadow: 0 0 18px rgba(0, 240, 255, 0.45); }
  50% { box-shadow: 0 0 30px rgba(0, 240, 255, 0.75); }
}
.head-text { flex: 1; }
.head-title { font-size: 17px; font-weight: 700; color: #d9f8ff; letter-spacing: 0.5px; }
.head-sub { font-size: 12px; color: var(--cyber-text-dim); margin-top: 3px; font-family: Consolas, monospace; letter-spacing: 1px; }
.panel-tag {
  font-size: 10px;
  letter-spacing: 3px;
  color: #00f0ff;
  border: 1px solid rgba(0, 240, 255, 0.4);
  border-radius: 20px;
  padding: 4px 12px;
  box-shadow: 0 0 10px rgba(0, 240, 255, 0.25);
}
.term-body { position: relative; }
.term-line {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-family: Consolas, monospace;
  font-size: 11px;
  letter-spacing: 1px;
}
.term-prompt { color: #00f0ff; text-shadow: 0 0 8px rgba(0, 240, 255, 0.7); }
.term-label { color: rgba(0, 240, 255, 0.55); }
.text-input :deep(.el-textarea__inner) {
  font-size: 14.5px;
  line-height: 1.7;
  padding: 14px 16px;
  font-family: Consolas, 'PingFang SC', monospace;
}
.term-scanline {
  position: absolute;
  left: 0; right: 0; bottom: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, #00f0ff, transparent);
  opacity: 0.7;
  animation: termScan 2.4s ease-in-out infinite;
}
@keyframes termScan {
  0%, 100% { transform: translateX(-100%); opacity: 0; }
  50% { opacity: 0.8; }
  0%, 100% { opacity: 0; }
}
.samples { margin: 14px 0; display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.samples-label { font-size: 12px; color: var(--cyber-text-dim); font-family: Consolas, monospace; }
.sample-tag { cursor: pointer; }
.sample-tag:hover { opacity: 0.85; box-shadow: 0 0 10px rgba(0, 240, 255, 0.3); }
.actions { margin-top: 8px; display: flex; gap: 12px; align-items: center; }
.analyze-btn {
  border-radius: 10px;
  padding: 0 30px;
  letter-spacing: 2px;
  font-weight: 600;
}
.engine-readout {
  margin-left: auto;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  font-family: Consolas, monospace;
}
.er-label { font-size: 9px; letter-spacing: 2px; color: rgba(127, 180, 201, 0.5); }
.er-val { font-size: 12px; color: #00f0ff; text-shadow: 0 0 8px rgba(0, 240, 255, 0.5); }
/* ===== 右：全息投影台 ===== */
.holo-panel {
  position: relative;
  min-height: 480px;
  border-radius: 16px;
  border: 1px dashed rgba(0, 240, 255, 0.35);
  background: linear-gradient(160deg, rgba(8, 14, 34, 0.7), rgba(12, 18, 44, 0.55));
  backdrop-filter: blur(12px);
  box-shadow: inset 0 0 40px rgba(0, 240, 255, 0.05);
  overflow: hidden;
}
.holo-panel::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(0, 240, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 240, 255, 0.05) 1px, transparent 1px);
  background-size: 26px 26px;
  pointer-events: none;
}
.holo-panel.has-result {
  border-style: solid;
  border-color: var(--result-border, rgba(0, 240, 255, 0.4));
}
.result-body {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px 26px;
  min-height: 478px;
}
.holo-top {
  width: 100%;
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  font-family: Consolas, monospace;
  font-size: 10px;
  letter-spacing: 2px;
}
.holo-tag {
  color: #00f0ff;
  border: 1px solid rgba(0, 240, 255, 0.4);
  padding: 3px 10px;
  border-radius: 4px;
  box-shadow: 0 0 10px rgba(0, 240, 255, 0.25);
}
.holo-id { color: rgba(127, 180, 201, 0.6); }
.r-1 { --result-border: rgba(0, 230, 166, 0.5); }
.r-0 { --result-border: rgba(255, 201, 77, 0.5); }
.r--1 { --result-border: rgba(255, 61, 110, 0.5); }
.result-icon-ring {
  width: 96px; height: 96px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 50%;
  position: relative;
  background: rgba(5, 8, 20, 0.85);
}
.result-icon-ring::before {
  content: '';
  position: absolute; inset: -9px;
  border-radius: 50%;
  border: 2px dashed currentColor;
  opacity: 0.5;
  animation: spin 14s linear infinite;
}
.result-icon-ring::after {
  content: '';
  position: absolute; inset: -14px;
  border-radius: 50%;
  border: 1px solid currentColor;
  opacity: 0.2;
  animation: spin 22s linear infinite reverse;
}
@keyframes spin { to { transform: rotate(360deg); } }
.r-1 .result-icon-ring { color: #00e6a6; box-shadow: 0 0 30px rgba(0, 230, 166, 0.45); border: 1px solid rgba(0,230,166,0.6); }
.r-0 .result-icon-ring { color: #ffc94d; box-shadow: 0 0 30px rgba(255, 201, 77, 0.45); border: 1px solid rgba(255,201,77,0.6); }
.r--1 .result-icon-ring { color: #ff3d6e; box-shadow: 0 0 30px rgba(255, 61, 110, 0.45); border: 1px solid rgba(255,61,110,0.6); }
.result-label {
  font-size: 34px;
  font-weight: 800;
  letter-spacing: 4px;
  margin-top: 26px;
}
.result-meta { display: flex; gap: 26px; margin-top: 14px; flex-wrap: wrap; align-items: center; justify-content: center; }
.meta-item { display: flex; align-items: baseline; gap: 6px; font-size: 13px; color: var(--cyber-text-dim); }
.meta-item i { font-style: normal; }
.meta-item b { font-size: 19px; color: #d9f8ff; }
.engine-badge {
  font-size: 11px; letter-spacing: 1px;
  color: #00f0ff;
  background: rgba(0, 240, 255, 0.12);
  border: 1px solid rgba(0, 240, 255, 0.4);
  padding: 3px 12px; border-radius: 20px;
  box-shadow: 0 0 8px rgba(0, 240, 255, 0.25);
}
.score-bar {
  width: 100%;
  height: 10px; border-radius: 5px;
  background: rgba(255, 255, 255, 0.07);
  margin-top: 20px;
  overflow: hidden;
  border: 1px solid rgba(0, 240, 255, 0.15);
}
.score-fill { height: 100%; border-radius: 5px; transition: width 0.7s ease; }
.keywords { margin-top: 16px; display: flex; align-items: center; gap: 8px; flex-wrap: wrap; justify-content: center; }
.kw-label { font-size: 12px; color: var(--cyber-text-dim); }
.result-text {
  margin-top: 16px;
  width: 100%;
  font-size: 13px;
  color: #a9c9d9;
  line-height: 1.8;
  background: rgba(5, 8, 20, 0.6);
  border-left: 2px solid #00f0ff;
  border-radius: 0 8px 8px 0;
  padding: 10px 14px;
}
/* ===== 空状态 ===== */
.empty {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 478px;
  padding: 30px;
  text-align: center;
}
.empty-cross {
  position: relative;
  width: 96px; height: 96px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cross-h, .cross-v {
  position: absolute;
  background: rgba(0, 240, 255, 0.25);
  box-shadow: 0 0 12px rgba(0, 240, 255, 0.2);
}
.cross-h { width: 100%; height: 1px; }
.cross-v { width: 1px; height: 100%; }
.empty-icon {
  font-size: 40px;
  color: rgba(0, 240, 255, 0.6);
  filter: drop-shadow(0 0 14px rgba(0, 240, 255, 0.5));
  animation: floaty 3s ease-in-out infinite;
}
@keyframes floaty {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}
.empty-main {
  margin-top: 22px;
  font-family: Consolas, monospace;
  font-size: 15px;
  letter-spacing: 4px;
  color: #00f0ff;
  text-shadow: 0 0 14px rgba(0, 240, 255, 0.6);
}
.empty-sub {
  margin-top: 10px;
  font-size: 13px;
  color: var(--cyber-text-dim);
  max-width: 260px;
  line-height: 1.8;
}
.empty-scan {
  position: absolute;
  left: 0; right: 0; bottom: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(0, 240, 255, 0.6), transparent);
  animation: emptyScan 3s ease-in-out infinite;
}
@keyframes emptyScan {
  0% { transform: translateY(0); }
  100% { transform: translateY(-478px); }
}
/* ===== 响应式 ===== */
@media (max-width: 1000px) {
  .console-grid { grid-template-columns: 1fr; }
  .holo-panel, .empty, .result-body { min-height: 320px; }
}
</style>
