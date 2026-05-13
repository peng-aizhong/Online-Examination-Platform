<template>
  <div class="paper-manage-container">
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>试卷管理</span>
          <el-button type="success" @click="openPaperDialog()">新增试卷</el-button>
        </div>
      </template>
      <el-table :data="papers" v-loading="loading" stripe>
        <el-table-column prop="paperId" label="ID" width="100" />
        <el-table-column prop="paperName" label="试卷名称" min-width="150" />
        <el-table-column prop="subjectName" label="科目" width="100" />
        <el-table-column prop="duration" label="时长(分钟)" width="100" />
        <el-table-column prop="totalScore" label="总分" width="80" />
        <el-table-column prop="difficultyLevel" label="难度" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === '启用' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openPaperDialog(row)">编辑</el-button>
            <el-button size="small" type="primary" @click="openComposeDialog(row)">组题</el-button>
            <el-button size="small" type="warning" @click="openPreviewDialog(row)">预览</el-button>
            <el-button size="small" type="danger" @click="handleDeletePaper(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑试卷弹窗 -->
    <el-dialog v-model="paperDialogVisible" :title="editingPaper ? '编辑试卷' : '新增试卷'" width="500px" destroy-on-close>
      <el-form :model="paperForm" label-width="80px">
        <el-form-item label="试卷名称" required>
          <el-input v-model="paperForm.paperName" placeholder="请输入试卷名称" />
        </el-form-item>
        <el-form-item label="科目" required>
          <el-select v-model="paperForm.subjectId" placeholder="选择科目" style="width:100%">
            <el-option v-for="s in subjects" :key="s.subjectId" :label="s.subjectName" :value="s.subjectId" />
          </el-select>
        </el-form-item>
        <el-form-item label="时长" required>
          <el-input v-model.number="paperForm.duration" type="number" placeholder="分钟">
            <template #append>分钟</template>
          </el-input>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="paperForm.difficultyLevel" style="width:100%">
            <el-option label="简单" value="简单" />
            <el-option label="中等" value="中等" />
            <el-option label="困难" value="困难" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="paperDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handlePaperSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 组题弹窗 -->
    <el-dialog v-model="composeDialogVisible" title="试卷组题" width="900px" destroy-on-close>
      <div style="display:flex;gap:20px">
        <!-- 左侧: 题库 -->
        <div style="flex:1;border-right:1px solid #eee;padding-right:16px">
          <h4>题库 (勾选添加)</h4>
          <el-form :inline="true" size="small" style="margin-bottom:8px">
            <el-form-item>
              <el-select v-model="composeFilter.subjectId" placeholder="科目" clearable style="width:120px" @change="loadComposeQuestions">
                <el-option v-for="s in subjects" :key="s.subjectId" :label="s.subjectName" :value="s.subjectId" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-input v-model="composeFilter.keyword" placeholder="关键词" clearable style="width:140px" @keyup.enter="loadComposeQuestions" />
            </el-form-item>
            <el-form-item>
              <el-button size="small" type="primary" @click="loadComposeQuestions">搜索</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="availableQuestions" size="small" max-height="400" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="40" />
            <el-table-column prop="content" label="题干" min-width="150" show-overflow-tooltip />
            <el-table-column prop="questionType" label="题型" width="70" />
            <el-table-column prop="difficulty" label="难度" width="60" />
          </el-table>
          <el-button type="success" size="small" style="margin-top:8px" :disabled="selectedQuestions.length===0" @click="addSelectedQuestions">
            添加选中题目 ({{ selectedQuestions.length }})
          </el-button>
        </div>
        <!-- 右侧: 已选题目 -->
        <div style="flex:1">
          <h4>已选题目 ({{ paperQuestions.length }} 题，总分: {{ computedTotalScore }})</h4>
          <el-table :data="paperQuestions" size="small" max-height="450">
            <el-table-column prop="content" label="题干" min-width="150" show-overflow-tooltip />
            <el-table-column prop="questionType" label="题型" width="70" />
            <el-table-column label="分值" width="100">
              <template #default="{ row }">
                <el-input v-model.number="row.score" size="small" type="number" min="0" @change="onScoreChange" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="70">
              <template #default="{ row }">
                <el-button size="small" type="danger" link @click="removeQuestion(row)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="composeDialogVisible = false">关闭</el-button>
        <el-button type="primary" :loading="submitting" @click="handleComposeSubmit">保存组题</el-button>
      </template>
    </el-dialog>

    <!-- 试卷预览弹窗 -->
    <el-dialog v-model="previewDialogVisible" :title="'试卷预览: ' + previewPaperName" width="800px" destroy-on-close>
      <div v-if="previewQuestions.length > 0" class="preview-container">
        <div class="preview-header">
          <p><strong>试卷名称：</strong>{{ previewPaperName }} | <strong>总分：</strong>{{ previewTotalScore }}分 | <strong>题目数：</strong>{{ previewQuestions.length }}题</p>
        </div>
        <div v-for="(q, index) in previewQuestions" :key="q.questionId" class="preview-question">
          <h4>{{ index + 1 }}. {{ q.content }}
            <el-tag size="small" style="margin-left:8px">{{ typeLabelMap[q.questionType] || q.questionType }}</el-tag>
            <span style="float:right;color:#999;font-size:13px">{{ q.score }}分</span>
          </h4>

          <!-- 单选题 -->
          <div v-if="q.questionType === 'single' || !q.questionType" class="preview-options">
            <p>A. {{ q.optionA }}</p>
            <p>B. {{ q.optionB }}</p>
            <p>C. {{ q.optionC }}</p>
            <p>D. {{ q.optionD }}</p>
          </div>

          <!-- 多选题 -->
          <div v-else-if="q.questionType === 'multiple'" class="preview-options">
            <p>A. {{ q.optionA }}</p>
            <p>B. {{ q.optionB }}</p>
            <p>C. {{ q.optionC }}</p>
            <p>D. {{ q.optionD }}</p>
          </div>

          <!-- 判断题 -->
          <div v-else-if="q.questionType === 'judge'" class="preview-options">
            <p>A. 正确 &nbsp;&nbsp; B. 错误</p>
          </div>

          <!-- 填空题 -->
          <div v-else-if="q.questionType === 'fill'" class="preview-options">
            <p style="color:#999">（请在此填写答案）</p>
          </div>

          <!-- 主观题 -->
          <div v-else-if="q.questionType === 'subjective'" class="preview-options">
            <p style="color:#999">（请在此作答）</p>
          </div>

          <div class="preview-answer">
            <strong>正确答案：</strong><span style="color:#67c23a">{{ q.answer }}</span>
            <span v-if="q.knowledgeTag" style="margin-left:16px;color:#999">知识点: {{ q.knowledgeTag }}</span>
          </div>
        </div>
      </div>
      <el-empty v-else description="该试卷暂无题目" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getPapers, getPaper, createPaper, updatePaper, deletePaper,
  getQuestions, getSubjects, addPaperQuestion, removePaperQuestion
} from '../../api/teacher'

const loading = ref(false)
const submitting = ref(false)
const papers = ref([])
const subjects = ref([])

// 试卷编辑
const paperDialogVisible = ref(false)
const editingPaper = ref(null)
const paperForm = reactive({ paperName: '', subjectId: '', duration: 60, difficultyLevel: '中等' })

// 组题
const composeDialogVisible = ref(false)
const composingPaper = ref(null)
const availableQuestions = ref([])
const selectedQuestions = ref([])
const paperQuestions = ref([])
const composeFilter = reactive({ subjectId: '', keyword: '' })

// 试卷预览
const previewDialogVisible = ref(false)
const previewPaperName = ref('')
const previewTotalScore = ref(0)
const previewQuestions = ref([])
const typeLabelMap = { single: '单选题', multiple: '多选题', judge: '判断题', fill: '填空题', subjective: '主观题' }

const computedTotalScore = computed(() => paperQuestions.value.reduce((sum, q) => sum + (q.score || 0), 0))

const loadSubjects = async () => {
  try {
    const res = await getSubjects()
    if (res.code === 200) subjects.value = res.data
  } catch {}
}

const loadPapers = async () => {
  loading.value = true
  try {
    const res = await getPapers()
    if (res.code === 200) papers.value = res.data
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '获取试卷列表失败')
  } finally {
    loading.value = false
  }
}

const openPaperDialog = (paper) => {
  if (paper) {
    editingPaper.value = paper
    Object.assign(paperForm, {
      paperName: paper.paperName, subjectId: paper.subjectId,
      duration: paper.duration, difficultyLevel: paper.difficultyLevel
    })
  } else {
    editingPaper.value = null
    Object.assign(paperForm, { paperName: '', subjectId: '', duration: 60, difficultyLevel: '中等' })
  }
  paperDialogVisible.value = true
}

const handlePaperSubmit = async () => {
  if (!paperForm.paperName || !paperForm.subjectId || !paperForm.duration) {
    ElMessage.warning('请填写必填项')
    return
  }
  submitting.value = true
  try {
    const data = { ...paperForm, questions: [] }
    const res = editingPaper.value
      ? await updatePaper(editingPaper.value.paperId, data)
      : await createPaper(data)
    if (res.code === 200) {
      ElMessage.success(editingPaper.value ? '更新成功' : '创建成功')
      paperDialogVisible.value = false
      loadPapers()
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleDeletePaper = (row) => {
  ElMessageBox.confirm(`确定删除试卷 "${row.paperName}" ?`, '确认删除', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    try {
      await deletePaper(row.paperId)
      ElMessage.success('删除成功')
      loadPapers()
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '删除失败')
    }
  }).catch(() => {})
}

// 组题逻辑
const openComposeDialog = async (paper) => {
  composingPaper.value = paper
  selectedQuestions.value = []
  try {
    const res = await getPaper(paper.paperId)
    if (res.code === 200) {
      paperQuestions.value = (res.data.questions || []).map(q => ({
        questionId: q.questionId, content: q.content,
        questionType: q.questionType, difficulty: q.difficulty, score: q.score || 0
      }))
    }
  } catch {}
  await loadComposeQuestions()
  composeDialogVisible.value = true
}

const loadComposeQuestions = async () => {
  try {
    const res = await getQuestions(composeFilter)
    if (res.code === 200) {
      const existingIds = new Set(paperQuestions.value.map(q => q.questionId))
      availableQuestions.value = res.data.filter(q => !existingIds.has(q.id))
    }
  } catch {}
}

const handleSelectionChange = (selection) => {
  selectedQuestions.value = selection
}

const addSelectedQuestions = () => {
  for (const q of selectedQuestions.value) {
    if (!paperQuestions.value.find(pq => pq.questionId === q.id)) {
      paperQuestions.value.push({
        questionId: q.id, content: q.content,
        questionType: q.questionType, difficulty: q.difficulty, score: 0
      })
    }
  }
  const ids = new Set(selectedQuestions.value.map(q => q.id))
  availableQuestions.value = availableQuestions.value.filter(q => !ids.has(q.id))
  selectedQuestions.value = []
}

const removeQuestion = (row) => {
  paperQuestions.value = paperQuestions.value.filter(q => q.questionId !== row.questionId)
  loadComposeQuestions()
}

const onScoreChange = () => {}

const openPreviewDialog = async (paper) => {
  previewPaperName.value = paper.paperName
  previewTotalScore.value = paper.totalScore || 0
  previewQuestions.value = []
  try {
    const res = await getPaper(paper.paperId)
    if (res.code === 200) {
      previewQuestions.value = res.data.questions || []
      previewTotalScore.value = res.data.totalScore || 0
    }
  } catch {}
  previewDialogVisible.value = true
}

const handleComposeSubmit = async () => {
  if (!composingPaper.value) return
  submitting.value = true
  try {
    // First update paper info with new total score
    const paperData = {
      paperName: composingPaper.value.paperName,
      subjectId: composingPaper.value.subjectId,
      duration: composingPaper.value.duration,
      totalScore: computedTotalScore.value,
      difficultyLevel: composingPaper.value.difficultyLevel,
      questions: paperQuestions.value.map(q => ({ questionId: q.questionId, score: q.score }))
    }
    const res = await updatePaper(composingPaper.value.paperId, paperData)
    if (res.code === 200) {
      ElMessage.success('组题保存成功')
      composeDialogVisible.value = false
      loadPapers()
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadSubjects()
  loadPapers()
})
</script>

<style scoped>
.paper-manage-container { padding: 0; }
h4 { margin: 0 0 12px 0; color: #333; }
.preview-container { max-height: 65vh; overflow-y: auto; }
.preview-header { padding: 12px; background: #f5f7fa; border-radius: 6px; margin-bottom: 16px; }
.preview-header p { margin: 0; }
.preview-question { border: 1px solid #ebeef5; border-radius: 6px; padding: 16px; margin-bottom: 12px; }
.preview-question h4 { margin: 0 0 12px 0; font-size: 15px; }
.preview-options { padding-left: 16px; }
.preview-options p { margin: 6px 0; color: #333; }
.preview-answer { margin-top: 10px; padding-top: 10px; border-top: 1px dashed #eee; font-size: 13px; }
</style>
