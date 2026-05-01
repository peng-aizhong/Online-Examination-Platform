# 快速启动指南

## 系统要求

- **Java**: 8 或更高版本（推荐 11+）
- **MySQL**: 8.0 或更高版本
- **Node.js**: 14 或更高版本
- **npm**: 6 或更高版本

## 一键启动（Windows）

### 步骤 1: 创建 MySQL 数据库

打开 PowerShell 或 CMD，执行：

```bash
mysql -u root -proot -e "CREATE DATABASE IF NOT EXISTS examination_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

**说明**: 
- `-u root`: MySQL 用户名为 root
- `-proot`: MySQL 密码为 root（无空格）
- 如果你的 MySQL 用户名或密码不同，请相应修改

### 步骤 2: 启动后端应用

```bash
cd backend
mvn clean package -DskipTests
java -jar target/examination-platform-1.0.0.jar
```

**预期输出**:
```
Started ExaminationPlatformApplication in X.XXX seconds (JVM running for X.XXX)
Tomcat started on port(s): 8080 (http) with context path '/api'
```

### 步骤 3: 启动前端应用（新终端）

```bash
cd frontend
npm install
npm run dev
```

**预期输出**:
```
VITE v4.5.14  ready in XXX ms

  ➜  Local:   http://localhost:5173/
```

### 步骤 4: 访问应用

打开浏览器访问: **http://localhost:5173**

---

## 测试登录

### 预置账户

系统已预置 3 个测试账户，可直接使用：

#### 学生账户
- **用户名**: `student01`
- **密码**: `123456`
- **角色**: 学生

#### 教师账户
- **用户名**: `teacher01`
- **密码**: `123456`
- **角色**: 教师

#### 管理员账户
- **用户名**: `admin01`
- **密码**: `123456`
- **角色**: 管理员

### 注册新账户

1. 点击登录页面的"没有账号？立即注册"
2. 填写表单：
   - 账号（3-20 字符）
   - 邮箱（有效的邮箱格式）
   - 密码（6-20 字符）
   - 确认密码
   - 真实姓名
   - 勾选用户协议
3. 点击"立即注册"
4. 返回登录页面用新账户登录

---

## API 端点

### 认证 API

#### 用户登录
```bash
POST http://localhost:8080/api/auth/login

{
  "username": "student01",
  "password": "123456",
  "role": "student"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "token": "eyJhbGc...",
    "userInfo": {
      "id": 1,
      "username": "student01",
      "email": "student01@example.com",
      "realName": "张三",
      "role": "STUDENT",
      "active": true
    }
  }
}
```

#### 用户注册
```bash
POST http://localhost:8080/api/auth/register

{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "Password123",
  "realName": "新用户",
  "role": "STUDENT"
}
```

#### 获取用户信息
```bash
GET http://localhost:8080/api/auth/userinfo
Authorization: Bearer <token>
```

---

## 常见问题

### 问题 1: MySQL 连接错误
**错误**: `Unknown database 'examination_platform'`

**解决**:
```bash
mysql -u root -proot -e "CREATE DATABASE IF NOT EXISTS examination_platform CHARACTER SET utf8mb4;"
```

### 问题 2: 端口 8080 已被占用
**错误**: `Tomcat started on port(s): 8080`（但无法访问）

**解决**: 修改后端配置文件 `backend/src/main/resources/application.properties`:
```properties
server.port=8081
```

### 问题 3: 前端无法连接后端
**错误**: `Failed to load resource: the server responded with a status of 401`

**检查**:
1. 确保后端运行在 http://localhost:8080/api
2. 检查 JWT 令牌是否有效
3. 查看浏览器开发者工具的 Network 标签

### 问题 4: 登录成功但无法进入仪表板
**原因**: 可能的 JWT 令牌过期或刷新问题

**解决**: 
1. 清除浏览器缓存和 localStorage
2. 重新登录

---

## 开发模式

### 后端开发

使用 Maven Spring Boot 插件直接运行（支持热重载）：

```bash
cd backend
mvn spring-boot:run
```

### 前端开发

Vite 开发服务器已配置热模块替换 (HMR)：

```bash
cd frontend
npm run dev
```

修改源代码后，浏览器会自动刷新。

---

## 构建生产版本

### 后端打包

```bash
cd backend
mvn clean package -DskipTests
```

生成的 JAR 文件位于: `backend/target/examination-platform-1.0.0.jar`

### 前端打包

```bash
cd frontend
npm run build
```

生成的文件位于: `frontend/dist/`

---

## 数据库管理

### H2 数据库控制台（仅开发）

访问: http://localhost:8080/api/h2-console

**默认凭证**:
- Driver Class: `org.h2.Driver`
- JDBC URL: 可从控制台日志中找到

### MySQL 数据库

```bash
# 连接 MySQL
mysql -u root -proot

# 选择数据库
USE examination_platform;

# 查看用户表
SELECT * FROM users;

# 查看表结构
DESCRIBE users;
```

---

## 日志文件

### 后端日志

后端应用启动时会输出详细日志到控制台，包括：
- 应用启动信息
- 数据库连接状态
- Hibernate SQL 语句
- Security 过滤链配置

### 前端日志

打开浏览器开发者工具 (F12)，查看：
- **Console**: JavaScript 错误和日志
- **Network**: API 请求和响应
- **Application**: localStorage 数据和缓存

---

## 修改配置

### 修改数据库连接

编辑 `backend/src/main/resources/application.properties`:

```properties
# 如果使用不同的 MySQL 用户名和密码
spring.datasource.username=your_username
spring.datasource.password=your_password

# 如果 MySQL 不在本地或使用不同端口
spring.datasource.url=jdbc:mysql://192.168.1.100:3306/examination_platform?useSSL=false&serverTimezone=UTC
```

### 修改 JWT 过期时间

编辑 `backend/src/main/resources/application.properties`:

```properties
# JWT 令牌过期时间（毫秒）
# 86400000 = 24 小时
# 3600000 = 1 小时
jwt.expiration=3600000
```

### 修改前端 API 地址

如果后端运行在其他地址，编辑 `frontend/src/api/auth.js`:

```javascript
const API_BASE_URL = 'http://your-backend-url/api'
```

---

## 故障排查

### 启动后端时出错

1. 检查 Java 版本:
   ```bash
   java -version
   ```

2. 检查 Maven 是否正确安装:
   ```bash
   mvn -v
   ```

3. 查看完整错误日志并搜索相关解决方案

### 启动前端时出错

1. 清除 npm 缓存:
   ```bash
   npm cache clean --force
   ```

2. 删除 `node_modules` 和 `package-lock.json`:
   ```bash
   rm -r node_modules package-lock.json
   npm install
   ```

3. 确保 Node.js 版本兼容:
   ```bash
   node -v
   ```

---

## 相关文档

- [项目完成报告](PROJECT_COMPLETION_REPORT.md) - 详细的项目信息
- [项目结构](PROJECT_STRUCTURE.md) - 代码组织说明
- [实现总结](IMPLEMENTATION_SUMMARY.md) - 技术实现细节

---

**🎉 祝你使用愉快！如有问题，请参考相关文档。**
