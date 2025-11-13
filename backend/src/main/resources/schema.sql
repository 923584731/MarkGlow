-- ============================================
-- MarkGlow 数据库初始化脚本
-- 数据库: markglow
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_unicode_ci
-- ============================================

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
    KEY `idx_updated_at` (`updated_at`) COMMENT '更新时间索引，用于排序',
    KEY `idx_title` (`title`(100)) COMMENT '标题索引，用于搜索'
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='文档表，存储Markdown文档信息';

-- ============================================
-- 初始化数据（可选）
-- ============================================
-- INSERT INTO `documents` (`title`, `original_content`, `beautified_content`, `theme`, `created_at`, `updated_at`)
-- VALUES 
--     ('欢迎使用 MarkGlow', '# 欢迎使用 MarkGlow\n\n这是一个示例文档。', '# 欢迎使用 MarkGlow\n\n这是一个示例文档。', 'default', NOW(), NOW());

