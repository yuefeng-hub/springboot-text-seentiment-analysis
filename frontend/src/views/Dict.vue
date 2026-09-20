<template>
  <div class="dict-page">
    <!-- 页面标题 -->
    <div class="cyber-page-head">
      <div>
        <div class="cyber-page-title">自定义情感词典</div>
        <div class="cyber-page-sub">SENTIMENT LEXICON · 共 {{ list.length }} 个自定义情感词</div>
      </div>
      <el-tag type="primary" effect="light" round class="live-tag">
        <el-icon style="margin-right:4px;"><Connection /></el-icon>新增即生效
      </el-tag>
    </div>

    <div class="cyber-panel dict-panel">
      <el-alert type="info" :closable="false" show-icon class="tip-alert">
        新增或删除情感词后 <b>立即生效</b>，无需重启服务。可扩充「强烈推荐」「物超所值」等词提升准确率，这也是本系统的核心功能之一。
      </el-alert>

      <!-- 新增表单 -->
      <div class="add-bar">
        <div class="add-label">NEW_WORD //</div>
        <el-input v-model="form.word" placeholder="情感词（必填）" class="in-word" :prefix-icon="EditPen" />
        <el-select v-model="form.polarity" placeholder="极性" class="in-polarity">
          <el-option label="正向" :value="1" />
          <el-option label="负向" :value="-1" />
        </el-select>
        <el-input v-model.number="form.weight" placeholder="权重(默认1)" class="in-weight" />
        <el-input v-model="form.category" placeholder="类别(可选)" class="in-category" />
        <el-button type="primary" size="large" class="add-btn" @click="handleAdd">
          <el-icon style="margin-right:5px;"><Plus /></el-icon>新增
        </el-button>
      </div>

      <el-table :data="list" stripe class="dict-table">
        <el-table-column prop="word" label="情感词" min-width="140">
          <template #default="{ row }">
            <span class="word-cell">{{ row.word }}</span>
          </template>
        </el-table-column>
        <el-table-column label="极性" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.polarity === 1 ? 'success' : 'danger'" effect="dark" round>
              {{ row.polarity === 1 ? '正向' : '负向' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="weight" label="权重" width="90" align="center" />
        <el-table-column prop="category" label="类别" min-width="120">
          <template #default="{ row }">
            <el-tag v-if="row.category" size="small" effect="plain">{{ row.category }}</el-tag>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="175" />
        <el-table-column label="操作" width="90" align="center">
          <template #default="{ row }">
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
const list = ref([])
const form = ref({ word: '', polarity: 1, weight: 1, category: '' })
async function load() {
  const res = await request.get('/dict/list')
  if (res.code === 200) list.value = res.data
}
async function handleAdd() {
  if (!form.value.word.trim()) {
    ElMessage.warning('请输入情感词')
    return
  }
  const res = await request.post('/dict/add', form.value)
  if (res.code === 200) {
    ElMessage.success('已新增，词典已生效')
    form.value = { word: '', polarity: 1, weight: 1, category: '' }
    load()
  } else {
    ElMessage.error(res.message)
  }
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除情感词「${row.word}」吗？`, '提示', { type: 'warning' })
  const res = await request.delete(`/dict/delete/${row.id}`)
  if (res.code === 200) {
    ElMessage.success('已删除，词典已生效')
    load()
  } else {
    ElMessage.error(res.message)
  }
}
onMounted(load)
</script>
<style scoped>
.dict-page { max-width: 1000px; margin: 0 auto; }
.dict-panel { padding: 20px 22px; }
.live-tag { font-size: 13px; padding: 6px 14px; }
.tip-alert {
  margin-bottom: 16px;
  border-radius: 10px;
  border: 1px solid rgba(0, 240, 255, 0.3) !important;
  background: rgba(0, 240, 255, 0.07) !important;
}
.add-bar {
  display: flex; gap: 10px; flex-wrap: wrap; align-items: center;
  padding: 16px;
  background: rgba(4, 7, 18, 0.6);
  border: 1px dashed rgba(0, 240, 255, 0.35);
  border-radius: 12px;
  margin-bottom: 16px;
}
.add-label {
  font-size: 11px; letter-spacing: 2px;
  color: rgba(0, 240, 255, 0.6);
  font-family: Consolas, monospace;
}
.in-word { width: 170px; }
.in-polarity { width: 110px; }
.in-weight { width: 130px; }
.in-category { width: 130px; }
.add-btn {
  border-radius: 10px;
  padding: 0 24px;
  font-weight: 600;
  letter-spacing: 1px;
}
.word-cell {
  font-weight: 700; color: #00f0ff;
  text-shadow: 0 0 8px rgba(0, 240, 255, 0.4);
}
.muted { color: rgba(127, 180, 201, 0.5); }
</style>
