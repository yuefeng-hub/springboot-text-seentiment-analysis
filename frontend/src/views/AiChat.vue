<template>
  <div class="chat-page">
    <div class="cyber-panel chat-panel">
      <!-- 头部 -->
      <div class="chat-head">
        <div class="ai-avatar"><span>AI</span></div>
        <div class="head-text">
          <div class="chat-title">AI 智能问答</div>
          <div class="chat-sub">NEURAL CHAT · 由大语言模型驱动 · 支持多轮对话</div>
        </div>
        <el-button size="small" round class="clear-btn" @click="clearChat">
          <el-icon style="margin-right:4px;"><Delete /></el-icon>清空对话
        </el-button>
      </div>

      <!-- 消息区 -->
      <div class="msg-area" ref="msgArea">
        <!-- 空状态 + 快捷提问 -->
        <div v-if="!messages.length" class="empty-chat">
          <div class="ai-avatar big"><span>AI</span></div>
          <p class="empty-tip">你好，我是本系统的 AI 助手。可以问我情感分析、情绪调节或任何问题。</p>
          <div class="suggestions">
            <div v-for="s in suggestions" :key="s" class="sug" @click="quickAsk(s)">
              <el-icon><ChatLineSquare /></el-icon>{{ s }}
            </div>
          </div>
        </div>

        <!-- 对话消息 -->
        <div v-for="(m, i) in messages" :key="i" class="msg-row" :class="m.role">
          <div v-if="m.role === 'assistant'" class="ai-avatar sm"><span>AI</span></div>
          <div class="bubble" :class="m.role">{{ m.content }}</div>
        </div>

        <!-- 打字中 -->
        <div v-if="loading" class="msg-row assistant">
          <div class="ai-avatar sm"><span>AI</span></div>
          <div class="bubble assistant typing">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="input-bar">
        <el-input v-model="input" type="textarea" :rows="2" resize="none"
                  class="chat-input" maxlength="2000"
                  placeholder="输入你的问题，Enter 发送，Shift+Enter 换行"
                  @keydown.enter.exact.prevent="send" />
        <el-button type="primary" size="large" class="send-btn" :loading="loading" @click="send">
          <el-icon style="margin-right:5px;"><Promotion /></el-icon>发送
        </el-button>
      </div>
      <div class="input-note">> 内容由 AI 生成，仅供学习参考，不构成专业建议</div>
    </div>
  </div>
</template>
<script setup>
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
const messages = ref([])
const input = ref('')
const loading = ref(false)
const msgArea = ref(null)
const suggestions = [
  '帮我分析这句话的情感：今天心情真好',
  '最近考试压力很大，怎么办',
  '什么是情感分析？有哪些方法？',
  '长期失眠会对情绪有什么影响？'
]
function scrollBottom() {
  nextTick(() => {
    if (msgArea.value) {
      msgArea.value.scrollTop = msgArea.value.scrollHeight
    }
  })
}
async function send() {
  const text = input.value.trim()
  if (!text || loading.value) return
  messages.value.push({ role: 'user', content: text })
  input.value = ''
  loading.value = true
  scrollBottom()
  try {
    // 把除当前这条以外的对话作为历史传入，支持多轮上下文
    const history = messages.value.slice(0, -1).map(m => ({ role: m.role, content: m.content }))
    // AI 回复可能较慢，单独放宽超时到 120s
    const res = await request.post('/ai/chat', { message: text, history }, { timeout: 120000 })
    if (res.code === 200) {
      messages.value.push({ role: 'assistant', content: res.data.reply })
    } else {
      messages.value.push({ role: 'assistant', content: '抱歉，出了点问题：' + res.message })
    }
  } catch (e) {
    const msg = (e.response && e.response.data && e.response.data.message) || '网络异常，请稍后再试'
    messages.value.push({ role: 'assistant', content: '抱歉，出错了：' + msg })
  } finally {
    loading.value = false
    scrollBottom()
  }
}
function quickAsk(s) {
  input.value = s
  send()
}
function clearChat() {
  messages.value = []
  ElMessage.success('对话已清空')
}
</script>
<style scoped>
.chat-page { max-width: 860px; margin: 0 auto; }

.chat-panel { display: flex; flex-direction: column; padding: 8px 10px 12px; }

/* 头部 */
.chat-head {
  display: flex; align-items: center; gap: 12px;
  padding: 10px 8px 14px;
  border-bottom: 1px solid rgba(0, 240, 255, 0.16);
}
.ai-avatar {
  width: 44px; height: 44px; flex: 0 0 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, #00c2d6, #7b2dff);
  color: #fff; font-weight: 700; font-size: 16px;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 0 16px rgba(0, 240, 255, 0.45);
  border: 1px solid rgba(0, 240, 255, 0.4);
}
.ai-avatar.big { width: 60px; height: 60px; flex-basis: 60px; font-size: 20px; border-radius: 18px; }
.ai-avatar.sm { width: 32px; height: 32px; flex-basis: 32px; font-size: 12px; border-radius: 9px; box-shadow: 0 0 10px rgba(0, 240, 255, 0.35); }
.head-text { flex: 1; }
.chat-title {
  font-size: 17px; font-weight: 700;
  background: linear-gradient(90deg, #00f0ff, #7b2dff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
.chat-sub { font-size: 11.5px; color: var(--cyber-text-dim); margin-top: 3px; letter-spacing: 0.5px; }
.clear-btn { border-color: rgba(255, 61, 110, 0.4) !important; color: #ff5c84 !important; }
.clear-btn:hover { border-color: #ff3d6e !important; color: #ff5c84 !important; box-shadow: 0 0 12px rgba(255, 61, 110, 0.25) !important; }

/* 消息区 */
.msg-area {
  height: calc(100vh - 340px);
  min-height: 320px;
  overflow-y: auto;
  padding: 18px 8px;
  display: flex; flex-direction: column; gap: 14px;
}

/* 空状态 */
.empty-chat { display: flex; flex-direction: column; align-items: center; padding: 30px 0 10px; }
.empty-tip { margin: 16px 0 20px; font-size: 14px; color: var(--cyber-text-dim); text-align: center; }
.suggestions { display: flex; flex-direction: column; gap: 10px; width: 100%; max-width: 480px; }
.sug {
  display: flex; align-items: center; gap: 9px;
  background: rgba(250, 252, 255, 0.92);
  border: 1px solid rgba(0, 240, 255, 0.4);
  border-radius: 10px;
  padding: 11px 14px; font-size: 13.5px; color: #1e2a44;
  cursor: pointer; transition: all 0.18s;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.18);
}
.sug:hover {
  border-color: #00f0ff;
  background: #ffffff;
  box-shadow: 0 0 16px rgba(0, 240, 255, 0.35);
  transform: translateX(3px);
}
.sug .el-icon { color: #0a9bb5; }

/* 消息气泡 */
.msg-row { display: flex; gap: 10px; align-items: flex-start; }
.msg-row.user { justify-content: flex-end; }
.bubble {
  max-width: 76%;
  padding: 12px 16px;
  font-size: 14.5px;
  line-height: 1.75;
  white-space: pre-wrap;
  word-break: break-word;
  border-radius: 14px;
}
.bubble.user {
  background: linear-gradient(135deg, #00c2d6, #7b2dff);
  color: #fff;
  border-bottom-right-radius: 4px;
  box-shadow: 0 0 16px rgba(0, 240, 255, 0.3);
}
.bubble.assistant {
  background: rgba(250, 252, 255, 0.95);
  color: #1e2a44;
  border: 1px solid rgba(0, 240, 255, 0.45);
  box-shadow: 0 3px 14px rgba(0, 0, 0, 0.25), 0 0 16px rgba(0, 240, 255, 0.18);
  border-bottom-left-radius: 4px;
}

/* 打字动画 */
.typing { display: inline-flex; gap: 5px; padding: 15px 16px; }
.typing span {
  width: 7px; height: 7px; border-radius: 50%;
  background: #00f0ff;
  box-shadow: 0 0 8px #00f0ff;
  animation: blink 1.2s infinite;
}
.typing span:nth-child(2) { animation-delay: 0.2s; }
.typing span:nth-child(3) { animation-delay: 0.4s; }
@keyframes blink { 0%, 80%, 100% { opacity: 0.25; } 40% { opacity: 1; } }

/* 输入区 */
.input-bar { display: flex; gap: 12px; align-items: flex-end; padding: 12px 8px 4px; }
.chat-input :deep(.el-textarea__inner) { font-size: 14px; border-radius: 12px; padding: 10px 14px; }
.send-btn {
  height: 52px; border-radius: 12px;
  padding: 0 26px;
  font-weight: 600;
  letter-spacing: 1px;
}
.input-note {
  font-size: 11px; color: rgba(127, 180, 201, 0.55);
  text-align: center; padding: 6px 0 2px;
  font-family: Consolas, monospace;
}
</style>
