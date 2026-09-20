<template>
  <div class="login-page">
    <ParticleBackground />
    <!-- 左侧品牌区 -->
    <div class="brand-panel">
      <div class="brand-scan"></div>
      <div class="brand-inner">
        <div class="logo">
          <svg viewBox="0 0 48 48" width="60" height="60" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="4" y="10" width="36" height="26" rx="7" fill="#00f0ff" fill-opacity="0.12" stroke="#00f0ff" stroke-width="1.6" />
            <path d="M14 36l-5 7 9-6h12" fill="#00f0ff" fill-opacity="0.12" stroke="#00f0ff" stroke-width="1.6" />
            <circle cx="16" cy="23" r="2.6" fill="#00f0ff" />
            <circle cx="24" cy="23" r="2.6" fill="#7b2dff" />
            <circle cx="32" cy="23" r="2.6" fill="#ff2d95" />
          </svg>
        </div>
        <h1 class="brand-title">在线文本情感分析系统</h1>
        <p class="en">SENTIMENT ANALYSIS SYSTEM</p>
        <p class="desc">基于 SpringBoot + Vue3 的中文文本情感分析平台<br />实时判定 · 历史留存 · 可视化统计</p>
      </div>
      <div class="features">
        <div class="feature"><el-icon><CircleCheckFilled /></el-icon><span>情感词典法 · 秒级实时分析</span></div>
        <div class="feature"><el-icon><CircleCheckFilled /></el-icon><span>正向 / 中性 / 负向 多维判定</span></div>
        <div class="feature"><el-icon><CircleCheckFilled /></el-icon><span>ECharts 情感可视化统计</span></div>
        <div class="feature"><el-icon><CircleCheckFilled /></el-icon><span>自定义情感词典 · 实时生效</span></div>
      </div>
      <!-- 实时情感数据流 -->
      <div class="data-stream">
        <div class="stream-head">
          <span class="stream-title">LIVE EMOTION FEED</span>
          <span class="stream-live"><i></i>LIVE</span>
        </div>
        <div class="stream-window">
          <div class="stream-track">
            <div v-for="(row, i) in streamRows" :key="'a' + i" class="stream-row">
              <span class="sr-sent" :class="'s-' + row.type">{{ row.type === 1 ? '正向' : row.type === -1 ? '负向' : '中性' }}</span>
              <span class="sr-score" :class="'s-' + row.type">{{ row.type === 1 ? '+' : '' }}{{ row.score }}</span>
              <span class="sr-text">“{{ row.text }}”</span>
            </div>
            <div v-for="(row, i) in streamRows" :key="'b' + i" class="stream-row">
              <span class="sr-sent" :class="'s-' + row.type">{{ row.type === 1 ? '正向' : row.type === -1 ? '负向' : '中性' }}</span>
              <span class="sr-score" :class="'s-' + row.type">{{ row.type === 1 ? '+' : '' }}{{ row.score }}</span>
              <span class="sr-text">“{{ row.text }}”</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="form-panel">
      <div class="cyber-panel form-card">
        <div class="form-corner-tl"></div>
        <div class="form-corner-br"></div>
        <h2 class="form-title">{{ activeTab === 'login' ? '欢迎回来' : '创建账号' }}</h2>
        <p class="form-sub">{{ activeTab === 'login' ? '登录以继续使用情感分析服务' : '注册一个账号，开始体验' }}</p>
        <el-tabs v-model="activeTab" stretch class="auth-tabs">
          <el-tab-pane label="登录" name="login">
            <el-form :model="loginForm" :rules="loginRules" ref="loginRef" size="large">
              <el-form-item prop="username">
                <el-input v-model="loginForm.username" placeholder="用户名" :prefix-icon="User" />
              </el-form-item>
              <el-form-item prop="password">
                <el-input v-model="loginForm.password" type="password" show-password
                          placeholder="密码" :prefix-icon="Lock" @keyup.enter="handleLogin" />
              </el-form-item>
              <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleLogin">
                登 录
              </el-button>
            </el-form>
          </el-tab-pane>
          <el-tab-pane label="注册" name="register">
            <el-form :model="regForm" :rules="regRules" ref="regRef" size="large">
              <el-form-item prop="username">
                <el-input v-model="regForm.username" placeholder="用户名（3-20位）" :prefix-icon="User" />
              </el-form-item>
              <el-form-item prop="password">
                <el-input v-model="regForm.password" type="password" show-password
                          placeholder="密码（6-32位）" :prefix-icon="Lock" />
              </el-form-item>
              <el-form-item prop="nickname">
                <el-input v-model="regForm.nickname" placeholder="昵称（可选）" :prefix-icon="Postcard" />
              </el-form-item>
              <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleRegister">
                注册并登录
              </el-button>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
      <p class="copyright">毕业设计 · 在线文本情感分析系统</p>
    </div>
  </div>
</template>
<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Postcard } from '@element-plus/icons-vue'
import request from '../utils/request'
import ParticleBackground from '../components/ParticleBackground.vue'
const router = useRouter()
const activeTab = ref('login')
const loading = ref(false)
// 实时情感数据流（模拟展示）
const streamRows = [
  { type: 1, score: 128, text: '这个商品质量非常好，强烈推荐！' },
  { type: -1, score: 76, text: '发货太慢了，客服态度也差。' },
  { type: 0, score: 12, text: '今天天气不错，出门走走。' },
  { type: 1, score: 96, text: '客服小姐姐很有耐心，五星好评。' },
  { type: -1, score: 58, text: '系统老是崩溃，体验糟糕。' },
  { type: 1, score: 82, text: '电影太燃了，全程无尿点！' },
  { type: 0, score: 6, text: '这本书的内容一般般。' },
  { type: 1, score: 141, text: '奖学金终于到账了，开心！' },
  { type: -1, score: 45, text: '食堂涨价了，还不怎么好吃。' },
  { type: 0, score: 20, text: '会议改到下周了。' }
]
const loginForm = reactive({ username: '', password: '' })
const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const regForm = reactive({ username: '', password: '', nickname: '' })
const regRules = {
  username: [{ required: true, min: 3, max: 20, message: '用户名长度需 3-20', trigger: 'blur' }],
  password: [{ required: true, min: 6, max: 32, message: '密码长度需 6-32', trigger: 'blur' }]
}
function saveLogin(data) {
  localStorage.setItem('token', data.token)
  localStorage.setItem('user', JSON.stringify(data.user))
  router.push('/analyze')
}
async function handleLogin() {
  loading.value = true
  try {
    const res = await request.post('/auth/login', loginForm)
    if (res.code === 200) {
      ElMessage.success('登录成功，欢迎回来')
      saveLogin(res.data)
    } else {
      ElMessage.error(res.message)
    }
  } finally {
    loading.value = false
  }
}
async function handleRegister() {
  loading.value = true
  try {
    const res = await request.post('/auth/register', regForm)
    if (res.code === 200) {
      ElMessage.success('注册成功，正在进入系统')
      saveLogin(res.data)
    } else {
      ElMessage.error(res.message)
    }
  } finally {
    loading.value = false
  }
}
</script>
<style scoped>
.login-page {
  position: relative;
  display: flex;
  min-height: 100vh;
  background: #060a16;
}
.brand-panel,
.form-panel {
  position: relative;
  z-index: 1;
}

/* ===== 左侧品牌区 ===== */
.brand-panel {
  flex: 1.15;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 60px 56px;
  color: #fff;
  background: rgba(8, 12, 30, 0.6);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-right: 1px solid rgba(0, 240, 255, 0.22);
}
.brand-scan {
  position: absolute; top: 0; left: 0; right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, #00f0ff, #7b2dff, #ff2d95, transparent);
  animation: scanX 3s linear infinite;
  box-shadow: 0 0 12px rgba(0, 240, 255, 0.8);
}
@keyframes scanX {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}
.brand-panel::before,
.brand-panel::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.28;
}
.brand-panel::before {
  width: 360px; height: 360px;
  background: #00f0ff;
  top: -100px; right: -90px;
}
.brand-panel::after {
  width: 300px; height: 300px;
  background: #7b2dff;
  bottom: -80px; left: -70px;
}
.brand-inner, .features { position: relative; z-index: 1; }
.logo {
  margin-bottom: 18px;
  filter: drop-shadow(0 0 18px rgba(0, 240, 255, 0.5));
}
.brand-title {
  margin: 0;
  font-size: 32px; letter-spacing: 1px;
  background: linear-gradient(90deg, #00f0ff, #7b2dff, #ff2d95);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
.brand-inner .en {
  margin: 8px 0 16px;
  font-size: 13px;
  letter-spacing: 3px;
  color: rgba(0, 240, 255, 0.6);
}
.brand-inner .desc {
  font-size: 15px;
  line-height: 1.9;
  color: rgba(217, 248, 255, 0.85);
}
.features {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.feature {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  color: rgba(217, 248, 255, 0.92);
  background: rgba(0, 240, 255, 0.06);
  border: 1px solid rgba(0, 240, 255, 0.22);
  border-radius: 12px;
  padding: 12px 18px;
  backdrop-filter: blur(4px);
  transition: all 0.2s;
}
.feature:hover {
  border-color: rgba(0, 240, 255, 0.55);
  box-shadow: 0 0 14px rgba(0, 240, 255, 0.15);
  transform: translateX(4px);
}
.feature .el-icon { font-size: 20px; color: #00f0ff; filter: drop-shadow(0 0 6px rgba(0, 240, 255, 0.6)); }

/* ===== 实时情感数据流 ===== */
.data-stream {
  position: relative;
  z-index: 1;
  margin-top: 22px;
  max-width: 420px;
}
.stream-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.stream-title {
  font-family: Consolas, monospace;
  font-size: 10px;
  letter-spacing: 2px;
  color: rgba(0, 240, 255, 0.6);
}
.stream-live {
  display: flex;
  align-items: center;
  gap: 5px;
  font-family: Consolas, monospace;
  font-size: 9px;
  letter-spacing: 2px;
  color: #ff2d95;
}
.stream-live i {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: #ff2d95;
  box-shadow: 0 0 8px #ff2d95;
  animation: statusPulse 1.2s ease-in-out infinite;
}
.stream-window {
  position: relative;
  height: 168px;
  overflow: hidden;
  border: 1px solid rgba(0, 240, 255, 0.22);
  border-radius: 12px;
  background: rgba(3, 6, 18, 0.6);
  backdrop-filter: blur(4px);
}
.stream-window::after {
  content: '';
  position: absolute;
  left: 0; right: 0; top: 0;
  height: 40px;
  background: linear-gradient(180deg, rgba(3, 6, 18, 0.9), transparent);
  pointer-events: none;
  z-index: 2;
}
.stream-window::before {
  content: '';
  position: absolute;
  left: 0; right: 0; bottom: 0;
  height: 40px;
  background: linear-gradient(0deg, rgba(3, 6, 18, 0.9), transparent);
  pointer-events: none;
  z-index: 2;
}
.stream-track {
  animation: streamUp 16s linear infinite;
}
@keyframes streamUp {
  0% { transform: translateY(0); }
  100% { transform: translateY(-50%); }
}
.stream-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  font-size: 12px;
  border-bottom: 1px solid rgba(0, 240, 255, 0.08);
  white-space: nowrap;
  overflow: hidden;
}
.sr-sent {
  flex: 0 0 34px;
  text-align: center;
  font-size: 10px;
  padding: 1px 0;
  border-radius: 4px;
}
.sr-score {
  flex: 0 0 38px;
  font-family: Consolas, monospace;
  font-size: 12px;
  font-weight: 700;
}
.sr-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  color: rgba(217, 248, 255, 0.75);
}
.s-1 { color: #00e6a6; text-shadow: 0 0 8px rgba(0, 230, 166, 0.5); }
.s--1 { color: #ff5c84; text-shadow: 0 0 8px rgba(255, 92, 132, 0.5); }
.s-0 { color: #ffc94d; text-shadow: 0 0 8px rgba(255, 201, 77, 0.5); }
/* ===== 右侧表单区 ===== */
.form-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 32px;
  background: transparent;
}
.form-card {
  width: 400px;
  max-width: 100%;
  position: relative;
  padding: 36px 38px 30px;
  animation: cardEnter 0.7s cubic-bezier(0.22, 1, 0.36, 1);
}
@keyframes cardEnter {
  from { opacity: 0; transform: translateY(26px) scale(0.96); filter: blur(6px); }
  to { opacity: 1; transform: translateY(0) scale(1); filter: blur(0); }
}
/* 登录卡旋转流光边框 */
.form-card::before {
  content: '';
  position: absolute;
  inset: -1px;
  border-radius: 17px;
  padding: 1px;
  background: conic-gradient(from var(--ang, 0deg), #00f0ff, #7b2dff, #ff2d95, #ffe600, #00f0ff);
  -webkit-mask: linear-gradient(#000 0 0) content-box, linear-gradient(#000 0 0);
  -webkit-mask-composite: xor;
  mask: linear-gradient(#000 0 0) content-box, linear-gradient(#000 0 0);
  mask-composite: exclude;
  animation: borderSpin 5s linear infinite;
  pointer-events: none;
}
@property --ang {
  syntax: '<angle>';
  initial-value: 0deg;
  inherits: false;
}
@keyframes borderSpin {
  to { --ang: 360deg; }
}
.form-corner-tl,
.form-corner-br {
  position: absolute;
  width: 26px; height: 26px;
  border: 2px solid #00f0ff;
  filter: drop-shadow(0 0 6px rgba(0, 240, 255, 0.8));
  opacity: 0.85;
}
.form-corner-tl { top: -2px; left: -2px; border-right: none; border-bottom: none; border-radius: 16px 0 0 0; }
.form-corner-br { bottom: -2px; right: -2px; border-left: none; border-top: none; border-radius: 0 0 16px 0; }
.form-title {
  margin: 0; font-size: 24px; color: #d9f8ff;
  text-shadow: 0 0 14px rgba(0, 240, 255, 0.3);
}
.form-sub { margin: 8px 0 20px; font-size: 14px; color: var(--cyber-text-dim); }
.auth-tabs :deep(.el-tabs__item) { font-size: 15px; }
.submit-btn {
  width: 100%;
  margin-top: 6px;
  border-radius: 10px;
  letter-spacing: 2px;
  font-weight: 600;
}
.copyright { margin-top: 26px; font-size: 12px; color: rgba(127, 180, 201, 0.55); letter-spacing: 1px; }

/* ===== 移动端适配 ===== */
@media (max-width: 860px) {
  .login-page { flex-direction: column; }
  .brand-panel { flex: none; padding: 40px 28px; min-height: 300px; }
  .features { display: none; }
  .form-panel { padding: 32px 20px; }
  .form-card { padding: 28px 24px; }
}
</style>
