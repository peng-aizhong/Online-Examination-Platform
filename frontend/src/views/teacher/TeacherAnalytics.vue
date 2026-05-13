<template>
  <div class="teacher-analytics-page">
    <el-page-header @back="goBack" content="学情分析">
      <template #extra>
        <el-button type="primary" @click="openPublishDialog">发布考试</el-button>
      </template>
    </el-page-header>

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

      <!-- 已发布考试列表 -->
      <el-card class="section-card">
        <template #header>
          <div style="display:flex;justify-content:space-between;align-items:center">
            <span>已发布考试</span>
            <el-button size="small" @click="loadAssignments">刷新</el-button>
          </div>
        </template>
        <el-table :data="assignments" border v-loading="assignLoading">
          <el-table-column prop="assignmentName" label="考试名称" min-width="160" />
          <el-table-column prop="paperName" label="试卷" min-width="140" />
          <el-table-column label="考试时间" min-width="200">
            <template #default="scope">
              {{ scope.row.examStartTime ? dayjs(scope.row.examStartTime).format('MM-DD HH:mm') : '-' }}
              ~
              {{ scope.row.examEndTime ? dayjs(scope.row.examEndTime).format('MM-DD HH:mm') : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="durationMinutes" label="时长(分)" width="90" />
          <el-table-column prop="assignedStudentCount" label="指定人数" width="90" />
          <el-table-column prop="status" label="状态" width="90">
            <template #default="scope">
              <el-tag :type="scope.row.status === '进行中' ? 'success' : scope.row.status === '已结束' ? 'info' : 'warning'" size="small">
                {{ scope.row.status || '待开始' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button type="primary" link @click="viewGrading(scope.row.assignmentId)">查看阅卷</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 发布考试弹窗 -->
      <el-dialog v-model="publishDialogVisible" title="发布考试" width="600px" destroy-on-close>
        <el-form :model="publishForm" label-width="100px">
          <el-form-item label="考试名称" required>
            <el-input v-model="publishForm.assignmentName" placeholder="请输入考试名称" />
          </el-form-item>
          <el-form-item label="选择试卷" required>
            <el-select v-model="publishForm.paperId" placeholder="请选择试卷" style="width:100%" @change="onPaperChange">
              <el-option v-for="p in paperList" :key="p.paperId" :label="`${p.paperName}（${p.totalScore}分）`" :value="p.paperId" />
            </el-select>
          </el-form-item>
          <el-form-item label="考试时间" required>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-date-picker v-model="publishForm.examStartTime" type="datetime" placeholder="开始时间" style="width:100%" value-format="YYYY-MM-DDTHH:mm:ss" />
              </el-col>
              <el-col :span="12">
                <el-date-picker v-model="publishForm.examEndTime" type="datetime" placeholder="结束时间" style="width:100%" value-format="YYYY-MM-DDTHH:mm:ss" />
              </el-col>
            </el-row>
          </el-form-item>
          <el-form-item label="考试时长" required>
            <el-input-number v-model="publishForm.durationMinutes" :min="10" :max="300" :step="10" />
            <span style="margin-left:8px;color:#999">分钟</span>
          </el-form-item>
          <el-form-item label="最大次数">
            <el-input-number v-model="publishForm.maxAttempts" :min="1" :max="10" />
          </el-form-item>
          <el-form-item label="指定学生" required>
            <el-checkbox-group v-model="publishForm.studentIds">
              <el-checkbox v-for="s in studentList" :key="s.userId" :value="s.userId">
                {{ s.realName || s.username }}（{{ s.department || '未分班' }}）
              </el-checkbox>
            </el-checkbox-group>
            <div v-if="studentList.length === 0" style="color:#999;font-size:13px">暂无学生数据</div>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="publishDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="publishLoading" @click="handlePublish">确认发布</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import * as echarts from 'echarts'
import { getTeacherStatistics, getPapers, getStudentList, getTeacherAssignments, createAssignment } from '../../api/teacher'

const router = useRouter()
const loading = ref(false)
const stats = ref({})
const distributionChartRef = ref(null)
const weakChartRef = ref(null)

// 发布考试
const publishDialogVisible = ref(false)
const publishLoading = ref(false)
const paperList = ref([])
const studentList = ref([])
const assignments = ref([])
const assignLoading = ref(false)
const publishForm = reactive({
  assignmentName: '',
  paperId: '',
  examStartTime: '',
  examEndTime: '',
  durationMinutes: 60,
  maxAttempts: 1,
  studentIds: []
})

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

const openPublishDialog = async () => {
  Object.assign(publishForm, {
    assignmentName: '', paperId: '', examStartTime: '', examEndTime: '',
    durationMinutes: 60, maxAttempts: 1, studentIds: []
  })
  try {
    const [papersRes, studentsRes] = await Promise.all([getPapers(), getStudentList()])
    if (papersRes.code === 200) paperList.value = papersRes.data || []
    if (studentsRes.code === 200) studentList.value = studentsRes.data || []
  } catch {}
  publishDialogVisible.value = true
}

const onPaperChange = (paperId) => {
  const paper = paperList.value.find(p => p.paperId === paperId)
  if (paper && paper.duration) publishForm.durationMinutes = paper.duration
}

const handlePublish = async () => {
  if (!publishForm.assignmentName || !publishForm.paperId || !publishForm.examStartTime || !publishForm.examEndTime) {
    ElMessage.warning('请填写必填项')
    return
  }
  if (publishForm.studentIds.length === 0) {
    ElMessage.warning('请至少选择一名学生')
    return
  }
  publishLoading.value = true
  try {
    const res = await createAssignment(publishForm)
    if (res.code === 200) {
      ElMessage.success('考试发布成功')
      publishDialogVisible.value = false
      loadAssignments()
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '发布失败')
  } finally {
    publishLoading.value = false
  }
}

const loadAssignments = async () => {
  assignLoading.value = true
  try {
    const res = await getTeacherAssignments()
    if (res.code === 200) assignments.value = res.data || []
  } catch {} finally {
    assignLoading.value = false
  }
}

onMounted(() => {
  loadStatistics()
  loadAssignments()
})
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
