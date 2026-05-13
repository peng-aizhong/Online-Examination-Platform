<template>
  <div class="question-bank-container">
    <el-card class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="科目">
          <el-select v-model="filters.subjectId" placeholder="全部科目" clearable style="width: 160px">
            <el-option v-for="s in subjects" :key="s.subjectId" :label="s.subjectName" :value="s.subjectId" />
          </el-select>
        </el-form-item>
        <el-form-item label="题型">
          <el-select v-model="filters.questionType" placeholder="全部题型" clearable style="width: 120px">
            <el-option label="单选题" value="单选题" />
            <el-option label="多选题" value="多选题" />
            <el-option label="判断题" value="判断题" />
            <el-option label="填空题" value="填空题" />
            <el-option label="简答题" value="简答题" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" placeholder="搜索题干/知识点" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadQuestions">查询</el-button>
          <el-button type="success" @click="openDialog()">新增题目</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 16px">
      <el-table :data="questions" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="100" />
        <el-table-column prop="content" label="题干" min-width="200" show-overflow-tooltip />
        <el-table-column prop="questionType" label="题型" width="90" />
        <el-table-column prop="difficulty" label="难度" width="80">
          <template #default="{ row }">
            <el-tag :type="row.difficulty === '简单' ? 'success' : row.difficulty === '困难' ? 'danger' : 'warning'" size="small">
              {{ row.difficulty }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="knowledgeTag" label="知识点" width="120" show-overflow-tooltip />
        <el-table-column prop="subjectName" label="科目" width="100" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingQuestion ? '编辑题目' : '新增题目'" width="650px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="科目" required>
          <el-select v-model="form.subjectId" placeholder="选择科目" style="width: 100%">
            <el-option v-for="s in subjects" :key="s.subjectId" :label="s.subjectName" :value="s.subjectId" />
          </el-select>
        </el-form-item>
        <el-form-item label="题型" required>
          <el-select v-model="form.questionType" placeholder="选择题型" style="width: 100%">
            <el-option label="单选题" value="单选题" />
            <el-option label="多选题" value="多选题" />
            <el-option label="判断题" value="判断题" />
            <el-option label="填空题" value="填空题" />
            <el-option label="简答题" value="简答题" />
          </el-select>
        </el-form-item>
        <el-form-item label="题干" required>
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入题目内容" />
        </el-form-item>
        <template v-if="form.questionType === '单选题' || form.questionType === '多选题'">
          <el-form-item label="选项A">
            <el-input v-model="form.optionA" />
          </el-form-item>
          <el-form-item label="选项B">
            <el-input v-model="form.optionB" />
          </el-form-item>
          <el-form-item label="选项C">
            <el-input v-model="form.optionC" />
          </el-form-item>
          <el-form-item label="选项D">
            <el-input v-model="form.optionD" />
          </el-form-item>
        </template>
        <el-form-item label="正确答案" required>
          <el-input v-model="form.answer" placeholder="如: A 或 A,B 或 正确" />
        </el-form-item>
        <el-form-item label="难度" required>
          <el-select v-model="form.difficulty" style="width: 100%">
            <el-option label="简单" value="简单" />
            <el-option label="中等" value="中等" />
            <el-option label="困难" value="困难" />
          </el-select>
        </el-form-item>
        <el-form-item label="知识点">
          <el-input v-model="form.knowledgeTag" placeholder="知识点标签" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getQuestions, createQuestion, updateQuestion, deleteQuestion, getSubjects } from '../../api/teacher'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const questions = ref([])
const subjects = ref([])
const editingQuestion = ref(null)

const filters = reactive({ subjectId: '', questionType: '', keyword: '' })

const defaultForm = {
  subjectId: '', content: '', questionType: '单选题',
  optionA: '', optionB: '', optionC: '', optionD: '',
  answer: '', difficulty: '中等', knowledgeTag: ''
}
const form = reactive({ ...defaultForm })

const loadSubjects = async () => {
  try {
    const res = await getSubjects()
    if (res.code === 200) subjects.value = res.data
  } catch {}
}

const loadQuestions = async () => {
  loading.value = true
  try {
    const res = await getQuestions(filters)
    if (res.code === 200) questions.value = res.data
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '获取题目列表失败')
  } finally {
    loading.value = false
  }
}

const openDialog = (question) => {
  if (question) {
    editingQuestion.value = question
    Object.assign(form, {
      subjectId: question.subjectId, content: question.content,
      questionType: question.questionType, optionA: question.optionA,
      optionB: question.optionB, optionC: question.optionC, optionD: question.optionD,
      answer: question.answer, difficulty: question.difficulty, knowledgeTag: question.knowledgeTag
    })
  } else {
    editingQuestion.value = null
    Object.assign(form, defaultForm)
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.subjectId || !form.content || !form.questionType || !form.answer || !form.difficulty) {
    ElMessage.warning('请填写必填项')
    return
  }
  submitting.value = true
  try {
    const res = editingQuestion.value
      ? await updateQuestion(editingQuestion.value.id, form)
      : await createQuestion(form)
    if (res.code === 200) {
      ElMessage.success(editingQuestion.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadQuestions()
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除题目 "${row.content?.substring(0, 20)}..." ?`, '确认删除', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    try {
      await deleteQuestion(row.id)
      ElMessage.success('删除成功')
      loadQuestions()
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadSubjects()
  loadQuestions()
})
</script>

<style scoped>
.question-bank-container { padding: 0; }
.filter-card :deep(.el-form-item) { margin-bottom: 0; }
</style>
