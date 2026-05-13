<template>
  <div class="student-page">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>我的考试</span>
          <el-button type="primary" @click="loadAssignments">刷新</el-button>
        </div>
      </template>

      <el-table :data="assignments" v-loading="loading" border>
        <el-table-column prop="assignmentName" label="考试名称" min-width="200" />
        <el-table-column prop="paperName" label="试卷" min-width="150" />
        <el-table-column prop="subjectName" label="科目" width="120" />
        <el-table-column prop="durationMinutes" label="时长(分钟)" width="110" />
        <el-table-column prop="totalScore" label="总分" width="80" />
        <el-table-column label="状态" width="110">
          <template #default="scope">
            <el-tag :type="statusType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最佳成绩" width="100">
          <template #default="scope">
            {{ scope.row.bestScore != null ? scope.row.bestScore : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="考试次数" width="100">
          <template #default="scope">
            {{ scope.row.myAttempts }}/{{ scope.row.maxAttempts }}
          </template>
        </el-table-column>
        <el-table-column label="开始时间" min-width="170">
          <template #default="scope">{{ formatTime(scope.row.examStartTime) }}</template>
        </el-table-column>
        <el-table-column label="结束时间" min-width="170">
          <template #default="scope">{{ formatTime(scope.row.examEndTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              link
              :disabled="scope.row.status !== 'active'"
              @click="goToExam(scope.row.assignmentId)"
            >
              开始作答
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { getStudentAssignments } from '../../api/student'

const router = useRouter()
const loading = ref(false)
const assignments = ref([])

const loadAssignments = async () => {
  loading.value = true
  try {
    const res = await getStudentAssignments()
    assignments.value = res.data || []
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '获取考试列表失败')
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'

const statusLabel = (status) => {
  const map = { scheduled: '未开始', active: '进行中', finished: '已结束', completed: '已完成' }
  return map[status] || status
}

const statusType = (status) => {
  const map = { scheduled: 'info', active: 'success', finished: 'warning', completed: '' }
  return map[status] || 'info'
}

const goToExam = (assignmentId) => {
  router.push(`/student/exams/${assignmentId}`)
}

onMounted(loadAssignments)
</script>

<style scoped>
.student-page { padding: 20px; }
.header-row { display: flex; align-items: center; justify-content: space-between; }
</style>
