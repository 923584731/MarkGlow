# MarkGlow - Markdown 美化平台

一个全栈的 Markdown 美化平台，支持编辑、预览、主题切换、AI 美化、文档管理和导出功能。

## 功能特点

- ✨ **实时预览** - 所见即所得的 Markdown 编辑器
- 🎨 **主题切换** - 多种精美的 CSS 主题
- 🤖 **AI 功能** - 丰富的AI功能，支持文心一言和通义千问
  - AI 美化 - 智能优化 Markdown 排版
  - AI 写作 - 根据标题生成完整文档
  - 语言润色 - 优化文本表达
  - 语法检查 - 检查语法和格式问题
  - 生成摘要 - 自动生成文档摘要
  - 翻译文档 - 多语言翻译
  - 解释代码 - 详细解释代码功能
  - 智能补全 - 智能文本补全
  - 扩展段落 - 扩展简短段落
  - 生成列表/表格 - 根据描述生成列表和表格
  - 文档问答 - 对文档内容进行问答
  - 文档分析 - 分析文档结构、主题和关键词
- 💾 **文档管理** - 保存、查看、删除文档
- 📤 **文件导出** - 导出为 .md 文件
- 📥 **文件导入** - 导入本地 .md 文件
- 🔄 **AI服务切换** - 支持在文心一言和通义千问之间切换

## 技术栈

### 后端
- Spring Boot 2.7.18
- JDK 1.8
- H2 内存数据库
- JPA

### 前端
- React 18.2.0
- Node.js 18.17.0
- react-markdown
- axios
- react-split

## 项目结构

```
MarkGlow/
├── backend/                 # Spring Boot 后端
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/markglow/
│   │   │   │   ├── controller/    # REST API 控制器
│   │   │   │   ├── service/        # 业务逻辑层
│   │   │   │   ├── repository/     # 数据访问层
│   │   │   │   ├── entity/         # 实体类
│   │   │   │   └── dto/            # 数据传输对象
│   │   │   └── resources/
│   │   │       └── application.yml # 配置文件
│   │   └── test/
│   └── pom.xml
├── frontend/               # React 前端
│   ├── public/
│   ├── src/
│   │   ├── components/     # React 组件
│   │   ├── services/       # API 服务
│   │   ├── themes/         # 主题配置
│   │   └── App.js
│   └── package.json
└── README.md
```

## 快速开始

### 前置要求

- JDK 1.8
- Maven 3.6+
- Node.js 18.17.0
- npm 或 yarn

### 启动后端

1. 进入后端目录：
```bash
cd backend
```

2. 使用 Maven 启动项目：
```bash
mvn spring-boot:run
```

或者先编译再运行：
```bash
mvn clean package
java -jar target/markglow-backend-1.0.0.jar
```

后端服务将在 `http://localhost:8080` 启动。

### 启动前端

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

前端应用将在 `http://localhost:3000` 启动。

## API 接口

### 文档管理

- `GET /api/documents` - 获取所有文档列表
- `GET /api/documents/{id}` - 获取指定文档
- `POST /api/documents` - 创建新文档
- `PUT /api/documents/{id}` - 更新文档
- `DELETE /api/documents/{id}` - 删除文档

### AI 功能

- `POST /api/beautify` - 美化 Markdown 内容
- `POST /api/ai/beautify` - AI美化Markdown
- `POST /api/ai/generate` - AI写作（根据标题生成内容）
- `POST /api/ai/improve` - 语言润色
- `POST /api/ai/check-grammar` - 语法检查
- `POST /api/ai/summarize` - 生成摘要
- `POST /api/ai/translate` - 翻译文档
- `POST /api/ai/explain-code` - 解释代码
- `POST /api/ai/complete` - 智能补全
- `POST /api/ai/expand` - 扩展段落
- `POST /api/ai/generate-list` - 生成列表
- `POST /api/ai/optimize-titles` - 优化标题
- `POST /api/ai/generate-table` - 生成表格
- `POST /api/ai/qa` - 文档问答
- `POST /api/ai/analyze` - 分析文档
- `POST /api/ai/switch-provider` - 切换AI服务提供商
- `GET /api/ai/provider` - 获取当前AI服务提供商

详细API文档请参考代码中的 Controller 类。

**注意**: 使用AI功能前，需要配置AI服务的API密钥。详见 [AI_CONFIG.md](AI_CONFIG.md)

## 使用说明

### 编辑文档

1. 在编辑器中输入或粘贴 Markdown 内容
2. 右侧实时预览渲染效果
3. 使用工具栏功能：
   - **导入文件** - 导入本地 .md 文件
   - **AI 美化** - 优化 Markdown 排版
   - **保存文档** - 保存到数据库
   - **导出文件** - 导出为 .md 文件

### 选择主题

在编辑器工具栏右侧选择不同的主题：
- default - 默认主题
- dark - 暗色主题
- github - GitHub 风格
- elegant - 优雅风格
- minimal - 极简风格
- cozy - 舒适风格

### 管理文档

1. 点击导航栏的"文档列表"查看所有已保存的文档
2. 点击文档卡片上的"查看"按钮查看完整内容
3. 点击"删除"按钮删除文档
4. 在文档查看页面可以导出文档

## 数据库

项目使用 H2 内存数据库，数据存储在内存中。重启应用后数据会清空。

如需持久化存储，可以修改 `application.yml` 中的数据库配置，使用 MySQL 或 PostgreSQL。

## 开发说明

### AI 功能配置

项目已集成文心一言和通义千问两个AI服务，支持实时切换。

1. **配置API密钥**
   - 文心一言：获取百度千帆平台的API Key和Secret Key
   - 通义千问：获取阿里云DashScope的API Key
   - 详细配置方法请参考 [AI_CONFIG.md](AI_CONFIG.md)

2. **切换AI服务**
   - 在前端点击"🤖 AI 功能"按钮，在菜单顶部切换服务
   - 或在配置文件中修改 `ai.provider` 值

3. **使用AI功能**
   - 所有AI功能都可以通过前端界面使用
   - 支持的功能包括：美化、写作、润色、翻译、代码解释等

### 自定义主题

在 `frontend/src/themes/themes.js` 中添加新主题配置。

## 注意事项

- 确保后端服务在 `http://localhost:8080` 运行
- 确保前端服务在 `http://localhost:3000` 运行
- 如果端口冲突，可以修改配置文件中的端口号

## 许可证

MIT License

