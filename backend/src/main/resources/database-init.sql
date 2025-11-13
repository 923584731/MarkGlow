-- ============================================
-- MarkGlow 数据库完整初始化脚本
-- 用途: 手动执行此脚本可以快速创建数据库和表结构
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
-- 表: documents (文档表)
-- ============================================
DROP TABLE IF EXISTS `documents`;

CREATE TABLE `documents` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '文档ID，主键',
    `title` VARCHAR(255) NOT NULL COMMENT '文档标题',
    `original_content` TEXT COMMENT '原始Markdown内容',
    `beautified_content` TEXT COMMENT '美化后的Markdown内容',
    `theme` VARCHAR(50) DEFAULT NULL COMMENT '使用的主题名称',
    `created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_updated_at` (`updated_at`) COMMENT '更新时间索引，用于按更新时间排序',
    KEY `idx_title` (`title`(100)) COMMENT '标题索引，用于标题搜索',
    KEY `idx_created_at` (`created_at`) COMMENT '创建时间索引'
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='文档表，存储Markdown文档信息';

-- ============================================
-- 显示表结构（验证）
-- ============================================
SHOW CREATE TABLE `documents`;

-- ============================================
-- 显示表信息
-- ============================================
SHOW TABLE STATUS LIKE 'documents';

-- ============================================
-- 完成提示
-- ============================================
SELECT 'Database and tables created successfully!' AS message;

