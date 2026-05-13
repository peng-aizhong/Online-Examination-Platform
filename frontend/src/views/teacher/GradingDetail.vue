<template>
  <div class="grading-detail-page">
    <el-page-header @back="goBack" content="阅卷详情" />

    <div v-loading="loading" class="grading-content">
      <el-card v-if="results.length > 0" class="section-card">
        <template #header><span>考试：{{ results[0]?.assignmentName }} | 试卷：{{ results[0]?.paperName }}</span></template>
        <el-table :data="results" border>
          <el-table-column prop="studentName" label="学生" width="120" />
          <el-table-column prop="attemptNumber" label="第几次" width="80" />
          <el-table-column label="得分" width="120">
            <template #default="scope">{{ scope.row.score }}/{{ scope.row.totalScore }}</template>
          </el-table-column>
          <el-table-column label="正确率" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.accuracy >= 60 ? 'success' : 'danger'">{{ scope.row.accuracy }}%</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="提交时间" min-width="170">
            <template #default="scope">{{ scope.row.submittedAt ? dayjs(scope.row.submittedAt).format('YYYY-MM-DD HH:mm:ss') : '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button type="primary" link @click="showDetail(scope.row)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-empty v-else-if="!loading" description="暂无阅卷数据" />

      <el-dialog v-model="dialogVisible" title="学生答题详情" width="800px" top="5vh">
        <template v-if="selectedResult">
          <el-descriptions :column="3" border>
            <el-descriptions-item label="学生">{{ selectedResult.studentName }}</el-descriptions-item>
            <el-descriptions-item label="得分">{{ selectedResult.score }}/{{ selectedResult.totalScore }}</el-descriptions-item>
            <el-descriptions-item label="正确率">{{ selectedResult.accuracy }}%</el-descriptions-item>
          </el-descriptions>

          <el-divider />
          <h4>题型得分分布</h4>
          <el-table :data="typeBreakdownList" border size="small" style="margin-bottom: 20px;">
            <el-table-column prop="typeLabel" label="题型" width="120" />
            <el-table-column prop="total" label="题数" width="80" />
            <el-table-column prop="correct" label="正确数" width="80" />
            <el-table-column label="得分" width="120">
              <template #default="scope">{{ scope.row.earned }}/{{ scope.row.possible }}</template>
            </el-table-column>
          </el-table>

          <h4>逐题详情</h4>
          <div v-for="(q, index) in selectedResult.questions" :key="q.questionId" class="question-item">
            <div class="question-header">
              <span class="question-order">第{{ index + 1 }}题</span>
              <el-tag size="small" :type="q.correct ? 'success' : 'danger'">{{ q.correct ? '正确' : '错误' }}</el-tag>
              <el-tag size="small" type="info" v-if="q.knowledgeTag">{{ q.knowledgeTag }}</el-tag>
              <el-tag size="small" type="warning" v-if="q.questionType === 'subjective'">主观题</el-tag>
              <span class="question-score">{{ q.earned }}/{{ q.score }}分</span>
            </div>
            <p class="question-content">{{ q.content }}</p>
            <div class="answer-row">
              <span>学生答案：<strong :class="q.correct ? 'correct-text' : 'wrong-text'">{{ q.yourAnswer || '未作答' }}</strong></span>
              <span v-if="!q.correct && q.questionType !== 'subjective'">正确答案：<strong class="correct-text">{{ q.correctAnswer }}</strong></span>
            </div>

            <!-- 主观题评分区域 -->
            <div v-if="q.questionType === 'subjective' && gradingScores[q.questionId]" class="grading-box">
              <el-row :gutter="16">
                <el-col :span="6">
                  <label>评分：</label>
                  <el-input-number v-model="gradingScores[q.questionId].score" :min="0" :max="q.score" size="small" style="width:100%" />
                </el-col>
                <el-col :span="18">
                  <label>评语：</label>
                  <el-input v-model="gradingScores[q.questionId].feedback" type="textarea" :rows="2" placeholder="请输入评语" />
                </el-col>
              </el-row>
            </div>

            <div v-if="q.feedback && q.questionType !== 'subjective'" class="analysis-box">
              <strong>解析：</strong>{{ q.feedback }}
            </div>
          </div>

          <div v-if="selectedResult.questions.some(q => q.questionType === 'subjective')" style="text-align:right;margin-top:16px;">
            <el-button type="primary" :loading="gradingLoading" @click="saveGrading">保存主观题评分</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { getTeacherGradingResults, gradeSubjective } from '../../api/teacher'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const results = ref([])
const dialogVisible = ref(false)
const selectedResult = ref(null)
const gradingScores = ref({})
const gradingLoading = ref(false)

const typeNames = { single: '单选题', multiple: '多选题', judge: '判断题', fill: '填空题', subjective: '主观题' }

const typeBreakdownList = computed(() => {
  if (!selectedResult.value?.typeBreakdowns) return []
  return Object.values(selectedResult.value.typeBreakdowns).map(tb => ({
    ...tb,
    typeLabel: typeNames[tb.type] || tb.type
  }))
})

const goBack = () => router.push('/teacher/analytics')

const showDetail = (result) => {
  selectedResult.value = result
  const scores = {}
  for (const q of result.questions || []) {
    if (q.questionType === 'subjective') {
      scores[q.questionId] = {
        score: q.earned || 0,
        feedback: q.feedback || ''
      }
    }
  }
  gradingScores.value = scores
  dialogVisible.value = true
}

const loadGrading = async () => {
  loading.value = true
  try {
    const res = await getTeacherGradingResults(route.params.assignmentId)
    results.value = res.data || []
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '加载阅卷数据失败')
  } finally {
    loading.value = false
  }
}

const saveGrading = async () => {
  if (!selectedResult.value) return
  const items = Object.entries(gradingScores.value).map(([questionId, g]) => ({
    questionId, score: g.score, feedback: g.feedback
  }))
  if (items.length === 0) return
  gradingLoading.value = true
  try {
    const res = await gradeSubjective(selectedResult.value.sessionId, { items })
    if (res.code === 200) {
      ElMessage.success('评分保存成功')
      dialogVisible.value = false
      loadGrading()
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '保存失败')
  } finally {
    gradingLoading.value = false
  }
}

onMounted(loadGrading)
</script>

<style scoped>
.grading-detail-page { padding: 20px; }
.grading-content { margin-top: 20px; }
.section-card { margin-bottom: 20px; }
.question-item { padding: 14px; border-bottom: 1px solid #ebeef5; }
.question-item:last-child { border-bottom: none; }
.question-header { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.question-order { font-weight: bold; color: #333; }
.question-score { margin-left: auto; color: #666; }
.question-content { margin: 6px 0; color: #333; line-height: 1.6; }
.answer-row { display: flex; gap: 30px; font-size: 14px; color: #666; }
.correct-text { color: #67c23a; }
.wrong-text { color: #f56c6c; }
.analysis-box { margin-top: 8px; padding: 10px; background: #f5f7fa; border-radius: 6px; font-size: 13px; color: #666; line-height: 1.6; }
.grading-box { margin-top: 12px; padding: 14px; background: #fdf6ec; border: 1px solid #e6a23c; border-radius: 6px; }
.grading-box label { font-size: 13px; color: #606266; margin-bottom: 4px; display: block; }
</style>
