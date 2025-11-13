# MarkGlow 数据库设置说明

## 数据库表结构

本项目需要以下5个数据库表：

### 1. documents（文档表）
存储 Markdown 文档的基本信息。

### 2. prompt_templates（提示词模板表）
存储 AI 提示词模板，支持变量替换。

### 3. ai_usage_records（AI使用记录表）
记录每次 AI 调用的详细信息，用于统计和成本分析。

### 4. knowledge_bases（知识库表）
存储 RAG 知识库的基本信息。

### 5. knowledge_chunks（知识库块表）
存储知识库的文档块和向量嵌入。

## SQL 脚本文件说明

### 1. database-init.sql（推荐使用）
**用途**：完整的数据库初始化脚本，包含所有表的创建语句。

**使用方式**：
```bash
mysql -u root -p < database-init.sql
```

**特点**：
- 自动创建数据库 `markglow`
- 使用 `DROP TABLE IF EXISTS` 确保干净的环境
- 包含所有索引和外键约束
- 适合全新安装

### 2. schema.sql
**用途**：表结构定义脚本，使用 `CREATE TABLE IF NOT EXISTS`。

**使用方式**：
```bash
mysql -u root -p markglow < schema.sql
```

**特点**：
- 不会删除现有表
- 如果表已存在则跳过
- 适合已有数据库环境

### 3. migration_add_new_features.sql
**用途**：迁移脚本，只包含新增功能的表（提示词模板、使用统计、RAG知识库）。

**使用方式**：
```bash
mysql -u root -p markglow < migration_add_new_features.sql
```

**特点**：
- 只创建新功能的表
- 适合已有 `documents` 表的升级场景
- 包含示例数据

## 表结构详情

### prompt_templates（提示词模板表）
- `id`: 主键，自增
- `name`: 模板名称（必填）
- `description`: 模板描述
- `category`: 分类（写作助手、内容优化、代码相关、分析工具等）
- `content`: 模板内容，支持 `{{variable}}` 变量
- `variables`: JSON格式的变量定义
- `created_at`: 创建时间
- `updated_at`: 更新时间

**索引**：
- `idx_category`: 分类索引
- `idx_name`: 名称索引
- `idx_created_at`: 创建时间索引

### ai_usage_records（AI使用记录表）
- `id`: 主键，自增
- `action`: AI操作类型（beautify, improve, summarize等）
- `provider`: AI服务提供商（ernie, qwen）
- `model`: 使用的模型
- `input_tokens`: 输入token数
- `output_tokens`: 输出token数
- `cost`: 成本（元，精度6位小数）
- `duration`: 耗时（毫秒）
- `user_id`: 用户ID（预留字段）
- `created_at`: 创建时间

**索引**：
- `idx_action`: 操作类型索引
- `idx_provider`: 服务提供商索引
- `idx_created_at`: 创建时间索引（用于统计查询）
- `idx_user_id`: 用户ID索引

### knowledge_bases（知识库表）
- `id`: 主键，自增
- `name`: 知识库名称（必填）
- `description`: 知识库描述
- `created_at`: 创建时间
- `updated_at`: 更新时间

**索引**：
- `idx_name`: 名称索引
- `idx_created_at`: 创建时间索引

### knowledge_chunks（知识库块表）
- `id`: 主键，自增
- `knowledge_base_id`: 所属知识库ID（必填，外键）
- `content`: 文档块内容（必填）
- `embedding`: JSON格式存储的向量（1024维）
- `metadata`: JSON格式存储元数据
- `created_at`: 创建时间

**索引**：
- `idx_knowledge_base_id`: 知识库ID索引
- `idx_created_at`: 创建时间索引

**外键**：
- `fk_chunk_knowledge_base`: 关联 `knowledge_bases.id`，级联删除

## 字符集和排序规则

所有表使用：
- 字符集：`utf8mb4`
- 排序规则：`utf8mb4_unicode_ci`
- 存储引擎：`InnoDB`

## 注意事项

1. **外键约束**：`knowledge_chunks` 表有外键约束，删除知识库时会自动删除相关的文档块。

2. **时间字段**：所有时间字段使用 `DATETIME` 类型，并设置默认值为 `CURRENT_TIMESTAMP`。

3. **TEXT 字段**：大量文本内容使用 `TEXT` 类型，支持存储较长的内容。

4. **JSON 字段**：变量定义、向量嵌入、元数据等使用 `TEXT` 类型存储 JSON 字符串，由应用层解析。

5. **索引优化**：为常用查询字段创建了索引，提高查询性能。

## 验证安装

执行脚本后，可以使用以下命令验证：

```sql
USE markglow;
SHOW TABLES;
DESCRIBE prompt_templates;
DESCRIBE ai_usage_records;
DESCRIBE knowledge_bases;
DESCRIBE knowledge_chunks;
```

## 升级说明

如果您的数据库已经存在 `documents` 表，请使用 `migration_add_new_features.sql` 脚本添加新表。

如果是从零开始，请使用 `database-init.sql` 脚本。

