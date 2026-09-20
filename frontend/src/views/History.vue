<template>
  <div class="history-page">
    <!-- 页面标题 -->
    <div class="cyber-page-head">
      <div>
        <div class="cyber-page-title">历史分析记录</div>
        <div class="cyber-page-sub">RECORD ARCHIVE · 共 {{ total }} 条记录 · 每次分析自动留存</div>
      </div>
    </div>

    <div class="cyber-panel table-panel">
      <el-table :data="list" v-loading="loading" stripe class="record-table">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="text" label="分析文本" min-width="240" show-overflow-tooltip />
        <el-table-column label="情感" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="tagType(row.sentimentType)" effect="dark" round>
              {{ sentimentLabel(row.sentimentType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="得分" width="90" align="center">
          <template #default="{ row }">
            <span :style="{ color: scoreColor(row.score), fontWeight: 600, textShadow: '0 0 8px ' + scoreColor(row.score) }">{{ row.score }}</span>
          </template>
        </el-table-column>
        <el-table-column label="置信度" width="100" align="center">
          <template #default="{ row }">
            {{ Math.round(row.confidence * 100) }}%
          </template>
        </el-table-column>
        <el-table-column prop="keywords" label="情感词" min-width="160">
          <template #default="{ row }">
            <template v-if="row.keywords && row.keywords.length">
              <el-tag v-for="k in row.keywords" :key="k" size="small" effect="plain" class="kw">
                {{ k }}
              </el-tag>
            </template>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="分析时间" width="180" />
        <el-table-column label="操作" width="90" align="center">
          <template #default="{ row }">
            <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination background layout="total, prev, pager, next" :total="total"
                       :page-size="pageSize" :current-page="current"
                       @current-change="load" />
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
const list = ref([])
const total = ref(0)
const current = ref(1)
const pageSize = 10
const loading = ref(false)
async function load(page = 1) {
  current.value = page
  loading.value = true
  try {
    const res = await request.get('/record/list', { params: { page, size: pageSize } })
    if (res.code === 200) {
      list.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } finally {
    loading.value = false
  }
}
async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除这条记录吗？', '提示', { type: 'warning' })
  const res = await request.delete(`/record/delete/${id}`)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    load(current.value)
  } else {
    ElMessage.error(res.message)
  }
}
function tagType(t) {
  return t === 1 ? 'success' : t === -1 ? 'danger' : 'warning'
}
function sentimentLabel(t) {
  return t === 1 ? '正向' : t === -1 ? '负向' : '中性'
}
function scoreColor(s) {
  return s > 0 ? '#00e6a6' : s < 0 ? '#ff3d6e' : '#ffc94d'
}
onMounted(() => load(1))
</script>
<style scoped>
.history-page { max-width: 1100px; margin: 0 auto; }
.table-panel { padding: 18px 20px; }
.record-table { border-radius: 10px; }
.kw { margin-right: 6px; border-radius: 20px; }
.muted { color: rgba(127, 180, 201, 0.5); }
.pager { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
