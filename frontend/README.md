# 在线智能考试系统 - 前端

基于 Vue 3 + Vite + Element Plus 的在线考试平台前端应用。

## 技术栈

- Vue 3 (Composition API + `<script setup>`)
- Vite 构建工具
- Element Plus UI 组件库
- Pinia 状态管理
- Vue Router 路由管理 (含路由守卫)
- Axios HTTP 客户端 (封装统一请求/响应拦截)
- ECharts 数据可视化

## 快速开始

```bash
npm install
npm run dev
```

访问 `http://localhost:5173`，生产环境由后端在 `http://8080` 统一提供服务。

## 项目结构

```
src/
├── api/                  # API 请求模块
│   ├── auth.js           # 认证相关 (登录、注册、用户信息)
│   ├── student.js        # 学生端接口 (考试、成绩、资源)
│   └── teacher.js        # 教师端接口 (题库、试卷、资源、阅卷)
├── router/               # 路由配置 + 权限守卫
├── store/                # Pinia 状态管理 (用户信息、Token)
├── styles/               # 全局样式
└── views/
    ├── auth/             # 登录、注册
    ├── dashboard/        # 仪表板 (按角色展示不同菜单和统计)
    ├── student/          # 学生端页面
    │   ├── ExamList.vue      # 考试列表
    │   ├── ExamDetail.vue    # 答题页面
    │   ├── ScoreList.vue     # 成绩列表
    │   ├── ScoreReport.vue   # 成绩报告
    │   └── ResourceList.vue  # 学习资源浏览
    └── teacher/          # 教师端页面
        ├── QuestionBank.vue      # 题库管理
        ├── PaperManage.vue       # 试卷管理 (含组卷)
        ├── ResourceManage.vue    # 学习资源管理
        ├── TeacherAnalytics.vue  # 学情分析
        └── GradingDetail.vue     # 阅卷评分
```

## 页面功能

### 认证模块
- 登录页 — 账号密码登录，支持学生/教师/管理员三种身份
- 注册页 — 新用户注册，支持选择身份

### 学生端
- 考试列表 — 查看教师分配的可参加考试
- 在线答题 — 进入考试、逐题作答、提交试卷
- 成绩查询 — 历史考试成绩列表
- 成绩报告 — 单次考试详细得分，含每题正误情况
- 学习资源 — 按科目/类型筛选，支持文档下载、视频/图片预览、链接跳转

### 教师端
- 题库管理 — 题目 CRUD，支持单选/多选/判断/填空/简答，按科目/类型/关键词筛选
- 试卷管理 — 创建试卷、从题库选题组卷、设置分值
- 学习资源 — 上传文件资源 (拖拽上传) 或添加外部链接，管理已有资源
- 学情分析 — 班级考试数据统计
- 阅卷评分 — 查看并批改学生答卷

### 管理员端
- 用户管理、系统配置

## 开发规范

- Vue 3 Composition API + `<script setup>` 语法
- Element Plus 组件统一使用
- CSS Scoped 样式隔离
- API 统一通过 `src/api/` 模块封装，使用 Axios 拦截器自动附加 JWT Token
- 路由守卫控制权限，按角色 (`student`/`teacher`/`admin`) 限制页面访问
