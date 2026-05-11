<template>
  <div class="student-page">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>成绩查询</span>
          <el-button type="primary" @click="loadData">刷新</el-button>
        </div>
      </template>

      <el-table :data="results" border v-loading="loadingResults">
        <el-table-column prop="assignmentName" label="考试名称" min-width="200" />
        <el-table-column prop="paperName" label="试卷" min-width="150" />
        <el-table-column label="得分" width="120">
          <template #default="scope">{{ scope.row.score }}/{{ scope.row.totalScore }}</template>
        </el-table-column>
        <el-table-column prop="accuracy" label="正确率" width="100">
          <template #default="scope">{{ scope.row.accuracy }}%</template>
        </el-table-column>
        <el-table-column label="最高分" width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.isBestScore" type="success" size="small">最佳</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="attemptNumber" label="第几次" width="80" />
        <el-table-column prop="submittedAt" label="提交时间" min-width="170">
          <template #default="scope">{{ scope.row.submittedAt ? dayjs(scope.row.submittedAt).format('YYYY-MM-DD HH:mm:ss') : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button type="primary" link @click="viewReport(scope.row.sessionId)">查看报告</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="wrong-book">
      <template #header><span>错题本</span></template>

      <el-empty v-if="wrongQuestions.length === 0" description="暂无错题" />

      <el-collapse v-else>
        <el-collapse-item
          v-for="(item, index) in wrongQuestions"
          :key="`${item.assignmentId}-${item.questionId}-${index}`"
          :title="`${item.assignmentName} - 题目${item.questionId}`"
        >
          <p><strong>题目：</strong>{{ item.questionContent }}</p>
          <p v-if="item.knowledgeTag"><strong>知识点：</strong><el-tag size="small" type="info">{{ item.knowledgeTag }}</el-tag></p>
          <p><strong>你的答案：</strong><span style="color: #f56c6c;">{{ item.yourAnswer || '未作答' }}</span></p>
          <p><strong>正确答案：</strong><span style="color: #67c23a;">{{ item.correctAnswer }}</span></p>
          <p><strong>分值：</strong>{{ item.score }}</p>
          <p v-if="item.feedback"><strong>解析：</strong>{{ item.feedback }}</p>
        </el-collapse-item>
      </el-collapse>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { getStudentResults, getStudentWrongQuestions } from '../../api/student'

const router = useRouter()
const loadingResults = ref(false)
const results = ref([])
const wrongQuestions = ref([])

const viewReport = (sessionId) => {
  router.push(`/student/report/${sessionId}`)
}

const loadResults = async () => {
  loadingResults.value = true
  try {
    const res = await getStudentResults()
    results.value = res.data || []
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '加载成绩失败')
  } finally {
    loadingResults.value = false
  }
}

const loadWrongQuestions = async () => {
  try {
    const res = await getStudentWrongQuestions()
    wrongQuestions.value = res.data?.items || []
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '加载错题本失败')
  }
}

const loadData = async () => {
  await Promise.all([loadResults(), loadWrongQuestions()])
}

onMounted(loadData)
</script>

<style scoped>
.student-page { padding: 20px; display: grid; gap: 20px; }
.header-row { display: flex; justify-content: space-between; align-items: center; }
.wrong-book p { margin: 8px 0; }
</style>
