# 在线智能考试系统 - 前端

## 项目介绍
这是一个基于Vue 3 + Vite + Element Plus的现代化前端应用，用于在线智能考试系统的前端部分。

## 技术栈
- Vue 3 - 渐进式JavaScript框架
- Vite - 下一代前端构建工具
- Element Plus - Vue 3的UI组件库
- Axios - HTTP客户端
- Pinia - 状态管理
- Vue Router - 路由管理

## 快速开始

### 安装依赖
```bash
npm install
```

### 开发服务器
```bash
npm run dev
```
访问 http://localhost:5173

### 构建生产版本
```bash
npm run build
```

## 项目结构
```
src/
├── components/       # 可复用组件
├── views/           # 页面组件
│   ├── auth/        # 登录/注册页面
│   ├── dashboard/   # 仪表板页面
│   └── ...
├── router/          # 路由配置
├── store/           # Pinia 状态管理
├── api/             # API 请求模块
├── styles/          # 全局样式
└── main.js          # 入口文件
```

## 功能说明

### 1. 登录页面 (`views/auth/Login.vue`)
- 账号、密码、身份(学生/教师/管理员)登录
- 记住密码功能
- 表单验证

### 2. 注册页面 (`views/auth/Register.vue`)
- 账号、邮箱、密码、真实姓名等字段
- 支持选择身份(学生/教师)
- 用户协议同意

### 3. 仪表板页面 (`views/dashboard/Dashboard.vue`)
- 根据用户角色显示不同菜单
- 用户信息展示
- 统计数据展示

## API 接口约定

### 认证接口 (`/api/auth`)
- `POST /auth/login` - 登录
- `POST /auth/register` - 注册
- `GET /auth/userinfo` - 获取用户信息

## 开发规范
- 遵循Vue 3 Composition API风格
- 组件使用 `<script setup>` 语法
- 统一使用Element Plus组件
- CSS Scoped 隔离样式

## 后续功能
- [ ] 学生考试模块
- [ ] 教师管理模块
- [ ] 管理员管理模块
- [ ] 成绩分析与可视化
- [ ] 题库管理
- [ ] 试卷管理
