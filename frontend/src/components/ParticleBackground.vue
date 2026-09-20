<template>
  <div class="particle-bg" aria-hidden="true">
    <!-- 原图底图（霓虹隧道，Ken Burns 慢呼吸动态化） -->
    <div class="tunnel-img"></div>
    <!-- 上下霓虹呼吸光带 -->
    <div class="neon-band top"></div>
    <div class="neon-band bottom"></div>
    <!-- 动态叠加层：粒子网络 + 流光 + 流星 + 网格 + 光晕 -->
    <canvas ref="canvasRef"></canvas>
    <!-- CRT 扫描线 -->
    <div class="scanlines"></div>
    <!-- 四周暗角 -->
    <div class="vignette"></div>
  </div>
</template>
<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
const canvasRef = ref(null)
let ctx = null
let raf = null
let W = 0
let H = 0
let cx = 0
let cy = 0
let running = true
let particles = []
let streams = []
let meteors = []
let gridPts = []
const t0 = performance.now()
// 霓虹色板
const PAL = {
  cyan: { c: '0,240,255', hex: '#00f0ff' },
  pink: { c: '255,45,149', hex: '#ff2d95' },
  purple: { c: '180,100,255', hex: '#b464ff' },
  gold: { c: '255,230,0', hex: '#ffe600' }
}
const KEYS = ['cyan', 'pink', 'purple', 'cyan', 'pink', 'cyan', 'gold']
function pick() {
  return KEYS[(Math.random() * KEYS.length) | 0]
}
function resize() {
  const c = canvasRef.value
  if (!c) return
  W = window.innerWidth
  H = window.innerHeight
  const dpr = Math.min(window.devicePixelRatio || 1, 2)
  c.width = W * dpr
  c.height = H * dpr
  c.style.width = W + 'px'
  c.style.height = H + 'px'
  ctx = c.getContext('2d')
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
  cx = W / 2
  cy = H / 2
  initParticles()
  initStreams()
  initGrid()
  meteors = []
}
// 漂浮霓虹粒子（带网络连线）
function initParticles() {
  const count = Math.max(42, Math.min(95, Math.floor((W * H) / 20000)))
  particles = []
  for (let i = 0; i < count; i++) {
    particles.push({
      x: Math.random() * W,
      y: Math.random() * H,
      vx: (Math.random() - 0.5) * 0.4,
      vy: (Math.random() - 0.5) * 0.4,
      r: Math.random() * 1.9 + 0.7,
      color: PAL[pick()],
      alpha: Math.random() * 0.5 + 0.25,
      phase: Math.random() * Math.PI * 2,
      speed: Math.random() * 0.02 + 0.006
    })
  }
}
// 隧道流光点
function initStreams() {
  const n = 36
  streams = []
  for (let i = 0; i < n; i++) {
    streams.push(newStream(true))
  }
}
function newStream(random) {
  return {
    t: random ? Math.random() : 0,
    speed: 0.008 + Math.random() * 0.026,
    ang: Math.random() * Math.PI * 2,
    color: PAL[pick()],
    len: 0.06 + Math.random() * 0.09
  }
}
// 动态网格交点
function initGrid() {
  const gs = 64
  gridPts = []
  for (let gx = 0; gx < W; gx += gs) {
    for (let gy = 0; gy < H; gy += gs) {
      gridPts.push({
        x: gx,
        y: gy,
        phase: Math.random() * Math.PI * 2,
        speed: 0.01 + Math.random() * 0.02
      })
    }
  }
}
function spawnMeteor() {
  if (meteors.length >= 3) return
  const fromLeft = Math.random() > 0.5
  meteors.push({
    x: fromLeft ? -60 : Math.random() * W * 0.7,
    y: Math.random() * H * 0.35,
    vx: 5 + Math.random() * 4,
    vy: 3 + Math.random() * 2.5,
    life: 0,
    maxLife: 60 + Math.random() * 30,
    color: PAL[['cyan', 'pink', 'gold'][(Math.random() * 3) | 0]]
  })
}
function draw() {
  if (!ctx) return
  ctx.clearRect(0, 0, W, H)
  const now = performance.now() - t0
  // 中心光晕脉冲（呼吸）
  const pulse = 0.8 + 0.2 * Math.sin(now / 900)
  const glow = ctx.createRadialGradient(cx, cy, 0, cx, cy, Math.max(W, H) * 0.45 * pulse)
  glow.addColorStop(0, 'rgba(0,240,255,0.12)')
  glow.addColorStop(0.4, 'rgba(180,100,255,0.06)')
  glow.addColorStop(1, 'rgba(0,0,0,0)')
  ctx.fillStyle = glow
  ctx.fillRect(0, 0, W, H)
  drawGrid()
  drawSweep()
  drawStreams()
  drawParticles()
  drawMeteors()
  raf = requestAnimationFrame(draw)
}
// 动态网格 + 交点脉冲
function drawGrid() {
  ctx.strokeStyle = 'rgba(0,240,255,0.045)'
  ctx.lineWidth = 1
  const gs = 64
  ctx.beginPath()
  for (let gx = 0; gx <= W; gx += gs) {
    ctx.moveTo(gx, 0)
    ctx.lineTo(gx, H)
  }
  for (let gy = 0; gy <= H; gy += gs) {
    ctx.moveTo(0, gy)
    ctx.lineTo(W, gy)
  }
  ctx.stroke()
  for (const p of gridPts) {
    p.phase += p.speed
    const a = Math.max(0, 0.22 * Math.abs(Math.sin(p.phase)))
    ctx.fillStyle = `rgba(0,240,255,${a})`
    ctx.beginPath()
    ctx.arc(p.x, p.y, 1.2, 0, Math.PI * 2)
    ctx.fill()
  }
}
// 双色霓虹光柱扫描（青 + 紫反向）
function drawSweep() {
  const span = Math.hypot(W, H)
  const angle = Math.atan2(H, W) * 0.55
  const cos = Math.cos(angle)
  const sin = Math.sin(angle)
  // 青色光柱
  const t1 = ((performance.now() - t0) % 9500) / 9500
  const pos1 = t1 * (W + span * 1.2) - span * 0.6
  drawBeam(pos1, cos, sin, '0,240,255', 0.12, 150)
  // 紫色反向光柱
  const t2 = (((performance.now() - t0) % 11000) / 11000 + 0.5) % 1
  const pos2 = t2 * (W + span * 1.2) - span * 0.6
  drawBeam(pos2, -cos, -sin, '255,45,149', 0.09, 120)
}
function drawBeam(pos, cos, sin, rgba, alpha, halfW) {
  const grad = ctx.createLinearGradient(
    cx + (pos - halfW) * cos,
    cy + (pos - halfW) * sin,
    cx + (pos + halfW) * cos,
    cy + (pos + halfW) * sin
  )
  grad.addColorStop(0, `rgba(${rgba},0)`)
  grad.addColorStop(0.5, `rgba(${rgba},${alpha})`)
  grad.addColorStop(1, `rgba(${rgba},0)`)
  ctx.fillStyle = grad
  ctx.fillRect(0, 0, W, H)
}
// 隧道流光
function drawStreams() {
  const maxR = Math.hypot(W, H) * 0.5
  for (const s of streams) {
    s.t += s.speed
    if (s.t >= 1) {
      s.t = 0
      s.ang = Math.random() * Math.PI * 2
      s.color = PAL[pick()]
      s.len = 0.06 + Math.random() * 0.09
    }
    const a = s.t
    const bx = Math.cos(s.ang) * maxR * Math.max(0, a - s.len) + cx
    const by = Math.sin(s.ang) * maxR * Math.max(0, a - s.len) + cy
    const ex = Math.cos(s.ang) * maxR * a + cx
    const ey = Math.sin(s.ang) * maxR * a + cy
    const alpha = Math.max(0, 1 - a) * 0.75
    ctx.strokeStyle = `rgba(${s.color.c},${alpha})`
    ctx.lineWidth = 2
    ctx.lineCap = 'round'
    ctx.shadowColor = s.color.hex
    ctx.shadowBlur = 10
    ctx.beginPath()
    ctx.moveTo(bx, by)
    ctx.lineTo(ex, ey)
    ctx.stroke()
    ctx.shadowBlur = 0
  }
}
// 网络粒子 + 连线
function drawParticles() {
  for (const p of particles) {
    p.x += p.vx
    p.y += p.vy
    p.phase += p.speed
    if (p.x < -10 || p.x > W + 10) p.vx *= -1
    if (p.y < -10 || p.y > H + 10) p.vy *= -1
    const tw = 0.7 + 0.3 * Math.sin(p.phase)
    ctx.fillStyle = `rgba(${p.color.c},${Math.max(0.08, Math.min(0.95, p.alpha * tw))})`
    ctx.shadowColor = p.color.hex
    ctx.shadowBlur = 8
    ctx.beginPath()
    ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2)
    ctx.fill()
    ctx.shadowBlur = 0
  }
  // 粒子网络连线
  ctx.lineWidth = 0.7
  for (let i = 0; i < particles.length; i++) {
    const a = particles[i]
    for (let j = i + 1; j < particles.length; j++) {
      const b = particles[j]
      const dx = a.x - b.x
      const dy = a.y - b.y
      const d2 = dx * dx + dy * dy
      if (d2 < 110 * 110) {
        const alpha = (1 - Math.sqrt(d2) / 110) * 0.22
        ctx.strokeStyle = `rgba(0,240,255,${alpha})`
        ctx.beginPath()
        ctx.moveTo(a.x, a.y)
        ctx.lineTo(b.x, b.y)
        ctx.stroke()
      }
    }
  }
}
// 流星
function drawMeteors() {
  if (Math.random() < 0.008) spawnMeteor()
  for (let i = meteors.length - 1; i >= 0; i--) {
    const m = meteors[i]
    m.life++
    m.x += m.vx
    m.y += m.vy
    if (m.life >= m.maxLife || m.x > W + 80 || m.y > H + 80) {
      meteors.splice(i, 1)
      continue
    }
    const fade = 1 - m.life / m.maxLife
    const tail = 18
    const grad = ctx.createLinearGradient(m.x, m.y, m.x - m.vx * tail * 0.6, m.y - m.vy * tail * 0.6)
    grad.addColorStop(0, `rgba(${m.color.c},${0.85 * fade})`)
    grad.addColorStop(1, `rgba(${m.color.c},0)`)
    ctx.strokeStyle = grad
    ctx.lineWidth = 2.4
    ctx.lineCap = 'round'
    ctx.shadowColor = m.color.hex
    ctx.shadowBlur = 14
    ctx.beginPath()
    ctx.moveTo(m.x, m.y)
    ctx.lineTo(m.x - m.vx * tail * 0.6, m.y - m.vy * tail * 0.6)
    ctx.stroke()
    ctx.shadowBlur = 0
    // 流星头
    ctx.fillStyle = `rgba(${m.color.c},${0.95 * fade})`
    ctx.shadowBlur = 12
    ctx.beginPath()
    ctx.arc(m.x, m.y, 1.8, 0, Math.PI * 2)
    ctx.fill()
    ctx.shadowBlur = 0
  }
}
function onVisibility() {
  running = document.visibilityState === 'visible'
  if (running) {
    cancelAnimationFrame(raf)
    raf = requestAnimationFrame(draw)
  }
}
onMounted(() => {
  resize()
  window.addEventListener('resize', resize)
  document.addEventListener('visibilitychange', onVisibility)
  raf = requestAnimationFrame(draw)
})
onBeforeUnmount(() => {
  cancelAnimationFrame(raf)
  window.removeEventListener('resize', resize)
  document.removeEventListener('visibilitychange', onVisibility)
})
</script>
<style scoped>
.particle-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;
  background: #06081a;
}
/* 原图底图：竖版 cover，上下裁掉水印；Ken Burns 慢呼吸 */
.tunnel-img {
  position: absolute;
  inset: -3%;
  background-image: url('../assets/bg_tunnel.jpg');
  background-position: center center;
  background-size: cover;
  background-repeat: no-repeat;
  animation: kenburns 32s ease-in-out infinite alternate;
}
@keyframes kenburns {
  from { transform: scale(1) translate(0, 0); }
  to { transform: scale(1.06) translate(-0.6%, 0.8%); }
}
canvas {
  display: block;
  position: absolute;
  inset: 0;
}
/* 上下霓虹呼吸光带 */
.neon-band {
  position: absolute;
  left: 0; right: 0;
  height: 90px;
  pointer-events: none;
}
.neon-band.top {
  top: 0;
  background: linear-gradient(180deg, rgba(0, 240, 255, 0.16), rgba(123, 45, 255, 0.05), transparent);
  animation: bandBreathT 5s ease-in-out infinite;
}
.neon-band.bottom {
  bottom: 0;
  background: linear-gradient(0deg, rgba(255, 45, 149, 0.14), rgba(123, 45, 255, 0.04), transparent);
  animation: bandBreathB 6s ease-in-out infinite;
}
@keyframes bandBreathT {
  0%, 100% { opacity: 0.55; }
  50% { opacity: 1; }
}
@keyframes bandBreathB {
  0%, 100% { opacity: 0.45; }
  50% { opacity: 1; }
}
/* CRT 扫描线 */
.scanlines {
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(
    to bottom,
    rgba(0, 0, 0, 0) 0px,
    rgba(0, 0, 0, 0) 3px,
    rgba(0, 0, 0, 0.12) 4px
  );
  mix-blend-mode: multiply;
  opacity: 0.5;
}
.vignette {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: radial-gradient(ellipse at center, transparent 52%, rgba(2, 4, 12, 0.72) 100%);
}
</style>
