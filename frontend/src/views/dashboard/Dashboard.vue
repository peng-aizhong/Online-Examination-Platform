<template>
  <div class="dashboard-container">
    <el-header class="header">
      <div class="header-left">
        <h1>🎓 在线智能考试系统</h1>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            {{ userStore.userInfo?.realName || userStore.userInfo?.username }}
            <el-icon class="el-icon--right">
              <arrow-down />
            </el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item command="settings">设置</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container class="main-container">
      <el-aside class="sidebar" width="200px">
        <el-menu
          :default-active="activeMenu"
          @select="handleMenuSelect"
          mode="vertical"
        >
          <el-menu-item index="dashboard">
            <el-icon><home-filled /></el-icon>
            <span>首页</span>
          </el-menu-item>

          <template v-if="userStore.userInfo?.role === 'student'">
            <el-menu-item index="exams">
              <el-icon><document /></el-icon>
              <span>参加考试</span>
            </el-menu-item>
            <el-menu-item index="scores">
              <el-icon><data-analysis /></el-icon>
              <span>成绩查询</span>
            </el-menu-item>
          </template>

          <template v-if="userStore.userInfo?.role === 'teacher'">
            <el-menu-item index="paper-manage">
              <el-icon><files /></el-icon>
              <span>试卷管理</span>
            </el-menu-item>
            <el-menu-item index="question-bank">
              <el-icon><list /></el-icon>
              <span>题库管理</span>
            </el-menu-item>
            <el-menu-item index="grading">
              <el-icon><document-checked /></el-icon>
              <span>阅卷评分</span>
            </el-menu-item>
          </template>

          <template v-if="userStore.userInfo?.role === 'admin'">
            <el-menu-item index="user-manage">
              <el-icon><user /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="system-config">
              <el-icon><setting /></el-icon>
              <span>系统配置</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>

      <el-main class="main-content">
        <el-card class="welcome-card">
          <h2>欢迎回来，{{ userStore.userInfo?.realName || userStore.userInfo?.username }}！</h2>
          <p>你的身份：<el-tag>{{ getRoleLabel(userStore.userInfo?.role) }}</el-tag></p>
        </el-card>

        <div class="dashboard-grid">
          <el-card v-if="userStore.userInfo?.role === 'student'" class="stats-card">
            <template #header>
              <div class="card-header">
                <span>考试统计</span>
              </div>
            </template>
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="stat-item">
                  <div class="stat-value">0</div>
                  <div class="stat-label">总考试次数</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-item">
                  <div class="stat-value">0</div>
                  <div class="stat-label">待进行考试</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-item">
                  <div class="stat-value">0</div>
                  <div class="stat-label">平均分数</div>
                </div>
              </el-col>
            </el-row>
          </el-card>

          <el-card v-if="userStore.userInfo?.role === 'teacher'" class="stats-card">
            <template #header>
              <div class="card-header">
                <span>教师统计</span>
              </div>
            </template>
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="stat-item">
                  <div class="stat-value">0</div>
                  <div class="stat-label">已发布试卷</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-item">
                  <div class="stat-value">0</div>
                  <div class="stat-label">待阅卷数</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-item">
                  <div class="stat-value">0</div>
                  <div class="stat-label">题库题目</div>
                </div>
              </el-col>
            </el-row>
          </el-card>

          <el-card v-if="userStore.userInfo?.role === 'admin'" class="stats-card">
            <template #header>
              <div class="card-header">
                <span>系统统计</span>
              </div>
            </template>
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="stat-item">
                  <div class="stat-value">0</div>
                  <div class="stat-label">总用户数</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-item">
                  <div class="stat-value">0</div>
                  <div class="stat-label">活跃用户</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-item">
                  <div class="stat-value">0</div>
                  <div class="stat-label">系统健康度</div>
                </div>
              </el-col>
            </el-row>
          </el-card>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../store'

const router = useRouter()
const userStore = useUserStore()
const activeMenu = ref('dashboard')

const handleMenuSelect = (key) => {
  activeMenu.value = key
}

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    })
  } else if (command === 'profile') {
    ElMessage.info('个人信息功能开发中...')
  } else if (command === 'settings') {
    ElMessage.info('设置功能开发中...')
  }
}

const getRoleLabel = (role) => {
  const roleMap = {
    'student': '学生',
    'teacher': '教师',
    'admin': '管理员'
  }
  return roleMap[role] || role
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
  }
})
</script>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.header-left h1 {
  margin: 0;
  font-size: 24px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  cursor: pointer;
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  font-size: 14px;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.1);
}

.main-container {
  height: calc(100vh - 60px);
}

.sidebar {
  background: white;
  border-right: 1px solid #dcdfe6;
  overflow-y: auto;
}

.main-content {
  padding: 20px;
}

.welcome-card {
  margin-bottom: 20px;
}

.welcome-card h2 {
  margin: 0 0 10px 0;
  color: #333;
}

.welcome-card p {
  margin: 0;
  color: #666;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.stats-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-item {
  text-align: center;
  padding: 20px 0;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #667eea;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 10px;
}
</style>
