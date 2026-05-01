# 在线智能考试系统 - 项目结构

```
Online-Examination-Platform/
├── frontend/                              # 前端项目（Vue 3 + Element Plus）
│   ├── src/
│   │   ├── views/
│   │   │   ├── auth/
│   │   │   │   ├── Login.vue             # 登录页面
│   │   │   │   └── Register.vue          # 注册页面
│   │   │   ├── dashboard/
│   │   │   │   └── Dashboard.vue         # 仪表板页面
│   │   │   └── NotFound.vue              # 404页面
│   │   ├── components/                   # 可复用组件
│   │   ├── router/
│   │   │   └── index.js                  # 路由配置
│   │   ├── store/
│   │   │   └── index.js                  # Pinia状态管理
│   │   ├── api/
│   │   │   └── auth.js                   # 认证API
│   │   ├── styles/
│   │   │   └── main.css                  # 全局样式
│   │   ├── App.vue
│   │   └── main.js
│   ├── index.html
│   ├── package.json
│   ├── vite.config.js
│   └── README.md
│
├── backend/                               # 后端项目（Spring Boot + MySQL）
│   ├── src/main/
│   │   ├── java/com/examination/
│   │   │   ├── config/                   # Spring配置
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── WebConfig.java
│   │   │   ├── controller/               # REST控制器
│   │   │   │   └── AuthController.java
│   │   │   ├── service/                  # 业务逻辑层
│   │   │   │   └── AuthService.java
│   │   │   ├── repository/               # 数据访问层
│   │   │   │   └── UserRepository.java
│   │   │   ├── entity/                   # JPA实体
│   │   │   │   └── User.java
│   │   │   ├── dto/                      # 数据传输对象
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── LoginResponse.java
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   └── UserResponse.java
│   │   │   ├── security/                 # 安全认证
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   └── JwtAuthenticationFilter.java
│   │   │   ├── exception/                # 异常处理
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── common/                   # 公用工具
│   │   │   │   └── ApiResponse.java
│   │   │   └── ExaminationPlatformApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── init.sql                  # 数据库初始化脚本
│   ├── pom.xml
│   ├── .gitignore
│   └── README.md
│
├── Readme.md                              # 项目总体介绍
└── .gitignore
```

## 项目说明

### 🎯 核心功能已实现
✅ 前端框架搭建
✅ 登录页面（支持学生/教师/管理员）
✅ 注册页面
✅ 仪表板（根据用户角色显示不同内容）
✅ 后端框架搭建
✅ JWT认证
✅ 登录API（带密码验证）
✅ 注册API
✅ 用户管理API

### 🚀 快速开始

#### 后端启动
```bash
cd backend
# 1. 修改 src/main/resources/application.properties 中的数据库配置
# 2. 执行数据库初始化脚本 src/main/resources/init.sql
# 3. 运行项目
mvn spring-boot:run
```

#### 前端启动
```bash
cd frontend
npm install
npm run dev
# 访问 http://localhost:5173
```

#### 测试登录
默认用户（密码都是 123456）：
- 学生: student01 / 123456
- 教师: teacher01 / 123456
- 管理员: admin01 / 123456

### 📱 页面功能

#### 登录页 (`/login`)
- 支持账号/密码登录
- 支持选择用户身份
- 记住密码功能
- 表单验证

#### 注册页 (`/register`)
- 创建新账号
- 支持学生/教师身份选择
- 邮箱、密码验证
- 用户协议同意

#### 仪表板 (`/dashboard`)
- 根据用户角色显示不同菜单
- 学生：参加考试、成绩查询
- 教师：试卷管理、题库管理、阅卷评分
- 管理员：用户管理、系统配置
- 个人信息展示和统计数据

### 🔒 安全特性
- 密码使用BCrypt加密
- 支持JWT令牌认证
- CORS跨域配置
- Spring Security集成
- 自动令牌刷新

### 📊 数据库设计
- `users` 表：用户基本信息
- 索引优化：username、email、role、created_at

### 🔄 后续可开发的模块
- [ ] 学生考试管理
- [ ] 教师组卷系统
- [ ] 自动阅卷
- [ ] 题库管理
- [ ] 成绩分析与可视化
- [ ] 防作弊机制
- [ ] WebSocket实时监控
- [ ] 文件存储
