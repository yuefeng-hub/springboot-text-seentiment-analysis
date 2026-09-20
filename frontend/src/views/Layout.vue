<template>
  <el-container class="layout">
    <ParticleBackground />
    <!-- 侧边栏：赛博导航台 -->
    <el-aside width="232px" class="sidebar">
      <div class="side-top-scan"></div>
      <!-- 品牌区 -->
      <div class="side-brand">
        <div class="brand-logo">
          <svg viewBox="0 0 48 48" width="30" height="30" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="4" y="10" width="36" height="26" rx="7" fill="#00f0ff" fill-opacity="0.15" stroke="#00f0ff" stroke-width="1.6" />
            <path d="M14 36l-5 7 9-6h12" fill="#00f0ff" fill-opacity="0.15" stroke="#00f0ff" stroke-width="1.6" />
            <circle cx="16" cy="23" r="2.6" fill="#00f0ff" />
            <circle cx="24" cy="23" r="2.6" fill="#7b2dff" />
            <circle cx="32" cy="23" r="2.6" fill="#ff2d95" />
          </svg>
          <span class="logo-ring"></span>
        </div>
        <div class="brand-text-wrap">
          <div class="brand-text">情感分析系统</div>
          <div class="brand-en">CYBER · NLP CORE</div>
        </div>
        <span class="brand-pulse"></span>
      </div>
      <!-- 导航 -->
      <div class="menu-label">
        <span class="menu-label-line"></span>
        <span>NAVIGATION</span>
        <span class="menu-label-line"></span>
      </div>
      <el-menu :default-active="$route.path" router class="side-menu">
        <el-menu-item index="/analyze">
          <span class="menu-no">01</span>
          <el-icon><ChatDotRound /></el-icon><span>情感分析</span>
          <span class="menu-arrow">›</span>
        </el-menu-item>
        <el-menu-item index="/quiz">
          <span class="menu-no">02</span>
          <el-icon><Sunny /></el-icon><span>情感自测</span>
          <span class="menu-arrow">›</span>
        </el-menu-item>
        <el-menu-item index="/chat">
          <span class="menu-no">03</span>
          <el-icon><Cpu /></el-icon><span>AI问答</span>
          <span class="menu-arrow">›</span>
        </el-menu-item>
        <el-menu-item index="/history">
          <span class="menu-no">04</span>
          <el-icon><Clock /></el-icon><span>历史记录</span>
          <span class="menu-arrow">›</span>
        </el-menu-item>
        <el-menu-item index="/stats">
          <span class="menu-no">05</span>
          <el-icon><TrendCharts /></el-icon><span>统计分析</span>
          <span class="menu-arrow">›</span>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/dict">
          <span class="menu-no">06</span>
          <el-icon><Notebook /></el-icon><span>词典管理</span>
          <span class="menu-arrow">›</span>
        </el-menu-item>
      </el-menu>
      <!-- 底部 HUD -->
      <div class="side-foot">
        <div class="hud-row">
          <span class="hud-key">在线用户</span>
          <span class="hud-val">{{ onlineUsers }}</span>
        </div>
        <div class="hud-row">
          <span class="hud-key">处理量/分</span>
          <span class="hud-val">{{ throughput }}</span>
        </div>
        <div class="hud-divider"></div>
        <div class="foot-status">
          <span class="status-dot"></span>
          <span class="status-text">SYSTEM ONLINE</span>
        </div>
        <el-tag size="small" round class="role-tag">{{ isAdmin ? '管理员' : '普通用户' }}</el-tag>
      </div>
    </el-aside>
    <!-- 主区域 -->
    <el-container class="right-area">
      <!-- 顶栏：HUD 控制条 -->
      <el-header class="topbar">
        <div class="top-left">
          <div class="title-wrap">
            <span class="title-bar"></span>
            <div class="title-inner">
              <span class="page-title">{{ typedTitle }}</span>
              <span class="type-cursor" :class="{ blink: typing }"></span>
            </div>
            <span class="title-path">{{ titlePath }}</span>
          </div>
        </div>
        <div class="top-right">
          <span class="sys-chip">
            <span class="sys-dot"></span> ENGINE
          </span>
          <span class="clock">{{ clock }}</span>
          <el-dropdown @command="handleCommand">
            <div class="user-chip">
              <el-avatar :size="34" class="avatar">
                <span class="avatar-ring"></span>{{ avatarText }}
              </el-avatar>
              <span class="user-name">{{ nickname }}</span>
              <el-icon class="chevron"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>{{ nickname }} · {{ isAdmin ? '管理员' : '用户' }}</el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <!-- 内容区 -->
      <el-main class="main-area">
        <div class="hud-corner tl"></div>
        <div class="hud-corner tr"></div>
        <div class="hud-corner bl"></div>
        <div class="hud-corner br"></div>
        <router-view v-slot="{ Component }">
          <transition name="page-anim" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>
<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import ParticleBackground from '../components/ParticleBackground.vue'
const router = useRouter()
const route = useRoute()

const user = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('user') || 'null')
  } catch {
    return null
  }
})
const nickname = computed(() => (user.value && (user.value.nickname || user.value.username)) || '用户')
const avatarText = computed(() => nickname.value.slice(0, 1).toUpperCase())
const isAdmin = computed(() => user.value && user.value.role === 'ADMIN')

const titleMap = {
  '/analyze': '情感分析引擎',
  '/quiz': '情感自测系统',
  '/chat': 'AI 智能问答',
  '/history': '历史记录中心',
  '/stats': '统计分析中心',
  '/dict': '词典管理中心'
}
const pathMap = {
  '/analyze': 'SYS / SENTIMENT / ANALYZE',
  '/quiz': 'SYS / SENTIMENT / QUIZ',
  '/chat': 'SYS / SENTIMENT / AI-CHAT',
  '/history': 'SYS / SENTIMENT / HISTORY',
  '/stats': 'SYS / SENTIMENT / STATS',
  '/dict': 'SYS / SENTIMENT / DICT'
}
const fullTitle = computed(() => titleMap[route.path] || (route.meta.title || '系统'))
const titlePath = computed(() => pathMap[route.path] || 'SYS / SENTIMENT')

// 打字机标题
const typedTitle = ref('')
const typing = ref(false)
let typeTimer = null
function typeTitle(text) {
  if (typeTimer) clearInterval(typeTimer)
  typedTitle.value = ''
  typing.value = true
  let i = 0
  typeTimer = setInterval(() => {
    i++
    typedTitle.value = text.slice(0, i)
    if (i >= text.length) {
      clearInterval(typeTimer)
      typeTimer = null
      typing.value = false
    }
  }, 45)
}
watch(() => route.path, (p) => typeTitle(fullTitle.value), { immediate: true })

// 实时时钟
const clock = ref('00:00:00')
let clockTimer = null
function tick() {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  clock.value = `${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}
onMounted(() => {
  tick()
  clockTimer = setInterval(tick, 1000)
  onlineUsers.value = rand(120, 420)
  throughput.value = rand(500, 2400)
  hudTimer = setInterval(() => {
    onlineUsers.value = rand(120, 420)
    throughput.value = rand(500, 2400)
  }, 2200)
})
onBeforeUnmount(() => {
  if (clockTimer) clearInterval(clockTimer)
  if (typeTimer) clearInterval(typeTimer)
  if (hudTimer) clearInterval(hudTimer)
})

// HUD 模拟数据
const onlineUsers = ref(186)
const throughput = ref(1234)
let hudTimer = null
function rand(min, max) { return Math.floor(Math.random() * (max - min + 1)) + min }

function handleCommand(cmd) {
  if (cmd === 'logout') {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}
</script>
<style scoped>
.layout {
  position: relative;
  height: 100vh;
  background: #060a16;
  overflow: hidden;
}
/* ===== 侧边栏 ===== */
.sidebar {
  position: relative;
  z-index: 2;
  background: linear-gradient(180deg, rgba(6, 10, 26, 0.94) 0%, rgba(9, 14, 36, 0.9) 100%);
  border-right: 1px solid rgba(0, 240, 255, 0.28);
  box-shadow: 8px 0 44px rgba(0, 0, 0, 0.55);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.sidebar::after {
  content: '';
  position: absolute;
  top: 0; bottom: 0; right: 0;
  width: 1px;
  background: linear-gradient(180deg, transparent, rgba(0, 240, 255, 0.6), transparent);
  animation: sideEdge 3.5s linear infinite;
  opacity: 0.6;
}
@keyframes sideEdge {
  0% { transform: translateY(-100%); }
  100% { transform: translateY(100%); }
}
/* 顶部扫描光带 */
.side-top-scan {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, #00f0ff, #7b2dff, #ff2d95, transparent);
  animation: scanX 3s linear infinite;
  box-shadow: 0 0 12px rgba(0, 240, 255, 0.8);
}
@keyframes scanX {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}
.side-brand {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 18px 16px;
  border-bottom: 1px solid rgba(0, 240, 255, 0.16);
}
.brand-logo {
  position: relative;
  width: 44px; height: 44px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 12px;
  background: rgba(0, 240, 255, 0.08);
  border: 1px solid rgba(0, 240, 255, 0.4);
  box-shadow: 0 0 18px rgba(0, 240, 255, 0.3);
  animation: logoFloat 3s ease-in-out infinite;
}
@keyframes logoFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
}
.logo-ring {
  position: absolute;
  inset: -5px;
  border-radius: 14px;
  border: 1px dashed rgba(0, 240, 255, 0.5);
  animation: spin 9s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.brand-text {
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 1px;
  background: linear-gradient(90deg, #00f0ff, #7b2dff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
.brand-en {
  font-size: 9px;
  letter-spacing: 3px;
  color: rgba(0, 240, 255, 0.55);
  margin-top: 2px;
}
.brand-pulse {
  position: absolute;
  right: 16px;
  width: 7px; height: 7px;
  border-radius: 50%;
  background: #00e6a6;
  box-shadow: 0 0 10px #00e6a6;
  animation: statusPulse 1.6s ease-in-out infinite;
}
@keyframes statusPulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.25; } }
.menu-label {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 18px 20px 8px;
  font-size: 10px;
  letter-spacing: 3px;
  color: rgba(127, 180, 201, 0.55);
}
.menu-label-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 240, 255, 0.3));
}
.side-menu {
  flex: 1;
  border-right: none;
  background: transparent;
  padding: 4px 12px;
  --el-menu-bg-color: transparent;
  --el-menu-text-color: #9fc6da;
  --el-menu-hover-bg-color: transparent;
  --el-menu-active-color: #00f0ff;
}
.side-menu :deep(.el-menu-item) {
  position: relative;
  height: 48px;
  line-height: 48px;
  margin: 5px 0;
  border-radius: 10px;
  border: 1px solid transparent;
  color: #9fc6da;
  transition: all 0.22s;
  font-size: 14.5px;
  overflow: hidden;
}
.menu-no {
  font-family: Consolas, monospace;
  font-size: 10px;
  color: rgba(127, 180, 201, 0.4);
  margin-right: 10px;
  letter-spacing: 0;
}
.menu-arrow {
  margin-left: auto;
  font-size: 16px;
  color: rgba(0, 240, 255, 0.35);
  opacity: 0;
  transform: translateX(-6px);
  transition: all 0.25s;
}
.side-menu :deep(.el-menu-item:hover) {
  background: rgba(0, 240, 255, 0.08);
  color: #00f0ff;
  border-color: rgba(0, 240, 255, 0.3);
  box-shadow: 0 0 16px rgba(0, 240, 255, 0.12) inset;
}
.side-menu :deep(.el-menu-item:hover) .menu-arrow {
  opacity: 1;
  transform: translateX(0);
}
.side-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(0, 240, 255, 0.2), rgba(123, 45, 255, 0.18));
  color: #00f0ff !important;
  border-color: rgba(0, 240, 255, 0.6);
  box-shadow: 0 0 20px rgba(0, 240, 255, 0.3), 0 0 8px rgba(0, 240, 255, 0.4) inset;
  font-weight: 600;
}
.side-menu :deep(.el-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: -12px; top: 50%;
  transform: translateY(-50%);
  width: 3px; height: 24px;
  background: #00f0ff;
  border-radius: 2px;
  box-shadow: 0 0 10px #00f0ff;
}
.side-menu :deep(.el-menu-item.is-active)::after {
  content: '';
  position: absolute;
  top: 0; bottom: 0;
  width: 60%;
  background: linear-gradient(100deg, transparent, rgba(255, 255, 255, 0.12), transparent);
  animation: menuSweep 2.6s linear infinite;
}
@keyframes menuSweep {
  0% { left: -60%; }
  100% { left: 120%; }
}
.side-menu :deep(.el-menu-item.is-active) .menu-no {
  color: #00f0ff;
  text-shadow: 0 0 8px rgba(0, 240, 255, 0.7);
}
.side-menu :deep(.el-menu-item.is-active) .menu-arrow {
  opacity: 1;
  transform: translateX(0);
  color: #00f0ff;
}
.side-menu :deep(.el-menu-item .el-icon) { font-size: 18px; margin-right: 10px; }
/* 底部 HUD */
.side-foot {
  padding: 12px 18px 16px;
  border-top: 1px solid rgba(0, 240, 255, 0.16);
}
.hud-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 2px 0;
  font-family: Consolas, monospace;
  font-size: 11px;
}
.hud-key { color: rgba(127, 180, 201, 0.6); letter-spacing: 1px; }
.hud-val {
  color: #00f0ff;
  text-shadow: 0 0 8px rgba(0, 240, 255, 0.6);
  transition: all 0.3s;
}
.hud-divider {
  height: 1px;
  margin: 8px 0;
  background: linear-gradient(90deg, transparent, rgba(0, 240, 255, 0.35), transparent);
}
.foot-status { display: flex; align-items: center; gap: 7px; margin-bottom: 8px; }
.status-dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: #00f0ff;
  box-shadow: 0 0 8px #00f0ff;
  animation: statusPulse 1.6s ease-in-out infinite;
}
.status-text { font-size: 10px; letter-spacing: 2px; color: rgba(0, 240, 255, 0.75); }
.role-tag { float: right; }
/* ===== 顶栏 ===== */
.right-area {
  position: relative;
  z-index: 1;
  background: transparent;
  display: flex;
  flex-direction: column;
}
.topbar {
  height: 62px;
  flex: 0 0 62px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(5, 8, 20, 0.78);
  backdrop-filter: blur(14px);
  border-bottom: 1px solid rgba(0, 240, 255, 0.24);
  padding: 0 26px;
}
.top-left { display: flex; align-items: center; }
.title-wrap { display: flex; align-items: center; gap: 12px; }
.title-bar {
  width: 4px; height: 26px;
  border-radius: 2px;
  background: linear-gradient(180deg, #00f0ff, #ff2d95);
  box-shadow: 0 0 12px rgba(0, 240, 255, 0.8);
}
.title-inner { display: flex; align-items: center; }
.page-title {
  font-size: 18px;
  font-weight: 800;
  letter-spacing: 1px;
  background: linear-gradient(90deg, #00f0ff, #7b2dff, #ff2d95);
  background-size: 200% 100%;
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  animation: titleFlow 4s linear infinite;
  text-shadow: 0 0 20px rgba(0, 240, 255, 0.3);
}
@keyframes titleFlow {
  0% { background-position: 0% 0; }
  100% { background-position: 200% 0; }
}
.type-cursor {
  width: 2px; height: 20px;
  background: #00f0ff;
  margin-left: 3px;
  box-shadow: 0 0 8px #00f0ff;
  animation: cursorBlink 0.8s step-end infinite;
}
@keyframes cursorBlink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }
.title-path {
  font-family: Consolas, monospace;
  font-size: 10px;
  letter-spacing: 2px;
  color: rgba(0, 240, 255, 0.45);
  margin-left: 6px;
}
.top-right { display: flex; align-items: center; gap: 18px; }
.sys-chip {
  display: flex;
  align-items: center;
  gap: 7px;
  font-family: Consolas, monospace;
  font-size: 10px;
  letter-spacing: 2px;
  color: rgba(0, 240, 255, 0.6);
  border: 1px solid rgba(0, 240, 255, 0.22);
  border-radius: 20px;
  padding: 4px 12px;
}
.sys-dot {
  width: 6px; height: 6px; border-radius: 50%;
  background: #00f0ff;
  box-shadow: 0 0 8px #00f0ff;
  animation: statusPulse 1.4s ease-in-out infinite;
}
.clock {
  font-family: Consolas, monospace;
  font-size: 16px;
  font-weight: 600;
  color: #00f0ff;
  letter-spacing: 2px;
  text-shadow: 0 0 12px rgba(0, 240, 255, 0.7);
  border-left: 1px solid rgba(0, 240, 255, 0.2);
  padding-left: 18px;
}
.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 22px;
  border: 1px solid rgba(0, 240, 255, 0.28);
  background: rgba(0, 240, 255, 0.06);
  transition: all 0.2s;
}
.user-chip:hover {
  border-color: rgba(0, 240, 255, 0.65);
  box-shadow: 0 0 16px rgba(0, 240, 255, 0.25);
  background: rgba(0, 240, 255, 0.1);
}
.avatar {
  position: relative;
  background: linear-gradient(135deg, #00c2d6, #7b2dff);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 0 12px rgba(0, 240, 255, 0.55);
}
.avatar-ring {
  position: absolute;
  inset: -3px;
  border-radius: 50%;
  border: 1px solid rgba(0, 240, 255, 0.6);
  animation: spin 6s linear infinite;
}
.user-name { font-size: 14px; color: #d9f8ff; }
.chevron { font-size: 12px; color: rgba(0, 240, 255, 0.7); }
/* ===== 内容区 ===== */
.main-area {
  position: relative;
  flex: 1;
  z-index: 1;
  background: transparent;
  padding: 26px 30px;
  overflow-y: auto;
}
/* HUD 边角 */
.hud-corner {
  position: absolute;
  width: 22px; height: 22px;
  border: 2px solid rgba(0, 240, 255, 0.35);
  pointer-events: none;
  z-index: 5;
}
.hud-corner.tl { top: 10px; left: 10px; border-right: none; border-bottom: none; }
.hud-corner.tr { top: 10px; right: 10px; border-left: none; border-bottom: none; }
.hud-corner.bl { bottom: 10px; left: 10px; border-right: none; border-top: none; }
.hud-corner.br { bottom: 10px; right: 10px; border-left: none; border-top: none; }
/* 页面过渡动画 */
.page-anim-enter-active {
  transition: all 0.4s cubic-bezier(0.22, 1, 0.36, 1);
}
.page-anim-leave-active {
  transition: all 0.22s ease;
}
.page-anim-enter-from {
  opacity: 0;
  transform: translateY(16px) scale(0.985);
  filter: blur(4px);
}
.page-anim-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.99);
}
</style>
