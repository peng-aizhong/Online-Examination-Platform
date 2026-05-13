<template>
  <div class="student-page" v-loading="loading">
    <el-card v-if="assignment">
      <template #header>
        <div class="header-row">
          <div>
            <h2>{{ assignment.assignmentName }}</h2>
            <p>{{ assignment.paperName }} | 总分: {{ assignment.totalScore }}</p>
          </div>
          <el-tag type="danger">倒计时：{{ countdownText }}</el-tag>
        </div>
      </template>

      <el-form label-position="top">
        <div v-for="(question, index) in assignment.questions" :key="question.questionId" class="question-card">
          <h3>{{ index + 1 }}. {{ question.content }}（{{ question.score }}分）
            <el-tag size="small" type="info" v-if="question.knowledgeTag" style="margin-left: 8px;">{{ question.knowledgeTag }}</el-tag>
            <el-tag size="small" v-if="question.questionType === 'single'" style="margin-left: 4px;">单选题</el-tag>
            <el-tag size="small" type="warning" v-if="question.questionType === 'multiple'" style="margin-left: 4px;">多选题</el-tag>
            <el-tag size="small" type="success" v-if="question.questionType === 'judge'" style="margin-left: 4px;">判断题</el-tag>
            <el-tag size="small" type="danger" v-if="question.questionType === 'fill'" style="margin-left: 4px;">填空题</el-tag>
            <el-tag size="small" type="info" v-if="question.questionType === 'subjective'" style="margin-left: 4px;">主观题</el-tag>
          </h3>

          <!-- 单选题 -->
          <el-radio-group v-if="question.questionType === 'single' || !question.questionType" v-model="answers[question.questionId]">
            <el-radio value="A">A. {{ question.optionA }}</el-radio>
            <el-radio value="B">B. {{ question.optionB }}</el-radio>
            <el-radio value="C">C. {{ question.optionC }}</el-radio>
            <el-radio value="D">D. {{ question.optionD }}</el-radio>
          </el-radio-group>

          <!-- 多选题 -->
          <el-checkbox-group v-else-if="question.questionType === 'multiple'" v-model="multiAnswers[question.questionId]">
            <el-checkbox value="A">A. {{ question.optionA }}</el-checkbox>
            <el-checkbox value="B">B. {{ question.optionB }}</el-checkbox>
            <el-checkbox value="C">C. {{ question.optionC }}</el-checkbox>
            <el-checkbox value="D">D. {{ question.optionD }}</el-checkbox>
          </el-checkbox-group>

          <!-- 判断题 -->
          <el-radio-group v-else-if="question.questionType === 'judge'" v-model="answers[question.questionId]">
            <el-radio value="true">正确</el-radio>
            <el-radio value="false">错误</el-radio>
          </el-radio-group>

          <!-- 填空题 -->
          <el-input v-else-if="question.questionType === 'fill'" v-model="answers[question.questionId]" placeholder="请输入答案" style="max-width: 400px;" />

          <!-- 主观题 -->
          <el-input v-else-if="question.questionType === 'subjective'" type="textarea" :rows="4" v-model="answers[question.questionId]" placeholder="请输入你的回答" />
        </div>
      </el-form>

      <div class="actions">
        <el-button @click="goBack">返回</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">提交试卷</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStudentAssignmentDetail, submitStudentExam } from '../../api/student'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitLoading = ref(false)
const assignment = ref(null)
const sessionId = ref(null)
const answers = reactive({})
const multiAnswers = reactive({})
const secondsLeft = ref(0)
let timer = null

const countdownText = computed(() => {
  const minutes = Math.floor(secondsLeft.value / 60)
  const seconds = secondsLeft.value % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})

const startTimer = () => {
  if (timer) clearInterval(timer)
  timer = setInterval(() => {
    if (secondsLeft.value <= 0) {
      clearInterval(timer)
      handleSubmit(true)
      return
    }
    secondsLeft.value -= 1
  }, 1000)
}

const loadAssignment = async () => {
  loading.value = true
  try {
    const res = await getStudentAssignmentDetail(route.params.assignmentId)
    assignment.value = res.data
    sessionId.value = res.data.sessionId
    secondsLeft.value = (assignment.value?.durationMinutes || 0) * 60
    for (const q of assignment.value.questions || []) {
      if (q.questionType === 'multiple') {
        multiAnswers[q.questionId] = []
      }
    }
    startTimer()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '加载考试失败')
    router.push('/student/exams')
  } finally {
    loading.value = false
  }
}

const buildSubmitPayload = () => {
  const list = assignment.value?.questions || []
  return list.map((q) => {
    let answer = ''
    if (q.questionType === 'multiple') {
      const selected = multiAnswers[q.questionId] || []
      answer = [...selected].sort().join(',')
    } else {
      answer = answers[q.questionId] || ''
    }
    return { questionId: q.questionId, answer }
  })
}

const handleSubmit = async (force = false) => {
  if (!assignment.value) return

  if (!force) {
    try {
      await ElMessageBox.confirm('确认提交试卷吗？提交后不可修改。', '提示', {
        confirmButtonText: '确认提交',
        cancelButtonText: '取消',
        type: 'warning'
      })
    } catch {
      return
    }
  }

  submitLoading.value = true
  try {
    const res = await submitStudentExam(assignment.value.assignmentId, sessionId.value, buildSubmitPayload())
    ElMessage.success(`提交成功，得分 ${res.data.score}/${res.data.totalScore}`)
    router.push('/student/scores')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '提交失败')
  } finally {
    submitLoading.value = false
  }
}

const goBack = () => router.push('/student/exams')

onMounted(loadAssignment)
onBeforeUnmount(() => { if (timer) clearInterval(timer) })
</script>

<style scoped>
.student-page { padding: 20px; }
.header-row { display: flex; justify-content: space-between; align-items: center; gap: 16px; }
.question-card { border: 1px solid #ebeef5; border-radius: 8px; padding: 16px; margin-bottom: 16px; }
.actions { display: flex; justify-content: flex-end; gap: 12px; }
</style>
