<template>
  <div class="resource-manage-container">
    <el-card class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="科目">
          <el-select v-model="filters.subjectId" placeholder="全部科目" clearable style="width:160px" @change="loadResources">
            <el-option v-for="s in subjects" :key="s.subjectId" :label="s.subjectName" :value="s.subjectId" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="filters.type" placeholder="全部类型" clearable style="width:120px" @change="loadResources">
            <el-option label="文档" value="DOCUMENT" />
            <el-option label="视频" value="VIDEO" />
            <el-option label="图片" value="IMAGE" />
            <el-option label="链接" value="LINK" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="openUploadDialog">上传文件</el-button>
          <el-button type="primary" @click="openLinkDialog">添加链接</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top:16px">
      <el-table :data="resources" v-loading="loading" stripe>
        <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="typeTagMap[row.type]" size="small">{{ typeLabelMap[row.type] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="subjectName" label="科目" width="100" />
        <el-table-column label="大小" width="90">
          <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="70" />
        <el-table-column prop="downloadCount" label="下载" width="70" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 上传文件弹窗 -->
    <el-dialog v-model="uploadDialogVisible" title="上传文件" width="550px" destroy-on-close>
      <el-form :model="uploadForm" label-width="80px">
        <el-form-item label="文件" required>
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="onFileChange"
            :on-remove="onFileRemove"
            drag
          >
            <el-icon style="font-size:40px;color:#999"><upload-filled /></el-icon>
            <div>将文件拖到此处，或<em>点击上传</em></div>
          </el-upload>
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="uploadForm.title" placeholder="资源标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="uploadForm.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="uploadForm.category" placeholder="如: 课件、习题、参考资料" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="uploadForm.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
        <el-form-item label="科目">
          <el-select v-model="uploadForm.subjectId" placeholder="选择科目" clearable style="width:100%">
            <el-option v-for="s in subjects" :key="s.subjectId" :label="s.subjectName" :value="s.subjectId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleUpload">上传</el-button>
      </template>
    </el-dialog>

    <!-- 添加链接弹窗 -->
    <el-dialog v-model="linkDialogVisible" title="添加链接资源" width="500px" destroy-on-close>
      <el-form :model="linkForm" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="linkForm.title" placeholder="资源标题" />
        </el-form-item>
        <el-form-item label="链接" required>
          <el-input v-model="linkForm.externalUrl" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="linkForm.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="linkForm.category" placeholder="如: 在线课程、参考网站" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="linkForm.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
        <el-form-item label="科目">
          <el-select v-model="linkForm.subjectId" placeholder="选择科目" clearable style="width:100%">
            <el-option v-for="s in subjects" :key="s.subjectId" :label="s.subjectName" :value="s.subjectId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="linkDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleLinkSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑资源" width="500px" destroy-on-close>
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="editForm.category" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="editForm.tags" />
        </el-form-item>
        <el-form-item label="科目">
          <el-select v-model="editForm.subjectId" placeholder="选择科目" clearable style="width:100%">
            <el-option v-for="s in subjects" :key="s.subjectId" :label="s.subjectName" :value="s.subjectId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleEditSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTeacherResources, uploadResource, createLinkResource, updateResource, deleteResource, getSubjects } from '../../api/teacher'

const loading = ref(false)
const submitting = ref(false)
const resources = ref([])
const subjects = ref([])
const uploadFile = ref(null)

const filters = reactive({ subjectId: '', type: '' })

const typeLabelMap = { DOCUMENT: '文档', VIDEO: '视频', IMAGE: '图片', LINK: '链接' }
const typeTagMap = { DOCUMENT: 'primary', VIDEO: 'warning', IMAGE: 'success', LINK: 'info' }

const uploadDialogVisible = ref(false)
const linkDialogVisible = ref(false)
const editDialogVisible = ref(false)
const editingResource = ref(null)

const uploadForm = reactive({ title: '', description: '', category: '', tags: '', subjectId: '' })
const linkForm = reactive({ title: '', externalUrl: '', description: '', category: '', tags: '', subjectId: '' })
const editForm = reactive({ title: '', description: '', category: '', tags: '', subjectId: '' })

const formatSize = (bytes) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1048576).toFixed(1) + ' MB'
}

const loadSubjects = async () => {
  try {
    const res = await getSubjects()
    if (res.code === 200) subjects.value = res.data
  } catch {}
}

const loadResources = async () => {
  loading.value = true
  try {
    const res = await getTeacherResources(filters)
    if (res.code === 200) resources.value = res.data
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '获取资源列表失败')
  } finally {
    loading.value = false
  }
}

const openUploadDialog = () => {
  uploadFile.value = null
  Object.assign(uploadForm, { title: '', description: '', category: '', tags: '', subjectId: '' })
  uploadDialogVisible.value = true
}

const openLinkDialog = () => {
  Object.assign(linkForm, { title: '', externalUrl: '', description: '', category: '', tags: '', subjectId: '' })
  linkDialogVisible.value = true
}

const openEditDialog = (row) => {
  editingResource.value = row
  Object.assign(editForm, {
    title: row.title, description: row.description,
    category: row.category, tags: row.tags, subjectId: row.subjectId
  })
  editDialogVisible.value = true
}

const onFileChange = (file) => {
  uploadFile.value = file.raw
  if (!uploadForm.title) {
    uploadForm.title = file.name.replace(/\.[^/.]+$/, '')
  }
}

const onFileRemove = () => {
  uploadFile.value = null
}

const handleUpload = async () => {
  if (!uploadFile.value) { ElMessage.warning('请选择文件'); return }
  if (!uploadForm.title) { ElMessage.warning('请输入标题'); return }
  submitting.value = true
  try {
    const fd = new FormData()
    fd.append('file', uploadFile.value)
    fd.append('title', uploadForm.title)
    if (uploadForm.description) fd.append('description', uploadForm.description)
    if (uploadForm.category) fd.append('category', uploadForm.category)
    if (uploadForm.tags) fd.append('tags', uploadForm.tags)
    if (uploadForm.subjectId) fd.append('subjectId', uploadForm.subjectId)
    const res = await uploadResource(fd)
    if (res.code === 200) {
      ElMessage.success('上传成功')
      uploadDialogVisible.value = false
      loadResources()
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '上传失败')
  } finally {
    submitting.value = false
  }
}

const handleLinkSubmit = async () => {
  if (!linkForm.title || !linkForm.externalUrl) { ElMessage.warning('请填写标题和链接'); return }
  submitting.value = true
  try {
    const res = await createLinkResource(linkForm)
    if (res.code === 200) {
      ElMessage.success('添加成功')
      linkDialogVisible.value = false
      loadResources()
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '添加失败')
  } finally {
    submitting.value = false
  }
}

const handleEditSubmit = async () => {
  if (!editForm.title) { ElMessage.warning('请输入标题'); return }
  submitting.value = true
  try {
    const res = await updateResource(editingResource.value.id, editForm)
    if (res.code === 200) {
      ElMessage.success('更新成功')
      editDialogVisible.value = false
      loadResources()
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '更新失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除资源 "${row.title}" ?`, '确认删除', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    try {
      await deleteResource(row.id)
      ElMessage.success('删除成功')
      loadResources()
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadSubjects()
  loadResources()
})
</script>

<style scoped>
.resource-manage-container { padding: 0; }
.filter-card :deep(.el-form-item) { margin-bottom: 0; }
</style>
