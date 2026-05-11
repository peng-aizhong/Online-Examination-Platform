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
              <span class="question-score">{{ q.earned }}/{{ q.score }}分</span>
            </div>
            <p class="question-content">{{ q.content }}</p>
            <div class="answer-row">
              <span>学生答案：<strong :class="q.correct ? 'correct-text' : 'wrong-text'">{{ q.yourAnswer || '未作答' }}</strong></span>
              <span v-if="!q.correct">正确答案：<strong class="correct-text">{{ q.correctAnswer }}</strong></span>
            </div>
            <div v-if="q.feedback" class="analysis-box">
              <strong>解析：</strong>{{ q.feedback }}
            </div>
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
import { getTeacherGradingResults } from '../../api/teacher'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const results = ref([])
const dialogVisible = ref(false)
const selectedResult = ref(null)

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
</style>
