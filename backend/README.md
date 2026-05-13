# 在线智能考试系统 - 后端

## 项目介绍
这是一个基于Spring Boot 2.7 + MySQL的后端应用，为在线智能考试系统提供RESTful API接口。

## 技术栈
- Spring Boot 2.7.14 - 框架
- Spring Security - 安全认证
- Spring Data JPA - ORM
- JWT - 认证授权
- MySQL 8.0 - 数据库
- Lombok - 代码简化

## 快速开始

### 前置条件
- JDK 11+
- Maven 3.6+
- MySQL 8.0+

### 配置数据库
1. 创建数据库
```sql
CREATE DATABASE examination_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改 `application.properties` 中的数据库连接信息
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/examination_db
spring.datasource.username=root
spring.datasource.password=root
```

### 构建和运行
```bash
# 构建项目
mvn clean install

# 运行项目
mvn spring-boot:run
```

项目将在 http://localhost:8080/api 启动

## 项目结构
```
src/main/
├── java/com/examination/
│   ├── config/           # Spring配置类
│   ├── controller/       # REST控制器
│   ├── service/          # 业务逻辑层
│   ├── repository/       # 数据访问层
│   ├── entity/           # JPA实体类
│   ├── dto/              # 数据传输对象
│   ├── security/         # 安全相关
│   ├── exception/        # 异常处理
│   ├── common/           # 公用工具
│   └── ExaminationPlatformApplication.java
└── resources/
    └── application.properties
```

## API接口

### 认证模块 `/auth`

#### 1. 用户登录
- **URL**: POST `/auth/login`
- **请求体**:
```json
{
  "username": "student01",
  "password": "123456",
  "role": "student"
}
```
- **响应**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "token": "eyJhbGc...",
    "userInfo": {
      "id": 1,
      "username": "student01",
      "email": "student@example.com",
      "realName": "张三",
      "role": "STUDENT",
      "active": true
    }
  }
}
```

#### 2. 用户注册
- **URL**: POST `/auth/register`
- **请求体**:
```json
{
  "username": "student02",
  "email": "student02@example.com",
  "password": "123456",
  "realName": "李四",
  "role": "student"
}
```
- **响应**: 用户信息对象

#### 3. 获取用户信息
- **URL**: GET `/auth/userinfo`
- **认证**: 需要Bearer Token
- **响应**: 用户信息对象

## 数据库设计

### users 表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名(唯一) |
| email | VARCHAR(100) | 邮箱(唯一) |
| password | VARCHAR(255) | 密码(加密) |
| real_name | VARCHAR(100) | 真实姓名 |
| role | VARCHAR(20) | 角色(STUDENT/TEACHER/ADMIN) |
| active | TINYINT | 是否活跃(1/0) |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

## 安全说明
- 密码使用BCrypt加密存储
- 使用JWT进行身份认证和授权
- 支持CORS跨域请求
- 所有请求需要在Authorization头中携带JWT token

## 后续功能
- [ ] 学生考试模块
- [ ] 教师管理模块
- [ ] 管理员管理模块
- [ ] 题库管理
- [ ] 试卷管理
- [ ] 成绩统计分析
- [ ] WebSocket实时通知
