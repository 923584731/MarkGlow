-- MarkGlow 新功能数据库迁移脚本
-- 添加：提示词模板库、使用统计、RAG知识库
-- 执行时间: 2025-01-XX

-- ============================================
-- 1. 提示词模板表
-- ============================================
CREATE TABLE IF NOT EXISTS `prompt_templates` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL COMMENT '模板名称',
  `description` TEXT COMMENT '模板描述',
  `category` VARCHAR(100) COMMENT '分类：如 "写作助手", "内容优化", "代码相关", "分析工具"',
  `content` TEXT NOT NULL COMMENT '模板内容，支持 {{variable}} 变量',
  `variables` TEXT COMMENT 'JSON格式存储变量定义，如 [{"name":"topic","description":"主题","defaultValue":""}]',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_category` (`category`),
  INDEX `idx_name` (`name`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提示词模板库';

-- ============================================
-- 2. AI使用记录表
-- ============================================
CREATE TABLE IF NOT EXISTS `ai_usage_records` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `action` VARCHAR(100) NOT NULL COMMENT 'AI操作类型：beautify, improve, summarize等',
  `provider` VARCHAR(50) NOT NULL COMMENT 'AI服务提供商：ernie, qwen',
  `model` VARCHAR(100) COMMENT '使用的模型',
  `input_tokens` INT DEFAULT 0 COMMENT '输入token数',
  `output_tokens` INT DEFAULT 0 COMMENT '输出token数',
  `cost` DECIMAL(10, 6) DEFAULT 0.000000 COMMENT '成本（元）',
  `duration` BIGINT DEFAULT 0 COMMENT '耗时（毫秒）',
  `user_id` VARCHAR(100) COMMENT '用户ID（预留，当前可为空）',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_action` (`action`),
  INDEX `idx_provider` (`provider`),
  INDEX `idx_created_at` (`created_at`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI使用统计记录';

-- ============================================
-- 3. 知识库表
-- ============================================
CREATE TABLE IF NOT EXISTS `knowledge_bases` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL COMMENT '知识库名称',
  `description` TEXT COMMENT '知识库描述',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_name` (`name`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG知识库';

-- ============================================
-- 4. 知识库块表
-- ============================================
CREATE TABLE IF NOT EXISTS `knowledge_chunks` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `knowledge_base_id` BIGINT NOT NULL COMMENT '所属知识库ID',
  `content` TEXT NOT NULL COMMENT '文档块内容',
  `embedding` TEXT COMMENT 'JSON格式存储的向量（1024维）',
  `metadata` TEXT COMMENT 'JSON格式存储元数据（如来源、页码等）',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_knowledge_base_id` (`knowledge_base_id`),
  INDEX `idx_created_at` (`created_at`),
  CONSTRAINT `fk_chunk_knowledge_base` FOREIGN KEY (`knowledge_base_id`) 
    REFERENCES `knowledge_bases` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库文档块';

-- ============================================
-- 插入示例数据（可选）
-- ============================================

-- 示例提示词模板
INSERT INTO `prompt_templates` (`name`, `description`, `category`, `content`, `variables`) VALUES
('技术文档写作', '用于编写技术文档的模板', '写作助手', 
 '请帮我写一篇关于{{topic}}的技术文档，要求：\n1. 结构清晰\n2. 包含代码示例\n3. 易于理解', 
 '[{"name":"topic","description":"技术主题","defaultValue":"API使用"}]'),
('内容优化', '优化文本表达的模板', '内容优化', 
 '请优化以下内容，使其更加{{style}}：\n\n{{content}}', 
 '[{"name":"style","description":"优化风格","defaultValue":"专业、清晰"},{"name":"content","description":"待优化内容","defaultValue":""}]')
ON DUPLICATE KEY UPDATE `name`=`name`;

