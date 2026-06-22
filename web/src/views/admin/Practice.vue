<template>
  <div class="practice-container">
    <!-- 模式选择 -->
    <div v-if="!currentQuestion" class="mode-selection">
      <el-card class="mode-card">
        <template #header>
          <div class="mode-header">
            <h2>请选择练习模式</h2>
            <div class="mode-subtitle">
              <el-tag type="info" size="small">请根据需求选择练习方式</el-tag>
            </div>
          </div>
        </template>
        <div class="mode-options">
          <!-- 常规练习 -->
          <div class="mode-section">
            <h3 class="section-title">
              <el-icon><EditPen /></el-icon>
              常规练习
            </h3>
            <el-row :gutter="20">
              <el-col :span="12" :xs="24">
                <div class="mode-item" @click="startPractice('single')">
                  <el-card shadow="hover" class="mode-card-item">
                    <div class="mode-icon">
                      <el-icon size="40"><Select /></el-icon>
                    </div>
                    <h3>单选专练</h3>
                    <p>专门练习单选题</p>
                  </el-card>
                </div>
              </el-col>
              <el-col :span="12" :xs="24">
                <div class="mode-item" @click="startPractice('multiple')">
                  <el-card shadow="hover" class="mode-card-item">
                    <div class="mode-icon">
                      <el-icon size="40"><DataBoard /></el-icon>
                    </div>
                    <h3>多选专练</h3>
                    <p>专门练习多选题</p>
                  </el-card>
                </div>
              </el-col>
              <el-col :span="12" :xs="24">
                <div class="mode-item" @click="startPractice('judgment')">
                  <el-card shadow="hover" class="mode-card-item">
                    <div class="mode-icon">
                      <el-icon size="40"><CircleCheck /></el-icon>
                    </div>
                    <h3>判断专练</h3>
                    <p>专门练习判断题</p>
                  </el-card>
                </div>
              </el-col>
              <el-col :span="12" :xs="24">
                <div class="mode-item" @click="startPractice('mixed')">
                  <el-card shadow="hover" class="mode-card-item">
                    <div class="mode-icon">
                      <el-icon size="40"><Histogram /></el-icon>
                    </div>
                    <h3>混合训练</h3>
                    <p>包含所有题型</p>
                  </el-card>
                </div>
              </el-col>
            </el-row>
          </div>

          <!-- 错题练习 -->
          <div class="mode-section" style="margin-top: 40px;">
            <h3 class="section-title">
              <el-icon><Warning /></el-icon>
              错题专项
            </h3>
            <el-row :gutter="20">
              <el-col :span="24">
                <div class="mode-item" @click="startWrongPractice()">
                  <el-card shadow="hover" class="mode-card-item wrong-mode">
                    <div class="mode-icon">
                      <el-icon size="40"><Warning /></el-icon>
                    </div>
                    <h3>错题专项练习</h3>
                    <p>专门练习您答错的题目</p>
                    <div class="mode-stats" v-if="wrongStatistics.wrongCount > 0">
                      <el-space>
                        <el-tag type="danger" size="small">
                          当前错题：{{ wrongStatistics.wrongCount }} 道
                        </el-tag>
                        <el-tag type="warning" size="small">
                          正确率：{{ wrongStatistics.accuracy || 0 }}%
                        </el-tag>
                      </el-space>
                    </div>
                    <div class="mode-stats" v-else>
                      <el-tag type="success" size="small">暂无错题记录</el-tag>
                    </div>
                    <div class="mode-hint" v-if="wrongStatistics.wrongCount > 0">
                      <el-text type="info" size="small">
                        已为您准备好{{ wrongStatistics.wrongCount }}道错题进行专项训练
                      </el-text>
                    </div>
                  </el-card>
                </div>
              </el-col>
            </el-row>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 答题界面 -->
    <div v-else class="question-container">
      <el-card>
        <template #header>
          <div class="question-header">
            <el-space>
              <el-tag :type="getQuestionTypeTag(currentQuestion.questionType)">
                {{ currentQuestion.questionType }}
              </el-tag>
              <span>题目ID: {{ currentQuestion.questionId }}</span>
              <el-tag v-if="currentPracticeMode === 'wrong'" type="warning" size="small">
                错题专项
              </el-tag>
            </el-space>
            <div>
              <el-button @click="toggleShowAnswer" type="text">
                {{ showAnswer ? '隐藏答案' : '查看答案' }}
              </el-button>
            </div>
          </div>
        </template>

        <!-- 题目内容 -->
        <div class="question-content">
          <h3>{{ currentQuestion.content }}</h3>
        </div>

        <!-- 选项 -->
        <div class="question-options">
          <!-- 判断题 -->
          <div v-if="currentQuestion.questionType === '判断题'" class="judgment-options">
            <el-radio-group v-model="userAnswer" :disabled="showResult">
              <el-radio label="正确" size="large">
                <span class="option-text">正确</span>
              </el-radio>
              <el-radio label="错误" size="large">
                <span class="option-text">错误</span>
              </el-radio>
            </el-radio-group>
          </div>

          <!-- 单选题 -->
          <div v-else-if="currentQuestion.questionType === '单选题'" class="single-options">
            <el-radio-group v-model="userAnswer" :disabled="showResult">
              <div v-for="(option, key) in parsedOptions" :key="key"
                   class="option-item"
                   :class="getOptionClass(key)">
                <el-radio :label="key" size="large">
                  <span class="option-letter">{{ key }}.</span>
                  <span class="option-text">{{ option }}</span>
                </el-radio>
              </div>
            </el-radio-group>
          </div>

          <!-- 多选题 -->
          <div v-else-if="currentQuestion.questionType === '多选题'" class="multiple-options">
            <el-checkbox-group v-model="userAnswerArray" :disabled="showResult">
              <div v-for="(option, key) in parsedOptions" :key="key"
                   class="option-item"
                   :class="getOptionClass(key)">
                <el-checkbox :label="key">
                  <span class="option-letter">{{ key }}.</span>
                  <span class="option-text">{{ option }}</span>
                </el-checkbox>
              </div>
            </el-checkbox-group>
          </div>
        </div>

        <!-- 错题提示 -->
        <div v-if="currentPracticeMode === 'wrong' && !showResult" class="wrong-hint">
          <el-alert
              title="错题专项练习"
              type="warning"
              :closable="false"
              show-icon>
            <template #default>
              这是您之前答错的题目，请仔细思考后再作答
            </template>
          </el-alert>
        </div>

        <!-- 查看答案区域 -->
        <div v-if="showAnswer && !showResult" class="answer-preview">
          <el-alert
              title="正确答案预览"
              type="info"
              :closable="false"
          >
            <template #default>
              <div class="answer-info">
                <p><strong>正确答案：</strong>{{ correctAnswer }}</p>
                <p><strong>解析：</strong>{{ explanation || '暂无解析' }}</p>
              </div>
            </template>
          </el-alert>
        </div>

        <!-- 答案解析（提交后显示） -->
        <div v-if="showResult" class="result-panel">
          <el-alert
              :title="resultTitle"
              :type="resultType"
              show-icon
              :closable="false"
          >
            <template #default>
              <div class="result-info">
                <p><strong>你的答案：</strong>{{ submittedAnswer }}</p>
                <p><strong>正确答案：</strong>{{ correctAnswer }}</p>
                <p v-if="explanation"><strong>解析：</strong>{{ explanation }}</p>
                <div v-if="currentPracticeMode === 'wrong'" class="wrong-result">
                  <el-text v-if="!submitResult.correct" type="warning">
                    这道题已在您的错题列表中
                  </el-text>
                  <el-text v-else type="success">
                    恭喜您答对了这道错题！
                  </el-text>
                </div>
              </div>
            </template>
          </el-alert>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-space>
            <el-button
                type="primary"
                :icon="Star"
                @click="toggleMark"
                :class="{ 'is-marked': isMarked }"
                :disabled="showResult">
              {{ isMarked ? '已收藏' : '收藏' }}
            </el-button>

            <el-button
                type="primary"
                :icon="Check"
                @click="submitAnswer"
                :disabled="!canSubmit || showResult">
              提交
            </el-button>

            <el-button
                type="success"
                :icon="ArrowRight"
                @click="nextQuestion"
                :disabled="!showResult">
              下一题
            </el-button>

            <el-button
                type="info"
                :icon="Refresh"
                @click="restartPractice">
              重新开始
            </el-button>

            <el-button
                v-if="currentPracticeMode === 'wrong' && showResult && !submitResult.correct"
                type="warning"
                :icon="Warning"
                @click="markAsMastered">
              标记为已掌握
            </el-button>
          </el-space>
        </div>

        <!-- 练习统计 -->
        <div v-if="currentQuestion" class="practice-stats">
          <el-space>
            <el-text type="info" size="small">
              当前模式：{{ getPracticeModeName(currentPracticeMode) }}
            </el-text>
            <el-text v-if="currentPracticeMode === 'wrong'" type="warning" size="small">
              剩余错题：{{ wrongStatistics.wrongCount || 0 }} 道
            </el-text>
          </el-space>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import {
  Select,
  Check,
  CircleCheck,
  Histogram,
  Star,
  ArrowRight,
  Refresh,
  Warning
} from '@element-plus/icons-vue'
import request from '@/utils/http.js'
import { ElMessage } from 'element-plus'

const currentQuestion = ref(null)
const userAnswer = ref('')
const userAnswerArray = ref([])
const showResult = ref(false)
const showAnswer = ref(false)
const isMarked = ref(false)
const currentPracticeMode = ref('')  // 修改变量名，避免与mode冲突
const correctAnswer = ref('')
const explanation = ref('')
const submittedAnswer = ref('')
const submitResult = ref(null)
const wrongStatistics = ref({
  wrongCount: 0,
  accuracy: 0
})

// 计算属性
const parsedOptions = computed(() => {
  if (!currentQuestion.value?.options) return {}
  try {
    return JSON.parse(currentQuestion.value.options)
  } catch (e) {
    return {}
  }
})

const canSubmit = computed(() => {
  if (!currentQuestion.value || showResult.value) return false
  if (currentQuestion.value.questionType === '多选题') {
    return userAnswerArray.value.length > 0
  }
  return userAnswer.value !== ''
})

const userAnswerStr = computed(() => {
  if (currentQuestion.value?.questionType === '多选题') {
    return userAnswerArray.value.sort().join(',')
  }
  return userAnswer.value
})

const resultTitle = computed(() => {
  if (!submitResult.value) return ''
  return submitResult.value.correct ? '回答正确！' : '回答错误！'
})

const resultType = computed(() => {
  if (!submitResult.value) return 'info'
  return submitResult.value.correct ? 'success' : 'error'
})

// 获取练习模式名称
function getPracticeModeName(mode) {
  const modeMap = {
    'single': '单选专练',
    'multiple': '多选专练',
    'judgment': '判断专练',
    'mixed': '混合训练',
    'wrong': '错题专项'
  }
  return modeMap[mode] || '练习模式'
}

// 方法
function startPractice(mode) {
  currentPracticeMode.value = mode
  getNextQuestion()
}

// 开始错题练习
async function startWrongPractice() {
  // 先检查是否有错题
  await loadWrongStatistics()

  if (wrongStatistics.value.wrongCount === 0) {
    ElMessage.warning('您目前没有错题记录，请先进行常规练习')
    return
  }

  currentPracticeMode.value = 'wrong'
  getNextQuestion()
}

async function getNextQuestion() {
  try {
    const params = { mode: currentPracticeMode.value }

    const res = await request.get('/practice/next', {
      params: params
    })

    if (res.code === 200) {
      currentQuestion.value = res.data
      resetAnswer()
      showResult.value = false
      showAnswer.value = false
      // 获取收藏状态
      await checkMarkStatus()
      // 重置答案相关信息
      correctAnswer.value = ''
      explanation.value = ''
      submittedAnswer.value = ''
      submitResult.value = null

      // 如果是错题模式，显示提示
      if (currentPracticeMode.value === 'wrong') {
        ElMessage.info('正在练习错题，请仔细作答')
      }
    }
  } catch (error) {
    ElMessage.error('获取题目失败：' + error.message)

    // 如果是错题模式且没有题目，返回模式选择
    if (error.response?.data?.msg?.includes('暂无错题')) {
      ElMessage.warning('暂无错题可练习')
      restartPractice()
    }
  }
}

// 加载错题统计
async function loadWrongStatistics() {
  try {
    const res = await request.get('/practice/wrongStatistics')
    if (res.code === 200) {
      wrongStatistics.value = res.data
    }
  } catch (error) {
    console.error('加载错题统计失败:', error)
    // 如果接口不存在，使用默认值
    wrongStatistics.value = {
      wrongCount: 0,
      accuracy: 0
    }
  }
}

function resetAnswer() {
  userAnswer.value = ''
  userAnswerArray.value = []
}

async function submitAnswer() {
  if (!canSubmit.value) return

  const data = {
    questionId: currentQuestion.value.questionId,
    userAnswer: userAnswerStr.value
  }

  try {
    const res = await request.post('/practice/submit', data)

    if (res.code === 200) {
      submitResult.value = res.data
      showResult.value = true
      submittedAnswer.value = userAnswerStr.value
      correctAnswer.value = res.data.correctAnswer
      explanation.value = res.data.explanation
      // 更新收藏状态
      isMarked.value = res.data.marked || false

      // 如果是错题模式，更新统计
      if (currentPracticeMode.value === 'wrong') {
        await loadWrongStatistics()
      }

      ElMessage.success('答案已提交')
    }
  } catch (error) {
    ElMessage.error('提交失败：' + error.message)
  }
}

function nextQuestion() {
  // 如果是错题模式且没有错题了，提示用户
  if (currentPracticeMode.value === 'wrong' && wrongStatistics.value.wrongCount === 0) {
    ElMessage.success('恭喜！所有错题都已练习完成')
    restartPractice()
    return
  }
  getNextQuestion()
}

function restartPractice() {
  currentQuestion.value = null
  currentPracticeMode.value = ''
  resetAnswer()
  showResult.value = false
  showAnswer.value = false
  // 重新加载错题统计
  loadWrongStatistics()
}

async function toggleMark() {
  try {
    const res = await request.post('/practice/mark', null, {
      params: { questionId: currentQuestion.value.questionId }
    })

    if (res.code === 200) {
      isMarked.value = !isMarked.value
      ElMessage.success(isMarked.value ? '已添加到收藏' : '已取消收藏')
    }
  } catch (error) {
    ElMessage.error('操作失败：' + error.message)
  }
}

async function toggleShowAnswer() {
  if (showAnswer.value) {
    // 如果当前显示答案，则隐藏
    showAnswer.value = false
  } else {
    // 如果当前隐藏答案，则显示
    showAnswer.value = true

    // 如果还没有获取答案，则从服务器获取
    if (!correctAnswer.value) {
      try {
        const res = await request.get(`/practice/answer/${currentQuestion.value.questionId}`)
        if (res.code === 200) {
          correctAnswer.value = res.data.answer
          explanation.value = res.data.explanation
        }
      } catch (error) {
        ElMessage.error('获取答案失败：' + error.message)
      }
    }
  }
}

// 标记为已掌握（错题模式专用）
async function markAsMastered() {
  try {
    const res = await request.delete(`/wrong/clear/${currentQuestion.value.questionId}`)
    if (res.code === 200) {
      ElMessage.success('已标记为已掌握')
      // 更新统计
      await loadWrongStatistics()
      // 自动下一题
      nextQuestion()
    }
  } catch (error) {
    ElMessage.error('操作失败：' + error.message)
  }
}

async function checkMarkStatus() {
  if (!currentQuestion.value) return

  try {
    const res = await request.get(`/practice/checkMark/${currentQuestion.value.questionId}`)
    if (res.code === 200) {
      isMarked.value = res.data
    } else {
      isMarked.value = false
    }
  } catch (error) {
    console.error('检查收藏状态失败:', error)
    isMarked.value = false
  }
}

// 获取选项样式类
function getOptionClass(optionKey) {
  if (!showResult.value || !submitResult.value) return ''

  const userAnswers = submittedAnswer.value.split(',')
  const correctAnswers = correctAnswer.value.split(',')

  if (submitResult.value.correct) {
    // 如果回答正确，所有用户选择的选项都标记为正确
    if (userAnswers.includes(optionKey)) {
      return 'option-correct'
    }
  } else {
    // 如果回答错误
    if (correctAnswers.includes(optionKey)) {
      // 正确答案标记为正确
      return 'option-correct'
    } else if (userAnswers.includes(optionKey)) {
      // 用户选错的答案标记为错误
      return 'option-wrong'
    }
  }

  return ''
}

function getQuestionTypeTag(type) {
  switch (type) {
    case '单选题': return 'primary'
    case '多选题': return 'success'
    case '判断题': return 'warning'
    default: return 'info'
  }
}

// 监听多选题数组变化
watch(userAnswerArray, (newVal) => {
  if (newVal.length > 0) {
    userAnswer.value = newVal.sort().join(',')
  }
})

// 监听currentQuestion变化，自动检查收藏状态
watch(() => currentQuestion.value, () => {
  if (currentQuestion.value) {
    checkMarkStatus()
  }
})

// 初始化
onMounted(() => {
  loadWrongStatistics()
})
</script>

<style scoped>
.practice-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.mode-selection {
  max-width: 900px;
  margin: 0 auto;
}

.mode-header {
  text-align: center;
  padding: 20px 0;
}

.mode-header h2 {
  margin-bottom: 10px;
  color: #333;
}

.mode-subtitle {
  margin-top: 10px;
}

.mode-options {
  padding: 20px;
}

.mode-section {
  margin-bottom: 30px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  color: #333;
  font-size: 18px;
  border-left: 4px solid #409EFF;
  padding-left: 12px;
}

.mode-section:last-child .section-title {
  border-left-color: #E6A23C;
}

.mode-item {
  cursor: pointer;
  margin-bottom: 20px;
}

.mode-card-item {
  text-align: center;
  padding: 30px 20px;
  transition: all 0.3s ease;
  height: 100%;
  position: relative;
}

.mode-card-item.wrong-mode {
  border: 2px solid #fdf6ec;
  background: linear-gradient(135deg, #fdf6ec 0%, #f5dab1 100%);
}

.mode-card-item.wrong-mode:hover {
  border-color: #e6a23c;
  transform: translateY(-5px);
}

.mode-card-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1) !important;
}

.mode-icon {
  margin-bottom: 15px;
  color: #409EFF;
}

.mode-card-item.wrong-mode .mode-icon {
  color: #e6a23c;
}

.mode-card-item h3 {
  margin: 10px 0;
  color: #333;
}

.mode-card-item p {
  color: #666;
  font-size: 14px;
  margin-bottom: 15px;
}

.mode-stats {
  margin: 15px 0;
}

.mode-hint {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #eee;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.question-content {
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.question-content h3 {
  margin: 0;
  line-height: 1.6;
  color: #333;
}

.question-options {
  margin: 30px 0;
}

.option-item {
  margin: 15px 0;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.option-item:hover {
  background: #e9ecef;
}

.option-letter {
  font-weight: bold;
  color: #409EFF;
  margin-right: 8px;
}

.option-text {
  font-size: 16px;
  line-height: 1.5;
}

.judgment-options,
.single-options,
.multiple-options {
  margin-top: 20px;
}

.wrong-hint {
  margin: 20px 0;
}

.result-panel {
  margin: 30px 0;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.wrong-result {
  margin-top: 15px;
  padding: 10px;
  background: rgba(230, 162, 60, 0.1);
  border-radius: 4px;
}

.answer-preview {
  margin: 20px 0;
  padding: 15px;
  background-color: #f0f9ff;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.answer-info p {
  margin: 8px 0;
}

.result-info p {
  margin: 8px 0;
}

.action-buttons {
  margin: 30px 0;
  text-align: center;
}

.practice-stats {
  margin-top: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  text-align: center;
}

/* 选项样式 */
.option-correct {
  background-color: #f0f9eb !important;
  border-left: 4px solid #67c23a;
}

.option-wrong {
  background-color: #fef0f0 !important;
  border-left: 4px solid #f56c6c;
}

.option-correct :deep(.el-radio__label),
.option-correct :deep(.el-checkbox__label) {
  color: #67c23a;
  font-weight: bold;
}

.option-wrong :deep(.el-radio__label),
.option-wrong :deep(.el-checkbox__label) {
  color: #f56c6c;
  font-weight: bold;
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

/* 响应式调整 */
@media (max-width: 768px) {
  .mode-selection {
    padding: 10px;
  }

  .mode-card-item {
    padding: 20px 10px;
  }

  .question-content,
  .option-item {
    padding: 15px;
  }

  .action-buttons .el-space {
    flex-wrap: wrap;
    justify-content: center;
  }

  .action-buttons .el-button {
    margin: 5px;
  }
}

/* 添加一些动画效果 */
.option-item {
  transition: all 0.3s ease;
}

.option-item:hover {
  transform: translateX(5px);
}

:deep(.el-radio.is-disabled .el-radio__label) {
  color: inherit !important;
}

:deep(.el-checkbox.is-disabled .el-checkbox__label) {
  color: inherit !important;
}
</style>