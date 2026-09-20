<template>
  <div class="quiz-page">
    <!-- 页面标题 -->
    <div class="cyber-page-head">
      <div>
        <div class="cyber-page-title">情感自测终端</div>
        <div class="cyber-page-sub">EMOTION QUIZ TERMINAL · 根据近期情况自评情绪状态</div>
      </div>
    </div>

    <div class="cyber-panel quiz-panel">
      <!-- ===== 答题视图 ===== -->
      <template v-if="!finished">
        <div class="quiz-head">
          <div class="quiz-title">
            <div class="title-icon">
              <el-icon><Sunny /></el-icon>
            </div>
            <div>
              <div class="t1">近期情况情感自测</div>
              <div class="t2">请根据最近两周的实际情况，选择最符合你的选项</div>
            </div>
            <span class="panel-tag">SELF TEST</span>
          </div>
          <div class="disclaimer">
            <span class="disc-dot"></span>本测评仅供一般性情绪自评参考，不构成医疗诊断
          </div>
        </div>

        <!-- 进度 -->
        <div class="progress-wrap">
          <div class="progress-meta">
            <span class="step-text">QUESTION <b>{{ currentIndex + 1 }}</b> / {{ questions.length }}</span>
            <span class="done-text">已完成 {{ answeredCount }} 题</span>
          </div>
          <div class="progress-track">
            <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
          </div>
        </div>

        <!-- 题目 -->
        <div class="question-card" :key="currentIndex">
          <div class="q-no">Q{{ currentIndex + 1 }}</div>
          <div class="question-text">{{ currentQuestion }}</div>
          <div class="options">
            <div v-for="opt in options" :key="opt.value"
                 class="option" :class="{ active: answers[currentIndex] === opt.value }"
                 @click="select(opt.value)">
              <span class="option-dot"></span>
              <span class="option-label">{{ opt.label }}</span>
              <el-icon v-if="answers[currentIndex] === opt.value" class="option-check"><CircleCheckFilled /></el-icon>
            </div>
          </div>
        </div>

        <div class="nav">
          <el-button size="large" class="nav-btn" :disabled="currentIndex === 0" @click="prev">
            <el-icon style="margin-right:4px;"><ArrowLeft /></el-icon>上一题
          </el-button>
          <el-button v-if="!isLast" size="large" type="primary" class="next-btn"
                     :disabled="answers[currentIndex] === undefined" @click="next">
            下一题<el-icon style="margin-left:4px;"><ArrowRight /></el-icon>
          </el-button>
          <el-button v-else size="large" type="primary" class="next-btn" @click="finish">
            <el-icon style="margin-right:4px;"><MagicStick /></el-icon>查看测评结果
          </el-button>
        </div>
      </template>

      <!-- ===== 结果视图 ===== -->
      <template v-else>
        <div class="result-view">
          <div class="result-head">
            <div class="score-circle" :style="{ borderColor: result.color, color: result.color, boxShadow: '0 0 30px ' + result.color + '55' }">
              <div class="ring-a" :style="{ borderColor: result.color }"></div>
              <div class="score-num">{{ score }}</div>
              <div class="score-max">/ {{ MAX_SCORE }}</div>
            </div>
            <div class="result-info">
              <div class="level" :style="{ color: result.color, textShadow: '0 0 16px ' + result.color }">{{ result.level }}</div>
              <div class="level-desc">{{ result.desc }}</div>
              <div class="scale-bar">
                <div class="scale-fill" :style="{ width: scorePercent + '%', background: result.color, boxShadow: '0 0 10px ' + result.color }"></div>
              </div>
              <div class="scale-labels">
                <span>良好</span><span>一般</span><span>需关注</span>
              </div>
            </div>
          </div>

          <div class="advice-box">
            <div class="advice-title">
              <span class="adv-icon"><el-icon><ChatLineRound /></el-icon></span>给你的建议 / ADVICE
            </div>
            <ul class="advice-list">
              <li v-for="(a, i) in result.advice" :key="i">
                <span class="advice-dot" :style="{ background: result.color, boxShadow: '0 0 6px ' + result.color }"></span>{{ a }}
              </li>
            </ul>
          </div>

          <div v-if="topConcerns.length" class="concern-box">
            <div class="concern-title">// 近期较明显的感受</div>
            <div class="concern-tags">
              <el-tag v-for="c in topConcerns" :key="c" size="large" effect="light" round>{{ c }}</el-tag>
            </div>
          </div>

          <div class="result-disclaimer">
            <el-icon><Warning /></el-icon>
            <span>本测评结果仅基于你自选的 10 个问题计算，作为一般性参考，不代表任何医学诊断。如果你正经历持续的情绪困扰，请及时与信任的人沟通，并考虑寻求专业心理支持或就医。</span>
          </div>

          <div class="result-actions">
            <el-button size="large" class="nav-btn" @click="restart">重新测评</el-button>
            <el-button size="large" type="primary" class="next-btn" @click="goAnalyze">去文本情感分析</el-button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>
<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()
const questions = [
  '最近两周，你是否感到心情低落、沮丧或没有希望？',
  '你是否对平时感兴趣的事情失去了兴趣或乐趣？',
  '你是否感到疲惫、精力不足、做事提不起劲？',
  '你是否入睡困难、易醒或睡眠质量差？',
  '你是否感到紧张、焦虑或坐立不安？',
  '你是否对未来感到悲观或迷茫？',
  '你是否难以集中注意力、容易走神？',
  '你是否容易烦躁、发脾气或没有耐心？',
  '你是否觉得自己没有价值、拖累了别人？',
  '你是否感到孤独，缺少可以倾诉的人？'
]
const options = [
  { label: '完全没有', value: 0 },
  { label: '有几天', value: 1 },
  { label: '一半以上天数', value: 2 },
  { label: '几乎每天', value: 3 }
]
const MAX_SCORE = questions.length * 3
const currentIndex = ref(0)
const answers = ref([])
const finished = ref(false)
const score = ref(0)
const currentQuestion = computed(() => questions[currentIndex.value])
const isLast = computed(() => currentIndex.value === questions.length - 1)
const answeredCount = computed(() => answers.value.filter(a => a !== undefined).length)
const progressPercent = computed(() => (answeredCount.value / questions.length) * 100)
const result = computed(() => getResult(score.value))
const scorePercent = computed(() => (score.value / MAX_SCORE) * 100)
const topConcerns = computed(() => {
  const max = Math.max(...answers.value)
  if (max <= 1) return []
  return questions.filter((_, i) => answers.value[i] === max)
})
function select(val) {
  answers.value[currentIndex.value] = val
  // 选中后自动跳到下一题
  if (!isLast.value) {
    setTimeout(() => next(), 260)
  }
}
function next() {
  if (currentIndex.value < questions.length - 1) {
    currentIndex.value++
  }
}
function prev() {
  if (currentIndex.value > 0) {
    currentIndex.value--
  }
}
function finish() {
  score.value = answers.value.reduce((s, a) => s + (a || 0), 0)
  finished.value = true
}
function restart() {
  answers.value = []
  currentIndex.value = 0
  score.value = 0
  finished.value = false
}
function goAnalyze() {
  router.push('/analyze')
}
function getResult(s) {
  if (s <= 4) {
    return {
      level: '状态良好',
      color: '#10b981',
      desc: '近期你的整体情绪状态良好，请继续保持积极的生活方式。',
      advice: [
        '继续保持规律作息，保证每晚 7-8 小时的充足睡眠。',
        '坚持适量运动，如散步、慢跑或球类，有助于维持好心情。',
        '多与家人朋友交流，保持健康的社交连接，平衡好学习与休息。'
      ]
    }
  }
  if (s <= 9) {
    return {
      level: '状态不错，略有波动',
      color: '#3b82f6',
      desc: '你的整体状态不错，近期可能偶有小情绪波动，这属于正常现象。',
      advice: [
        '保证睡眠质量，减少熬夜，给身体足够的恢复时间。',
        '每周安排 3 次以上中等强度运动，帮助释放压力。',
        '感到疲惫或烦躁时，试着做几次深呼吸或短暂休息，及时自我调节。'
      ]
    }
  }
  if (s <= 14) {
    return {
      level: '存在轻度压力',
      color: '#f59e0b',
      desc: '近期你可能存在轻度的压力或情绪困扰，需要适当关注和调节。',
      advice: [
        '主动调整生活节奏：规律作息、减少熬夜、增加户外活动。',
        '找信任的亲友倾诉，或通过写情绪日记的方式梳理感受。',
        '减少无效刷手机时间，给自己设定可完成的小目标，找回掌控感。',
        '如果这种状态持续两周以上仍未缓解，建议考虑寻求专业支持。'
      ]
    }
  }
  if (s <= 19) {
    return {
      level: '情绪状态需重点关注',
      color: '#f97316',
      desc: '你的得分提示近期情绪状态波动较明显，可能已对生活产生一定影响，请认真对待。',
      advice: [
        '请重视当前的感受，尽快给自己安排放松和恢复的时间。',
        '与家人、朋友或信任的师长坦诚沟通你的真实感受，不要独自硬扛。',
        '减少高负荷安排，适当降低对自己的要求，接纳情绪的存在。',
        '强烈建议考虑寻求学校心理中心、专业心理咨询的帮助。'
      ]
    }
  }
  return {
    level: '需要重点关注与支持',
    color: '#ef4444',
    desc: '你的得分提示近期情绪困扰较为明显，请务必重视并尽快采取行动。',
    advice: [
      '请不要独自承受，立即向信任的家人、朋友或师长求助。',
      '建议尽快联系专业心理机构、心理咨询师或医院心理科进行评估和帮助。',
      '如果出现持续情绪低落、失眠加重或任何自我伤害的想法，请务必第一时间寻求专业帮助。',
      '可随时拨打全国心理援助热线 12356（24 小时），获取免费专业的支持。'
    ]
  }
}
</script>
<style scoped>
.quiz-page { max-width: 720px; margin: 0 auto; }

.quiz-panel { padding: 24px 26px 20px; }

/* 头部 */
.quiz-head { margin-bottom: 20px; }
.quiz-title { display: flex; align-items: center; gap: 14px; }
.title-icon {
  width: 46px; height: 46px; border-radius: 13px;
  background: linear-gradient(135deg, #ffc94d, #ff2d95);
  color: #fff; font-size: 24px;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 0 18px rgba(255, 201, 77, 0.45);
}
.t1 { font-size: 19px; font-weight: 700; color: #d9f8ff; letter-spacing: 0.5px; }
.t2 { font-size: 13px; color: var(--cyber-text-dim); margin-top: 3px; }
.panel-tag {
  margin-left: auto;
  font-size: 10px; letter-spacing: 3px;
  color: #ff2d95;
  border: 1px solid rgba(255, 45, 149, 0.4);
  border-radius: 20px;
  padding: 4px 12px;
  box-shadow: 0 0 10px rgba(255, 45, 149, 0.25);
}
.disclaimer {
  margin-top: 14px;
  display: flex; align-items: center; gap: 8px;
  font-size: 12px; color: #ffc94d;
  background: rgba(255, 201, 77, 0.08);
  border: 1px solid rgba(255, 201, 77, 0.25);
  border-radius: 10px; padding: 8px 14px;
}
.disc-dot {
  width: 7px; height: 7px; border-radius: 50%;
  background: #ffc94d;
  box-shadow: 0 0 6px #ffc94d;
  flex: 0 0 7px;
}

/* 进度 */
.progress-wrap { margin-bottom: 18px; }
.progress-meta {
  display: flex; justify-content: space-between;
  font-size: 12px; color: var(--cyber-text-dim);
  margin-bottom: 8px;
  letter-spacing: 1px;
}
.step-text b { color: #00f0ff; font-size: 14px; text-shadow: 0 0 8px rgba(0,240,255,0.6); }
.progress-track {
  height: 9px; background: rgba(255,255,255,0.07);
  border-radius: 5px; overflow: hidden;
  border: 1px solid rgba(0, 240, 255, 0.18);
}
.progress-fill {
  height: 100%; border-radius: 5px;
  background: linear-gradient(90deg, #00f0ff, #7b2dff, #ff2d95);
  box-shadow: 0 0 12px rgba(0, 240, 255, 0.6);
  transition: width 0.35s ease;
}

/* 题目 */
.question-card {
  position: relative;
  background: rgba(4, 7, 18, 0.6);
  border: 1px solid rgba(0, 240, 255, 0.16);
  border-radius: 14px; padding: 22px 20px;
}
.q-no {
  position: absolute; top: 14px; right: 16px;
  font-size: 11px; letter-spacing: 2px;
  color: rgba(0, 240, 255, 0.6);
  border: 1px solid rgba(0, 240, 255, 0.3);
  border-radius: 8px; padding: 2px 8px;
}
.question-text {
  font-size: 16px; font-weight: 600; color: #d9f8ff;
  line-height: 1.7; margin-bottom: 16px;
  padding-right: 60px;
}
.options { display: flex; flex-direction: column; gap: 10px; }
.option {
  display: flex; align-items: center; gap: 12px;
  background: rgba(8, 12, 28, 0.6);
  border: 1.5px solid rgba(0, 240, 255, 0.18);
  border-radius: 12px;
  padding: 13px 16px; cursor: pointer;
  transition: all 0.18s;
  position: relative;
}
.option:hover {
  border-color: rgba(0, 240, 255, 0.55);
  background: rgba(0, 240, 255, 0.07);
  box-shadow: 0 0 14px rgba(0, 240, 255, 0.15);
}
.option.active {
  border-color: #00f0ff;
  background: linear-gradient(90deg, rgba(0, 240, 255, 0.14), rgba(123, 45, 255, 0.12));
  box-shadow: 0 0 18px rgba(0, 240, 255, 0.25);
}
.option-dot {
  width: 14px; height: 14px; border-radius: 50%;
  border: 2px solid rgba(160, 210, 235, 0.5);
  flex: 0 0 14px;
  transition: all 0.18s;
  background: rgba(0, 240, 255, 0.08);
}
.option.active .option-dot {
  border-color: #00f0ff;
  background: #00f0ff;
  box-shadow: 0 0 10px #00f0ff;
}
.option-label { font-size: 15px; color: #cfeffb; flex: 1; }
.option-check { color: #00f0ff; font-size: 18px; filter: drop-shadow(0 0 6px #00f0ff); }

/* 导航 */
.nav { display: flex; justify-content: space-between; margin-top: 18px; gap: 12px; }
.nav-btn {
  border-radius: 10px;
}
.next-btn {
  border-radius: 10px;
  padding: 0 26px;
  letter-spacing: 1px;
  font-weight: 600;
}

/* ===== 结果视图 ===== */
.result-head { display: flex; gap: 26px; align-items: center; flex-wrap: wrap; }
.score-circle {
  width: 118px; height: 118px; border-radius: 50%;
  border: 5px solid;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  flex: 0 0 118px;
  position: relative;
  background: rgba(5, 8, 20, 0.8);
}
.ring-a {
  position: absolute; inset: -12px;
  border-radius: 50%;
  border: 2px dashed;
  opacity: 0.45;
  animation: spin 16s linear infinite;
}
.score-num { font-size: 34px; font-weight: 800; line-height: 1; text-shadow: 0 0 14px currentColor; }
.score-max { font-size: 12px; color: var(--cyber-text-dim); margin-top: 4px; }
@keyframes spin { to { transform: rotate(360deg); } }

.result-info { flex: 1; min-width: 240px; }
.level { font-size: 24px; font-weight: 800; letter-spacing: 1px; }
.level-desc { font-size: 14px; color: #a9c9d9; margin-top: 6px; line-height: 1.7; }
.scale-bar {
  height: 9px; background: rgba(255,255,255,0.08);
  border-radius: 5px; margin-top: 14px; overflow: hidden;
  border: 1px solid rgba(0, 240, 255, 0.15);
}
.scale-fill { height: 100%; border-radius: 5px; transition: width 0.6s ease; }
.scale-labels { display: flex; justify-content: space-between; font-size: 11px; color: var(--cyber-text-dim); margin-top: 5px; }

.advice-box {
  margin-top: 20px;
  background: rgba(4, 7, 18, 0.6);
  border: 1px solid rgba(0, 240, 255, 0.16);
  border-radius: 14px; padding: 18px 20px;
}
.advice-title {
  display: flex; align-items: center; gap: 10px;
  font-size: 15px; font-weight: 700; color: #00f0ff;
  letter-spacing: 1px;
  margin-bottom: 12px;
}
.adv-icon {
  width: 30px; height: 30px; border-radius: 9px;
  background: linear-gradient(135deg, #00c2d6, #7b2dff);
  color: #fff; font-size: 16px;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 0 12px rgba(0, 240, 255, 0.4);
}
.advice-list { list-style: none; margin: 0; padding: 0; }
.advice-list li {
  display: flex; gap: 10px; align-items: flex-start;
  font-size: 14px; color: #cfeffb; line-height: 1.7; margin-bottom: 9px;
}
.advice-dot { width: 7px; height: 7px; border-radius: 50%; margin-top: 8px; flex: 0 0 7px; }

.concern-box { margin-top: 16px; }
.concern-title {
  font-size: 12px; color: var(--cyber-text-dim);
  font-family: Consolas, monospace;
  margin-bottom: 10px;
}
.concern-tags { display: flex; gap: 8px; flex-wrap: wrap; }

.result-disclaimer {
  margin-top: 18px;
  display: flex; gap: 8px; align-items: flex-start;
  font-size: 12px; color: #ffc94d; line-height: 1.7;
  background: rgba(255, 201, 77, 0.07);
  border: 1px solid rgba(255, 201, 77, 0.25);
  border-radius: 10px; padding: 10px 14px;
}
.result-disclaimer .el-icon { font-size: 16px; margin-top: 1px; flex: 0 0 16px; }

.result-actions { display: flex; gap: 12px; margin-top: 20px; }
</style>
