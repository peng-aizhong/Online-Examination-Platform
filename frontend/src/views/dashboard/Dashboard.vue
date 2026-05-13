<template>
  <div class="dashboard-container">
    <el-header class="header">
      <div class="header-left">
        <h1>🎓 在线智能考试系统</h1>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleRoleSwitch" style="margin-right:16px">
          <span class="role-switch-btn">
            <el-tag :type="roleTagType" size="small">{{ getRoleLabel(userStore.userInfo?.role) }}</el-tag>
            <el-icon style="margin-left:4px;color:white"><arrow-down /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="student" :disabled="normalizedRole === 'student'">学生</el-dropdown-item>
              <el-dropdown-item command="teacher" :disabled="normalizedRole === 'teacher'">教师</el-dropdown-item>
              <el-dropdown-item command="admin" :disabled="normalizedRole === 'admin'">管理员</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
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

          <template v-if="normalizedRole === 'student'">
            <el-menu-item index="exams">
              <el-icon><document /></el-icon>
              <span>参加考试</span>
            </el-menu-item>
            <el-menu-item index="scores">
              <el-icon><data-analysis /></el-icon>
              <span>成绩查询</span>
            </el-menu-item>
            <el-menu-item index="student-resources">
              <el-icon><folder /></el-icon>
              <span>学习资源</span>
            </el-menu-item>
          </template>

          <template v-if="normalizedRole === 'teacher'">
            <el-menu-item index="teacher-analytics">
              <el-icon><data-analysis /></el-icon>
              <span>学情分析</span>
            </el-menu-item>
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
            <el-menu-item index="resource-manage">
              <el-icon><folder /></el-icon>
              <span>学习资源</span>
            </el-menu-item>
          </template>

          <template v-if="normalizedRole === 'admin'">
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
          <el-card v-if="normalizedRole === 'student'" class="stats-card">
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

          <el-card v-if="normalizedRole === 'teacher'" class="stats-card">
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

          <el-card v-if="normalizedRole === 'admin'" class="stats-card">
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

        <!-- 个人信息弹窗 -->
        <el-dialog v-model="profileDialogVisible" title="个人信息" width="480px" destroy-on-close>
          <el-form :model="profileForm" label-width="80px">
            <el-form-item label="账号">
              <el-input :model-value="userStore.userInfo?.username" disabled />
            </el-form-item>
            <el-form-item label="角色">
              <el-tag>{{ getRoleLabel(userStore.userInfo?.role) }}</el-tag>
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="院系">
              <el-input v-model="profileForm.department" placeholder="请输入院系/部门" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="profileDialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="profileLoading" @click="handleProfileSave">保存</el-button>
          </template>
        </el-dialog>

        <!-- 修改密码弹窗 -->
        <el-dialog v-model="passwordDialogVisible" title="修改密码" width="450px" destroy-on-close>
          <el-form :model="passwordForm" label-width="100px" ref="passwordFormRef">
            <el-form-item label="原密码" required>
              <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
            </el-form-item>
            <el-form-item label="新密码" required>
              <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码（至少6位）" />
            </el-form-item>
            <el-form-item label="确认密码" required>
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="passwordDialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="passwordLoading" @click="handlePasswordSave">保存</el-button>
          </template>
        </el-dialog>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { computed, ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../store'
import { updateProfile, changePassword } from '../../api/auth'

const router = useRouter()
const userStore = useUserStore()
const activeMenu = ref('dashboard')

const normalizedRole = computed(() => {
  const role = userStore.userInfo?.role
  return role ? String(role).toLowerCase() : ''
})

const handleMenuSelect = (key) => {
  activeMenu.value = key
  const routeMap = {
    'dashboard': '/dashboard',
    'exams': '/student/exams',
    'scores': '/student/scores',
    'student-resources': '/student/resources',
    'teacher-analytics': '/teacher/analytics',
    'question-bank': '/teacher/questions',
    'paper-manage': '/teacher/papers',
    'grading': '/teacher/analytics',
    'resource-manage': '/teacher/resources',
    'user-manage': '/admin/users',              // 👈 用户管理
    'system-config': '/admin/config'
  }
  if (routeMap[key]) {
    router.push(routeMap[key])
  } else {
    ElMessage.info('该功能开发中...')
  }
}

const roleTagType = computed(() => {
  const map = { student: 'success', teacher: 'warning', admin: 'danger' }
  return map[normalizedRole.value] || 'info'
})

const handleRoleSwitch = (role) => {
  if (role === normalizedRole.value) return
  ElMessageBox.confirm(`确定切换到"${getRoleLabel(role)}"角色吗？将退出当前登录。`, '切换角色', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    router.push(`/login?role=${role}`)
  })
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
    openProfileDialog()
  } else if (command === 'settings') {
    openPasswordDialog()
  }
}

const getRoleLabel = (role) => {
  const roleMap = {
    student: '学生',
    teacher: '教师',
    admin: '管理员'
  }
  const normalized = role ? String(role).toLowerCase() : ''
  return roleMap[normalized] || role
}

// 个人信息
const profileDialogVisible = ref(false)
const profileLoading = ref(false)
const profileForm = reactive({ email: '', phone: '', department: '' })

const openProfileDialog = () => {
  const u = userStore.userInfo || {}
  Object.assign(profileForm, {
    email: u.email || '',
    phone: u.phone || '',
    department: u.department || ''
  })
  profileDialogVisible.value = true
}

const handleProfileSave = async () => {
  profileLoading.value = true
  try {
    const res = await updateProfile(profileForm)
    if (res.code === 200) {
      userStore.setUser(res.data, userStore.token)
      ElMessage.success('个人信息更新成功')
      profileDialogVisible.value = false
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '保存失败')
  } finally {
    profileLoading.value = false
  }
}

// 修改密码
const passwordDialogVisible = ref(false)
const passwordLoading = ref(false)
const passwordFormRef = ref()
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const openPasswordDialog = () => {
  Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
  passwordDialogVisible.value = true
}

const handlePasswordSave = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    ElMessage.warning('请填写所有字段')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    ElMessage.warning('新密码长度不能少于6位')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  passwordLoading.value = true
  try {
    const res = await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    if (res.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      passwordDialogVisible.value = false
      setTimeout(() => {
        userStore.logout()
        router.push('/login')
      }, 1500)
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '修改失败')
  } finally {
    passwordLoading.value = false
  }
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  userStore.restoreFromLocal()
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

.user-info:hover, .role-switch-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.role-switch-btn {
  cursor: pointer;
  padding: 0 16px;
  height: 60px;
  display: flex;
  align-items: center;
  font-size: 14px;
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
