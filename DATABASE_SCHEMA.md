# 数据库表结构说明

## 数据库信息

- **数据库名**: `markglow`
- **字符集**: `utf8mb4`
- **排序规则**: `utf8mb4_unicode_ci`
- **存储引擎**: `InnoDB`

## 表结构

### documents 表（文档表）

存储 Markdown 文档的基本信息和内容。

#### 字段说明

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| `id` | BIGINT(20) | PRIMARY KEY, AUTO_INCREMENT | 文档ID，主键，自增 |
| `title` | VARCHAR(255) | NOT NULL | 文档标题 |
| `original_content` | TEXT | NULL | 原始Markdown内容 |
| `beautified_content` | TEXT | NULL | 美化后的Markdown内容 |
| `theme` | VARCHAR(50) | NULL | 使用的主题名称 |
| `created_at` | DATETIME | NULL | 创建时间 |
| `updated_at` | DATETIME | NULL | 更新时间 |

#### 索引说明

| 索引名 | 字段 | 类型 | 说明 |
|--------|------|------|------|
| PRIMARY | `id` | PRIMARY KEY | 主键索引 |
| `idx_updated_at` | `updated_at` | KEY | 更新时间索引，用于按更新时间排序 |
| `idx_title` | `title`(100) | KEY | 标题索引，用于标题搜索（前缀索引，前100字符） |
| `idx_created_at` | `created_at` | KEY | 创建时间索引 |

#### 表注释

- **表名**: `documents`
- **表注释**: 文档表，存储Markdown文档信息
- **存储引擎**: InnoDB
- **字符集**: utf8mb4
- **排序规则**: utf8mb4_unicode_ci

## SQL 脚本文件

项目提供了多个 SQL 脚本文件，位于 `backend/src/main/resources/` 目录：

### 1. schema.sql
Spring Boot 自动执行的建表脚本（如果配置了 `spring.sql.init.mode=always`）

### 2. database-init.sql
完整的数据库初始化脚本，可以手动执行：
```bash
mysql -u root -p < backend/src/main/resources/database-init.sql
```

### 3. data.sql
初始化数据脚本（可选，用于插入示例数据）

## 使用方法

### 方法 1: 使用 Spring Boot 自动创建（推荐）

在 `application.yml` 中配置：
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # 自动创建/更新表结构
```

启动应用后，Hibernate 会自动创建表结构。

### 方法 2: 手动执行 SQL 脚本

1. **使用 MySQL 命令行**:
   ```bash
   mysql -u root -p < backend/src/main/resources/database-init.sql
   ```

2. **使用 MySQL Workbench 或其他工具**:
   - 打开 `backend/src/main/resources/database-init.sql`
   - 复制内容到工具中执行

3. **使用命令行连接后执行**:
   ```bash
   mysql -u root -p
   source backend/src/main/resources/database-init.sql
   ```

### 方法 3: 使用 schema.sql（需要配置）

在 `application.yml` 中添加：
```yaml
spring:
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql
```

## 字段类型映射

| Java 类型 | MySQL 类型 | 说明 |
|-----------|------------|------|
| `Long` | `BIGINT(20)` | 长整型，用于主键 |
| `String` (无长度限制) | `VARCHAR(255)` | 字符串，默认255字符 |
| `String` (TEXT) | `TEXT` | 大文本，用于长内容 |
| `LocalDateTime` | `DATETIME` | 日期时间 |

## 注意事项

1. **字符集**: 使用 `utf8mb4` 而不是 `utf8`，以支持完整的 Unicode 字符（包括 emoji）

2. **索引优化**:
   - `updated_at` 索引：用于按更新时间排序（文档列表默认按更新时间倒序）
   - `title` 索引：用于标题搜索（使用前缀索引，只索引前100字符）
   - `created_at` 索引：用于按创建时间排序

3. **TEXT 字段**: `original_content` 和 `beautified_content` 使用 TEXT 类型，可以存储最大 65,535 字节的内容

4. **自动时间戳**: 
   - `created_at` 和 `updated_at` 由 JPA 的 `@PrePersist` 和 `@PreUpdate` 自动设置
   - 数据库层面不设置默认值，由应用层控制

5. **生产环境建议**:
   - 首次创建表后，将 `ddl-auto` 改为 `validate`，避免意外修改表结构
   - 使用 Flyway 或 Liquibase 进行数据库版本管理

## 扩展表结构

如果需要添加新表，参考以下步骤：

1. 创建新的 Entity 类（使用 Lombok `@Data` 注解）
2. 在 `database-init.sql` 中添加对应的 CREATE TABLE 语句
3. 更新本文档说明新表结构

## 验证表结构

执行以下 SQL 可以验证表结构：

```sql
-- 查看表结构
DESC documents;

-- 查看建表语句
SHOW CREATE TABLE documents;

-- 查看表信息
SHOW TABLE STATUS LIKE 'documents';

-- 查看索引
SHOW INDEX FROM documents;
```

