-- MarkGlow 提示词模板库初始化数据
-- 用途: 为系统添加默认的提示词模板示例
-- 执行方式: mysql -u root -p markglow < init-templates.sql

USE `markglow`;

-- 清空现有模板（可选，如果需要重置）
-- DELETE FROM `prompt_templates`;

-- 1. 技术文档写作（写作助手）
INSERT INTO `prompt_templates` (`name`, `description`, `category`, `content`, `variables`, `created_at`, `updated_at`) VALUES
('技术文档写作', '用于生成技术文档、API文档等专业文档', '写作助手', 
'请帮我写一篇关于{{topic}}的技术文档。\n\n要求：\n1. 文档结构清晰，包含概述、详细说明、示例代码\n2. 使用Markdown格式\n3. 包含实际可运行的代码示例\n4. 添加必要的注意事项和最佳实践\n\n主题：{{topic}}\n目标读者：{{audience}}\n技术栈：{{techStack}}', 
'[{"name":"topic","description":"技术主题","defaultValue":"RESTful API设计"},{"name":"audience","description":"目标读者","defaultValue":"开发人员"},{"name":"techStack","description":"技术栈","defaultValue":"Spring Boot"}]', 
NOW(), NOW());

-- 2. 产品介绍生成（写作助手）
INSERT INTO `prompt_templates` (`name`, `description`, `category`, `content`, `variables`, `created_at`, `updated_at`) VALUES
('产品介绍生成', '生成产品介绍、功能说明等营销文案', '写作助手', 
'请为{{productName}}产品写一份产品介绍文档。\n\n产品信息：\n- 产品名称：{{productName}}\n- 核心功能：{{features}}\n- 目标用户：{{targetUsers}}\n- 产品优势：{{advantages}}\n\n要求：\n1. 突出产品核心价值和优势\n2. 语言简洁有力，易于理解\n3. 包含使用场景说明\n4. 适合在官网、宣传材料中使用', 
'[{"name":"productName","description":"产品名称","defaultValue":"MarkGlow"},{"name":"features","description":"核心功能","defaultValue":"Markdown编辑、AI美化、实时预览"},{"name":"targetUsers","description":"目标用户","defaultValue":"内容创作者、开发者"},{"name":"advantages","description":"产品优势","defaultValue":"简单易用、功能强大"}]', 
NOW(), NOW());

-- 3. 邮件写作助手（写作助手）
INSERT INTO `prompt_templates` (`name`, `description`, `category`, `content`, `variables`, `created_at`, `updated_at`) VALUES
('邮件写作助手', '帮助撰写商务邮件、工作邮件等', '写作助手', 
'请帮我写一封{{emailType}}邮件。\n\n邮件信息：\n- 收件人：{{recipient}}\n- 主题：{{subject}}\n- 邮件类型：{{emailType}}\n- 主要内容：{{content}}\n- 语气：{{tone}}\n\n要求：\n1. 格式规范，包含称呼、正文、结尾\n2. 语言{{tone}}，符合商务场景\n3. 内容清晰，目的明确\n4. 长度适中，重点突出', 
'[{"name":"emailType","description":"邮件类型","defaultValue":"工作邮件"},{"name":"recipient","description":"收件人","defaultValue":"同事"},{"name":"subject","description":"邮件主题","defaultValue":"项目进度汇报"},{"name":"content","description":"主要内容","defaultValue":"汇报本周项目进展情况"},{"name":"tone","description":"语气","defaultValue":"专业、友好"}]', 
NOW(), NOW());

-- 4. 内容润色优化（内容优化）
INSERT INTO `prompt_templates` (`name`, `description`, `category`, `content`, `variables`, `created_at`, `updated_at`) VALUES
('内容润色优化', '优化文本表达，提升可读性和专业性', '内容优化', 
'请对以下内容进行润色优化，使其更加{{style}}：\n\n{{content}}\n\n优化要求：\n1. 保持原意不变\n2. 提升语言表达的{{style}}\n3. 改善段落结构和逻辑\n4. 修正语法错误和表达不当之处\n5. 优化用词，使其更加精准', 
'[{"name":"style","description":"优化风格","defaultValue":"专业、清晰"},{"name":"content","description":"待优化内容","defaultValue":""}]', 
NOW(), NOW());

-- 5. 标题优化（内容优化）
INSERT INTO `prompt_templates` (`name`, `description`, `category`, `content`, `variables`, `created_at`, `updated_at`) VALUES
('标题优化', '优化文章标题，使其更吸引人且准确', '内容优化', 
'请为以下内容生成或优化标题：\n\n内容摘要：{{summary}}\n\n要求：\n1. 标题要准确反映内容主题\n2. 吸引读者注意力\n3. 长度适中（建议10-20字）\n4. 风格：{{style}}\n5. 生成3-5个备选标题，并说明推荐理由', 
'[{"name":"summary","description":"内容摘要","defaultValue":"介绍如何使用AI工具提升工作效率"},{"name":"style","description":"标题风格","defaultValue":"简洁有力"}]', 
NOW(), NOW());

-- 6. 代码注释生成（代码相关）
INSERT INTO `prompt_templates` (`name`, `description`, `category`, `content`, `variables`, `created_at`, `updated_at`) VALUES
('代码注释生成', '为代码生成详细的注释和文档', '代码相关', 
'请为以下{{language}}代码生成详细的注释：\n\n```{{language}}\n{{code}}\n```\n\n要求：\n1. 为每个函数/方法添加功能说明\n2. 为关键变量和逻辑添加行内注释\n3. 使用{{commentStyle}}风格的注释\n4. 说明参数、返回值、异常情况\n5. 添加使用示例（如适用）', 
'[{"name":"language","description":"编程语言","defaultValue":"JavaScript"},{"name":"code","description":"代码内容","defaultValue":""},{"name":"commentStyle","description":"注释风格","defaultValue":"JSDoc"}]', 
NOW(), NOW());

-- 7. 代码审查（代码相关）
INSERT INTO `prompt_templates` (`name`, `description`, `category`, `content`, `variables`, `created_at`, `updated_at`) VALUES
('代码审查', '审查代码质量，发现潜在问题和改进建议', '代码相关', 
'请对以下{{language}}代码进行审查：\n\n```{{language}}\n{{code}}\n```\n\n审查重点：\n1. 代码逻辑是否正确\n2. 是否存在性能问题\n3. 代码风格和规范\n4. 安全性问题\n5. 可维护性和可读性\n6. 错误处理是否完善\n\n请提供详细的审查报告和改进建议。', 
'[{"name":"language","description":"编程语言","defaultValue":"Java"},{"name":"code","description":"代码内容","defaultValue":""}]', 
NOW(), NOW());

-- 8. 文档摘要生成（分析工具）
INSERT INTO `prompt_templates` (`name`, `description`, `category`, `content`, `variables`, `created_at`, `updated_at`) VALUES
('文档摘要生成', '为长文档生成简洁明了的摘要', '分析工具', 
'请为以下文档生成摘要：\n\n{{document}}\n\n要求：\n1. 摘要长度：{{length}}\n2. 突出核心观点和关键信息\n3. 保持逻辑清晰\n4. 使用简洁明了的语言\n5. 包含主要结论（如适用）', 
'[{"name":"document","description":"文档内容","defaultValue":""},{"name":"length","description":"摘要长度","defaultValue":"200-300字"}]', 
NOW(), NOW());

-- 9. 关键词提取（分析工具）
INSERT INTO `prompt_templates` (`name`, `description`, `category`, `content`, `variables`, `created_at`, `updated_at`) VALUES
('关键词提取', '从文本中提取关键词和主题', '分析工具', 
'请从以下文本中提取关键词：\n\n{{text}}\n\n要求：\n1. 提取{{count}}个关键词\n2. 按重要性排序\n3. 包含关键词的上下文说明\n4. 识别主题和子主题\n5. 标注关键词类型（如：技术术语、概念、实体等）', 
'[{"name":"text","description":"文本内容","defaultValue":""},{"name":"count","description":"关键词数量","defaultValue":"10"}]', 
NOW(), NOW());

-- 10. 通用问答模板（其他）
INSERT INTO `prompt_templates` (`name`, `description`, `category`, `content`, `variables`, `created_at`, `updated_at`) VALUES
('通用问答模板', '用于回答各种问题的通用模板', '其他', 
'请回答以下问题：\n\n问题：{{question}}\n\n上下文信息：{{context}}\n\n要求：\n1. 回答要准确、全面\n2. 如果涉及技术问题，提供代码示例\n3. 如果涉及概念，提供清晰的定义和解释\n4. 如果问题不明确，先澄清问题再回答\n5. 提供相关参考资料或延伸阅读（如适用）', 
'[{"name":"question","description":"问题内容","defaultValue":""},{"name":"context","description":"上下文信息","defaultValue":"无"}]', 
NOW(), NOW());

-- 11. 会议纪要生成（写作助手）
INSERT INTO `prompt_templates` (`name`, `description`, `category`, `content`, `variables`, `created_at`, `updated_at`) VALUES
('会议纪要生成', '根据会议内容生成规范的会议纪要', '写作助手', 
'请根据以下会议内容生成会议纪要：\n\n会议主题：{{topic}}\n会议时间：{{time}}\n参会人员：{{participants}}\n会议内容：{{content}}\n\n要求：\n1. 包含会议基本信息（时间、地点、参会人员）\n2. 记录主要讨论内容\n3. 明确会议决议和行动项\n4. 标注负责人和截止时间\n5. 格式规范，便于后续查阅', 
'[{"name":"topic","description":"会议主题","defaultValue":"项目进度讨论"},{"name":"time","description":"会议时间","defaultValue":"2025-01-XX"},{"name":"participants","description":"参会人员","defaultValue":"张三、李四、王五"},{"name":"content","description":"会议内容","defaultValue":"讨论项目进展和下一步计划"}]', 
NOW(), NOW());

-- 12. 需求文档生成（写作助手）
INSERT INTO `prompt_templates` (`name`, `description`, `category`, `content`, `variables`, `created_at`, `updated_at`) VALUES
('需求文档生成', '生成产品需求文档或功能需求文档', '写作助手', 
'请生成一份{{docType}}需求文档：\n\n需求概述：{{overview}}\n功能描述：{{features}}\n用户场景：{{scenarios}}\n技术要求：{{requirements}}\n\n要求：\n1. 文档结构完整（概述、功能描述、用户故事、技术要求等）\n2. 描述清晰，无歧义\n3. 包含验收标准\n4. 标注优先级和依赖关系\n5. 使用Markdown格式', 
'[{"name":"docType","description":"文档类型","defaultValue":"功能需求"},{"name":"overview","description":"需求概述","defaultValue":"开发一个用户管理功能"},{"name":"features","description":"功能描述","defaultValue":"用户注册、登录、信息管理"},{"name":"scenarios","description":"用户场景","defaultValue":"新用户注册账号"},{"name":"requirements","description":"技术要求","defaultValue":"支持手机号注册、密码加密存储"}]', 
NOW(), NOW());

