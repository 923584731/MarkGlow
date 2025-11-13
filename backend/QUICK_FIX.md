# 快速修复数据库连接问题

## 问题
启动后端时出现：`Unable to open JDBC Connection for DDL execution`

## 快速解决方案

### 方案 A: 修复 MySQL 连接（推荐）

#### 1. 检查 MySQL 服务
```bash
# Windows
net start | findstr MySQL

# 如果未运行，启动它
net start MySQL
```

#### 2. 创建数据库
```bash
mysql -u root -p
```

然后执行：
```sql
CREATE DATABASE IF NOT EXISTS markglow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;
```

#### 3. 配置密码（如果 MySQL root 有密码）

编辑 `backend/src/main/resources/application.yml`，修改：
```yaml
spring:
  datasource:
    password: 你的MySQL密码  # 如果有密码，必须填写
```

或者使用环境变量（更安全）：
```bash
# Windows PowerShell
$env:DB_PASSWORD="你的MySQL密码"

# Windows CMD
set DB_PASSWORD=你的MySQL密码

# Linux/Mac
export DB_PASSWORD=你的MySQL密码
```

#### 4. 重新启动应用

### 方案 B: 临时使用 H2 数据库（如果 MySQL 无法使用）

#### 1. 修改 `backend/pom.xml`

取消注释 H2 依赖，注释 MySQL 依赖：
```xml
<!-- 注释掉 MySQL -->
<!--
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
    <scope>runtime</scope>
</dependency>
-->

<!-- 取消注释 H2 -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

#### 2. 修改 `backend/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:markglowdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
```

#### 3. 重新编译和启动

```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

**注意**: H2 是内存数据库，重启后数据会丢失。

## 验证

启动成功后，应该看到：
- 没有数据库连接错误
- 看到表创建的 SQL 日志（`Hibernate: create table documents...`）

## 需要帮助？

查看 [TROUBLESHOOTING.md](../TROUBLESHOOTING.md) 获取详细的故障排查指南。

