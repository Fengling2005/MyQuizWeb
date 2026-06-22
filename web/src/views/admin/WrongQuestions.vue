<template>
  <div class="wrong-container">
    <el-card>
      <template #header>
        <div class="header-content">
          <h2>我的错题</h2>
          <div class="header-stats">
            <el-space>
              <el-statistic title="错题总数" :value="statistics.wrongCount || 0" />
              <el-statistic title="已答题数" :value="statistics.totalAnswered || 0" />
              <el-statistic title="正确率" :value="statistics.accuracy || 0">
                <template #suffix>%</template>
              </el-statistic>
            </el-space>
          </div>
        </div>
      </template>

      <!-- 操作按钮区域 -->
      <div class="action-bar">
        <el-space>
          <el-button
              type="danger"
              :icon="Delete"
              @click="clearAllWrong"
              :disabled="wrongList.length === 0">
            清空所有错题
          </el-button>
          <el-button
              type="primary"
              :icon="Select"
              @click="startPracticeWrong"
              :disabled="wrongList.length === 0">
            专项练习错题
          </el-button>
          <el-button
              type="success"
              :icon="Refresh"
              @click="refreshList">
            刷新列表
          </el-button>
        </el-space>
      </div>

      <!-- 错题列表 -->
      <el-table
          :data="wrongList"
          border
          style="width: 100%"
          v-loading="loading"
          @selection-change="handleSelectionChange"
          :default-sort="{ prop: 'recordId', order: 'descending' }">

        <el-table-column type="selection" width="55" />

        <el-table-column prop="questionId" label="题目ID" width="80" sortable />

        <el-table-column prop="questionType" label="题型" width="100">
          <template #default="scope">
            <el-tag :type="getQuestionTypeTag(scope.row.questionType)">
              {{ scope.row.questionType }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="content" label="题目内容" min-width="200">
          <template #default="scope">
            <div class="content-cell">
              {{ scope.row.content }}
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="userAnswer" label="我的答案" width="100">
          <template #default="scope">
            <span style="color: #f56c6c; font-weight: bold;">
              {{ scope.row.userAnswer }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="正确答案" width="100">
          <template #default="scope">
            <span style="color: #67c23a; font-weight: bold;">
              {{ scope.row.answer }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="errorTimes" label="错误次数" width="100">
          <template #default="scope">
            <el-tag type="danger" size="small">
              {{ getErrorCount(scope.row.questionId) }} 次
            </el-tag>
          </template>
        </el-table-column>

        <!-- 修改操作列，添加收藏状态显示 -->
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-space>
              <el-button
                  type="primary"
                  size="small"
                  @click="goToPractice(scope.row)"
                  :icon="Select">
                重新练习
              </el-button>
              <el-button
                  type="success"
                  size="small"
                  @click="clearWrong(scope.row)"
                  :icon="Check">
                标记掌握
              </el-button>
              <el-button
                  :type="scope.row.isMarked === 1 ? 'warning' : 'info'"
                  size="small"
                  @click="toggleMark(scope.row)"
                  :icon="Star"
                  :class="{ 'is-marked': scope.row.isMarked === 1 }">
                {{ scope.row.isMarked === 1 ? '已收藏' : '收藏' }}
              </el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <!-- 批量操作 -->
      <div v-if="selectionRows.length > 0" class="batch-actions">
        <el-alert
            title="批量操作"
            type="info"
            :closable="false"
            style="margin: 15px 0;">
          <template #default>
            <el-space>
              <span>已选择 {{ selectionRows.length }} 道错题</span>
              <el-button type="success" size="small" @click="batchClear">
                批量标记掌握
              </el-button>
              <el-button type="primary" size="small" @click="batchPractice">
                批量练习
              </el-button>
            </el-space>
          </template>
        </el-alert>
      </div>

      <!-- 空状态 -->
      <div v-if="wrongList.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无错题记录，加油保持！" />
        <div style="text-align: center; margin-top: 20px;">
          <el-button type="primary" @click="goToPracticePage">
            去练习题目
          </el-button>
        </div>
      </div>

      <!-- 统计图表（可选） -->
      <div v-if="wrongList.length > 0" class="chart-container">
        <el-card style="margin-top: 20px;">
          <template #header>
            <h3>错题分析</h3>
          </template>
          <div class="chart-content">
            <div class="chart-item">
              <div class="chart-title">题型分布</div>
              <div class="chart-data">
                <el-space direction="vertical">
                  <div v-for="(count, type) in typeDistribution" :key="type"
                       class="distribution-item">
                    <span>{{ type }}：</span>
                    <el-progress
                        :percentage="(count / wrongList.length) * 100"
                        :show-text="false"
                        :stroke-width="10"
                        :color="getTypeColor(type)" />
                    <span style="margin-left: 10px; min-width: 40px;">{{ count }} 题</span>
                  </div>
                </el-space>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Select, Refresh, Check, Star } from '@element-plus/icons-vue'
import request from '@/utils/http.js'

const router = useRouter()
const wrongList = ref([])
const loading = ref(false)
const selectionRows = ref([])
const statistics = ref({
  wrongCount: 0,
  totalAnswered: 0,
  accuracy: 0
})

// 计算属性：题型分布
const typeDistribution = computed(() => {
  const distribution = {}
  wrongList.value.forEach(item => {
    const type = item.questionType || '未知题型'
    distribution[type] = (distribution[type] || 0) + 1
  })
  return distribution
})

// 获取题目类型对应的tag样式
function getQuestionTypeTag(type) {
  if (!type) return 'info'
  switch (type) {
    case '单选题': return 'primary'
    case '多选题': return 'success'
    case '判断题': return 'warning'
    default: return 'info'
  }
}

// 获取题型对应的颜色
function getTypeColor(type) {
  switch (type) {
    case '单选题': return '#409EFF'
    case '多选题': return '#67C23A'
    case '判断题': return '#E6A23C'
    default: return '#909399'
  }
}

// 获取错误次数（简单实现，实际应从后端获取）
function getErrorCount(questionId) {
  // 这里可以根据实际需求实现错误次数统计
  // 暂时返回1
  return 1
}

// 加载错题列表
async function loadWrongList() {
  loading.value = true
  try {
    const res = await request.get('/wrong/list')
    if (res.code === 200) {
      wrongList.value = res.data
      // 加载每个题目的收藏状态
      await loadMarkStatusForAll()
    } else {
      ElMessage.error('加载错题列表失败: ' + res.msg)
    }
  } catch (error) {
    console.error('加载错题列表失败:', error)
    ElMessage.error('加载错题列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 为所有题目加载收藏状态
async function loadMarkStatusForAll() {
  for (let i = 0; i < wrongList.value.length; i++) {
    try {
      const res = await request.get(`/practice/checkMark/${wrongList.value[i].questionId}`)
      if (res.code === 200) {
        wrongList.value[i].isMarked = res.data ? 1 : 0
      }
    } catch (error) {
      console.error('加载收藏状态失败:', error)
      wrongList.value[i].isMarked = 0
    }
  }
}

// 加载统计信息
async function loadStatistics() {
  try {
    const res = await request.get('/wrong/statistics')
    if (res.code === 200) {
      statistics.value = res.data
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

// 刷新列表
async function refreshList() {
  await Promise.all([loadWrongList(), loadStatistics()])
  ElMessage.success('列表已刷新')
}

// 清除单个错题
async function clearWrong(item) {
  try {
    await ElMessageBox.confirm(
        `确定要将题目【${item.content?.substring(0, 30)}${item.content?.length > 30 ? '...' : ''}】标记为已掌握吗？`,
        '标记已掌握',
        {
          type: 'warning',
          confirmButtonText: '确定',
          cancelButtonText: '取消'
        }
    )

    const res = await request.delete(`/wrong/clear/${item.questionId}`)

    if (res.code === 200) {
      ElMessage.success('已标记为已掌握')
      // 从列表中移除该项
      const index = wrongList.value.findIndex(w => w.recordId === item.recordId)
      if (index !== -1) {
        wrongList.value.splice(index, 1)
      }
      // 重新加载统计
      loadStatistics()
    } else {
      ElMessage.error('操作失败: ' + res.msg)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 清空所有错题
async function clearAllWrong() {
  try {
    await ElMessageBox.confirm(
        '确定要清空所有错题记录吗？此操作不可恢复！',
        '清空确认',
        {
          type: 'error',
          confirmButtonText: '确定清空',
          cancelButtonText: '取消'
        }
    )

    const questionIds = wrongList.value.map(item => item.questionId)
    const res = await request.delete('/wrong/clearBatch', { data: questionIds })

    if (res.code === 200) {
      ElMessage.success('已清空所有错题')
      wrongList.value = []
      loadStatistics()
    } else {
      ElMessage.error('清空失败: ' + res.msg)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 跳转到练习页面（重新练习）
function goToPractice(item) {
  router.push({
    path: '/admin/practice',
    query: {
      questionId: item.questionId,
      fromWrong: true,
      mode: 'wrong'
    }
  })
}

// 开始错题专项练习
function startPracticeWrong() {
  if (wrongList.value.length === 0) {
    ElMessage.warning('暂无错题可练习')
    return
  }

  router.push({
    path: '/admin/practice',
    query: {
      mode: 'wrong',
      source: 'wrongList'
    }
  })
}

// 跳转到练习页面
function goToPracticePage() {
  router.push('/admin/practice')
}

// 收藏/取消收藏题目
async function toggleMark(item) {
  try {
    const res = await request.post('/practice/mark', null, {
      params: { questionId: item.questionId }
    })

    if (res.code === 200) {
      // 切换收藏状态
      const newMarkStatus = item.isMarked === 1 ? 0 : 1
      item.isMarked = newMarkStatus

      ElMessage.success(newMarkStatus === 1 ? '已添加到收藏' : '已取消收藏')
    } else {
      ElMessage.error('操作失败: ' + res.msg)
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败: ' + error.message)
  }
}

// 表格选择变化
function handleSelectionChange(rows) {
  selectionRows.value = rows
}

// 批量标记掌握
async function batchClear() {
  if (selectionRows.value.length === 0) return

  try {
    await ElMessageBox.confirm(
        `确定要批量标记${selectionRows.value.length}道题目为已掌握吗？`,
        '批量操作确认',
        {
          type: 'warning',
          confirmButtonText: '确定',
          cancelButtonText: '取消'
        }
    )

    const questionIds = selectionRows.value.map(item => item.questionId)
    const res = await request.delete('/wrong/clearBatch', { data: questionIds })

    if (res.code === 200) {
      ElMessage.success(`已批量标记${questionIds.length}道题目为已掌握`)
      // 重新加载列表
      await refreshList()
      selectionRows.value = []
    } else {
      ElMessage.error('批量操作失败: ' + res.msg)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 批量练习
function batchPractice() {
  if (selectionRows.value.length === 0) return

  const questionIds = selectionRows.value.map(item => item.questionId)
  router.push({
    path: '/admin/practice',
    query: {
      mode: 'wrong',
      questionIds: questionIds.join(',')
    }
  })
}

// 初始化
onMounted(() => {
  refreshList()
})
</script>

<style scoped>
.wrong-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 10px;
}

.header-stats {
  min-width: 400px;
}

.action-bar {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.content-cell {
  max-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
  background-color: #f8f9fa;
  border-radius: 8px;
  margin-top: 20px;
}

.batch-actions {
  margin-top: 20px;
}

.chart-container {
  margin-top: 30px;
}

.chart-content {
  display: flex;
  flex-wrap: wrap;
  gap: 30px;
}

.chart-item {
  flex: 1;
  min-width: 300px;
}

.chart-title {
  font-weight: bold;
  margin-bottom: 15px;
  color: #333;
}

.chart-data {
  padding: 10px;
}

.distribution-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.distribution-item span:first-child {
  min-width: 60px;
  margin-right: 10px;
}

/* 收藏按钮样式 */
.is-marked {
  background-color: #ffd700 !important;
  border-color: #ffd700 !important;
  color: #333 !important;
}

.is-marked:hover {
  background-color: #ffed4e !important;
  border-color: #ffed4e !important;
}

/* 操作列样式优化 */
:deep(.el-table__cell) {
  padding: 12px 0;
}

:deep(.el-table .el-space) {
  flex-wrap: nowrap;
}

/* 确保操作按钮完整显示 */
:deep(.el-table__fixed-right) {
  height: 100% !important;
}

:deep(.el-table__fixed-right .el-table__fixed-body-wrapper) {
  height: auto !important;
}

/* 表格行样式 */
:deep(.el-table__row) {
  transition: background-color 0.3s ease;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

@media (max-width: 768px) {
  .wrong-container {
    padding: 10px;
  }

  .header-content {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }

  .header-stats {
    min-width: auto;
    width: 100%;
  }

  .chart-content {
    flex-direction: column;
  }

  .chart-item {
    min-width: auto;
  }

  /* 移动端操作列适配 */
  :deep(.el-table__fixed-right) {
    position: relative !important;
    right: auto !important;
  }

  :deep(.el-table .el-space) {
    flex-wrap: wrap;
    justify-content: center;
  }

  :deep(.el-table .el-button) {
    margin: 2px;
    font-size: 12px;
    padding: 6px 8px;
  }
}
</style>