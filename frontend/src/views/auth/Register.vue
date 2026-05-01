<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h1>🎓 在线智能考试系统</h1>
        <p>创建账号</p>
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="rules"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入账号（3-20字符）"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            type="email"
            placeholder="请输入邮箱"
            prefix-icon="Message"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（6-20字符）"
            prefix-icon="Lock"
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="确认密码"
            prefix-icon="Lock"
          />
        </el-form-item>

        <el-form-item prop="role">
          <el-select
            v-model="registerForm.role"
            placeholder="请选择身份"
            class="full-width"
          >
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>

        <el-form-item prop="realName">
          <el-input
            v-model="registerForm.realName"
            placeholder="请输入真实姓名"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="agree">
          <el-checkbox v-model="registerForm.agree">
            我已阅读并同意
            <el-link type="primary">《用户协议》</el-link>
            和
            <el-link type="primary">《隐私政策》</el-link>
          </el-checkbox>
        </el-form-item>

        <el-button
          type="primary"
          class="register-btn"
          @click="handleRegister"
          :loading="loading"
        >
          立即注册
        </el-button>
      </el-form>

      <div class="register-footer">
        <el-link type="primary" @click="goToLogin">已有账号？立即登录</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../../api/auth'

const router = useRouter()
const registerFormRef = ref()
const loading = ref(false)

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  role: 'student',
  realName: '',
  agree: false
})

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (registerForm.confirmPassword !== '') {
      registerFormRef.value?.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePassword2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度必须在3-20之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在6-20之间', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePassword2, trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择身份', trigger: 'change' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  agree: [
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请同意用户协议'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true

    try {
      const response = await register({
        username: registerForm.username,
        email: registerForm.email,
        password: registerForm.password,
        role: registerForm.role,
        realName: registerForm.realName
      })

      if (response.code === 200) {
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } else {
        ElMessage.error(response.message || '注册失败')
      }
    } catch (error) {
      const message = error.response?.data?.message || error.message || '网络错误，请重试'
      ElMessage.error(message)
    } finally {
      loading.value = false
    }
  })
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-box {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 480px;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h1 {
  color: #667eea;
  margin: 0;
  font-size: 28px;
  margin-bottom: 10px;
}

.register-header p {
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

.register-btn {
  width: 100%;
  height: 40px;
  font-size: 16px;
  margin-bottom: 20px;
}

.register-footer {
  text-align: center;
  font-size: 14px;
}

.full-width {
  width: 100%;
}

@media (max-width: 480px) {
  .register-box {
    padding: 20px;
    max-width: 100%;
    margin: 0 20px;
  }

  .register-header h1 {
    font-size: 24px;
  }
}
</style>
