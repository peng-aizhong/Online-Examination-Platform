import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/auth/Register.vue')
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/dashboard/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/student/exams',
    name: 'StudentExamList',
    component: () => import('../views/student/ExamList.vue'),
    meta: { requiresAuth: true, role: 'student' }
  },
  {
    path: '/student/exams/:assignmentId',
    name: 'StudentExamDetail',
    component: () => import('../views/student/ExamDetail.vue'),
    meta: { requiresAuth: true, role: 'student' }
  },
  {
    path: '/student/scores',
    name: 'StudentScoreList',
    component: () => import('../views/student/ScoreList.vue'),
    meta: { requiresAuth: true, role: 'student' }
  },
  {
    path: '/student/report/:sessionId',
    name: 'StudentScoreReport',
    component: () => import('../views/student/ScoreReport.vue'),
    meta: { requiresAuth: true, role: 'student' }
  },
  {
    path: '/teacher/analytics',
    name: 'TeacherAnalytics',
    component: () => import('../views/teacher/TeacherAnalytics.vue'),
    meta: { requiresAuth: true, role: 'teacher' }
  },
  {
    path: '/teacher/grading/:assignmentId',
    name: 'TeacherGradingDetail',
    component: () => import('../views/teacher/GradingDetail.vue'),
    meta: { requiresAuth: true, role: 'teacher' }
  },
  {
    path: '/teacher/questions',
    name: 'QuestionBank',
    component: () => import('../views/teacher/QuestionBank.vue'),
    meta: { requiresAuth: true, role: 'teacher' }
  },
  {
    path: '/teacher/papers',
    name: 'PaperManage',
    component: () => import('../views/teacher/PaperManage.vue'),
    meta: { requiresAuth: true, role: 'teacher' }
  },
  {
    path: '/teacher/resources',
    name: 'ResourceManage',
    component: () => import('../views/teacher/ResourceManage.vue'),
    meta: { requiresAuth: true, role: 'teacher' }
  },
  {
    path: '/student/resources',
    name: 'StudentResources',
    component: () => import('../views/student/ResourceList.vue'),
    meta: { requiresAuth: true, role: 'student' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const getStoredRole = () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
    return userInfo?.role ? String(userInfo.role).toLowerCase() : null
  } catch {
    return null
  }
}

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = getStoredRole()

  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }

  if ((to.path === '/login' || to.path === '/register') && token) {
    next('/dashboard')
    return
  }

  if (to.meta.role && role !== to.meta.role) {
    next('/dashboard')
    return
  }

  next()
})

export default router
