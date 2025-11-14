# MarkGlow 技术文档

本技术文档面向后续维护与二次开发人员，系统性介绍 MarkGlow 的架构设计、技术栈、运行配置、数据库设计、后端 API、AI 集成、前端结构、日志与排障、部署与发布等关键信息。

## 1. 项目概览

### 1.1 项目目标
MarkGlow 是一个功能完整的全栈 Markdown 编辑与美化平台，集成了丰富的 AI 功能，支持实时预览、主题切换、文档管理、AI 辅助写作和文档分析等功能。

### 1.2 核心特性
- **文档管理**：创建、保存、搜索、查看、删除文档，支持标题和全文检索
- **AI 功能**：14 项 AI 功能，包括美化、润色、摘要、翻译、代码解释、扩展、列表/表格生成、标题优化、问答、分析等
- **AI 服务**：支持百度千帆平台（文心一言 ERNIE），可扩展支持其他 AI 服务
- **多模型支持**：支持 ERNIE-x1.1-preview、Kimi-K2-Instruct、ERNIE-4.5-Turbo-128K
- **流式输出**：支持实时流式输出，提供打字机效果
- **参数可调**：支持调整温度（Temperature）和最大 Token 数（MaxTokens）
- **模板管理**：AI 写作功能支持使用预设模板，支持变量占位符
- **使用统计**：完整的 AI 使用统计、成本分析和详细记录
- **文档分析**：实时显示字符数、词数、阅读时间、复杂度等指标
- **前端界面**：基于 Ant Design 5 的现代化 UI，支持模态框、分屏编辑、实时预览

## 2. 技术栈

### 2.1 后端技术栈
- **框架**：Spring Boot 2.7.18
- **语言**：Java 1.8
- **数据库**：MySQL 8.0（支持 H2 内存数据库用于开发测试）
- **ORM**：Spring Data JPA (Hibernate)
- **工具库**：
  - **Lombok**：减少样板代码，使用 `@Data`、`@Slf4j` 等注解
  - **Apache HttpClient**：HTTP 客户端，用于调用 AI 服务
  - **Fastjson**：JSON 处理
  - **Logback**：统一日志管理
- **架构特性**：
  - AOP 请求日志记录（`RequestLoggingAspect`）
  - 统一异常处理
  - RESTful API 设计
  - SSE（Server-Sent Events）流式传输

### 2.2 前端技术栈
- **框架**：React 18.2.0
- **UI 组件库**：Ant Design 5.28.1
- **核心依赖**：
  - `react-markdown`：Markdown 渲染
  - `remark-gfm`：GitHub Flavored Markdown 支持
  - `axios`：HTTP 请求
  - `react-split`：分屏布局
- **构建工具**：Create React App

## 3. 项目结构

### 3.1 后端结构

```
backend/src/main/java/com/markglow/
├── config/                          # 配置类
│   ├── AIConfig.java                # AI 服务配置（@ConfigurationProperties）
│   └── RequestLoggingAspect.java    # AOP 请求日志切面
├── controller/                      # REST API 控制器
│   ├── AIController.java           # AI 功能接口（14 项功能 + 流式输出）
│   ├── AIBeautifyController.java    # AI 美化接口（独立接口）
│   ├── AIStatisticsController.java  # AI 使用统计接口
│   ├── DocumentController.java     # 文档管理接口（CRUD + 搜索 + 分析）
│   └── PromptTemplateController.java # 提示词模板管理接口
├── service/                         # 业务逻辑层
│   ├── ai/                          # AI 服务
│   │   ├── AIService.java           # AI 服务接口（抽象方法）
│   │   ├── AIServiceFactory.java   # AI 服务工厂（单例模式）
│   │   ├── EnhancedAIService.java   # AI 功能增强服务（聚合各功能）
│   │   ├── AIProvider.java         # AI 服务提供商枚举
│   │   └── impl/
│   │       └── ErnieAIService.java  # 文心一言实现（支持流式/非流式）
│   ├── DocumentService.java        # 文档服务
│   ├── DocumentAnalysisService.java # 文档分析服务（字符数、词数、阅读时间、复杂度）
│   ├── PromptTemplateService.java  # 模板服务（CRUD + 渲染）
│   ├── AIStatisticsService.java    # 统计服务（使用记录、成本计算）
│   └── AIBeautifyService.java      # 美化服务（独立实现）
├── repository/                      # 数据访问层（JPA Repository）
│   ├── DocumentRepository.java     # 文档仓库（支持标题搜索）
│   ├── PromptTemplateRepository.java # 模板仓库
│   └── AIUsageRecordRepository.java # 使用记录仓库
├── entity/                          # 实体类（使用 Lombok @Data）
│   ├── Document.java                # 文档实体
│   ├── PromptTemplate.java          # 提示词模板实体
│   └── AIUsageRecord.java           # AI 使用记录实体
├── dto/                             # 数据传输对象
│   ├── AIRequest.java               # AI 请求 DTO
│   ├── AIResponse.java              # AI 响应 DTO
│   ├── DocumentDTO.java             # 文档 DTO
│   ├── PromptTemplateDTO.java       # 模板 DTO
│   ├── BeautifyRequest.java         # 美化请求 DTO
│   ├── BeautifyResponse.java        # 美化响应 DTO
│   └── TemplateRenderRequest.java   # 模板渲染请求 DTO
└── MarkGlowApplication.java         # Spring Boot 主类
```

### 3.2 前端结构

```
frontend/src/
├── components/                      # React 组件
│   ├── Editor.js                    # 主编辑器组件（分屏编辑 + 预览）
│   ├── AISidebar.js                 # AI 功能模态框（统一入口）
│   ├── AIDropdown.js                # AI 功能下拉菜单（快捷入口）
│   ├── AIResultModal.js             # AI 结果展示模态框
│   ├── AIStreamModal.js             # AI 流式输出模态框
│   ├── DocumentList.js              # 文档列表（卡片网格）
│   ├── DocumentView.js              # 文档查看页面
│   ├── DocumentAnalysisPanel.js     # 文档分析面板（侧边栏）
│   ├── StatisticsDashboard.js       # 统计仪表板
│   ├── PromptTemplateManager.js    # 模板管理器
│   ├── FloatingToolbar.js           # 浮动工具栏（选中文本时显示）
│   └── *.css                        # 对应样式文件
├── services/
│   └── api.js                       # API 服务封装（Axios）
├── themes/
│   └── themes.js                    # 主题配置（6 种主题）
├── App.js                           # 主应用组件（路由）
└── index.js                         # 入口文件
```

### 3.3 资源文件

```
backend/src/main/resources/
├── application.yml                   # 主配置文件（数据库、AI、日志）
├── application-example.yml           # 配置示例文件
├── logback-spring.xml               # Logback 日志配置
├── schema.sql                       # 数据库建表脚本
├── data.sql                         # 示例数据脚本
├── database-init.sql                 # 完整初始化脚本
└── init-templates.sql               # 模板初始化脚本
```

## 4. 环境与运行

### 4.1 后端环境要求
- **JDK**：1.8+
- **Maven**：3.6+
- **数据库**：MySQL 8.0+（或 H2 用于开发测试）

### 4.2 后端配置

#### 4.2.1 数据库配置（`application.yml`）

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/markglow?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update  # 生产环境建议使用受控迁移
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
```

**注意**：
- 生产环境建议禁用 `ddl-auto: update`，使用 Flyway 或 Liquibase 进行数据库迁移
- 支持环境变量覆盖：`DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USER`、`DB_PASSWORD`

#### 4.2.2 AI 服务配置

```yaml
ai:
  provider: ernie  # 当前使用的 AI 服务提供商
  ernie:
    api-key: your-api-key
    app-id: your-app-id
    model: ernie-4.5-turbo-128k
    api-url: https://qianfan.baidubce.com/v2/chat/completions
```

**配置说明**：
- `api-key`：百度千帆平台的 API Key
- `app-id`：应用 ID
- `model`：默认使用的模型
- `api-url`：API 端点地址

详细配置方法请参考 `AI_CONFIG.md`。

#### 4.2.3 日志配置

项目使用 Logback 进行统一日志管理，配置文件为 `logback-spring.xml`：

- **日志文件位置**：`backend/logs/markglow.log`
- **日志级别**：可在 `application.yml` 中配置
- **日志保留**：14 天
- **日志格式**：`%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n`
- **请求日志**：通过 AOP 自动记录所有 API 请求（`RequestLoggingAspect`）

### 4.3 后端启动

#### 方式一：使用 Maven 插件
```bash
cd backend
mvn spring-boot:run
```

#### 方式二：编译后运行
```bash
cd backend
mvn clean package
java -jar target/markglow-backend-1.0.0.jar
```

#### 方式三：使用启动脚本
- **Windows**：`start-backend.bat`
- **Linux/Mac**：`./start-backend.sh`

后端服务将在 `http://localhost:8080` 启动。

### 4.4 前端环境要求
- **Node.js**：18.17.0+
- **npm** 或 **yarn**

### 4.5 前端启动

```bash
cd frontend
npm install
npm start
```

前端应用将在 `http://localhost:3000` 启动，浏览器会自动打开。

## 5. 数据库设计

### 5.1 表结构

#### 5.1.1 documents 表（文档表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| title | VARCHAR | 文档标题（非空） |
| original_content | TEXT | 原始内容 |
| beautified_content | TEXT | 美化后内容 |
| theme | VARCHAR | 使用的主题 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

**索引**：
- `idx_documents_title`：标题索引（用于模糊搜索）

**实体类**：`Document.java`（使用 Lombok `@Data`，`@PrePersist/@PreUpdate` 维护时间）

#### 5.1.2 prompt_templates 表（提示词模板表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| name | VARCHAR | 模板名称（非空） |
| description | TEXT | 模板描述 |
| category | VARCHAR | 分类（如：写作助手、内容优化、代码相关、分析工具） |
| content | TEXT | 模板内容，支持 `{{variable}}` 变量占位符（非空） |
| variables | TEXT | JSON 格式存储变量定义 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

**实体类**：`PromptTemplate.java`

#### 5.1.3 ai_usage_records 表（AI 使用记录表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| action | VARCHAR | AI 操作类型（beautify、improve、summarize 等）（非空） |
| provider | VARCHAR | AI 服务提供商（ernie、qwen）（非空） |
| model | VARCHAR | 使用的模型 |
| input_tokens | INT | 输入 token 数 |
| output_tokens | INT | 输出 token 数 |
| cost | DOUBLE | 成本（元） |
| duration | BIGINT | 耗时（毫秒） |
| user_id | VARCHAR | 用户 ID（预留，当前可为空） |
| created_at | DATETIME | 创建时间 |

**实体类**：`AIUsageRecord.java`

### 5.2 数据库初始化

项目支持自动建表（`ddl-auto: update`），也可以使用 SQL 脚本手动初始化：

- `schema.sql`：建表脚本
- `data.sql`：示例数据
- `database-init.sql`：完整初始化脚本
- `init-templates.sql`：模板初始化脚本

## 6. 后端 API 接口

### 6.1 文档管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/documents` | 获取所有文档列表 |
| GET | `/api/documents/search?keyword=&type=all\|title` | 搜索文档（支持标题和全文搜索） |
| GET | `/api/documents/{id}` | 获取指定文档 |
| POST | `/api/documents` | 创建新文档 |
| PUT | `/api/documents/{id}` | 更新文档 |
| DELETE | `/api/documents/{id}` | 删除文档 |
| POST | `/api/documents/{id}/analyze` | 分析文档内容（字符数、词数、阅读时间、复杂度） |
| POST | `/api/documents/analyze` | 分析内容（不保存文档） |

### 6.2 AI 功能接口

所有 AI 功能接口统一使用 `POST` 方法，请求体为 `AIRequest`，响应为 `AIResponse`。

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/ai/beautify` | AI 美化 Markdown |
| POST | `/api/ai/generate` | AI 写作（根据标题生成内容） |
| POST | `/api/ai/improve` | 语言润色 |
| POST | `/api/ai/check-grammar` | 语法检查 |
| POST | `/api/ai/summarize` | 生成摘要 |
| POST | `/api/ai/translate` | 翻译文档 |
| POST | `/api/ai/explain-code` | 解释代码 |
| POST | `/api/ai/complete` | 智能补全 |
| POST | `/api/ai/expand` | 扩展段落 |
| POST | `/api/ai/generate-list` | 生成列表 |
| POST | `/api/ai/optimize-titles` | 优化标题 |
| POST | `/api/ai/generate-table` | 生成表格 |
| POST | `/api/ai/qa` | 文档问答 |
| POST | `/api/ai/analyze` | 分析文档 |
| GET | `/api/ai/stream` | 流式输出（SSE，详见 7.1 节） |
| POST | `/api/ai/switch-provider` | 切换 AI 服务提供商 |
| GET | `/api/ai/provider` | 获取当前 AI 服务提供商 |

**请求体格式（AIRequest）**：
```json
{
  "action": "improve",
  "content": "待处理内容",
  "title": "文档标题（AI写作时使用）",
  "context": "上下文（可选）",
  "style": "期望风格（润色时使用）",
  "targetLang": "目标语言（翻译时使用）",
  "language": "代码语言（解释代码时使用）",
  "question": "问题（问答时使用）",
  "model": "ernie-4.5-turbo-128k",
  "temperature": 0.7,
  "maxTokens": 2000,
  "useStream": false,
  "templateId": 1
}
```

**响应格式（AIResponse）**：
```json
{
  "content": "AI生成的内容",
  "provider": "ernie",
  "message": "错误信息（如有）"
}
```

### 6.3 提示词模板管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/templates` | 获取所有模板 |
| GET | `/api/templates/{id}` | 获取指定模板 |
| POST | `/api/templates` | 创建新模板 |
| PUT | `/api/templates/{id}` | 更新模板 |
| DELETE | `/api/templates/{id}` | 删除模板 |
| POST | `/api/templates/{id}/render` | 渲染模板（填充变量） |

### 6.4 使用统计接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/statistics/summary` | 获取统计摘要（总调用次数、总成本等） |
| GET | `/api/statistics/usage?startDate=&endDate=&groupBy=day\|month` | 获取使用统计（分组统计） |
| GET | `/api/statistics/records?page=&size=&startDate=&endDate=` | 获取使用记录（分页查询） |
| GET | `/api/statistics/cost?startDate=&endDate=` | 获取成本统计 |

### 6.5 API 设计原则

1. **统一响应格式**：所有接口返回统一的 JSON 格式
2. **错误处理**：使用 HTTP 状态码表示错误类型，错误信息在响应体中
3. **参数验证**：使用 `@Valid` 注解进行参数验证
4. **日志记录**：所有 API 请求通过 AOP 自动记录（`RequestLoggingAspect`）
5. **统计记录**：所有 AI 功能调用自动记录使用统计

## 7. AI 服务实现

### 7.1 架构设计

AI 功能采用分层架构：

```
AIController
  ↓
EnhancedAIService（路由层，根据 action 构建提示词）
  ↓
AIServiceFactory（工厂模式，获取当前 AI 服务实例）
  ↓
ErnieAIService（实现层，调用百度千帆 API）
```

**关键设计**：
- **统一接口**：`AIService` 接口定义统一的 AI 服务方法
- **工厂模式**：`AIServiceFactory` 管理 AI 服务实例，支持动态切换
- **增强服务**：`EnhancedAIService` 聚合所有 AI 功能，提供统一的路由方法
- **参数透传**：支持 `model`、`temperature`、`maxTokens` 等参数

### 7.2 AI 服务实现（ErnieAIService）

#### 7.2.1 非流式调用

```java
public String generateContent(String prompt, String systemPrompt,
                             Double temperature, Integer maxTokens,
                             String model) {
    // 1. 构建请求体
    Map<String, Object> requestBody = buildRequestBody(
        prompt, systemPrompt, model, temperature, maxTokens, false
    );
    
    // 2. 发送 HTTP POST 请求
    CloseableHttpResponse response = httpClient.execute(httpPost);
    
    // 3. 解析响应 JSON
    JSONObject jsonResponse = JSON.parseObject(responseBody);
    
    // 4. 提取生成的内容
    String content = jsonResponse.getJSONArray("choices")
        .getJSONObject(0)
        .getJSONObject("message")
        .getString("content");
    
    return content;
}
```

#### 7.2.2 流式调用（详见 7.3 节）

### 7.3 AI 流式传输实现（Streaming）

#### 7.3.1 技术架构

流式传输采用 **SSE (Server-Sent Events)** + **回调函数模式** 实现真正的实时数据传输。

**核心技术栈**：
- **SSE (Server-Sent Events)**：Spring Boot 的 `SseEmitter` 用于服务器主动推送数据
- **Java Consumer 函数式接口**：`java.util.function.Consumer<String>` 用于回调处理
- **Apache HttpClient**：用于接收上游 AI 服务的流式响应
- **BufferedReader**：逐行读取 SSE 格式的流式数据
- **前端 EventSource API**：接收 SSE 数据流

#### 7.3.2 数据流转过程

```
前端 EventSource 
  ↓ (GET /api/ai/stream)
AIController.stream()
  ↓ (调用 routeActionStream，传递回调函数)
EnhancedAIService.routeActionStream()
  ↓ (构建 systemPrompt 和 prompt，调用 generateContentStream)
AIService.generateContentStream() (ErnieAIService)
  ↓ (HTTP POST 到 AI 服务，接收流式响应)
逐行读取 SSE 数据 (data: {...})
  ↓ (解析每个 chunk，提取 delta.content)
立即调用回调函数 chunkConsumer.accept(deltaContent)
  ↓ (回调函数中)
通过 SseEmitter 实时发送给前端
  ↓
前端 EventSource 接收并实时显示
```

#### 7.3.3 核心代码实现

**1. AIService 接口定义**

```java
/**
 * 流式生成内容，每收到一个chunk就调用回调函数
 * @param prompt 提示词
 * @param systemPrompt 系统提示词
 * @param temperature 温度参数
 * @param maxTokens 最大输出Token
 * @param model 指定模型
 * @param chunkConsumer chunk回调函数，每收到一个chunk就调用
 * @return 完整内容（用于兼容性）
 */
String generateContentStream(String prompt, String systemPrompt,
                            Double temperature, Integer maxTokens,
                            String model, Consumer<String> chunkConsumer);
```

**2. ErnieAIService 实现**

```java
public String generateContentStream(String prompt, String systemPrompt,
                                   Double temperature, Integer maxTokens,
                                   String model, Consumer<String> chunkConsumer) {
    // 1. 构建请求体，设置 stream=true
    Map<String, Object> requestBody = buildRequestBody(
        prompt, systemPrompt, model, temperature, maxTokens, true
    );
    requestBody.put("stream", true);  // 关键：启用流式模式
    
    // 2. 发送 HTTP 请求
    CloseableHttpResponse response = httpClient.execute(httpPost);
    
    // 3. 使用 BufferedReader 逐行读取流式响应
    BufferedReader reader = new BufferedReader(
        new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8)
    );
    
    // 4. 循环读取每一行
    String line;
    while ((line = reader.readLine()) != null) {
        // 5. 处理 SSE 格式：data: {...}
        if (line.startsWith("data:")) {
            String payload = line.substring(5).trim();  // 移除 "data:" 前缀
            
            // 6. 检查结束标记
            if ("[DONE]".equalsIgnoreCase(payload)) {
                break;  // 流结束
            }
            
            // 7. 解析 JSON chunk
            JSONObject chunk = JSON.parseObject(payload);
            
            // 8. 提取 delta.content（流式增量内容）
            JSONObject choice = chunk.getJSONArray("choices").getJSONObject(0);
            JSONObject delta = choice.getJSONObject("delta");
            String deltaContent = delta.getString("content");
            
            // 9. 立即调用回调函数，实现真正的流式传输
            if (chunkConsumer != null && deltaContent != null && !deltaContent.isEmpty()) {
                chunkConsumer.accept(deltaContent);  // 关键：实时回调
            }
        }
    }
    
    return fullContent.toString();  // 返回完整内容
}
```

**3. AIController 流式接口**

```java
@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter stream(@RequestParam String action,
                        @RequestParam(required = false) String content,
                        @RequestParam(required = false) Double temperature,
                        @RequestParam(required = false) Integer maxTokens,
                        @RequestParam(required = false) String model,
                        ...) {
    SseEmitter emitter = new SseEmitter(0L);  // 0L 表示无超时限制
    
    CompletableFuture.runAsync(() -> {
        try {
            // 调用流式路由，传递回调函数
            String result = enhancedAIService.routeActionStream(
                action, content, temperature, maxTokens, model, style, targetLang,
                chunk -> {
                    // 回调函数：每收到一个chunk就立即通过SSE发送给前端
                    try {
                        // 处理换行符：对包含特殊字符的chunk进行JSON编码
                        String encodedChunk = encodeChunkForSSE(chunk);
                        emitter.send(SseEmitter.event()
                            .name("chunk")      // 事件名称
                            .data(encodedChunk) // 数据内容
                        );
                    } catch (Exception e) {
                        log.error("发送chunk到前端失败", e);
                        throw new RuntimeException("发送chunk失败", e);
                    }
                }
            );
            
            // 流式传输完成，发送结束事件
            emitter.send(SseEmitter.event().name("end")
                .data("{\"done\":true}"));
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    });
    
    return emitter;
}
```

#### 7.3.4 SSE 换行符处理

**问题**：SSE 协议会将换行符解释为数据行结束标记，导致前端接收到的数据中换行符丢失。

**解决方案**：
1. **后端编码**：检测 chunk 是否包含特殊字符（`\n`、`\r`、`"`、`\`），如果包含，进行 JSON 字符串编码
2. **前端解码**：检查接收到的数据是否是 JSON 编码的字符串，如果是，使用 `JSON.parse()` 解析

**实现代码**：
```java
// 后端编码
private String encodeChunkForSSE(String chunk) {
    if (chunk.contains("\n") || chunk.contains("\r") || 
        chunk.contains("\"") || chunk.contains("\\")) {
        // JSON 字符串编码
        return "\"" + chunk
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t") + "\"";
    }
    return chunk;
}
```

```javascript
// 前端解码
if (chunkData.startsWith('"') && chunkData.endsWith('"')) {
  chunkData = JSON.parse(chunkData);  // 自动还原换行符
}
```

#### 7.3.5 关键设计要点

1. **真正的实时传输**：不再等待完整响应，每收到一个 chunk 就立即发送
2. **资源管理**：使用 `try-finally` 确保 `BufferedReader` 和 HTTP 连接正确关闭
3. **错误处理**：单个 chunk 解析失败不影响后续处理
4. **日志记录**：记录每个 chunk 的接收和发送情况（debug 级别）

### 7.4 AI 功能路由（EnhancedAIService）

`EnhancedAIService` 根据不同的 `action` 构建对应的提示词，然后调用底层 AI 服务：

```java
public String routeAction(String action, String content, 
                          Double temperature, Integer maxTokens, 
                          String model, String style, String targetLang) {
    String systemPrompt = "";
    String prompt = "";
    
    switch (action) {
        case "beautify":
            systemPrompt = "你是一个Markdown文档美化专家...";
            prompt = "请美化以下Markdown内容：\n\n" + content;
            break;
        case "improve":
            systemPrompt = "你是一个专业的文本润色专家...";
            prompt = "请润色以下文本，使其更加" + style + "：\n\n" + content;
            break;
        // ... 其他 action
    }
    
    return aiService.generateContent(prompt, systemPrompt, 
                                   temperature, maxTokens, model);
}
```

**支持的 action**：
- `beautify`：AI 美化
- `generate`：AI 写作
- `improve`：语言润色
- `check-grammar`：语法检查
- `summarize`：生成摘要
- `translate`：翻译文档
- `explain-code`：解释代码
- `complete`：智能补全
- `expand`：扩展段落
- `generate-list`：生成列表
- `optimize-titles`：优化标题
- `generate-table`：生成表格
- `qa`：文档问答
- `analyze`：分析文档

### 7.5 使用统计记录

所有 AI 功能调用都会自动记录使用统计：

```java
private void recordAIUsage(String action, String content, String result, 
                          String model, long duration) {
    // 估算 token 数
    int inputTokens = content != null ? (int)(content.length() / 2.0) : 0;
    int outputTokens = result != null ? (int)(result.length() / 2.0) : 0;
    
    // 计算成本
    double cost = statisticsService.calculateCost(provider, model, 
                                                 inputTokens, outputTokens);
    
    // 记录使用记录
    statisticsService.recordUsage(action, provider, model, 
                                 inputTokens, outputTokens, duration);
}
```

## 8. 前端结构与交互

### 8.1 核心组件

#### 8.1.1 Editor.js（主编辑器）

**功能**：
- 左右分屏编辑和预览（使用 `react-split`）
- 工具栏：导入、AI 功能、保存、导出、主题选择
- 快捷键支持：
  - `Ctrl/Cmd + K`：打开 AI 模态框
  - `Ctrl/Cmd + B`：快速美化
- 选中文本时显示 `FloatingToolbar`
- AI 结果显示在 `AIResultModal` 或 `AIStreamModal`

**关键实现**：
```javascript
// 分屏布局
<Split sizes={[50, 50]} minSize={300}>
  <textarea className="editor-input" ... />
  <div className="editor-preview">
    <ReactMarkdown remarkPlugins={[remarkGfm]}>
      {content}
    </ReactMarkdown>
  </div>
</Split>
```

#### 8.1.2 AISidebar.js（AI 功能模态框）

**功能**：
- 统一的 AI 功能入口（Modal 组件）
- 动态表单渲染：根据选择的 `action` 显示不同的输入字段
- 参数配置：
  - 模型选择（ERNIE-x1.1-preview、Kimi-K2-Instruct、ERNIE-4.5-Turbo-128K）
  - 温度（Temperature）：0-2，默认 0.7
  - 最大 Token（MaxTokens）：默认 2000
  - 流式输出开关
  - 模板选择（仅 AI 写作功能）

**支持的 action 配置**：
```javascript
const ACTION_CONFIG = {
  improve: {
    label: 'AI文档美化',
    fields: [
      { key: 'text', type: 'textarea', label: '待润色内容' },
      { key: 'style', type: 'input', label: '期望风格' }
    ]
  },
  generate: {
    label: 'AI写作',
    template: true,  // 支持模板
    fields: [
      { key: 'title', type: 'input', label: '文档标题', required: true }
    ]
  },
  // ... 其他 action
};
```

#### 8.1.3 AIDropdown.js（AI 功能下拉菜单）

**功能**：
- 工具栏快捷入口（Ant Design Dropdown）
- 点击功能项后打开 `AISidebar` 模态框
- 支持所有 14 项 AI 功能

#### 8.1.4 AIStreamModal.js（流式输出模态框）

**功能**：
- 使用 `EventSource` 接收 SSE 数据流
- 实时显示生成内容（打字机效果）
- 支持复制、替换、追加等操作

**关键实现**：
```javascript
const eventSource = new EventSource(`/api/ai/stream?action=${action}&...`);

eventSource.addEventListener('chunk', (e) => {
  let chunkData = e.data;
  // 解码 JSON 编码的字符串
  if (chunkData.startsWith('"') && chunkData.endsWith('"')) {
    chunkData = JSON.parse(chunkData);
  }
  setStreamText(prev => prev + chunkData);
});

eventSource.addEventListener('end', () => {
  eventSource.close();
});
```

#### 8.1.5 DocumentList.js（文档列表）

**功能**：
- 卡片网格布局（Ant Design List.grid）
- 搜索功能（标题和全文）
- 点击卡片查看文档详情（无需单独"查看"按钮）
- 支持编辑和删除

#### 8.1.6 DocumentAnalysisPanel.js（文档分析面板）

**功能**：
- 实时显示文档统计信息：
  - 字符数（中文字符数）
  - 词数（英文单词数）
  - 阅读时间（估算）
  - 复杂度指标（可读性分数）

#### 8.1.7 StatisticsDashboard.js（统计仪表板）

**功能**：
- 统计摘要（总调用次数、总成本等）
- 使用统计图表（按日期分组）
- 使用记录列表（分页）
- 成本分析

#### 8.1.8 PromptTemplateManager.js（模板管理器）

**功能**：
- 模板列表展示
- 创建、编辑、删除模板
- 模板变量定义和渲染

### 8.2 前端服务（api.js）

使用 Axios 封装所有 API 调用：

```javascript
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

// 文档管理
export const getDocuments = () => axios.get(`${API_BASE_URL}/documents`);
export const createDocument = (data) => axios.post(`${API_BASE_URL}/documents`, data);
// ... 其他 API

// AI 功能
export const aiImprove = (data) => axios.post(`${API_BASE_URL}/ai/improve`, data);
// ... 其他 AI API
```

### 8.3 主题系统

主题配置在 `themes/themes.js` 中：

```javascript
export const themes = {
  default: { /* 样式配置 */ },
  dark: { /* 样式配置 */ },
  github: { /* 样式配置 */ },
  elegant: { /* 样式配置 */ },
  minimal: { /* 样式配置 */ },
  cozy: { /* 样式配置 */ }
};
```

## 9. 日志与排障

### 9.1 日志系统

项目使用 **SLF4J + Logback** 进行统一日志管理：

- **日志文件**：`backend/logs/markglow.log`
- **日志级别**：可在 `application.yml` 中配置
- **日志保留**：14 天（按天滚动）
- **日志格式**：`%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n`

### 9.2 AOP 请求日志

`RequestLoggingAspect` 自动记录所有 API 请求：

- **记录内容**：请求路径、方法、参数、响应、执行时间
- **排除端点**：流式输出端点（避免日志过多）
- **日志级别**：INFO

### 9.3 常见问题排查

#### 9.3.1 MySQL 连接异常

**错误信息**：`Unable to open JDBC Connection for DDL execution`

**排查步骤**：
1. 检查 MySQL 服务是否启动
2. 检查数据库是否存在：`CREATE DATABASE markglow;`
3. 检查账号权限
4. 检查 `application.yml` 中的数据库配置
5. 检查环境变量（如使用）

#### 9.3.2 AI API 调用失败

**错误信息**：`invalid_iam_token`

**排查步骤**：
1. 检查 `application.yml` 中的 `ai.ernie.api-key` 和 `ai.ernie.app-id` 是否正确
2. 检查 API Key 是否完整（不要截断）
3. 检查网络连接
4. 查看后端日志了解详细错误信息

#### 9.3.3 前端构建/样式问题

**问题**：Ant Design 样式未加载

**解决方案**：
- 确保在 `index.js` 中导入：`import 'antd/dist/reset.css'`
- 检查 `package.json` 中的 `antd` 版本

**问题**：Dropdown 组件报错

**解决方案**：
- Ant Design 5 使用 `menu={{items,onClick}}` 而不是 `overlay`

#### 9.3.4 流式传输问题

**问题**：流式传输中断

**排查步骤**：
1. 检查网络连接稳定性
2. 查看后端日志中的 chunk 接收情况
3. 确认 AI 服务是否正常返回流式数据
4. 检查 SSE 连接是否建立（浏览器开发者工具 Network 标签）

**问题**：换行符丢失

**排查步骤**：
1. 检查后端是否对包含换行符的 chunk 进行了 JSON 编码
2. 检查前端是否正确解析了 JSON 编码的字符串
3. 查看浏览器控制台，确认接收到的数据格式

## 10. 构建与部署

### 10.1 后端构建

```bash
cd backend
mvn clean package
```

生成的 JAR 文件位于：`backend/target/markglow-backend-1.0.0.jar`

### 10.2 后端部署

#### 10.2.1 配置环境变量

生产环境建议使用环境变量覆盖敏感配置：

```bash
export DB_HOST=your-db-host
export DB_PORT=3306
export DB_NAME=markglow
export DB_USER=your-db-user
export DB_PASSWORD=your-db-password
```

#### 10.2.2 运行 JAR 文件

```bash
java -jar markglow-backend-1.0.0.jar
```

#### 10.2.3 生产环境建议

1. **数据库迁移**：禁用 `ddl-auto: update`，使用 Flyway 或 Liquibase
2. **日志配置**：调整日志级别为 INFO 或 WARN
3. **性能优化**：配置连接池、缓存等
4. **安全配置**：配置 HTTPS、CORS 白名单等

### 10.3 前端构建

```bash
cd frontend
npm run build
```

生成的静态文件位于：`frontend/build/`

### 10.4 前端部署

#### 10.4.1 静态资源托管

将 `build/` 目录部署到：
- Nginx
- Apache
- 其他静态资源服务器

#### 10.4.2 反向代理配置（Nginx 示例）

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 前端静态资源
    location / {
        root /path/to/frontend/build;
        try_files $uri $uri/ /index.html;
    }
    
    # 后端 API 代理
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

#### 10.4.3 后端静态资源代理

也可以将前端构建产物放在后端 `src/main/resources/static/` 目录，由 Spring Boot 直接提供静态资源。

## 11. 代码规范与约定

### 11.1 Java 代码规范

1. **使用 Lombok**：
   - 实体类使用 `@Data` 注解
   - 服务类使用 `@Slf4j` 注解
   - 减少样板代码

2. **JavaDoc 注释**：
   - 所有类和方法都有 JavaDoc 注释
   - 说明方法功能、参数、返回值

3. **异常处理**：
   - Controller 层捕获异常并返回合适的 HTTP 状态码
   - Service 层抛出业务异常

4. **日志记录**：
   - 使用 `log.info()`、`log.error()` 等
   - 记录关键操作和错误信息
   - 敏感信息（如 API Key）只打印脱敏前缀

### 11.2 JavaScript/React 代码规范

1. **组件职责单一**：
   - 每个组件只负责一个功能
   - 尽量无副作用

2. **使用 Ant Design**：
   - 统一使用 Ant Design 组件实现 UI
   - 通过 `message`、`Modal` 统一反馈

3. **API 调用**：
   - 统一使用 `api.js` 中的封装方法
   - 错误处理统一在 `api.js` 中处理

4. **状态管理**：
   - 使用 React Hooks（`useState`、`useEffect`）
   - 避免不必要的状态提升

### 11.3 命名约定

- **Java**：
  - 类名：大驼峰（`DocumentController`）
  - 方法名：小驼峰（`getDocumentById`）
  - 常量：全大写下划线（`DEFAULT_MODEL`）

- **JavaScript**：
  - 组件名：大驼峰（`DocumentList`）
  - 函数名：小驼峰（`handleSubmit`）
  - 常量：全大写下划线（`API_BASE_URL`）

## 12. 扩展开发指南

### 12.1 添加新的 AI 功能

1. **在 `EnhancedAIService` 中添加路由**：
```java
case "new-action":
    systemPrompt = "你的系统提示词";
    prompt = "你的提示词模板：" + content;
    break;
```

2. **在 `AIController` 中添加接口**：
```java
@PostMapping("/new-action")
public ResponseEntity<AIResponse> newAction(@RequestBody AIRequest request) {
    // 实现逻辑
}
```

3. **在前端 `AISidebar.js` 中添加配置**：
```javascript
const ACTION_CONFIG = {
  'new-action': {
    label: '新功能',
    fields: [/* 字段配置 */]
  }
};
```

### 12.2 添加新的 AI 服务提供商

1. **实现 `AIService` 接口**：
```java
@Component
public class NewAIService implements AIService {
    // 实现 generateContent 和 generateContentStream 方法
}
```

2. **在 `AIServiceFactory` 中注册**：
```java
public void init() {
    services.put("new-provider", newAIService);
}
```

3. **更新配置**：
```yaml
ai:
  provider: new-provider
  new-provider:
    api-key: your-api-key
    # ... 其他配置
```

### 12.3 添加新的主题

在 `frontend/src/themes/themes.js` 中添加：

```javascript
export const themes = {
  // ... 现有主题
  newTheme: {
    color: '#your-color',
    backgroundColor: '#your-bg-color',
    // ... 其他样式
  },
};
```

### 12.4 数据库迁移

生产环境建议使用 Flyway 或 Liquibase：

1. **Flyway 配置**：
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

2. **创建迁移脚本**：
```
src/main/resources/db/migration/V1__Create_documents_table.sql
```

## 13. 快捷参考

### 13.1 快捷键

- `Ctrl/Cmd + K`：打开 AI 模态框
- `Ctrl/Cmd + B`：快速美化
- `Ctrl/Cmd + I`：快速润色（如实现）
- `Ctrl/Cmd + Shift + S`：生成摘要（如实现）

### 13.2 关键文件路径

- **后端配置**：`backend/src/main/resources/application.yml`
- **数据库脚本**：`backend/src/main/resources/schema.sql`
- **日志配置**：`backend/src/main/resources/logback-spring.xml`
- **前端入口**：`frontend/src/index.js`、`frontend/src/App.js`
- **前端服务**：`frontend/src/services/api.js`
- **主题配置**：`frontend/src/themes/themes.js`

### 13.3 常用命令

```bash
# 后端
mvn spring-boot:run          # 启动后端
mvn clean package            # 构建后端

# 前端
npm start                    # 启动前端开发服务器
npm run build                # 构建前端
```

## 14. 变更记录

### 14.1 最新变更

- **AI 功能重构**：从侧边栏改为统一模态框界面
- **多模型支持**：支持 ERNIE-x1.1-preview、Kimi-K2-Instruct、ERNIE-4.5-Turbo-128K
- **参数可调**：支持调整温度、最大 Token 数
- **流式输出**：实现真正的流式传输（SSE + 回调函数模式）
- **模板管理**：添加提示词模板管理功能
- **使用统计**：完整的 AI 使用统计和成本分析
- **文档分析**：实时显示文档统计信息
- **代码优化**：使用 Lombok 减少样板代码，添加 JavaDoc 注释
- **日志系统**：统一使用 Logback，添加 AOP 请求日志

### 14.2 历史变更

- 集成 Ant Design 5，重构主要 UI
- 从 H2 迁移到 MySQL，新增搜索能力
- 实现真正的 AI 流式传输
- 解决 SSE 传输中换行符丢失问题

---

**MarkGlow 技术文档** - 最后更新：2024年

如需进一步的二次开发建议（比如接入鉴权与多用户、草稿/版本管理、协同编辑、导出 PDF/HTML、自定义主题等），可在此基础上扩展模块并完善接口与 UI。祝使用顺利！
