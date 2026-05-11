<template>
  <div class="score-report-page">
    <el-page-header @back="goBack" :content="`成绩报告 - ${report.assignmentName || ''}`" />

    <div v-loading="loading" class="report-content">
      <template v-if="report.sessionId">
        <el-row :gutter="20" class="overview-cards">
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-value primary">{{ report.score }}</div>
                <div class="stat-label">得分 / {{ report.totalScore }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-value success">{{ report.accuracy }}%</div>
                <div class="stat-label">正确率</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-value warning">{{ report.rank }}</div>
                <div class="stat-label">排名 / {{ report.totalParticipants }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-value info">{{ report.submittedAt ? dayjs(report.submittedAt).format('MM-DD HH:mm') : '-' }}</div>
                <div class="stat-label">提交时间</div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="20" class="chart-row">
          <el-col :span="12">
            <el-card>
              <template #header><span>知识点掌握度雷达图</span></template>
              <div ref="radarChartRef" style="height: 350px;"></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card>
              <template #header><span>题型得分率</span></template>
              <div ref="barChartRef" style="height: 350px;"></div>
            </el-card>
          </el-col>
        </el-row>

        <el-card class="section-card">
          <template #header><span>知识点得分明细</span></template>
          <el-table :data="kpList" border>
            <el-table-column prop="knowledgeTag" label="知识点" min-width="180" />
            <el-table-column label="得分" width="120">
              <template #default="scope">{{ scope.row.earned }}/{{ scope.row.total }}</template>
            </el-table-column>
            <el-table-column label="正确率" width="120">
              <template #default="scope">
                <el-tag :type="scope.row.accuracy >= 60 ? 'success' : 'danger'">{{ scope.row.accuracy }}%</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="题数" width="100">
              <template #default="scope">{{ scope.row.correctCount }}/{{ scope.row.count }}</template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card class="section-card">
          <template #header>
            <div class="header-row">
              <span>答题详情</span>
              <el-radio-group v-model="viewMode" size="small">
                <el-radio-button value="simple">简洁版</el-radio-button>
                <el-radio-button value="detail">详细版</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div v-for="(q, index) in report.questions" :key="q.questionId" class="question-item">
            <div class="question-header">
              <span class="question-order">第{{ index + 1 }}题</span>
              <el-tag size="small" :type="q.correct ? 'success' : 'danger'">{{ q.correct ? '正确' : '错误' }}</el-tag>
              <el-tag size="small" type="info" v-if="q.knowledgeTag">{{ q.knowledgeTag }}</el-tag>
              <span class="question-score">{{ q.earned }}/{{ q.score }}分</span>
            </div>
            <p class="question-content">{{ q.content }}</p>
            <div class="answer-row">
              <span>你的答案：<strong :class="q.correct ? 'correct-text' : 'wrong-text'">{{ q.yourAnswer || '未作答' }}</strong></span>
              <span v-if="!q.correct">正确答案：<strong class="correct-text">{{ q.correctAnswer }}</strong></span>
            </div>
            <div v-if="viewMode === 'detail' && q.feedback" class="analysis-box">
              <strong>解析：</strong>{{ q.feedback }}
            </div>
          </div>
        </el-card>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import * as echarts from 'echarts'
import { getStudentScoreReport } from '../../api/student'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const report = ref({})
const viewMode = ref('simple')
const kpList = ref([])
const radarChartRef = ref(null)
const barChartRef = ref(null)

const goBack = () => router.push('/student/scores')

const loadReport = async () => {
  loading.value = true
  try {
    const res = await getStudentScoreReport(route.params.sessionId)
    report.value = res.data || {}
    kpList.value = Object.values(report.value.knowledgePointScores || {})
    await nextTick()
    renderCharts()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '加载报告失败')
  } finally {
    loading.value = false
  }
}

const renderCharts = () => {
  const kpValues = Object.values(report.value.knowledgePointScores || {})
  if (kpValues.length > 0 && radarChartRef.value) {
    const radarChart = echarts.init(radarChartRef.value)
    radarChart.setOption({
      tooltip: {},
      radar: {
        indicator: kpValues.map(kp => ({ name: kp.knowledgeTag, max: 100 })),
        radius: '65%'
      },
      series: [{
        type: 'radar',
        data: [{
          value: kpValues.map(kp => kp.accuracy),
          name: '掌握度',
          areaStyle: { opacity: 0.2 }
        }]
      }]
    })
  }

  const typeValues = Object.values(report.value.typeScores || {})
  if (typeValues.length > 0 && barChartRef.value) {
    const barChart = echarts.init(barChartRef.value)
    const typeNames = { single: '单选题', multiple: '多选题', judge: '判断题', fill: '填空题', subjective: '主观题' }
    barChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: typeValues.map(t => typeNames[t.type] || t.type) },
      yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
      series: [{
        type: 'bar',
        data: typeValues.map(t => ({
          value: t.total === 0 ? 0 : Math.round(t.earned * 100 / t.total),
          itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#667eea' }, { offset: 1, color: '#764ba2' }
          ])}
        })),
        barWidth: '40%',
        label: { show: true, position: 'top', formatter: '{c}%' }
      }]
    })
  }
}

onMounted(loadReport)
</script>

<style scoped>
.score-report-page { padding: 20px; }
.report-content { margin-top: 20px; }
.overview-cards { margin-bottom: 20px; }
.stat-item { text-align: center; padding: 10px 0; }
.stat-value { font-size: 28px; font-weight: bold; }
.stat-value.primary { color: #667eea; }
.stat-value.success { color: #67c23a; }
.stat-value.warning { color: #e6a23c; }
.stat-value.info { color: #909399; font-size: 18px; }
.stat-label { font-size: 13px; color: #999; margin-top: 6px; }
.chart-row { margin-bottom: 20px; }
.section-card { margin-bottom: 20px; }
.header-row { display: flex; justify-content: space-between; align-items: center; }
.question-item { padding: 16px; border-bottom: 1px solid #ebeef5; }
.question-item:last-child { border-bottom: none; }
.question-header { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.question-order { font-weight: bold; color: #333; }
.question-score { margin-left: auto; color: #666; }
.question-content { margin: 8px 0; color: #333; line-height: 1.6; }
.answer-row { display: flex; gap: 30px; font-size: 14px; color: #666; }
.correct-text { color: #67c23a; }
.wrong-text { color: #f56c6c; }
.analysis-box { margin-top: 10px; padding: 12px; background: #f5f7fa; border-radius: 6px; font-size: 13px; color: #666; line-height: 1.6; }
</style>
