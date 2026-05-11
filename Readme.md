# 在线智能考试系统

基于 Spring Boot + Vue 3 的前后端分离在线考试平台，支持学生考试、教师组卷阅卷、题库管理、学习资源共享等完整业务流程。

## 技术栈

**后端**

- Java 23 + Spring Boot 3.4.5
- Spring Security + JWT (HS512) 认证
- Spring Data JPA + Hibernate + MySQL 8
- BCrypt 密码加密
- 文件上传 (MultipartFile, 100MB 限制)

**前端**

- Vue 3 + Vite
- Element Plus 组件库
- Pinia 状态管理
- Axios HTTP 客户端
- Vue Router 路由守卫

## 功能概览

### 学生端

- 参加考试 — 浏览可参加的考试列表，进入答题页面，提交试卷后自动评分
- 成绩查询 — 查看历史考试成绩列表
- 成绩报告 — 单次考试的详细得分报告，含每题得分情况
- 错题本 — 汇总答错的题目，便于复习
- 学习资源 — 浏览教师上传的学习资料，支持文档下载、视频预览、图片预览、链接跳转

### 教师端

- 题库管理 — 题目的增删改查，支持单选/多选/判断/填空/简答等题型，按科目、类型、关键词筛选
- 试卷管理 — 创建试卷、从题库选题组卷、设置每题分值，支持编辑和删除
- 阅卷评分 — 查看待批改的考试答卷，进行人工评分
- 学情分析 — 查看班级整体考试数据与统计
- 学习资源 — 上传文件（PDF、视频、图片等）或添加外部链接资源，管理已上传资料

### 管理员端

- 用户管理 — 查看和管理所有用户账号
- 系统配置 — 系统参数设置

## 项目结构

```
Online-Examination-Platform/
├── backend/                          # Spring Boot 后端
│   └── src/main/java/com/examination/
│       ├── config/                   # 配置类 (Security, CORS, 数据初始化)
│       ├── controller/               # REST 控制器
│       ├── dto/                      # 数据传输对象
│       ├── entity/                   # JPA 实体类
│       ├── exception/                # 全局异常处理
│       ├── repository/               # 数据访问层
│       ├── security/                 # JWT 过滤器
│       └── service/                  # 业务逻辑层
├── frontend/                         # Vue 3 前端
│   └── src/
│       ├── api/                      # API 请求封装
│       ├── router/                   # 路由配置
│       ├── store/                    # Pinia 状态管理
│       ├── styles/                   # 全局样式
│       └── views/                    # 页面组件
│           ├── auth/                 # 登录、注册
│           ├── dashboard/            # 仪表板
│           ├── student/              # 学生端页面
│           └── teacher/              # 教师端页面
└── README.md
```

## 数据库设计

数据库名 `exam_system`，核心表结构：

| 表名 | 说明 |
|------|------|
| `user` | 用户表 (学生/教师/管理员) |
| `subject` | 科目表 |
| `question` | 题目表 (支持多种题型) |
| `paper` | 试卷表 |
| `paper_question` | 试卷-题目关联表 (含分值) |
| `exam_assignment` | 考试分配表 (教师发布考试) |
| `exam_assignment_student` | 考试-学生关联表 |
| `exam_session` | 考试会话表 (记录每次考试) |
| `exam_session_answer` | 答题记录表 |
| `learning_resource` | 学习资源表 |

主键采用 VARCHAR 类型 (如 `U001`, `Q001`, `P001`)，外键关联实现数据完整性。

## 快速启动

### 环境要求

- JDK 23+
- Maven 3.6+
- Node.js 16+
- MySQL 8.0+

### 1. 创建数据库

```sql
CREATE DATABASE exam_system DEFAULT CHARACTER SET utf8mb4;
```

执行项目中的建表脚本，或让 JPA 自动建表 (`spring.jpa.hibernate.ddl-auto=update`)。

### 2. 配置数据库连接

编辑 `backend/src/main/resources/application.properties`：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/exam_system?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password
```

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

启动成功后会打印：

```
============================================
   Online Examination Platform Started!
============================================
   Frontend URL: http://localhost:8080
============================================
```

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端开发服务器启动在 `http://localhost:5173`，生产构建由后端在 8080 端口统一提供服务。

### 5. 访问系统

浏览器打开 `http://localhost:8080`。

默认测试账号：

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 学生 | student01 | 123456 |
| 教师 | teacher01 | 123456 |
| 管理员 | admin01 | 123456 |

## API 接口

### 认证

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 用户登录 |
| POST | `/api/auth/register` | 用户注册 |

### 学生端

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/student/assignments` | 获取考试列表 |
| GET | `/api/student/assignments/{id}` | 考试详情 |
| POST | `/api/student/assignments/{id}/submit` | 提交答卷 |
| GET | `/api/student/results` | 成绩列表 |
| GET | `/api/student/report/{sessionId}` | 成绩报告 |
| GET | `/api/student/wrong-questions` | 错题本 |
| GET | `/api/student/resources` | 学习资源列表 |
| GET | `/api/student/resources/{id}` | 资源详情 |
| GET | `/api/student/resources/{id}/download` | 下载资源 |

### 教师端

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/teacher/questions` | 题目列表 |
| POST | `/api/teacher/questions` | 创建题目 |
| PUT | `/api/teacher/questions/{id}` | 更新题目 |
| DELETE | `/api/teacher/questions/{id}` | 删除题目 |
| GET | `/api/teacher/papers` | 试卷列表 |
| POST | `/api/teacher/papers` | 创建试卷 |
| GET | `/api/teacher/papers/{id}` | 试卷详情 |
| PUT | `/api/teacher/papers/{id}` | 更新试卷 |
| DELETE | `/api/teacher/papers/{id}` | 删除试卷 |
| POST | `/api/teacher/papers/{id}/questions` | 向试卷添加题目 |
| DELETE | `/api/teacher/papers/{id}/questions/{qid}` | 从试卷移除题目 |
| GET | `/api/teacher/resources` | 资源列表 |
| POST | `/api/teacher/resources/upload` | 上传文件资源 |
| POST | `/api/teacher/resources/link` | 添加链接资源 |
| PUT | `/api/teacher/resources/{id}` | 更新资源 |
| DELETE | `/api/teacher/resources/{id}` | 删除资源 |
| GET | `/api/teacher/analytics` | 学情分析数据 |

### 系统

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/system/subjects` | 获取科目列表 |

所有接口均需 JWT 认证（`Authorization: Bearer <token>`），登录和注册接口除外。

## 部署说明

### 生产构建

```bash
# 构建前端
cd frontend
npm run build

# 将 dist/ 内容复制到 backend/src/main/resources/static/
cp -r dist/* ../backend/src/main/resources/static/

# 打包后端
cd ../backend
mvn clean package -DskipTests

# 运行
java -jar target/examination-platform-0.0.1-SNAPSHOT.jar
```

生产环境只需运行后端 JAR，前后端统一由 Spring Boot 在 8080 端口提供服务。

### 文件上传

上传的学习资源文件存储在项目根目录下的 `uploads/` 文件夹，可通过 `application.properties` 中的 `app.upload.dir` 配置修改路径。
