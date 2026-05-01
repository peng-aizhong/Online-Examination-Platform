# 🚀 快速启动指南

## 📋 前置环境要求

### 后端开发环境
- **JDK 11+** - [下载链接](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.6+** - [下载链接](https://maven.apache.org/download.cgi)
- **MySQL 8.0+** - [下载链接](https://dev.mysql.com/downloads/mysql/)

### 前端开发环境
- **Node.js 16+** - [下载链接](https://nodejs.org/)

---

## 🗄️ 第一步：数据库配置

### 1. 创建数据库
打开MySQL命令行或图形化工具（如 Navicat、DBeaver）：

```sql
CREATE DATABASE IF NOT EXISTS examination_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 执行初始化脚本
使用 MySQL 执行以下脚本创建表和插入测试数据：

**文件位置**: `backend/src/main/resources/init.sql`

或直接执行命令行：
```bash
mysql -u root -p examination_db < backend/src/main/resources/init.sql
```

### 3. 验证数据库
连接到 `examination_db` 数据库，确认 `users` 表已创建：

```sql
SELECT * FROM users;
```

应该能看到3条测试数据（student01, teacher01, admin01）

---

## 🖥️ 第二步：后端启动

### 1. 配置数据库连接
编辑文件：`backend/src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/examination_db
spring.datasource.username=root
spring.datasource.password=root  # 修改为你的MySQL密码
```

### 2. 构建项目
```bash
cd backend
mvn clean install
```

### 3. 启动项目
```bash
mvn spring-boot:run
```

✅ 看到以下日志表示启动成功：
```
Started ExaminationPlatformApplication in X.XXX seconds
```

**后端API地址**: `http://localhost:8080/api`

---

## 🎨 第三步：前端启动

### 1. 安装依赖
```bash
cd frontend
npm install
```

如果npm很慢，可以使用淘宝镜像：
```bash
npm install -g cnpm --registry=https://registry.npm.taobao.org
cnpm install
```

### 2. 启动开发服务器
```bash
npm run dev
```

✅ 看到以下输出表示启动成功：
```
  Local:        http://localhost:5173/
```

**前端地址**: `http://localhost:5173`

---

## 🧪 第四步：测试功能

### 测试登录
打开浏览器访问 `http://localhost:5173`，会自动跳转到登录页面

**测试用户**（密码都是 `123456`）：

| 用户名 | 密码 | 身份 |
|-------|------|------|
| student01 | 123456 | 学生 |
| teacher01 | 123456 | 教师 |
| admin01 | 123456 | 管理员 |

#### 登录步骤：
1. 输入用户名：`student01`
2. 输入密码：`123456`
3. 选择身份：学生
4. 点击"登 录"
5. 进入仪表板首页

### 测试注册
1. 点击"没有账号？立即注册"
2. 填写注册信息
3. 点击"立即注册"

---

## 🔧 API 接口测试

使用 Postman 或 cURL 测试 API

### 登录接口
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "student01",
    "password": "123456",
    "role": "student"
  }'
```

**响应示例**：
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
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

### 注册接口
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "student02",
    "email": "student02@example.com",
    "password": "123456",
    "realName": "李四",
    "role": "student"
  }'
```

### 获取用户信息接口
```bash
curl -X GET http://localhost:8080/api/auth/userinfo \
  -H "Authorization: Bearer {token}"
```

替换 `{token}` 为登录获取的令牌

---

## 📁 项目结构

```
Online-Examination-Platform/
├── frontend/           # Vue 3前端项目
│   ├── src/
│   │   ├── views/     # 页面组件
│   │   ├── api/       # API调用
│   │   ├── router/    # 路由
│   │   └── store/     # 状态管理
│   └── package.json
├── backend/           # Spring Boot后端项目
│   ├── src/
│   │   ├── java/      # Java源代码
│   │   └── resources/ # 配置文件
│   └── pom.xml
└── README.md         # 项目说明
```

---

## 🆘 常见问题

### 问题1：后端启动报数据库连接错误
**解决方案**：
1. 检查MySQL是否启动
2. 验证数据库名称、用户名、密码是否正确
3. 确保已执行 `init.sql` 初始化脚本

### 问题2：前端API请求失败（404 或 CORS错误）
**解决方案**：
1. 确保后端已启动在 `http://localhost:8080`
2. 检查浏览器控制台错误信息
3. 确认 `vite.config.js` 中代理配置正确

### 问题3：npm install 很慢
**解决方案**：
```bash
# 使用淘宝镜像
npm install -g cnpm --registry=https://registry.npm.taobao.org
cnpm install

# 或者直接配置npm镜像
npm config set registry https://registry.npm.taobao.org
```

### 问题4：Maven 依赖下载失败
**解决方案**：
在 `backend/pom.xml` 中添加阿里云镜像：
```xml
<repositories>
  <repository>
    <id>aliyun</id>
    <name>Aliyun Repository</name>
    <url>https://maven.aliyun.com/repository/public</url>
  </repository>
</repositories>
```

---

## 📚 进阶开发

### 构建前端生产版本
```bash
cd frontend
npm run build
# 生成的文件在 dist/ 目录下
```

### 打包后端为可执行JAR
```bash
cd backend
mvn clean package
# 生成的文件在 target/ 目录下
java -jar target/examination-platform-1.0.0.jar
```

---

## 📞 技术支持

如遇到问题，请检查：
1. ✅ 后端 README: `backend/README.md`
2. ✅ 前端 README: `frontend/README.md`
3. ✅ 项目结构文档: `PROJECT_STRUCTURE.md`

---

**祝你开发愉快！** 🎉
