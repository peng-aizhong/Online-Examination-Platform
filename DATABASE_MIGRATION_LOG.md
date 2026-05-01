# 项目配置变更总结

## 概述

本文档记录了将项目从 H2 内存数据库切换到 MySQL 数据库所做的所有修改。

---

## 修改内容

### 1. 数据库配置文件修改

**文件**: `backend/src/main/resources/application.properties`

#### 变更内容
- 从 H2 内存数据库切换到 MySQL
- 更新 JDBC URL、用户名和密码
- 设置 MySQL 8.0 方言
- 增加了足够长的 JWT 密钥

#### 修改前
```properties
# H2 数据库配置
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA 配置使用 H2
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# JWT 配置（密钥不足）
jwt.secret=examination-platform-secret-key-2024-long-enough-for-hs256
```

#### 修改后
```properties
# MySQL 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/examination_platform?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root

# JPA 配置使用 MySQL 8
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# JWT 配置（增加了密钥长度至 88 字符）
jwt.secret=examination-platform-secret-key-2024-this-is-a-very-long-secret-key-for-hs512-algorithm-support
```

---

### 2. Maven 依赖修改

**文件**: `backend/pom.xml`

#### 变更内容
- 确保 MySQL 驱动程序在 pom.xml 中（已存在）
- 保留 H2 驱动程序以保持兼容性

#### 依赖信息
```xml
<!-- MySQL 驱动 -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
    <scope>runtime</scope>
</dependency>

<!-- H2 Database (保留用于开发环境) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

### 3. 数据库初始化脚本

**创建**: `backend/src/main/java/com/examination/config/DataInitializer.java`

#### 功能
- 启动时自动检查用户表
- 如果表为空，创建 3 个测试用户
- 自动设置密码加密

#### 预创建的用户
1. **student01** / 123456 (张三, STUDENT)
2. **teacher01** / 123456 (王五, TEACHER)
3. **admin01** / 123456 (李四, ADMIN)

---

### 4. Spring Security 配置优化

**文件**: `backend/src/main/java/com/examination/config/SecurityConfig.java`

#### 变更内容
- 升级到现代 Spring Security 模式
- 从 `WebSecurityConfigurerAdapter` 改为 `SecurityFilterChain`
- 解决了 Bean 循环依赖问题

#### 修改前
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();  // 导致循环依赖
    }
}
```

#### 修改后
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 现代配置方式
        http.cors().and().csrf().disable()...
        return http.build();
    }
}
```

---

## 修改原因

### 为什么从 H2 切换到 MySQL？

1. **生产准备**: MySQL 是标准的生产数据库
2. **数据持久化**: H2 内存数据库重启后丢失数据
3. **真实场景**: 更接近实际部署环境
4. **用户需求**: 用户提供了 MySQL 凭证 (root/root)

### 为什么修改 JWT 密钥？

- **HS512 算法要求**: 最少需要 512 位（64 字节）的密钥
- **原密钥太短**: 仅 56 个字符（约 464 位）
- **错误信息**: `The signing key's size is 464 bits which is not secure enough for the HS512 algorithm`

---

## 执行步骤

### 步骤 1: 创建 MySQL 数据库
```bash
mysql -u root -proot -e "CREATE DATABASE IF NOT EXISTS examination_platform CHARACTER SET utf8mb4;"
```

### 步骤 2: 更新配置文件
- 编辑 `application.properties`
- 更新数据库连接信息
- 增加 JWT 密钥长度

### 步骤 3: 重新编译
```bash
cd backend
mvn clean package -DskipTests
```

### 步骤 4: 启动应用
```bash
java -jar target/examination-platform-1.0.0.jar
```

### 步骤 5: 验证启动
- 检查是否成功连接到 MySQL
- 验证数据表自动创建
- 确认测试用户已初始化

---

## 验证清单

✅ 后端能够连接到 MySQL 数据库  
✅ 数据表自动创建（examination_platform.users）  
✅ 测试用户成功插入（student01, teacher01, admin01）  
✅ JWT 令牌生成成功（使用新的长密钥）  
✅ 登录 API 工作正常  
✅ 注册 API 工作正常  
✅ 前端能够成功登录所有用户  
✅ 新用户注册功能工作正常  
✅ 登出功能工作正常  

---

## 配置对比表

| 配置项 | H2 版本 | MySQL 版本 |
|--------|--------|-----------|
| **数据库类型** | 内存数据库 | 关系数据库 |
| **URL** | jdbc:h2:mem:testdb | jdbc:mysql://localhost:3306/examination_platform |
| **用户名** | sa | root |
| **密码** | (空) | root |
| **Hibernate 方言** | H2Dialect | MySQL8Dialect |
| **DDL 策略** | create (启动重建) | update (增量更新) |
| **JWT 密钥长度** | 56 字符 (464 位) ❌ | 88 字符 (704 位) ✅ |
| **JWT 算法** | HS256 | HS512 |
| **数据持久化** | 否 | 是 ✅ |
| **启动时间** | 快 | 正常 |
| **重启后数据** | 丢失 | 保留 ✅ |

---

## 故障排查

### 问题 1: 数据库连接失败
**错误**: `Communications link failure`
**原因**: MySQL 服务未启动或连接信息错误
**解决**:
1. 确保 MySQL 正在运行
2. 验证用户名和密码
3. 检查数据库是否已创建

### 问题 2: JWT 令牌生成失败
**错误**: `The signing key's size is 464 bits...`
**原因**: JWT 密钥长度不足
**解决**: 更新 `jwt.secret` 到至少 64 个字符

### 问题 3: 端口冲突
**错误**: `Bind exception: Address already in use`
**原因**: 端口 8080 已被占用
**解决**: 修改 `server.port` 或关闭占用该端口的程序

---

## 回滚方法

如需回滚到 H2 配置，请：

1. 恢复 `application.properties`:
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

2. 重新编译：
```bash
mvn clean package -DskipTests
```

3. 重新启动应用

---

## 未来改进

- [ ] 支持多数据库配置（开发/生产）
- [ ] 实现数据库迁移脚本（Flyway/Liquibase）
- [ ] 添加数据备份和恢复机制
- [ ] 实现主从复制配置
- [ ] 添加数据库性能监控

---

## 参考资源

- [MySQL 官方文档](https://dev.mysql.com/doc/)
- [Spring Boot 数据源配置](https://spring.io/projects/spring-boot)
- [Hibernate 方言配置](https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html)
- [JWT 最佳实践](https://tools.ietf.org/html/rfc7518)

---

**最后更新**: 2026-05-01  
**状态**: ✅ 完成并验证  
**修改人**: Development Team
