# 日志使用指南

## 日志配置

后端已配置详细的日志记录，方便查看AI调用流程。

### 日志位置

1. **控制台输出**：启动后端时在控制台查看
2. **日志文件**：`backend/logs/markglow.log`

### 日志级别

- **INFO**：正常操作流程
- **DEBUG**：详细的调试信息（请求/响应内容）
- **ERROR**：错误信息

### 日志格式

```
2024-01-01 12:00:00.000 [main] INFO  com.markglow.service.ai.impl.ErnieAIService - ========== ERNIE AI 调用开始 ==========
```

## 日志内容

### AI调用流程日志

每次AI调用会记录以下信息：

1. **调用开始标记**
   ```
   ========== ERNIE AI 调用开始 ==========
   ```

2. **配置信息**
   - 使用的模型
   - API URL
   - App ID
   - 系统提示词（前100字符）

3. **请求信息**
   - 用户提示词长度
   - 请求体大小
   - 请求体内容（DEBUG级别）

4. **HTTP请求**
   - 发送HTTP请求
   - HTTP请求耗时
   - HTTP状态码

5. **响应信息**
   - 响应体大小
   - 响应体内容（DEBUG级别）

6. **结果信息**
   - AI调用成功/失败
   - 返回内容长度
   - 总耗时

7. **调用结束标记**
   ```
   ========== ERNIE AI 调用结束 ==========
   ```

### Controller层日志

每个API请求会记录：
- 收到请求（包含关键参数）
- 请求处理成功/失败
- 错误详情（如果失败）

### 服务层日志

每个AI功能会记录：
- 功能调用开始
- 输入参数信息
- 功能调用完成
- 输出结果信息

## 查看日志

### 方式一：控制台查看

启动后端服务后，在控制台可以看到实时日志：

```bash
cd backend
mvn spring-boot:run
```

### 方式二：查看日志文件

日志文件保存在 `backend/logs/markglow.log`：

```bash
# Windows
type backend\logs\markglow.log

# Linux/Mac
tail -f backend/logs/markglow.log
```

### 方式三：使用IDE

在IDE中打开日志文件，支持语法高亮和搜索。

## 日志示例

### 成功的AI调用

```
2024-01-01 12:00:00.000 [http-nio-8080-exec-1] INFO  com.markglow.controller.AIController - 收到AI美化请求，内容长度: 500
2024-01-01 12:00:00.001 [http-nio-8080-exec-1] INFO  com.markglow.service.ai.EnhancedAIService - >>> 调用AI美化功能
2024-01-01 12:00:00.002 [http-nio-8080-exec-1] INFO  com.markglow.service.ai.EnhancedAIService - 输入内容长度: 500 字符
2024-01-01 12:00:00.003 [http-nio-8080-exec-1] INFO  com.markglow.service.ai.impl.ErnieAIService - ========== ERNIE AI 调用开始 ==========
2024-01-01 12:00:00.004 [http-nio-8080-exec-1] INFO  com.markglow.service.ai.impl.ErnieAIService - 模型: ernie-4.5-turbo-128k
2024-01-01 12:00:00.005 [http-nio-8080-exec-1] INFO  com.markglow.service.ai.impl.ErnieAIService - API URL: https://qianfan.baidubce.com/v2/chat/completions
2024-01-01 12:00:00.006 [http-nio-8080-exec-1] INFO  com.markglow.service.ai.impl.ErnieAIService - App ID: app-0BApSgld
2024-01-01 12:00:00.007 [http-nio-8080-exec-1] INFO  com.markglow.service.ai.impl.ErnieAIService - 用户提示词长度: 600 字符
2024-01-01 12:00:00.008 [http-nio-8080-exec-1] INFO  com.markglow.service.ai.impl.ErnieAIService - 请求体大小: 800 字符
2024-01-01 12:00:00.009 [http-nio-8080-exec-1] INFO  com.markglow.service.ai.impl.ErnieAIService - 发送HTTP请求...
2024-01-01 12:00:01.500 [http-nio-8080-exec-1] INFO  com.markglow.service.ai.impl.ErnieAIService - HTTP请求完成，耗时: 1491 ms
2024-01-01 12:00:01.501 [http-nio-8080-exec-1] INFO  com.markglow.service.ai.impl.ErnieAIService - HTTP状态码: 200
2024-01-01 12:00:01.502 [http-nio-8080-exec-1] INFO  com.markglow.service.ai.impl.ErnieAIService - 响应体大小: 1200 字符
2024-01-01 12:00:01.503 [http-nio-8080-exec-1] INFO  com.markglow.service.ai.impl.ErnieAIService - AI调用成功，返回内容长度: 1000 字符
2024-01-01 12:00:01.504 [http-nio-8080-exec-1] INFO  com.markglow.service.ai.impl.ErnieAIService - 总耗时: 1501 ms
2024-01-01 12:00:01.505 [http-nio-8080-exec-1] INFO  com.markglow.service.ai.impl.ErnieAIService - ========== ERNIE AI 调用结束 ==========
2024-01-01 12:00:01.506 [http-nio-8080-exec-1] INFO  com.markglow.service.ai.EnhancedAIService - 美化完成，输出长度: 1000 字符
2024-01-01 12:00:01.507 [http-nio-8080-exec-1] INFO  com.markglow.controller.AIController - AI美化请求处理成功
```

### 失败的AI调用

```
2024-01-01 12:00:00.000 [http-nio-8080-exec-1] INFO  com.markglow.controller.AIController - 收到AI美化请求，内容长度: 500
2024-01-01 12:00:00.001 [http-nio-8080-exec-1] INFO  com.markglow.service.ai.EnhancedAIService - >>> 调用AI美化功能
2024-01-01 12:00:00.002 [http-nio-8080-exec-1] INFO  com.markglow.service.ai.impl.ErnieAIService - ========== ERNIE AI 调用开始 ==========
2024-01-01 12:00:00.003 [http-nio-8080-exec-1] INFO  com.markglow.service.ai.impl.ErnieAIService - 发送HTTP请求...
2024-01-01 12:00:00.500 [http-nio-8080-exec-1] ERROR com.markglow.service.ai.impl.ErnieAIService - AI调用失败: 错误: Invalid API Key
2024-01-01 12:00:00.501 [http-nio-8080-exec-1] ERROR com.markglow.service.ai.impl.ErnieAIService - 错误详情: {"error":{"message":"Invalid API Key"}}
2024-01-01 12:00:00.502 [http-nio-8080-exec-1] ERROR com.markglow.controller.AIController - AI美化请求处理失败
```

## 调试技巧

### 1. 查看完整请求/响应

设置日志级别为DEBUG：

```yaml
logging:
  level:
    com.markglow.service.ai: DEBUG
```

### 2. 过滤特定功能

使用grep过滤：

```bash
# 只查看AI美化相关日志
grep "AI美化" backend/logs/markglow.log

# 只查看错误日志
grep "ERROR" backend/logs/markglow.log

# 只查看ERNIE相关日志
grep "ERNIE" backend/logs/markglow.log
```

### 3. 实时监控

使用tail实时查看日志：

```bash
tail -f backend/logs/markglow.log | grep "AI"
```

## 常见问题排查

### 问题1：API调用失败

查看日志中的错误信息：
```
ERROR com.markglow.service.ai.impl.ErnieAIService - AI调用失败: 错误: Invalid API Key
```

**解决方法**：
- 检查API Key是否正确
- 检查App ID是否正确
- 检查网络连接

### 问题2：响应时间过长

查看日志中的耗时信息：
```
INFO com.markglow.service.ai.impl.ErnieAIService - HTTP请求完成，耗时: 5000 ms
```

**解决方法**：
- 检查网络连接
- 尝试切换到另一个AI服务
- 减少请求内容长度

### 问题3：无法解析响应

查看日志中的响应内容：
```
DEBUG com.markglow.service.ai.impl.ErnieAIService - 响应体内容: {...}
ERROR com.markglow.service.ai.impl.ErnieAIService - 无法解析响应: {...}
```

**解决方法**：
- 检查API响应格式是否变化
- 查看完整的响应内容
- 联系API服务提供商

## 日志文件管理

- **文件位置**：`backend/logs/markglow.log`
- **文件大小限制**：10MB
- **保留历史**：30个文件
- **自动轮转**：超过大小限制时自动创建新文件

## 性能监控

通过日志可以监控：
- AI调用频率
- 平均响应时间
- 错误率
- 不同功能的使用情况

建议定期查看日志，了解系统运行状况。

