<template>
  <div class="splash" :class="{ 'fade-out': fading }" aria-label="系统加载中">
    <!-- 霓虹粒子 Canvas -->
    <canvas ref="canvasRef"></canvas>
    <!-- 透视霓虹网格地面 -->
    <div class="grid-floor"></div>
    <!-- 地面霓虹反光带 -->
    <div class="ground-glow"></div>
    <!-- 扫描线 -->
    <div class="scanlines"></div>

    <!-- 赛博超跑动画 -->
    <div class="car-stage">
      <div class="car-run">
        <!-- 速度线 -->
        <div class="speedline sl1"></div>
        <div class="speedline sl2"></div>
        <div class="speedline sl3"></div>
        <div class="speedline sl4"></div>
        <div class="speedline sl5"></div>
        <!-- 残影拖尾 -->
        <div class="car-ghost" v-for="g in 4" :key="g" :style="{ '--delay': (g * 0.08) + 's' }">
          <svg viewBox="0 0 280 110" fill="none" xmlns="http://www.w3.org/2000/svg" class="car-svg">
            <path d="M8 78 C18 66 40 58 62 52 L88 44 C104 38 124 36 148 40 L196 52 C226 60 250 68 264 78 C272 84 270 92 258 92 L24 92 C10 92 4 86 8 78 Z"
                  stroke="#00f0ff" stroke-width="2.5" fill="#0a1630" fill-opacity="0.35" />
            <path d="M96 46 C102 34 118 30 136 32 L158 38 C142 42 130 48 122 52 Z" stroke="#ff2d95" stroke-width="2" />
            <path d="M98 46 C104 36 118 33 134 34 L150 38 C136 42 126 47 118 51 Z" fill="#7b2dff" fill-opacity="0.55" />
            <path d="M20 58 L12 40 L38 46 L36 56 Z" stroke="#ff2d95" stroke-width="2" />
            <path d="M258 66 L272 72 L272 80 L258 84 Z" fill="#00f0ff" />
            <path d="M12 70 L4 74 L8 80 L16 76 Z" fill="#ff2d95" />
            <line x1="28" y1="92" x2="252" y2="92" stroke="#00f0ff" stroke-width="3" />
            <circle cx="78" cy="92" r="17" stroke="#00f0ff" stroke-width="3" fill="#060a16" />
            <circle cx="78" cy="92" r="7" fill="#00f0ff" />
            <circle cx="202" cy="92" r="17" stroke="#00f0ff" stroke-width="3" fill="#060a16" />
            <circle cx="202" cy="92" r="7" fill="#00f0ff" />
          </svg>
        </div>
        <!-- 主跑车 -->
        <div class="car-main">
          <svg viewBox="0 0 280 110" fill="none" xmlns="http://www.w3.org/2000/svg" class="car-svg">
            <defs>
              <linearGradient id="bodyGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#0a1630" />
                <stop offset="100%" stop-color="#1a0a2e" />
              </linearGradient>
            </defs>
            <path d="M8 78 C18 66 40 58 62 52 L88 44 C104 38 124 36 148 40 L196 52 C226 60 250 68 264 78 C272 84 270 92 258 92 L24 92 C10 92 4 86 8 78 Z"
                  stroke="#00f0ff" stroke-width="3" fill="url(#bodyGrad)" fill-opacity="0.5" />
            <path d="M96 46 C102 34 118 30 136 32 L158 38 C142 42 130 48 122 52 Z" stroke="#ff2d95" stroke-width="2.5" />
            <path d="M98 46 C104 36 118 33 134 34 L150 38 C136 42 126 47 118 51 Z" fill="#7b2dff" fill-opacity="0.6" />
            <path d="M20 58 L12 40 L38 46 L36 56 Z" stroke="#ff2d95" stroke-width="2.5" />
            <path d="M258 64 L276 70 L276 80 L258 85 Z" fill="#00f0ff" />
            <circle cx="270" cy="76" r="4" fill="#00f0ff" opacity="0.9" />
            <path d="M12 70 L2 74 L6 80 L16 76 Z" fill="#ff2d95" />
            <line x1="28" y1="92" x2="252" y2="92" stroke="#00f0ff" stroke-width="3.5" />
            <circle cx="78" cy="92" r="18" stroke="#00f0ff" stroke-width="3" fill="#060a16" />
            <circle cx="78" cy="92" r="8" fill="#00f0ff" />
            <circle cx="202" cy="92" r="18" stroke="#00f0ff" stroke-width="3" fill="#060a16" />
            <circle cx="202" cy="92" r="8" fill="#00f0ff" />
          </svg>
          <!-- 车头霓虹光束 -->
          <div class="head-beam"></div>
        </div>
      </div>
    </div>

    <!-- 霓虹标题 -->
    <div class="title-block">
      <h1 class="glitch" data-text="在线文本情感分析系统">在线文本情感分析系统</h1>
      <p class="sub">SENTIMENT ANALYSIS SYSTEM</p>
      <div class="title-rule"><span></span></div>
    </div>

    <!-- 霓虹进度条 -->
    <div class="bar">
      <div class="bar-fill" :style="{ width: progress + '%' }"></div>
      <div class="bar-glow" :style="{ left: progress + '%' }"></div>
    </div>
    <p class="tip">SYSTEM BOOTING <span class="dot d1">.</span><span class="dot d2">.</span><span class="dot d3">.</span></p>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

const emit = defineEmits(['done'])
const canvasRef = ref(null)
const progress = ref(0)
const fading = ref(false)

let ctx = null
let raf = null
let raf2 = null
let particles = []
let width = 0
let height = 0
let startTime = 0
const DURATION = 2600 // 进度条时长 ms
// 赛博霓虹色板
const NEON = ['#00f0ff', '#ff2d95', '#7b2dff', '#ffe600', '#00f0ff', '#ff2d95']

function resize() {
  const c = canvasRef.value
  if (!c) return
  width = window.innerWidth
  height = window.innerHeight
  const dpr = Math.min(window.devicePixelRatio || 1, 2)
  c.width = width * dpr
  c.height = height * dpr
  c.style.width = width + 'px'
  c.style.height = height + 'px'
  ctx = c.getContext('2d')
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
  initParticles()
}

function initParticles() {
  const cx = width / 2
  const cy = height / 2
  const count = Math.min(170, Math.floor((width * height) / 10000))
  particles = []
  for (let i = 0; i < count; i++) {
    const angle = Math.random() * Math.PI * 2
    const radius = 90 + Math.random() * 150
    particles.push({
      x: Math.random() * width,
      y: Math.random() * height,
      targetX: cx + Math.cos(angle) * radius,
      targetY: cy + Math.sin(angle) * radius * 0.7,
      ease: 0.02 + Math.random() * 0.025,
      r: Math.random() * 2.2 + 0.8,
      color: NEON[Math.floor(Math.random() * NEON.length)],
      alpha: Math.random() * 0.6 + 0.35,
      orbit: Math.random() * Math.PI * 2,
      orbitSpeed: (Math.random() - 0.5) * 0.03
    })
  }
}

function draw() {
  if (!ctx) return
  ctx.clearRect(0, 0, width, height)
  const cx = width / 2
  const cy = height / 2

  for (const p of particles) {
    p.x += (p.targetX - p.x) * p.ease
    p.y += (p.targetY - p.y) * p.ease
    p.orbit += p.orbitSpeed
    p.x += Math.cos(p.orbit) * 0.4
    p.y += Math.sin(p.orbit) * 0.4

    ctx.globalAlpha = Math.min(1, p.alpha)
    ctx.beginPath()
    ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2)
    ctx.fillStyle = p.color
    ctx.fill()
    ctx.globalAlpha = Math.min(0.5, p.alpha * 0.45)
    ctx.beginPath()
    ctx.arc(p.x, p.y, p.r * 2.8, 0, Math.PI * 2)
    ctx.fillStyle = p.color
    ctx.fill()
  }
  ctx.globalAlpha = 1

  // 中心霓虹呼吸光晕
  const t = performance.now() / 1000
  const pulse = 0.45 + 0.3 * Math.sin(t * 3)
  const grad = ctx.createRadialGradient(cx, cy, 0, cx, cy, 150)
  grad.addColorStop(0, `rgba(0, 240, 255, ${0.16 * pulse})`)
  grad.addColorStop(0.5, `rgba(255, 45, 149, ${0.08 * pulse})`)
  grad.addColorStop(1, 'rgba(0, 240, 255, 0)')
  ctx.fillStyle = grad
  ctx.beginPath()
  ctx.arc(cx, cy, 150, 0, Math.PI * 2)
  ctx.fill()

  raf = requestAnimationFrame(draw)
}

function runProgress() {
  startTime = performance.now()
  const tick = (now) => {
    const elapsed = now - startTime
    progress.value = Math.min(100, (elapsed / DURATION) * 100)
    if (elapsed >= DURATION) {
      setTimeout(() => {
        fading.value = true
        setTimeout(() => emit('done'), 700)
      }, 300)
      return
    }
    raf2 = requestAnimationFrame(tick)
  }
  raf2 = requestAnimationFrame(tick)
}

onMounted(() => {
  resize()
  window.addEventListener('resize', resize)
  raf = requestAnimationFrame(draw)
  runProgress()
})

onBeforeUnmount(() => {
  cancelAnimationFrame(raf)
  cancelAnimationFrame(raf2)
  window.removeEventListener('resize', resize)
})
</script>

<style scoped>
.splash {
  position: fixed;
  inset: 0;
  z-index: 9999;
  overflow: hidden;
  background: radial-gradient(ellipse at 50% 30%, #0b1030 0%, #06081a 60%, #03040d 100%);
  transition: opacity 0.7s ease;
}
.splash.fade-out { opacity: 0; pointer-events: none; }

canvas { display: block; position: absolute; inset: 0; }

/* ===== 霓虹网格地面 ===== */
.grid-floor {
  position: absolute;
  left: -60%;
  right: -60%;
  bottom: -8%;
  height: 46%;
  background-image:
    linear-gradient(rgba(0, 240, 255, 0.30) 1.5px, transparent 1.5px),
    linear-gradient(90deg, rgba(0, 240, 255, 0.30) 1.5px, transparent 1.5px);
  background-size: 46px 46px;
  transform: perspective(480px) rotateX(62deg);
  transform-origin: top center;
  animation: gridMove 0.45s linear infinite;
  mask-image: linear-gradient(to bottom, transparent 0%, #000 25%, #000 100%);
  -webkit-mask-image: linear-gradient(to bottom, transparent 0%, #000 25%, #000 100%);
}
@keyframes gridMove {
  from { background-position: 0 0; }
  to { background-position: 0 46px; }
}

/* ===== 地面霓虹反光 ===== */
.ground-glow {
  position: absolute;
  left: 0; right: 0;
  bottom: 0;
  height: 22%;
  background: linear-gradient(to top, rgba(0, 240, 255, 0.14), rgba(255, 45, 149, 0.06) 55%, transparent);
  filter: blur(12px);
}

/* ===== 扫描线 ===== */
.scanlines {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: repeating-linear-gradient(
    to bottom,
    rgba(0, 0, 0, 0) 0px,
    rgba(0, 0, 0, 0) 3px,
    rgba(0, 240, 255, 0.035) 3px,
    rgba(0, 240, 255, 0.035) 4px
  );
  mix-blend-mode: overlay;
  animation: scanMove 0.35s linear infinite;
}
@keyframes scanMove {
  from { transform: translateY(0); }
  to { transform: translateY(4px); }
}

/* ===== 跑车舞台 ===== */
.car-stage {
  position: absolute;
  left: 0; right: 0;
  bottom: 8%;
  height: 150px;
  overflow: visible;
  z-index: 3;
}
.car-run {
  position: absolute;
  top: 50%;
  left: 0;
  width: 280px;
  transform: translateY(-50%);
  animation: carRace 3s cubic-bezier(0.22, 0.61, 0.36, 1) infinite;
}
@keyframes carRace {
  0%   { left: -32%; }
  100% { left: 108%; }
}

/* 跑车主车 */
.car-main { position: relative; }
.car-svg {
  width: 280px;
  height: auto;
  filter: drop-shadow(0 0 10px rgba(0, 240, 255, 0.65))
          drop-shadow(0 0 26px rgba(0, 240, 255, 0.35));
}
.car-main .car-svg {
  filter: drop-shadow(0 0 12px rgba(0, 240, 255, 0.85))
          drop-shadow(0 0 34px rgba(255, 45, 149, 0.45));
  animation: carBounce 0.14s ease-in-out infinite;
}
@keyframes carBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-2.5px); }
}

/* 车头光束 */
.head-beam {
  position: absolute;
  right: 0;
  top: 52px;
  width: 130px;
  height: 18px;
  background: linear-gradient(to left, rgba(0, 240, 255, 0.85), rgba(0, 240, 255, 0));
  filter: blur(6px);
  transform-origin: right center;
  animation: beamPulse 0.2s ease-in-out infinite;
}
@keyframes beamPulse {
  0%, 100% { opacity: 0.85; }
  50% { opacity: 1; }
}

/* 残影拖尾 */
.car-ghost {
  position: absolute;
  top: 0; left: 0;
  opacity: 0;
  animation: ghostFade 3s linear infinite;
  animation-delay: var(--delay);
}
.car-ghost .car-svg {
  filter: drop-shadow(0 0 8px rgba(0, 240, 255, 0.5));
  opacity: 0.3;
  transform: scale(0.96);
}
@keyframes ghostFade {
  0%   { opacity: 0; transform: translateX(-60px) scale(0.9); }
  18%  { opacity: 0.55; }
  38%  { opacity: 0; }
  100% { opacity: 0; transform: translateX(-60px) scale(0.9); }
}

/* 速度线 */
.speedline {
  position: absolute;
  height: 2px;
  background: linear-gradient(to right, transparent, rgba(0, 240, 255, 0.9), transparent);
  border-radius: 2px;
  opacity: 0;
  animation: speedFlash 0.22s linear infinite;
}
.sl1 { width: 170px; top: -38px; left: 8%; animation-delay: 0s; }
.sl2 { width: 120px; top: 44px; left: 82%; animation-delay: 0.05s; }
.sl3 { width: 200px; top: 92px; left: 30%; animation-delay: 0.1s; }
.sl4 { width: 90px; top: -20px; left: 62%; animation-delay: 0.16s; }
.sl5 { width: 140px; top: 70px; left: 50%; animation-delay: 0.21s; }
@keyframes speedFlash {
  0% { opacity: 0; transform: translateX(-10px); }
  30% { opacity: 1; }
  100% { opacity: 0; transform: translateX(14px); }
}

/* ===== 霓虹标题 ===== */
.title-block {
  position: absolute;
  top: 13%;
  left: 0; right: 0;
  text-align: center;
  z-index: 4;
  padding: 0 24px;
}
.glitch {
  position: relative;
  margin: 0;
  font-size: 40px;
  font-weight: 800;
  letter-spacing: 5px;
  color: #fff;
  text-shadow: 0 0 14px rgba(0, 240, 255, 0.75), 0 0 40px rgba(0, 240, 255, 0.4);
  animation: titleIn 0.5s ease both;
}
.glitch::before,
.glitch::after {
  content: attr(data-text);
  position: absolute;
  left: 0; top: 0;
  width: 100%;
  overflow: hidden;
}
.glitch::before {
  color: #ff2d95;
  text-shadow: 0 0 12px rgba(255, 45, 149, 0.8);
  clip-path: polygon(0 0, 100% 0, 100% 34%, 0 34%);
  transform: translateX(-3px);
  animation: glitchTop 1.6s infinite linear alternate-reverse;
}
.glitch::after {
  color: #00f0ff;
  text-shadow: 0 0 12px rgba(0, 240, 255, 0.8);
  clip-path: polygon(0 62%, 100% 62%, 100% 100%, 0 100%);
  transform: translateX(3px);
  animation: glitchBot 1.8s infinite linear alternate-reverse;
}
@keyframes glitchTop {
  0%, 86% { opacity: 0; transform: translateX(-3px); }
  88% { opacity: 1; transform: translateX(-6px); }
  92% { opacity: 0; transform: translateX(2px); }
  96% { opacity: 1; transform: translateX(-4px); }
  100% { opacity: 0; }
}
@keyframes glitchBot {
  0%, 86% { opacity: 0; transform: translateX(3px); }
  88% { opacity: 1; transform: translateX(6px); }
  92% { opacity: 0; transform: translateX(-2px); }
  96% { opacity: 1; transform: translateX(4px); }
  100% { opacity: 0; }
}
@keyframes titleIn {
  from { opacity: 0; transform: translateY(-16px) scale(0.94); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.sub {
  margin: 14px 0 0;
  font-size: 13px;
  letter-spacing: 6px;
  color: rgba(0, 240, 255, 0.7);
  text-shadow: 0 0 10px rgba(0, 240, 255, 0.5);
  animation: subIn 0.7s ease 0.25s both;
}
@keyframes subIn {
  from { opacity: 0; letter-spacing: 16px; }
  to { opacity: 1; letter-spacing: 6px; }
}

.title-rule {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 18px auto 0;
  width: 300px;
  max-width: 70vw;
}
.title-rule::before,
.title-rule::after {
  content: '';
  flex: 1;
  height: 1px;
  background: linear-gradient(to right, transparent, rgba(255, 45, 149, 0.7));
}
.title-rule::after {
  background: linear-gradient(to left, transparent, rgba(0, 240, 255, 0.7));
}
.title-rule span {
  width: 8px; height: 8px;
  transform: rotate(45deg);
  background: #ffe600;
  box-shadow: 0 0 10px rgba(255, 230, 0, 0.9);
}

/* ===== 霓虹进度条 ===== */
.bar {
  position: absolute;
  bottom: 16%;
  left: 50%;
  transform: translateX(-50%);
  width: 360px;
  max-width: 76vw;
  height: 6px;
  border-radius: 3px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(0, 240, 255, 0.3);
  overflow: visible;
  z-index: 4;
}
.bar-fill {
  height: 100%;
  border-radius: 3px;
  background: linear-gradient(90deg, #7b2dff, #00f0ff, #ffe600);
  box-shadow: 0 0 14px rgba(0, 240, 255, 0.9), 0 0 30px rgba(123, 45, 255, 0.6);
  transition: width 0.1s linear;
}
.bar-glow {
  position: absolute;
  top: -5px;
  width: 4px;
  height: 16px;
  background: #fff;
  border-radius: 2px;
  box-shadow: 0 0 14px #00f0ff, 0 0 26px #00f0ff;
  transition: left 0.1s linear;
}

.tip {
  position: absolute;
  bottom: 10%;
  left: 0; right: 0;
  margin: 0;
  text-align: center;
  font-size: 11px;
  letter-spacing: 3px;
  color: rgba(0, 240, 255, 0.55);
  z-index: 4;
}
.dot { animation: blink 0.5s infinite; }
.d2 { animation-delay: 0.15s; }
.d3 { animation-delay: 0.3s; }
@keyframes blink { 0%, 100% { opacity: 0.2; } 50% { opacity: 1; } }

/* ===== 移动端 ===== */
@media (max-width: 640px) {
  .glitch { font-size: 24px; letter-spacing: 2px; }
  .car-svg { width: 220px; }
  .car-run { width: 220px; }
  .car-stage { height: 120px; }
  .title-rule { width: 220px; }
}
</style>
