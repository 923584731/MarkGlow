# MySQL 数据库配置指南

## 一、数据库准备

### 1. 安装 MySQL（如果未安装）

- Windows: 下载 MySQL Installer from https://dev.mysql.com/downloads/installer/
- 或使用 XAMPP/WAMP 等集成环境

### 2. 创建数据库

登录 MySQL 后执行：

```sql
CREATE DATABASE IF NOT EXISTS markglow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 创建用户（可选，推荐）

```sql
CREATE USER 'markglow_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON markglow.* TO 'markglow_user'@'localhost';
FLUSH PRIVILEGES;
```

## 二、配置数据库连接

### 修改 `backend/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/markglow?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root  # 修改为你的MySQL用户名
    password: your_password  # 修改为你的MySQL密码
```

### 如果使用自定义用户：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/markglow?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: markglow_user
    password: your_password
```

## 三、启动应用

1. 确保 MySQL 服务正在运行
2. 启动后端应用
3. Spring Boot 会自动创建表结构（`ddl-auto: update`）

## 四、验证

启动应用后，检查日志中是否有数据库连接成功的消息。如果看到表创建日志，说明配置成功。

## 五、注意事项

1. **首次运行后**，建议将 `ddl-auto` 从 `update` 改为 `validate`，避免意外修改表结构：
   ```yaml
   jpa:
     hibernate:
       ddl-auto: validate
   ```

2. **备份数据**：定期备份数据库，避免数据丢失

3. **字符编码**：确保使用 `utf8mb4` 字符集，支持完整的 Unicode 字符（包括 emoji）

## 六、常见问题

### 问题1：连接被拒绝
- 检查 MySQL 服务是否启动
- 检查端口号是否为 3306
- 检查防火墙设置

### 问题2：Access denied
- 检查用户名和密码是否正确
- 检查用户是否有访问数据库的权限

### 问题3：时区错误
- 确保 URL 中包含 `serverTimezone=Asia/Shanghai`
- 或在 MySQL 中设置时区

### 问题4：字符编码问题
- 确保数据库使用 `utf8mb4` 字符集
- 确保连接 URL 中包含 `useUnicode=true&characterEncoding=utf8`

