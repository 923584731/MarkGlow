# 故障排查指南

## 数据库连接错误

### 错误信息
```
Unable to open JDBC Connection for DDL execution
Error creating bean with name 'entityManagerFactory'
```

### 可能原因

1. **MySQL 服务未启动**
2. **数据库不存在**
3. **用户名/密码错误**
4. **端口号错误**
5. **网络连接问题**

## 排查步骤

### 步骤 1: 检查 MySQL 服务是否运行

**Windows:**
```bash
# 方式1: 检查服务
services.msc
# 查找 MySQL 服务，确保状态为"正在运行"

# 方式2: 使用命令行
net start | findstr MySQL
```

**Linux/Mac:**
```bash
# 检查 MySQL 服务状态
sudo systemctl status mysql
# 或
sudo service mysql status
```

**如果服务未启动，启动它：**
```bash
# Windows
net start MySQL

# Linux/Mac
sudo systemctl start mysql
# 或
sudo service mysql start
```

### 步骤 2: 测试 MySQL 连接

```bash
# 尝试连接 MySQL
mysql -u root -p
```

如果无法连接，说明 MySQL 服务有问题。

### 步骤 3: 检查数据库是否存在

```sql
-- 登录 MySQL 后执行
SHOW DATABASES LIKE 'markglow';
```

如果数据库不存在，创建它：
```sql
CREATE DATABASE IF NOT EXISTS markglow 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;
```

### 步骤 4: 检查用户名和密码

在 `application.yml` 中确认：
```yaml
spring:
  datasource:
    username: root  # 你的 MySQL 用户名
    password:       # 你的 MySQL 密码（如果有密码，必须填写）
```

**重要**: 如果 MySQL root 用户有密码，必须在配置文件中填写！

### 步骤 5: 检查端口号

默认 MySQL 端口是 3306。如果使用其他端口，修改 URL：
```yaml
url: jdbc:mysql://localhost:你的端口号/markglow?...
```

### 步骤 6: 测试连接 URL

可以使用以下 Java 代码测试连接，或使用数据库工具（如 MySQL Workbench、Navicat）测试连接。

## 快速解决方案

### 方案 1: 使用 H2 内存数据库（临时方案）

如果暂时无法解决 MySQL 连接问题，可以临时使用 H2 数据库：

1. **取消注释 H2 依赖**（在 `pom.xml` 中）：
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

2. **修改 `application.yml`**：
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

3. **注释掉 MySQL 依赖**（在 `pom.xml` 中）：
```xml
<!-- MySQL 驱动 -->
<!--
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
    <scope>runtime</scope>
</dependency>
-->
```

**注意**: H2 是内存数据库，重启后数据会丢失。

### 方案 2: 修复 MySQL 连接

#### 2.1 确保 MySQL 服务运行

#### 2.2 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS markglow 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;
```

#### 2.3 配置正确的用户名和密码

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/markglow?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root          # 修改为你的 MySQL 用户名
    password: your_password # 修改为你的 MySQL 密码（如果有）
```

#### 2.4 验证配置

使用 MySQL 客户端工具测试连接：
- 主机: `localhost`
- 端口: `3306`
- 用户名: `root`（或你配置的用户名）
- 密码: 你配置的密码
- 数据库: `markglow`

### 方案 3: 使用环境变量配置（推荐）

为了避免在配置文件中暴露密码，可以使用环境变量：

1. **修改 `application.yml`**：
```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:markglow}?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: ${DB_USER:root}
    password: ${DB_PASSWORD:}
```

2. **设置环境变量**：

**Windows (PowerShell):**
```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="markglow"
$env:DB_USER="root"
$env:DB_PASSWORD="your_password"
```

**Windows (CMD):**
```cmd
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=markglow
set DB_USER=root
set DB_PASSWORD=your_password
```

**Linux/Mac:**
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=markglow
export DB_USER=root
export DB_PASSWORD=your_password
```

## 常见错误及解决方案

### 错误 1: `Communications link failure`

**原因**: MySQL 服务未启动或无法连接

**解决**:
1. 启动 MySQL 服务
2. 检查防火墙设置
3. 检查 MySQL 是否监听在 3306 端口

### 错误 2: `Access denied for user 'root'@'localhost'`

**原因**: 用户名或密码错误

**解决**:
1. 检查 `application.yml` 中的用户名和密码
2. 使用 MySQL 客户端测试连接
3. 如果忘记密码，重置 MySQL root 密码

### 错误 3: `Unknown database 'markglow'`

**原因**: 数据库不存在

**解决**:
```sql
CREATE DATABASE markglow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 错误 4: `The server time zone value 'xxx' is unrecognized`

**原因**: 时区配置问题

**解决**: 确保连接 URL 中包含 `serverTimezone=Asia/Shanghai`

### 错误 5: `Public Key Retrieval is not allowed`

**原因**: MySQL 8.0 的安全限制

**解决**: 确保连接 URL 中包含 `allowPublicKeyRetrieval=true`

## 验证连接

启动应用后，查看日志应该看到类似信息：
```
Hibernate: create table documents ...
```

如果看到表创建的 SQL 语句，说明连接成功。

## 获取帮助

如果以上方法都无法解决问题，请提供：
1. 完整的错误日志
2. MySQL 版本信息：`mysql --version`
3. 操作系统信息
4. `application.yml` 配置（隐藏敏感信息）

