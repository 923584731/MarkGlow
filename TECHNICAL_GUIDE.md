## MarkGlow 技术文档（最终版）

本文件面向后续维护与二次开发人员，系统性介绍 MarkGlow 的架构、技术栈、运行配置、数据库设计、后端 API、AI 集成、前端结构（含 Ant Design 集成）、日志与排障、部署与发布等关键信息。

### 1. 项目概览
- 目标：Markdown 文档的编辑、美化、AI 辅助创作与管理。
- 特性：
  - 文档的创建、保存到 MySQL、搜索（标题与全文检索）。
  - 基于 AI 的多功能：美化、润色、摘要、翻译、代码解释、扩展、列表/表格生成、标题优化、问答、分析等。
  - AI 服务可切换（文心一言 ERNIE、通义千问 Qwen，经千帆兼容接口）。
  - 前端支持 Editor 实时预览、AI 侧边栏/下拉/浮动工具栏/快捷键、结果弹窗；已全面采用 Ant Design。

### 2. 代码结构
- 后端（Spring Boot）
  - `backend/src/main/java/com/markglow/controller/`
    - `DocumentController.java` 文档 CRUD 与搜索。
    - `AIController.java` AI 功能 HTTP 接口。
  - `backend/src/main/java/com/markglow/service/`
    - `DocumentService.java` 文档业务逻辑。
    - `ai/EnhancedAIService.java` 聚合各 AI 功能的服务层。
    - `ai/AIServiceFactory.java` AI 服务提供商工厂与切换。
    - `ai/impl/ErnieAIService.java` 文心一言实现。
    - `ai/impl/QwenAIService.java` 通义千问实现。
  - `backend/src/main/java/com/markglow/repository/DocumentRepository.java` 文档 JPA 仓库与搜索。
  - `backend/src/main/java/com/markglow/entity/Document.java` 文档实体（使用 Lombok `@Data`）。
  - 资源与文档：
    - `backend/src/main/resources/application.yml` 主配置（数据库、AI）。
    - `backend/src/main/resources/{schema.sql,data.sql,database-init.sql}` 数据库建表与示例数据。
- 前端（React + Ant Design）
  - `frontend/src/components/`
    - `Editor.js` 编辑器与预览、AI 集成入口。
    - `AISidebar.js` AI 侧边栏（Drawer + Tabs）。
    - `AIDropdown.js` 工具栏快捷 AI 菜单（Dropdown）。
    - `FloatingToolbar.js` 选中文本浮动工具条。
    - `AIResultModal.js` 结果弹窗（替换/追加/复制等）。
    - `DocumentList.js` 文档列表（自适应卡片网格）。
    - `DocumentView.js` 文档阅读页（Card + Markdown 渲染）。
    - 对应样式：`*.css`
  - `frontend/src/services/api.js` 前端 Axios 封装。
  - `frontend/src/App.js` 页面路由与视图切换。

### 3. 技术栈
- 后端：Java 17+ / Spring Boot / Spring Web / JPA(Hibernate) / Lombok / MySQL
- 前端：React 18 / Ant Design 5 / react-markdown / remark-gfm / react-split / Axios
- 日志：SLF4J + Logback

### 4. 环境与运行
#### 4.1 后端
- 依赖：JDK 17、Maven、MySQL 8+
- 配置：`backend/src/main/resources/application.yml`
  - Spring DataSource（支持环境变量覆盖）
    - `DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USER`、`DB_PASSWORD`
  - JPA：`ddl-auto: update`（生产建议使用受控迁移）
  - AI 配置：
    - `ai.provider: ernie|qwen`
    - `ai.ernie|qwen`: `api-key`、`app-id`、`model`、`api-url`
- 启动：
  - IDE 运行 Spring Boot 主类，或 `mvn spring-boot:run`。

#### 4.2 前端
- 依赖：Node.js 16+ / npm
- 进入 `frontend/`：
  - `npm install`
  - `npm start`（默认 `http://localhost:3000`）

### 5. 数据库设计
- 表：`documents`
  - 字段：`id (PK)`, `title`, `original_content`, `beautified_content`, `theme`, `created_at`, `updated_at`
  - 索引：`idx_documents_title`（标题模糊搜索）
- 实体：`Document`（Lombok `@Data`，`@PrePersist/@PreUpdate` 维护时间）
- 脚本：`schema.sql`（建表）、`data.sql`（示例）、`database-init.sql`（完整初始化）

### 6. 后端 API（摘要）
- 文档
  - `GET /api/documents` 列表
  - `POST /api/documents` 新增
  - `PUT /api/documents/{id}` 更新
  - `DELETE /api/documents/{id}` 删除
  - `GET /api/documents/search?keyword=&type=all|title` 搜索
- AI（示例，具体以 `AIController` 为准）
  - `POST /api/ai/beautify`
  - `POST /api/ai/improve`
  - `POST /api/ai/translate`
  - `POST /api/ai/summarize`
  - `POST /api/ai/explainCode`
  - `POST /api/ai/expand`
  - `POST /api/ai/generate{,List,Table}`
  - `POST /api/ai/complete`
  - `POST /api/ai/qa`
  - `POST /api/ai/analyze`
  - `POST /api/ai/provider/{ernie|qwen}` 切换提供商
  - `GET /api/ai/provider` 当前提供商

请求与响应统一通过 `EnhancedAIService` 聚合，并在 `AIServiceFactory` 中根据 `ai.provider` 或切换请求选用具体实现。

### 7. AI 服务实现
- 统一通过 HTTP POST 调用千帆兼容接口（`api-url`），`Authorization: Bearer <api-key>`。
- `ErnieAIService` / `QwenAIService`：
  - 严禁对真实 `apiKey` 进行截断传输；日志只打印脱敏前缀。
  - 传入 `model`、`messages`（role/content）等参数，解析返回文本。
- 日志：记录请求开始/结束、耗时、截断后的 key 前缀、响应码与摘要；错误时输出错误体，便于排查。

### 7.1 AI 流式传输实现（Streaming）

#### 7.1.1 技术架构

流式传输采用 **SSE (Server-Sent Events)** + **回调函数模式** 实现真正的实时数据传输，避免等待完整响应后再发送。

**核心技术栈：**
- **SSE (Server-Sent Events)**：Spring Boot 的 `SseEmitter` 用于服务器主动推送数据到客户端
- **Java Consumer 函数式接口**：`java.util.function.Consumer<String>` 用于回调处理
- **Apache HttpClient**：用于接收上游 AI 服务的流式响应
- **BufferedReader**：逐行读取 SSE 格式的流式数据
- **前端 EventSource API**：接收 SSE 数据流

#### 7.1.2 数据流转过程

```
前端 EventSource 
  ↓ (GET /api/ai/stream)
AIController.stream()
  ↓ (调用 routeActionStream，传递回调函数)
EnhancedAIService.routeActionStream()
  ↓ (构建 systemPrompt 和 prompt，调用 generateContentStream)
AIService.generateContentStream() (ErnieAIService/QwenAIService)
  ↓ (HTTP POST 到 AI 服务，接收流式响应)
逐行读取 SSE 数据 (data: {...})
  ↓ (解析每个 chunk，提取 delta.content)
立即调用回调函数 chunkConsumer.accept(deltaContent)
  ↓ (回调函数中)
通过 SseEmitter 实时发送给前端
  ↓
前端 EventSource 接收并实时显示
```

#### 7.1.3 核心代码实现

**1. AIService 接口定义（`AIService.java`）**

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
default String generateContentStream(String prompt, String systemPrompt,
                                    Double temperature, Integer maxTokens,
                                    String model, Consumer<String> chunkConsumer)
```

**代码含义：**
- `Consumer<String> chunkConsumer`：函数式接口，接受一个字符串参数（chunk内容），无返回值
- 当 AI 服务返回流式数据时，每解析到一个内容片段就立即调用此回调
- 返回完整内容用于兼容非流式场景

**2. ErnieAIService/QwenAIService 实现（`ErnieAIService.java`）**

关键代码片段：

```java
// 1. 构建请求体，设置 stream=true
Map<String, Object> requestBody = buildRequestBody(prompt, systemPrompt, targetModel, tempValue, maxTokenValue, true);
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
        JSONObject delta = choice.getJSONObject("delta");
        String deltaContent = delta.getString("content");
        
        // 9. 立即调用回调函数，实现真正的流式传输
        if (chunkConsumer != null && deltaContent != null && !deltaContent.isEmpty()) {
            chunkConsumer.accept(deltaContent);  // 关键：实时回调
        }
    }
}
```

**代码含义详解：**

- **`stream=true`**：请求参数，告诉 AI 服务返回流式数据（SSE 格式）
- **`BufferedReader`**：用于逐行读取 HTTP 响应流，避免一次性加载全部内容
- **`data:` 前缀**：SSE 标准格式，每行以 `data:` 开头，后跟 JSON 数据
- **`[DONE]`**：AI 服务发送的结束标记，表示流式传输完成
- **`delta.content`**：流式响应中的增量内容字段，每个 chunk 只包含新增的部分
- **`chunkConsumer.accept(deltaContent)`**：关键回调，每收到一个内容片段就立即执行，不等待完整响应

**3. EnhancedAIService 流式路由（`EnhancedAIService.java`）**

```java
public String routeActionStream(String action, String content, Double temperature, 
                                Integer maxTokens, String model, String style, 
                                String targetLang, Consumer<String> chunkConsumer) {
    // 根据 action 构建对应的 systemPrompt 和 prompt
    switch (action) {
        case "beautify":
            systemPrompt = "你是一个Markdown文档美化专家...";
            prompt = "请美化以下Markdown内容：\n\n" + content;
            break;
        case "improve":
            // ... 其他 action
    }
    
    // 调用底层 AI 服务的流式方法，传递回调函数
    return aiService.generateContentStream(prompt, systemPrompt, temperature, 
                                          maxTokens, model, chunkConsumer);
}
```

**代码含义：**
- 根据不同的 `action`（beautify、improve、summarize 等）构建对应的提示词
- 将回调函数透传给底层 AI 服务实现
- 保持与普通路由方法 `routeAction()` 的接口一致性

**4. AIController 流式接口（`AIController.java`）**

```java
@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter stream(...) {
    SseEmitter emitter = new SseEmitter(0L);  // 0L 表示无超时限制
    
    CompletableFuture.runAsync(() -> {
        try {
            // 调用流式路由，传递回调函数
            String result = enhancedAIService.routeActionStream(
                action, content, temperature, maxTokens, model, style, targetLang,
                chunk -> {
                    // 回调函数：每收到一个chunk就立即通过SSE发送给前端
                    try {
                        emitter.send(SseEmitter.event()
                            .name("chunk")      // 事件名称
                            .data(chunk)        // 数据内容
                        );
                    } catch (Exception e) {
                        logger.error("发送chunk到前端失败", e);
                        throw new RuntimeException("发送chunk失败", e);
                    }
                }
            );
            
            // 流式传输完成，发送结束事件
            emitter.send(SseEmitter.event().name("end")
                .data("{\"done\":true,\"cost\":" + cost + "}"));
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    });
    
    return emitter;
}
```

**代码含义详解：**

- **`@GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)`**：声明返回 SSE 流式数据
- **`SseEmitter`**：Spring Boot 提供的 SSE 发射器，用于向客户端推送数据
- **`SseEmitter(0L)`**：0L 表示无超时限制，适合长时间流式传输
- **`CompletableFuture.runAsync()`**：异步执行，避免阻塞主线程
- **`emitter.send(SseEmitter.event().name("chunk").data(chunk))`**：
  - `name("chunk")`：事件名称，前端通过 `eventSource.addEventListener('chunk', ...)` 监听
  - `data(chunk)`：实际数据内容
- **`emitter.complete()`**：标记流式传输完成，关闭连接
- **`emitter.completeWithError(e)`**：发生错误时关闭连接并传递错误信息

#### 7.1.4 前端实现（简要说明）

前端使用 `EventSource` API 接收 SSE 数据：

```javascript
const es = new EventSource('/api/ai/stream?action=improve&content=...');

// 监听 chunk 事件，实时接收数据
es.addEventListener('chunk', (e) => {
    setStreamText(prev => prev + e.data);  // 累积显示
});

// 监听结束事件
es.addEventListener('end', () => {
    es.close();  // 关闭连接
});
```

#### 7.1.5 关键设计要点

1. **真正的实时传输**：
   - 不再等待完整响应，每收到一个 chunk 就立即发送
   - 使用回调函数模式，解耦数据接收和数据发送

2. **资源管理**：
   - 使用 `try-finally` 确保 `BufferedReader` 和 HTTP 连接正确关闭
   - SSE 连接通过 `emitter.complete()` 或 `completeWithError()` 正确关闭

3. **错误处理**：
   - 单个 chunk 解析失败不影响后续处理（`catch` 后继续循环）
   - HTTP 错误或流式读取错误会中断并返回错误信息

4. **日志记录**：
   - 记录每个 chunk 的接收和发送情况（debug 级别）
   - 记录流式传输的总耗时和最终内容长度

#### 7.1.6 与旧实现的对比

**旧实现（伪流式）：**
```
接收所有 chunk → 累积到 buffer → 返回完整字符串 → Controller 分段发送
问题：前端需要等待所有数据接收完毕才能看到结果
```

**新实现（真流式）：**
```
接收 chunk 1 → 立即回调 → SSE 发送 → 前端实时显示
接收 chunk 2 → 立即回调 → SSE 发送 → 前端实时显示
...
接收 [DONE] → 发送结束事件 → 关闭连接
优势：前端可以实时看到打字机效果，用户体验更好
```

#### 7.1.7 SSE 换行符处理问题

**问题描述：**

在流式传输过程中，如果 AI 返回的内容包含换行符（`\n`），前端接收到的数据中换行符会丢失，导致文本显示时所有内容挤在一起。

**问题原因：**

根据 SSE（Server-Sent Events）规范，数据中的换行符会被解释为数据行的结束标记。当 `SseEmitter` 发送包含换行符的数据时，换行符会被 SSE 协议层处理，导致前端接收到的数据中换行符丢失。

**解决方案：**

1. **后端编码**（`AIController.java`）：
   - 检测 chunk 是否包含换行符或其他特殊字符（`\n`, `\r`, `"`, `\`）
   - 如果包含，将 chunk 进行 JSON 字符串编码：
     ```java
     // 转义特殊字符
     encodedChunk = chunk
         .replace("\\", "\\\\")  // 先转义反斜杠
         .replace("\"", "\\\"")  // 转义双引号
         .replace("\n", "\\n")   // 转义换行符
         .replace("\r", "\\r")   // 转义回车符
         .replace("\t", "\\t");  // 转义制表符
     // 包装在JSON字符串中
     encodedChunk = "\"" + encodedChunk + "\"";
     ```
   - 发送编码后的数据：`emitter.send(SseEmitter.event().name("chunk").data(encodedChunk))`

2. **前端解码**（`Editor.js`）：
   - 检查接收到的数据是否是 JSON 编码的字符串（以双引号开头和结尾）
   - 如果是，使用 `JSON.parse()` 解析，自动还原换行符：
     ```javascript
     if (chunkData.startsWith('"') && chunkData.endsWith('"')) {
       chunkData = JSON.parse(chunkData);  // 自动还原换行符
     }
     ```
   - 如果不是，直接使用原始数据（向后兼容）

**技术要点：**

- JSON 字符串编码可以安全地传输所有特殊字符，包括换行符、引号、反斜杠等
- `JSON.parse()` 会自动将转义的 `\n` 还原为真正的换行符
- 只对包含特殊字符的 chunk 进行编码，普通文本直接发送，减少性能开销

#### 7.1.8 常见问题排查

1. **流式传输中断**：
   - 检查网络连接稳定性
   - 查看后端日志中的 chunk 接收情况
   - 确认 AI 服务是否正常返回流式数据

2. **前端接收不到数据**：
   - 确认 `EventSource` 的事件名称与后端 `name("chunk")` 一致
   - 检查 CORS 配置，SSE 需要允许跨域
   - 查看浏览器控制台的网络请求，确认 SSE 连接是否建立

3. **换行符丢失**：
   - 检查后端是否对包含换行符的 chunk 进行了 JSON 编码
   - 检查前端是否正确解析了 JSON 编码的字符串
   - 查看浏览器控制台，确认接收到的数据格式

4. **性能问题**：
   - 如果 chunk 太小导致频繁发送，可以考虑批量累积后发送
   - 监控 `SseEmitter` 的连接数，避免过多并发连接

### 8. 前端结构与交互
- `Editor.js`：
  - 左侧 Markdown 输入、右侧 `react-markdown` 预览（`remark-gfm`）。
  - 工具栏：导入/AI 下拉/AI 美化/保存/导出、主题选择。
  - 快捷键：Ctrl/Cmd + B（美化）、I（润色）、Shift+S（摘要）、K（AI 侧边栏）。
  - 选中文本时显示 `FloatingToolbar`，可对选中内容进行润色/扩展/翻译/解释并替换选区。
  - AI 结果显示在 `AIResultModal`，按功能提供“替换/追加/替换选中/复制”等动作。
- `AISidebar.js`（Drawer + Tabs）：
  - 写作/优化/分析/代码四大页签；内置参数输入与操作按钮。
  - 顶部使用 `Radio.Group` 切换 AI 服务（文心一言/通义千问）。
- `AIDropdown.js`：
  - Antd v5 `Dropdown` 的 `menu={{items,onClick}}` 方案。
  - 常用功能：美化/润色/摘要/翻译；“所有功能...” 打开侧边栏。
- `DocumentList.js`：
  - 顶部搜索（关键字 + 类型）、刷新。
  - 自适应卡片网格（List.grid），标题 + 主题标签 + 预览多行省略 + 更新时间 + 查看/删除。
- `DocumentView.js`：
  - 只读预览，导出 Markdown。

### 9. 日志与排障
- 后端日志：SLF4J + Logback；主要在 `AIController`、`EnhancedAIService`、`AIServiceFactory`、`ErnieAIService`、`QwenAIService` 中添加了关键日志。
- 常见问题：
  - MySQL 连接异常（`Unable to open JDBC Connection for DDL execution`）：检查服务状态、库是否存在、账号权限、`application.yml` 和环境变量；必要时参考 `TROUBLESHOOTING.md` 与 `backend/QUICK_FIX.md`。
  - AI `invalid_iam_token`：确保完整 `apiKey` 传输；只在日志中脱敏打印。
  - 前端构建/样式问题：Antd v5 需要 `import 'antd/dist/reset.css'`；`Dropdown` 用 `menu={{items,onClick}}`。

### 10. 构建与部署
- 后端：
  - 构建：`mvn clean package`
  - 配置环境变量：`DB_*`、`AI_*` 等；生产禁用 `ddl-auto: update`，使用迁移工具（Flyway/Liquibase）。
  - 运行：`java -jar backend/target/*.jar`
- 前端：
  - 构建：`npm run build`（`frontend/`）
  - 部署：将 `build/` 静态资源托管到 Nginx/静态服务器；或由后端静态资源目录代理。
  - 反向代理：配置后端 API 转发（例如 `/api/` 指向 Spring Boot）。

### 11. 代码规范与约定
- Java：
  - 使用 Lombok `@Data` 简化实体。
  - Service 中尽量保持无副作用的纯逻辑与清晰日志。
  - Controller 层捕获异常并返回合适的 HTTP 状态码。
- JavaScript/React：
  - 组件职责单一，尽量无副作用。
  - 使用 Antd 组件实现一致 UI，并通过 `message/Modal` 统一反馈。
  - 避免在代码中硬编码敏感信息，使用 `.env` 或后端配置。

### 12. 快捷参考
- 快捷键：
  - `Ctrl/Cmd + B`：AI 美化
  - `Ctrl/Cmd + I`：语言润色
  - `Ctrl/Cmd + Shift + S`：生成摘要
  - `Ctrl/Cmd + K`：打开 AI 侧边栏
- 关键文件：
  - 后端配置：`backend/src/main/resources/application.yml`
  - 数据库脚本：`backend/src/main/resources/schema.sql`
  - 前端入口：`frontend/src/index.js`、`frontend/src/App.js`
  - 前端服务：`frontend/src/services/api.js`

### 13. 变更记录（本阶段）
- 集成 Ant Design，重构主要 UI。
- 修复 `AIDropdown` 在 Antd v5 的 `Dropdown` 用法导致的报错。
- AI 功能改为结果弹窗与可控替换策略，避免非美化功能直接覆盖文档。
- 增强 AI 调用日志与错误追踪。
- 从 H2 迁移到 MySQL，新增搜索能力（标题/全文）。
- 清理未使用的旧组件/样式（`AIMenu.*`）。
- **实现真正的 AI 流式传输**：
  - 采用 SSE (Server-Sent Events) + 回调函数模式
  - 分离流式和非流式逻辑，重构 `ErnieAIService` 和 `QwenAIService`
  - 每收到一个 chunk 立即通过 SSE 发送给前端，实现打字机效果
  - 解决 SSE 传输中换行符丢失问题：使用 JSON 字符串编码/解码机制
  - 详细技术文档见 7.1 节

---

如需进一步的二次开发建议（比如接入鉴权与多用户、草稿/版本管理、协同编辑、导出 PDF/HTML、自定义主题等），可在此基础上扩展模块并完善接口与 UI。祝使用顺利！

