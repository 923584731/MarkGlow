# MarkGlow - Markdown 美化平台

一个功能完整的全栈 Markdown 编辑与美化平台，集成了丰富的 AI 功能，支持实时预览、主题切换、文档管理、AI 辅助写作和文档分析等功能。

## ✨ 功能特点

### 核心编辑功能
- **实时预览** - 左右分屏编辑和预览，所见即所得的 Markdown 编辑体验
- **主题切换** - 6 种精美主题（default、dark、github、elegant、minimal、cozy），实时切换预览效果
- **文件导入导出** - 支持导入本地 `.md` 文件；支持导出为 Markdown、Word（.doc）2种格式
- **文档管理** - 完整的文档 CRUD 功能，支持标题和全文搜索，点击卡片即可查看详情

### 🤖 AI 功能（14 项）

所有 AI 功能统一通过模态框界面操作，支持模型选择、参数调整和流式输出：

1. **AI 美化** - 智能优化 Markdown 排版和格式
2. **AI 写作** - 根据标题和模板生成完整文档（支持模板库）
3. **语言润色** - 优化文本表达，提升可读性
4. **语法检查** - 检查 Markdown 语法和格式问题
5. **生成摘要** - 自动生成文档摘要和关键信息
6. **翻译文档** - 多语言翻译（支持英文、日文、韩文、法文、德文等）
7. **解释代码** - 详细解释代码功能、逻辑和使用方法
8. **智能补全** - 根据上下文智能补全文本
9. **扩展段落** - 将简短段落扩展为更详细的内容
10. **生成列表** - 根据主题生成结构化的 Markdown 列表
11. **优化标题** - 优化文档标题层级和文字
12. **生成表格** - 根据描述生成 Markdown 表格
13. **文档问答** - 对文档内容进行智能问答
14. **文档分析** - 分析文档结构、主题、关键词和阅读难度

### AI 功能特性
- **多模型支持** - 支持 ERNIE-x1.1-preview、Kimi-K2-Instruct、ERNIE-4.5-Turbo-128K
- **参数可调** - 支持调整温度（Temperature）和最大 Token 数（MaxTokens）
- **流式输出** - 支持实时流式输出，提升用户体验
- **模板库** - AI 写作功能支持使用预设模板
- **使用统计** - 完整的 AI 使用统计和成本分析

### 📊 其他功能
- **文档分析面板** - 实时显示字符数、词数、阅读时间、复杂度等指标
- **使用统计** - AI 功能使用统计、成本分析和详细记录
- **提示词模板管理** - 创建、编辑和管理 AI 写作模板

## 🛠️ 技术栈

### 后端
- **框架**: Spring Boot 2.7.18
- **语言**: Java 1.8
- **数据库**: MySQL 8.0（支持 H2 内存数据库用于开发测试）
- **ORM**: Spring Data JPA
- **工具库**:
  - Lombok - 减少样板代码
  - Apache HttpClient - HTTP 客户端
  - Fastjson - JSON 处理
  - Logback - 统一日志管理
- **架构特性**:
  - AOP 请求日志记录
  - 统一异常处理
  - RESTful API 设计

### 前端
- **框架**: React 18.2.0
- **UI 组件库**: Ant Design 5.28.1
- **核心依赖**:
  - `react-markdown` - Markdown 渲染
  - `remark-gfm` - GitHub Flavored Markdown 支持
  - `axios` - HTTP 请求
  - `react-split` - 分屏布局
- **构建工具**: Create React App

## 📁 项目结构

```
MarkGlow/
├── backend/                          # Spring Boot 后端
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/markglow/
│   │   │   │   ├── config/           # 配置类
│   │   │   │   │   ├── AIConfig.java              # AI 服务配置
│   │   │   │   │   └── RequestLoggingAspect.java  # AOP 请求日志
│   │   │   │   ├── controller/       # REST API 控制器
│   │   │   │   │   ├── AIController.java          # AI 功能接口
│   │   │   │   │   ├── AIBeautifyController.java  # AI 美化接口
│   │   │   │   │   ├── AIStatisticsController.java # 统计接口
│   │   │   │   │   ├── DocumentController.java    # 文档管理接口
│   │   │   │   │   └── PromptTemplateController.java # 模板管理接口
│   │   │   │   ├── service/          # 业务逻辑层
│   │   │   │   │   ├── ai/           # AI 服务
│   │   │   │   │   │   ├── AIService.java         # AI 服务接口
│   │   │   │   │   │   ├── AIServiceFactory.java  # AI 服务工厂
│   │   │   │   │   │   ├── EnhancedAIService.java # AI 功能增强服务
│   │   │   │   │   │   └── impl/
│   │   │   │   │   │       └── ErnieAIService.java # 文心一言实现
│   │   │   │   │   ├── DocumentService.java       # 文档服务
│   │   │   │   │   ├── DocumentAnalysisService.java # 文档分析服务
│   │   │   │   │   ├── PromptTemplateService.java  # 模板服务
│   │   │   │   │   ├── AIStatisticsService.java    # 统计服务
│   │   │   │   │   └── AIBeautifyService.java      # 美化服务
│   │   │   │   ├── repository/       # 数据访问层
│   │   │   │   │   ├── DocumentRepository.java
│   │   │   │   │   ├── PromptTemplateRepository.java
│   │   │   │   │   └── AIUsageRecordRepository.java
│   │   │   │   ├── entity/           # 实体类
│   │   │   │   │   ├── Document.java
│   │   │   │   │   ├── PromptTemplate.java
│   │   │   │   │   └── AIUsageRecord.java
│   │   │   │   └── dto/              # 数据传输对象
│   │   │   │       ├── AIRequest.java
│   │   │   │       ├── AIResponse.java
│   │   │   │       ├── DocumentDTO.java
│   │   │   │       └── PromptTemplateDTO.java
│   │   │   └── resources/
│   │   │       ├── application.yml   # 应用配置
│   │   │       ├── logback-spring.xml # 日志配置
│   │   │       └── *.sql             # 数据库初始化脚本
│   │   └── test/                      # 测试代码
│   └── pom.xml                        # Maven 配置
├── frontend/                          # React 前端
│   ├── public/
│   │   └── index.html
│   ├── src/
│   │   ├── components/                # React 组件
│   │   │   ├── Editor.js              # 主编辑器组件
│   │   │   ├── AISidebar.js           # AI 功能模态框
│   │   │   ├── AIDropdown.js          # AI 功能下拉菜单
│   │   │   ├── AIResultModal.js       # AI 结果展示模态框
│   │   │   ├── AIStreamModal.js       # AI 流式输出模态框
│   │   │   ├── DocumentList.js        # 文档列表
│   │   │   ├── DocumentView.js        # 文档查看
│   │   │   ├── DocumentAnalysisPanel.js # 文档分析面板
│   │   │   ├── StatisticsDashboard.js  # 统计仪表板
│   │   │   ├── PromptTemplateManager.js # 模板管理器
│   │   │   └── FloatingToolbar.js     # 浮动工具栏
│   │   ├── services/
│   │   │   └── api.js                 # API 服务封装
│   │   ├── themes/
│   │   │   └── themes.js              # 主题配置
│   │   ├── App.js                     # 主应用组件
│   │   └── index.js                   # 入口文件
│   └── package.json                    # NPM 配置
├── start-backend.bat/sh               # 后端启动脚本
├── start-frontend.bat/sh               # 前端启动脚本
├── README.md                           # 项目说明（本文件）
├── QUICKSTART.md                       # 快速开始指南
├── PROJECT_SUMMARY.md                  # 项目总结
├── AI_CONFIG.md                        # AI 配置指南
└── AI_FEATURES.md                      # AI 功能使用指南
```

## 🚀 快速开始

### 前置要求

- **JDK 1.8+**
- **Maven 3.6+**
- **Node.js 18.17.0+**
- **MySQL 8.0+**（或使用 H2 内存数据库进行开发测试）
- **npm** 或 **yarn**

### 一键启动

#### Windows

1. **启动后端**：双击 `start-backend.bat` 或在命令行运行：
   ```bash
   start-backend.bat
   ```

2. **启动前端**（新开一个命令行窗口）：
   ```bash
   start-frontend.bat
   ```

#### Linux/Mac

1. **启动后端**：
   ```bash
   chmod +x start-backend.sh
   ./start-backend.sh
   ```

2. **启动前端**（新开一个终端）：
   ```bash
   chmod +x start-frontend.sh
   ./start-frontend.sh
   ```

### 手动启动

#### 启动后端

1. 进入后端目录：
   ```bash
   cd backend
   ```

2. 配置数据库（如使用 MySQL）：
   - 修改 `src/main/resources/application.yml` 中的数据库连接信息
   - 创建数据库：`CREATE DATABASE markglow;`

3. 启动项目：
   ```bash
   mvn spring-boot:run
   ```

   或先编译再运行：
   ```bash
   mvn clean package
   java -jar target/markglow-backend-1.0.0.jar
   ```

后端服务将在 `http://localhost:8080` 启动。

#### 启动前端

1. 进入前端目录：
   ```bash
   cd frontend
   ```

2. 安装依赖：
   ```bash
   npm install
   ```

3. 启动开发服务器：
   ```bash
   npm start
   ```

前端应用将在 `http://localhost:3000` 启动，浏览器会自动打开。

## 📡 API 接口

### 文档管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/documents` | 获取所有文档列表 |
| GET | `/api/documents/search` | 搜索文档 |
| GET | `/api/documents/{id}` | 获取指定文档 |
| POST | `/api/documents` | 创建新文档 |
| PUT | `/api/documents/{id}` | 更新文档 |
| DELETE | `/api/documents/{id}` | 删除文档 |
| POST | `/api/documents/{id}/analyze` | 分析文档内容 |

### AI 功能

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
| POST | `/api/ai/stream` | 流式输出（SSE） |
| POST | `/api/ai/switch-provider` | 切换 AI 服务提供商 |
| GET | `/api/ai/provider` | 获取当前 AI 服务提供商 |

### 提示词模板管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/templates` | 获取所有模板 |
| GET | `/api/templates/{id}` | 获取指定模板 |
| POST | `/api/templates` | 创建新模板 |
| PUT | `/api/templates/{id}` | 更新模板 |
| DELETE | `/api/templates/{id}` | 删除模板 |
| POST | `/api/templates/{id}/render` | 渲染模板 |

### 使用统计

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/statistics/summary` | 获取统计摘要 |
| GET | `/api/statistics/usage` | 获取使用统计（分组） |
| GET | `/api/statistics/records` | 获取使用记录（分页） |
| GET | `/api/statistics/cost` | 获取成本统计 |

详细 API 文档请参考代码中的 Controller 类。

## 📖 使用说明

### 编辑文档

1. 在左侧编辑器中输入或粘贴 Markdown 内容
2. 右侧实时预览渲染效果
3. 使用工具栏功能：
   - **导入文件** - 导入本地 `.md` 文件
   - **保存文档** - 保存到数据库
   - **导出文件** - 导出为 `.md` 文件
   - **AI 功能** - 打开 AI 功能菜单

### 使用 AI 功能

1. **打开 AI 模态框**：
   - 点击工具栏的 "🤖 AI 功能" 按钮
   - 或使用快捷键 `Ctrl+K`（Windows/Linux）或 `Cmd+K`（Mac）

2. **选择功能**：
   - 从下拉菜单选择需要的 AI 功能
   - 模态框会自动打开并显示对应的输入字段

3. **配置参数**：
   - **模型选择**：选择 AI 模型（默认：ERNIE-4.5-Turbo-128K）
   - **温度**：控制输出的随机性（0-2，默认 0.7）
   - **最大 Token**：限制生成的最大长度（默认 2000）
   - **流式输出**：开启后实时显示生成内容
   - **模板选择**：AI 写作功能支持选择预设模板

4. **执行操作**：
   - 填写必要的输入内容（如标题、问题等）
   - 点击"执行"按钮开始处理
   - 结果会显示在结果模态框中，或直接插入编辑器

### 选择主题

在编辑器工具栏右侧选择不同的主题：
- **default** - 默认主题
- **dark** - 暗色主题
- **github** - GitHub 风格
- **elegant** - 优雅风格
- **minimal** - 极简风格
- **cozy** - 舒适风格

### 管理文档

1. 点击导航栏的"文档列表"查看所有已保存的文档
2. **点击文档卡片**即可查看完整内容（无需点击"查看"按钮）
3. 在文档卡片上可以：
   - 点击"编辑"按钮编辑文档
   - 点击"删除"按钮删除文档
4. 在文档查看页面可以导出文档

### 查看统计信息

1. 点击导航栏的"使用统计"
2. 查看 AI 功能使用摘要、详细记录和成本分析
3. 支持按日期范围筛选和分页查看

### 管理提示词模板

1. 点击导航栏的"模板管理"
2. 创建、编辑或删除 AI 写作模板
3. 模板支持变量占位符，可在使用时动态填充

## 🗄️ 数据库

### 默认配置

项目默认使用 **MySQL 8.0** 数据库，配置信息在 `backend/src/main/resources/application.yml` 中。

### 开发测试

如需使用 H2 内存数据库进行开发测试：

1. 在 `pom.xml` 中取消注释 H2 依赖
2. 修改 `application.yml` 中的数据库配置为 H2
3. 注意：H2 是内存数据库，重启后数据会清空

### 数据库表结构

- **documents** - 文档表
- **prompt_templates** - 提示词模板表
- **ai_usage_records** - AI 使用记录表

详细表结构请参考 `DATABASE_SCHEMA.md`。

## ⚙️ 配置说明

### AI 服务配置

使用 AI 功能前，需要配置 AI 服务的 API 密钥：

1. **文心一言（百度千帆）**：
   - 获取 API Key 和 App ID
   - 在 `application.yml` 中配置 `ai.ernie.api-key` 和 `ai.ernie.app-id`

2. **详细配置方法**：请参考 [AI_CONFIG.md](AI_CONFIG.md)

### 日志配置

项目使用 Logback 进行统一日志管理：
- 日志文件位置：`backend/logs/markglow.log`
- 日志级别：可在 `application.yml` 中配置
- 日志保留：14 天
- 请求日志：通过 AOP 自动记录所有 API 请求

### 自定义主题

在 `frontend/src/themes/themes.js` 中添加新主题配置：

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

## 📚 相关文档

- [QUICKSTART.md](QUICKSTART.md) - 快速开始指南
- [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) - 项目总结
- [AI_CONFIG.md](AI_CONFIG.md) - AI 配置指南
- [AI_FEATURES.md](AI_FEATURES.md) - AI 功能使用指南
- [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md) - 数据库结构说明

## ⚠️ 注意事项

- 确保后端服务在 `http://localhost:8080` 运行
- 确保前端服务在 `http://localhost:3000` 运行
- 如果端口冲突，可以修改配置文件中的端口号
- 使用 AI 功能需要配置有效的 API 密钥
- MySQL 数据库需要提前创建，表结构会自动创建（JPA DDL）

## 🔧 开发说明

### 代码规范

- **后端**：使用 Lombok 减少样板代码，所有类和方法都有 JavaDoc 注释
- **前端**：使用 ESLint 进行代码检查，遵循 React 最佳实践
- **日志**：统一使用 `@Slf4j` 注解，通过 Logback 管理

### 架构特点

- **分层架构**：Controller → Service → Repository
- **DTO 模式**：使用 DTO 进行数据传输，避免暴露实体类
- **统一异常处理**：全局异常处理机制
- **AOP 日志**：自动记录所有 API 请求和响应
- **流式输出**：使用 SSE（Server-Sent Events）实现 AI 流式输出

## 🐛 常见问题

### 后端启动失败

- 确保 JDK 1.8 已安装：`java -version`
- 确保 Maven 已安装：`mvn -version`
- 检查端口 8080 是否被占用
- 检查数据库连接配置是否正确

### 前端启动失败

- 确保 Node.js 18.17.0+ 已安装：`node -v`
- 如果版本不对，可以使用 nvm 切换
- 删除 `node_modules` 重新安装：`rm -rf node_modules && npm install`

### 前后端连接失败

- 确保后端已启动并运行在 8080 端口
- 检查浏览器控制台是否有 CORS 错误
- 检查 `frontend/src/services/api.js` 中的 API_BASE_URL 是否正确

### AI 功能无响应

- 检查 API 配置是否正确
- 检查网络连接是否正常
- 检查 API 额度是否充足
- 查看后端日志了解详细错误信息

## 📝 更新日志

详细更新日志请参考 [CHANGELOG.md](CHANGELOG.md)。

## 📄 许可证

MIT License

## 🙏 致谢

感谢所有开源项目的支持，特别是：
- Spring Boot
- React
- Ant Design
- react-markdown

---

**MarkGlow** - 让 Markdown 编辑更智能、更高效！
