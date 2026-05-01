<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>🎓 在线智能考试系统</h1>
        <p>欢迎登录</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入账号"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item prop="role">
          <el-select
            v-model="loginForm.role"
            placeholder="请选择身份"
            class="full-width"
          >
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>

        <el-checkbox v-model="rememberPassword" class="remember-check">
          记住密码
        </el-checkbox>

        <el-button
          type="primary"
          class="login-btn"
          @click="handleLogin"
          :loading="loading"
        >
          登 录
        </el-button>
      </el-form>

      <div class="login-footer">
        <el-link type="primary" @click="goToRegister">没有账号？立即注册</el-link>
        <el-link type="info">忘记密码？</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../../api/auth'
import { useUserStore } from '../../store'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref()
const loading = ref(false)
const rememberPassword = ref(false)

const loginForm = reactive({
  username: localStorage.getItem('remembered_username') || '',
  password: localStorage.getItem('remembered_password') || '',
  role: localStorage.getItem('remembered_role') || 'student'
})

const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度必须在3-20之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在6-20之间', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择身份', trigger: 'change' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true

    try {
      const response = await login(loginForm.username, loginForm.password)

      if (response.code === 200) {
        // 保存记住密码
        if (rememberPassword.value) {
          localStorage.setItem('remembered_username', loginForm.username)
          localStorage.setItem('remembered_password', loginForm.password)
          localStorage.setItem('remembered_role', loginForm.role)
        } else {
          localStorage.removeItem('remembered_username')
          localStorage.removeItem('remembered_password')
          localStorage.removeItem('remembered_role')
        }

        userStore.setUser(response.data.userInfo, response.data.token)
        ElMessage.success('登录成功')
        router.push('/dashboard')
      } else {
        ElMessage.error(response.message || '登录失败')
      }
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '网络错误，请重试')
    } finally {
      loading.value = false
    }
  })
}

const goToRegister = () => {
  router.push('/register')
}

onMounted(() => {
  if (rememberPassword.value) {
    rememberPassword.value = !!localStorage.getItem('remembered_username')
  }
})
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 420px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  color: #667eea;
  margin: 0;
  font-size: 28px;
  margin-bottom: 10px;
}

.login-header p {
  color: #999;
  margin: 0;
  font-size: 14px;
}

:deep(.el-form-item) {
  margin-bottom: 22px;
}

:deep(.el-input__prefix) {
  color: #999;
}

.remember-check {
  margin-bottom: 22px;
  color: #666;
}

.login-btn {
  width: 100%;
  height: 40px;
  font-size: 16px;
  margin-bottom: 20px;
}

.login-footer {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.full-width {
  width: 100%;
}

@media (max-width: 480px) {
  .login-box {
    padding: 20px;
    max-width: 100%;
    margin: 0 20px;
  }

  .login-header h1 {
    font-size: 24px;
  }
}
</style>
