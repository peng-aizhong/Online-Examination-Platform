<template>
  <div class="resource-list-container">
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
      </el-form>
    </el-card>

    <div v-loading="loading" class="resource-grid">
      <el-empty v-if="!loading && resources.length === 0" description="暂无学习资源" />
      <el-card v-for="item in resources" :key="item.id" class="resource-card" shadow="hover">
        <div class="resource-header">
          <el-tag :type="typeTagMap[item.type]" size="small">{{ typeLabelMap[item.type] }}</el-tag>
          <span class="subject-tag" v-if="item.subjectName">{{ item.subjectName }}</span>
        </div>
        <h3 class="resource-title" :title="item.title">{{ item.title }}</h3>
        <p class="resource-desc" v-if="item.description">{{ item.description }}</p>
        <div class="resource-meta">
          <span v-if="item.uploaderName">上传者: {{ item.uploaderName }}</span>
          <span>浏览: {{ item.viewCount || 0 }}</span>
          <span>下载: {{ item.downloadCount || 0 }}</span>
        </div>
        <div class="resource-tags" v-if="item.tags">
          <el-tag v-for="tag in item.tags.split(',')" :key="tag" size="small" type="info" style="margin-right:4px">{{ tag.trim() }}</el-tag>
        </div>
        <div class="resource-actions">
          <el-button v-if="item.type === 'LINK' && item.externalUrl" type="primary" size="small" @click="openLink(item.externalUrl)">
            打开链接
          </el-button>
          <el-button v-if="item.type === 'VIDEO' && item.filePath" type="warning" size="small" @click="previewVideo(item)">
            预览视频
          </el-button>
          <el-button v-if="item.type === 'IMAGE' && item.filePath" type="success" size="small" @click="previewImage(item)">
            预览图片
          </el-button>
          <el-button v-if="item.type === 'DOCUMENT' && item.filePath" type="primary" size="small" @click="handleDownload(item)">
            下载
          </el-button>
          <el-button v-if="item.filePath" size="small" @click="handleDownload(item)">
            下载文件
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 视频预览弹窗 -->
    <el-dialog v-model="videoDialogVisible" :title="previewTitle" width="720px" destroy-on-close>
      <video v-if="videoUrl" :src="videoUrl" controls style="width:100%;max-height:500px" />
    </el-dialog>

    <!-- 图片预览弹窗 -->
    <el-dialog v-model="imageDialogVisible" :title="previewTitle" width="800px" destroy-on-close>
      <img v-if="imageUrl" :src="imageUrl" style="max-width:100%;max-height:600px;display:block;margin:0 auto" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getStudentResources, downloadStudentResource, getSubjects } from '../../api/student'

const loading = ref(false)
const resources = ref([])
const subjects = ref([])
const filters = reactive({ subjectId: '', type: '' })

const typeLabelMap = { DOCUMENT: '文档', VIDEO: '视频', IMAGE: '图片', LINK: '链接' }
const typeTagMap = { DOCUMENT: 'primary', VIDEO: 'warning', IMAGE: 'success', LINK: 'info' }

const videoDialogVisible = ref(false)
const imageDialogVisible = ref(false)
const previewTitle = ref('')
const videoUrl = ref('')
const imageUrl = ref('')

const loadSubjects = async () => {
  try {
    const res = await getSubjects()
    if (res.code === 200) subjects.value = res.data
  } catch {}
}

const loadResources = async () => {
  loading.value = true
  try {
    const res = await getStudentResources(filters)
    if (res.code === 200) resources.value = res.data
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '获取资源列表失败')
  } finally {
    loading.value = false
  }
}

const openLink = (url) => {
  window.open(url, '_blank')
}

const previewVideo = (item) => {
  previewTitle.value = item.title
  videoUrl.value = '/' + item.filePath
  videoDialogVisible.value = true
}

const previewImage = (item) => {
  previewTitle.value = item.title
  imageUrl.value = '/' + item.filePath
  imageDialogVisible.value = true
}

const handleDownload = async (item) => {
  try {
    const res = await downloadStudentResource(item.id)
    const url = window.URL.createObjectURL(new Blob([res]))
    const a = document.createElement('a')
    a.href = url
    const ext = item.filePath ? item.filePath.substring(item.filePath.lastIndexOf('.')) : ''
    a.download = item.title + ext
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)
    item.downloadCount = (item.downloadCount || 0) + 1
  } catch (e) {
    ElMessage.error('下载失败')
  }
}

onMounted(() => {
  loadSubjects()
  loadResources()
})
</script>

<style scoped>
.resource-list-container { padding: 0; }
.filter-card { margin-bottom: 16px; }
.filter-card :deep(.el-form-item) { margin-bottom: 0; }
.resource-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}
.resource-card { cursor: default; }
.resource-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.subject-tag { font-size: 12px; color: #999; }
.resource-title { margin: 0 0 8px; font-size: 16px; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.resource-desc { margin: 0 0 8px; font-size: 13px; color: #666; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.resource-meta { font-size: 12px; color: #999; display: flex; gap: 12px; margin-bottom: 8px; }
.resource-tags { margin-bottom: 12px; }
.resource-actions { display: flex; gap: 8px; }
</style>
