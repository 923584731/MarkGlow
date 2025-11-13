# 快速开始指南

## 一键启动（Windows）

### 方式一：使用批处理文件

1. **启动后端**：双击 `start-backend.bat` 或在命令行运行：
   ```bash
   start-backend.bat
   ```

2. **启动前端**（新开一个命令行窗口）：
   ```bash
   start-frontend.bat
   ```

### 方式二：手动启动

#### 启动后端

```bash
cd backend
mvn spring-boot:run
```

等待看到类似以下输出：
```
Started MarkGlowApplication in X.XXX seconds
```

#### 启动前端

打开新的命令行窗口：

```bash
cd frontend
npm install
npm start
```

等待浏览器自动打开 `http://localhost:3000`

## 一键启动（Linux/Mac）

### 方式一：使用脚本

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

### 方式二：手动启动

#### 启动后端

```bash
cd backend
mvn spring-boot:run
```

#### 启动前端

打开新的终端：

```bash
cd frontend
npm install
npm start
```

## 验证安装

1. 后端应该运行在：http://localhost:8080
2. 前端应该运行在：http://localhost:3000
3. 访问 http://localhost:8080/h2-console 可以查看 H2 数据库控制台
   - JDBC URL: `jdbc:h2:mem:markglowdb`
   - 用户名: `sa`
   - 密码: (留空)

## 使用流程演示

1. **编辑文档**
   - 在编辑器中输入 Markdown 内容
   - 右侧实时预览效果

2. **选择主题**
   - 在工具栏右侧选择不同主题
   - 预览区域会立即应用新主题

3. **AI 美化**
   - 点击"AI 美化"按钮
   - 等待处理完成（模拟延迟 0.5-1 秒）
   - 内容会自动更新为美化后的版本

4. **保存文档**
   - 点击"保存文档"按钮
   - 输入文档标题
   - 文档保存到数据库

5. **查看文档列表**
   - 点击导航栏的"文档列表"
   - 查看所有已保存的文档

6. **查看文档**
   - 在文档列表中点击"查看"按钮
   - 查看完整渲染效果

7. **导出文档**
   - 在编辑器或文档查看页面点击"导出文件"
   - 下载 .md 文件到本地

## 常见问题

### 后端启动失败

- 确保 JDK 1.8 已安装：`java -version`
- 确保 Maven 已安装：`mvn -version`
- 检查端口 8080 是否被占用

### 前端启动失败

- 确保 Node.js 18.17.0 已安装：`node -v`
- 如果版本不对，可以使用 nvm 切换：`nvm use 18.17.0`
- 删除 `node_modules` 重新安装：`rm -rf node_modules && npm install`

### 前后端连接失败

- 确保后端已启动并运行在 8080 端口
- 检查浏览器控制台是否有 CORS 错误
- 检查 `frontend/src/services/api.js` 中的 API_BASE_URL 是否正确

### 数据库问题

- H2 是内存数据库，重启后端后数据会清空
- 如需持久化，修改 `backend/src/main/resources/application.yml` 使用 MySQL 或 PostgreSQL

## 下一步

- 查看 [README.md](README.md) 了解详细功能
- 自定义主题：编辑 `frontend/src/themes/themes.js`
- 集成真实 AI API：修改 `backend/src/main/java/com/markglow/service/AIBeautifyService.java`

