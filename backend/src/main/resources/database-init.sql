-- ============================================
-- MarkGlow 数据库完整初始化脚本
-- 用途: 手动执行此脚本可以快速创建数据库和所有表结构
-- 执行方式: mysql -u root -p < database-init.sql
-- ============================================

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `markglow` 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `markglow`;

-- ============================================
-- 1. 表: documents (文档表)
-- ============================================
DROP TABLE IF EXISTS `documents`;

CREATE TABLE `documents` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '文档ID，主键',
    `title` VARCHAR(255) NOT NULL COMMENT '文档标题',
    `original_content` TEXT COMMENT '原始Markdown内容',
    `beautified_content` TEXT COMMENT '美化后的Markdown内容',
    `theme` VARCHAR(50) DEFAULT NULL COMMENT '使用的主题名称',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_updated_at` (`updated_at`) COMMENT '更新时间索引，用于按更新时间排序',
    KEY `idx_title` (`title`(100)) COMMENT '标题索引，用于标题搜索',
    KEY `idx_created_at` (`created_at`) COMMENT '创建时间索引'
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='文档表，存储Markdown文档信息';

-- ============================================
-- 2. 表: prompt_templates (提示词模板表)
-- ============================================
DROP TABLE IF EXISTS `prompt_templates`;

CREATE TABLE `prompt_templates` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '模板ID，主键',
    `name` VARCHAR(255) NOT NULL COMMENT '模板名称',
    `description` TEXT COMMENT '模板描述',
    `category` VARCHAR(100) DEFAULT NULL COMMENT '分类：如 "写作助手", "内容优化", "代码相关", "分析工具"',
    `content` TEXT NOT NULL COMMENT '模板内容，支持 {{variable}} 变量',
    `variables` TEXT COMMENT 'JSON格式存储变量定义，如 [{"name":"topic","description":"主题","defaultValue":""}]',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`) COMMENT '分类索引',
    KEY `idx_name` (`name`) COMMENT '名称索引',
    KEY `idx_created_at` (`created_at`) COMMENT '创建时间索引'
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='提示词模板库';

-- ============================================
-- 3. 表: ai_usage_records (AI使用记录表)
-- ============================================
DROP TABLE IF EXISTS `ai_usage_records`;

CREATE TABLE `ai_usage_records` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID，主键',
    `action` VARCHAR(100) NOT NULL COMMENT 'AI操作类型：beautify, improve, summarize等',
    `provider` VARCHAR(50) NOT NULL COMMENT 'AI服务提供商：ernie, qwen',
    `model` VARCHAR(100) DEFAULT NULL COMMENT '使用的模型',
    `input_tokens` INT(11) DEFAULT 0 COMMENT '输入token数',
    `output_tokens` INT(11) DEFAULT 0 COMMENT '输出token数',
    `cost` DECIMAL(10, 6) DEFAULT 0.000000 COMMENT '成本（元）',
    `duration` BIGINT(20) DEFAULT 0 COMMENT '耗时（毫秒）',
    `user_id` VARCHAR(100) DEFAULT NULL COMMENT '用户ID（预留，当前可为空）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_action` (`action`) COMMENT '操作类型索引',
    KEY `idx_provider` (`provider`) COMMENT '服务提供商索引',
    KEY `idx_created_at` (`created_at`) COMMENT '创建时间索引，用于统计查询',
    KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引'
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='AI使用统计记录';

-- ============================================
-- 4. 表: knowledge_bases (知识库表)
-- ============================================
DROP TABLE IF EXISTS `knowledge_bases`;

CREATE TABLE `knowledge_bases` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '知识库ID，主键',
    `name` VARCHAR(255) NOT NULL COMMENT '知识库名称',
    `description` TEXT COMMENT '知识库描述',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`) COMMENT '名称索引',
    KEY `idx_created_at` (`created_at`) COMMENT '创建时间索引'
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='RAG知识库';

-- ============================================
-- 5. 表: knowledge_chunks (知识库块表)
-- ============================================
DROP TABLE IF EXISTS `knowledge_chunks`;

CREATE TABLE `knowledge_chunks` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '块ID，主键',
    `knowledge_base_id` BIGINT(20) NOT NULL COMMENT '所属知识库ID',
    `content` TEXT NOT NULL COMMENT '文档块内容',
    `embedding` TEXT COMMENT 'JSON格式存储的向量（1024维）',
    `metadata` TEXT COMMENT 'JSON格式存储元数据（如来源、页码等）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_knowledge_base_id` (`knowledge_base_id`) COMMENT '知识库ID索引',
    KEY `idx_created_at` (`created_at`) COMMENT '创建时间索引',
    CONSTRAINT `fk_chunk_knowledge_base` FOREIGN KEY (`knowledge_base_id`) 
        REFERENCES `knowledge_bases` (`id`) ON DELETE CASCADE COMMENT '外键：关联知识库表'
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='知识库文档块';

-- ============================================
-- 显示表结构（验证）
-- ============================================
SHOW CREATE TABLE `documents`;
SHOW CREATE TABLE `prompt_templates`;
SHOW CREATE TABLE `ai_usage_records`;
SHOW CREATE TABLE `knowledge_bases`;
SHOW CREATE TABLE `knowledge_chunks`;

-- ============================================
-- 显示表信息
-- ============================================
SHOW TABLE STATUS;

-- ============================================
-- 完成提示
-- ============================================
SELECT 'Database and all tables created successfully!' AS message;
