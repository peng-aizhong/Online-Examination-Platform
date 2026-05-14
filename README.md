# 考试系统 (Exam System)

基于 Spring Boot + Bootstrap 的在线考试管理系统，支持选择题、填空题、程序运行结果题、简答题和编程题的在线考试。

## 技术栈

| 层次 | 技术 | 版本 |
|------|------|------|
| 运行时 | Java | 23 |
| 框架 | Spring Boot | 3.2.5 |
| 构建 | Maven | - |
| ORM | Spring Data JPA (Hibernate) | - |
| 安全 | Spring Security | - |
| 模板引擎 | Thymeleaf | - |
| 数据库 | MySQL | - |
| 前端 | Bootstrap 5.3.0, Font Awesome 6.4.0 | CDN |
| 文件处理 | Apache POI | 5.2.4 |

## 功能模块

### 三种角色

- **管理员 (admin)**: 用户管理、科目管理、考试监控、数据统计
- **教师 (teacher)**: 题库管理、试卷管理、考试安排、阅卷评分
- **学生 (student)**: 参加考试、查看成绩、学习资源、个人中心

### 支持的题型

| 代码 | 题型 | 自动评分 |
|------|------|----------|
| C | 选择题 | 是 |
| F | 填空题 | 是 |
| R | 程序运行结果题 | 是 |
| S | 简答题 | 否（需教师评阅） |
| P | 编程题 | 否（需教师评阅） |

## 快速开始

### 环境要求

- JDK 23
- MySQL 8.0+
- Maven 3.6+

### 数据库配置

1. 创建 MySQL 数据库：

```sql
CREATE DATABASE exam_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. （可选）导入初始表结构和种子数据：

```bash
mysql -u root -p exam_system < src/main/resources/db/init.sql
```

> 注意：如果跳过此步骤，Hibernate 的 `ddl-auto: update` 会自动创建表结构（但不会导入种子数据）。

### 修改配置

编辑 `src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/exam_system?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 你的密码
```

### 运行项目

```bash
# 编译并启动
mvn clean spring-boot:run

# 或者先打包再运行
mvn clean package -DskipTests
java -jar target/exam-system-1.0.0.jar
```

启动后访问：`http://localhost:8080`

### 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 教师 | teacher1 | teacher123 |
| 学生 | student1 | student123 |

> 实际账号取决于数据库中 `user` 表中的数据。以上为 `init.sql` 中的种子数据示例。

### 注册新账号

访问登录页面，点击注册链接即可注册新学生账号。

## 项目结构

```
exam-system/
├── pom.xml
├── src/main/java/com/exam/
│   ├── ExamSystemApplication.java    # 启动类
│   ├── config/                       # 配置类（安全、缓存、文件上传等）
│   ├── controller/                   # 控制器
│   │   ├── MainController.java       # 登录/注册/首页
│   │   ├── StudentController.java    # 学生端功能
│   │   ├── TeacherController.java    # 教师端功能
│   │   ├── AdminController.java      # 管理员基类
│   │   ├── AdminUserController.java  # 用户管理
│   │   ├── AdminSubjectController.java # 科目管理
│   │   ├── AdminExamController.java  # 考试管理
│   │   ├── AdminStatisticsController.java # 数据统计
│   │   └── ProfileController.java    # 个人中心
│   ├── dto/                          # 数据传输对象
│   ├── entity/                       # 实体类
│   ├── repository/                   # 数据访问层
│   └── service/                      # 业务逻辑层
│       └── impl/                     # 业务逻辑实现
└── src/main/resources/
    ├── application.yml               # 应用配置
    ├── db/init.sql                   # 数据库初始化脚本
    ├── static/                       # 静态资源
    └── templates/                    # Thymeleaf 模板
        ├── login.html                # 登录/注册页
        ├── admin/                    # 管理员页面
        ├── teacher/                  # 教师页面
        ├── student/                  # 学生页面
        │   └── exam/
        │       ├── list.html         # 考试列表
        │       ├── active.html       # 进行中的考试
        │       ├── take.html         # 考试答题页
        │       ├── result.html       # 考试成绩页
        │       └── history.html      # 考试历史
        └── profile/                  # 个人中心页面
```

## 考试流程

1. **教师端**: 创建题目 → 组卷 → 安排考试（指定学生、时间、次数限制）
2. **学生端**: 查看可参加的考试 → 开始考试 → 答题（自动保存） → 提交试卷
3. **评分**: 客观题自动评分，主观题由教师评阅后出最终成绩

## 注意事项

- 该项目为学习用途，密码存储使用了明文（`PlainTextPasswordEncoder`），生产环境请替换为 `BCryptPasswordEncoder`
- CSRF 保护已禁用，生产环境应启用
- 文件上传限制为 50GB，生产环境应适当调整
- 前端所有 JavaScript 和 CSS 为内联编写，无独立静态文件
