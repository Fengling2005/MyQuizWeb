<template>
  <div class="marked-container">
    <el-card>
      <template #header>
        <div class="header-content">
          <h2>我的收藏</h2>
          <el-tag type="primary" size="large">
            共 {{ markedList.length }} 道收藏题目
          </el-tag>
        </div>
      </template>

      <el-table
          :data="markedList"
          border
          style="width: 100%"
          v-loading="loading"
          :default-sort="{ prop: 'recordId', order: 'descending' }">

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
            <span v-if="scope.row.userAnswer && scope.row.userAnswer.trim() !== ''">
              {{ scope.row.userAnswer }}
            </span>
            <span v-else style="color: #999;">未答题</span>
          </template>
        </el-table-column>

        <el-table-column prop="isCorrect" label="是否正确" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.userAnswer && scope.row.userAnswer.trim() !== ''"
                    :type="scope.row.isCorrect === 1 ? 'success' : 'danger'">
              {{ scope.row.isCorrect === 1 ? '正确' : '错误' }}
            </el-tag>
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-space>
              <el-button
                  type="primary"
                  size="small"
                  @click="goToPractice(scope.row)"
                  :icon="Select">
                练习
              </el-button>
              <el-button
                  type="danger"
                  size="small"
                  @click="removeMark(scope.row)"
                  :icon="Star">
                取消收藏
              </el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="markedList.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无收藏的题目" />
        <div style="text-align: center; margin-top: 20px;">
          <el-button type="primary" @click="goToPracticePage">
            去练习题目
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Select, Star } from '@element-plus/icons-vue'
import request from '@/utils/http.js'

const router = useRouter()
const markedList = ref([])
const loading = ref(false)

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

// 加载收藏列表
async function loadMarkedQuestions() {
  loading.value = true
  try {
    const res = await request.get('/mark/list')
    if (res.code === 200) {
      markedList.value = res.data
      console.log('收藏列表:', res.data) // 调试用
    } else {
      ElMessage.error('加载收藏列表失败: ' + res.msg)
    }
  } catch (error) {
    console.error('加载收藏列表失败:', error)
    ElMessage.error('加载收藏列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 取消收藏
async function removeMark(item) {
  try {
    await ElMessageBox.confirm(
        `确定要取消收藏题目【${item.content?.substring(0, 30)}${item.content?.length > 30 ? '...' : ''}】吗？`,
        '取消收藏确认',
        {
          type: 'warning',
          confirmButtonText: '确定',
          cancelButtonText: '取消'
        }
    )

    const res = await request.delete(`/mark/remove/${item.questionId}`)

    if (res.code === 200) {
      ElMessage.success('已取消收藏')
      // 从列表中移除该项
      const index = markedList.value.findIndex(m => m.recordId === item.recordId)
      if (index !== -1) {
        markedList.value.splice(index, 1)
      }
    } else {
      ElMessage.error('取消收藏失败: ' + res.msg)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 跳转到练习页面
function goToPractice(item) {
  router.push({
    path: '/admin/practice',
    query: {
      questionId: item.questionId,
      fromMarked: true
    }
  })
}

// 跳转到练习页面
function goToPracticePage() {
  router.push('/admin/practice')
}

onMounted(() => {
  loadMarkedQuestions()
})
</script>

<style scoped>
.marked-container {
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

:deep(.el-table__row) {
  transition: background-color 0.3s ease;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

@media (max-width: 768px) {
  .marked-container {
    padding: 10px;
  }

  .header-content {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }

  :deep(.el-table) {
    font-size: 12px;
  }
}
</style>