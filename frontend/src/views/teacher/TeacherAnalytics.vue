<template>
  <div class="teacher-analytics-page">
    <el-page-header @back="goBack" content="学情分析" />

    <div v-loading="loading" class="analytics-content">
      <template v-if="stats.totalAssignments !== undefined">
        <el-row :gutter="20" class="overview-cards">
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-value primary">{{ stats.totalStudents }}</div>
                <div class="stat-label">参考学生数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-value success">{{ stats.averageScore }}</div>
                <div class="stat-label">平均分</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-value warning">{{ stats.passRate }}%</div>
                <div class="stat-label">及格率</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-value info">{{ stats.highestScore }} / {{ stats.lowestScore }}</div>
                <div class="stat-label">最高分 / 最低分</div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="20" class="chart-row">
          <el-col :span="12">
            <el-card>
              <template #header><span>成绩分布直方图</span></template>
              <div ref="distributionChartRef" style="height: 350px;"></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card>
              <template #header><span>薄弱知识点统计</span></template>
              <div ref="weakChartRef" style="height: 350px;"></div>
            </el-card>
          </el-col>
        </el-row>

        <el-card class="section-card">
          <template #header><span>各考试成绩概览</span></template>
          <el-table :data="stats.assignmentStats" border>
            <el-table-column prop="assignmentName" label="考试名称" min-width="180" />
            <el-table-column prop="paperName" label="试卷" min-width="150" />
            <el-table-column prop="participantCount" label="参考人数" width="100" />
            <el-table-column label="平均分" width="100">
              <template #default="scope">{{ scope.row.averageScore }}</template>
            </el-table-column>
            <el-table-column label="最高分" width="100">
              <template #default="scope">{{ scope.row.highestScore }}</template>
            </el-table-column>
            <el-table-column label="最低分" width="100">
              <template #default="scope">{{ scope.row.lowestScore }}</template>
            </el-table-column>
            <el-table-column label="及格率" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.passRate >= 60 ? 'success' : 'danger'">{{ scope.row.passRate }}%</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button type="primary" link @click="viewGrading(scope.row.assignmentId)">查看阅卷</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card class="section-card">
          <template #header><span>知识点正确率排序（从低到高）</span></template>
          <el-table :data="stats.weakKnowledgePoints" border>
            <el-table-column prop="knowledgeTag" label="知识点" min-width="200" />
            <el-table-column label="正确率" width="120">
              <template #default="scope">
                <el-tag :type="scope.row.isWeak ? 'danger' : 'success'">{{ scope.row.accuracy }}%</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="totalQuestions" label="题目数" width="100" />
            <el-table-column prop="correctCount" label="正确次数" width="100" />
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag v-if="scope.row.isWeak" type="danger">薄弱</el-tag>
                <el-tag v-else type="success">良好</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getTeacherStatistics } from '../../api/teacher'

const router = useRouter()
const loading = ref(false)
const stats = ref({})
const distributionChartRef = ref(null)
const weakChartRef = ref(null)

const goBack = () => router.push('/dashboard')
const viewGrading = (assignmentId) => router.push(`/teacher/grading/${assignmentId}`)

const loadStatistics = async () => {
  loading.value = true
  try {
    const res = await getTeacherStatistics()
    stats.value = res.data || {}
    await nextTick()
    renderCharts()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '加载统计数据失败')
  } finally {
    loading.value = false
  }
}

const renderCharts = () => {
  if (stats.value.scoreDistribution && distributionChartRef.value) {
    const distChart = echarts.init(distributionChartRef.value)
    const labels = Object.keys(stats.value.scoreDistribution)
    const values = Object.values(stats.value.scoreDistribution)
    distChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: labels, name: '分数段' },
      yAxis: { type: 'value', name: '人数' },
      series: [{
        type: 'bar',
        data: values.map((v, i) => ({
          value: v,
          itemStyle: { color: i === 0 ? '#f56c6c' : new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#667eea' }, { offset: 1, color: '#764ba2' }
          ])}
        })),
        barWidth: '50%',
        label: { show: true, position: 'top' }
      }]
    })
  }

  if (stats.value.weakKnowledgePoints && stats.value.weakKnowledgePoints.length > 0 && weakChartRef.value) {
    const weakChart = echarts.init(weakChartRef.value)
    const kpNames = stats.value.weakKnowledgePoints.map(kp => kp.knowledgeTag)
    const kpValues = stats.value.weakKnowledgePoints.map(kp => kp.accuracy)
    weakChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: kpNames, axisLabel: { rotate: 30 } },
      yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
      series: [{
        type: 'bar',
        data: kpValues.map(v => ({
          value: v,
          itemStyle: { color: v < 60 ? '#f56c6c' : v < 80 ? '#e6a23c' : '#67c23a' }
        })),
        barWidth: '50%',
        label: { show: true, position: 'top', formatter: '{c}%' }
      }]
    })
  }
}

onMounted(loadStatistics)
</script>

<style scoped>
.teacher-analytics-page { padding: 20px; }
.analytics-content { margin-top: 20px; }
.overview-cards { margin-bottom: 20px; }
.stat-item { text-align: center; padding: 10px 0; }
.stat-value { font-size: 28px; font-weight: bold; }
.stat-value.primary { color: #667eea; }
.stat-value.success { color: #67c23a; }
.stat-value.warning { color: #e6a23c; }
.stat-value.info { color: #909399; }
.stat-label { font-size: 13px; color: #999; margin-top: 6px; }
.chart-row { margin-bottom: 20px; }
.section-card { margin-bottom: 20px; }
</style>
