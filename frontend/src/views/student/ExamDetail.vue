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
          </h3>
          <el-radio-group v-model="answers[question.questionId]">
            <el-radio value="A">A. {{ question.optionA }}</el-radio>
            <el-radio value="B">B. {{ question.optionB }}</el-radio>
            <el-radio value="C">C. {{ question.optionC }}</el-radio>
            <el-radio value="D">D. {{ question.optionD }}</el-radio>
          </el-radio-group>
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
  return list.map((q) => ({
    questionId: q.questionId,
    answer: answers[q.questionId] || ''
  }))
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
