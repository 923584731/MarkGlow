# 配置总结

## 已完成的修改

根据您提供的API调用示例，我已经完成了以下修改：

### 1. 更新了API调用方式

#### 文心一言（ERNIE 4.5 Turbo）
- **API端点**: `https://qianfan.baidubce.com/v2/chat/completions`
- **认证方式**: Bearer Token（直接使用API Key，格式：`bce-v3/ALTAK-xxx/xxx`）
- **请求头**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {api-key}`
  - `appid: {app-id}`
- **请求体**: 
  ```json
  {
    "model": "ernie-4.5-turbo-128k",
    "messages": [...],
    "stream": false,
    "temperature": 0.7,
    "max_output_tokens": 2000
  }
  ```

#### 通义千问（通义千问3-235B-A22B）
- **API端点**: `https://qianfan.baidubce.com/v2/chat/completions`（与文心一言相同）
- **认证方式**: Bearer Token（直接使用API Key）
- **请求头**: 与文心一言相同
- **请求体**: 
  ```json
  {
    "model": "qwen-3-235b-a22b",
    "messages": [...],
    "stream": false,
    "temperature": 0.7,
    "max_output_tokens": 2000
  }
  ```

### 2. 更新了配置结构

配置文件现在需要以下参数：

```yaml
ai:
  provider: ernie  # 或 qwen
  ernie:
    api-key: bce-v3/ALTAK-xxx/xxx  # Bearer Token
    app-id: app-xxxxx               # 应用ID
    model: ernie-4.5-turbo-128k     # 模型名称
    api-url: https://qianfan.baidubce.com/v2/chat/completions
  qwen:
    api-key: bce-v3/ALTAK-xxx/xxx   # Bearer Token
    app-id: app-xxxxx               # 应用ID
    model: qwen-3-235b-a22b         # 模型名称
    api-url: https://qianfan.baidubce.com/v2/chat/completions
```

### 3. 主要变更

1. **移除了OAuth2.0认证流程**
   - 不再需要获取access_token
   - 直接使用Bearer Token格式的API Key

2. **添加了appid参数**
   - 需要在请求头中添加`appid`字段
   - 配置文件中新增`app-id`配置项

3. **统一了API端点**
   - 文心一言和通义千问都使用相同的API端点
   - 通过`model`参数区分不同的模型

4. **更新了响应解析**
   - 支持标准的`choices`格式响应
   - 兼容`result`格式（向后兼容）

## 配置步骤

### 方式一：使用环境变量

**Windows (PowerShell):**
```powershell
$env:ERNIE_API_KEY="bce-v3/ALTAK-fUJrfrzDYVLOTTwkrha87/bd38d6e59dfcbb11b38ad66729f2868c24d8ce5a"
$env:ERNIE_APP_ID="app-0BApSgld"
$env:ERNIE_MODEL="ernie-4.5-turbo-128k"
```

**Linux/Mac:**
```bash
export ERNIE_API_KEY="bce-v3/ALTAK-fUJrfrzDYVLOTTwkrha87/bd38d6e59dfcbb11b38ad66729f2868c24d8ce5a"
export ERNIE_APP_ID="app-0BApSgld"
export ERNIE_MODEL="ernie-4.5-turbo-128k"
```

### 方式二：修改配置文件

直接编辑 `backend/src/main/resources/application.yml`：

```yaml
ai:
  provider: ernie
  ernie:
    api-key: bce-v3/ALTAK-fUJrfrzDYVLOTTwkrha87/bd38d6e59dfcbb11b38ad66729f2868c24d8ce5a
    app-id: app-0BApSgld
    model: ernie-4.5-turbo-128k
    api-url: https://qianfan.baidubce.com/v2/chat/completions
```

## 测试

配置完成后，可以：

1. 启动后端服务
2. 在前端点击"🤖 AI 功能"
3. 尝试使用任意AI功能
4. 如果配置正确，应该能看到AI生成的内容

## 注意事项

1. **API Key格式**: 必须是Bearer Token格式，如 `bce-v3/ALTAK-xxx/xxx`
2. **App ID**: 必须与API Key对应的应用ID匹配
3. **模型名称**: 
   - 文心一言：`ernie-4.5-turbo-128k`
   - 通义千问：`qwen-3-235b-a22b`
4. **API端点**: 两个服务使用相同的端点，通过`model`参数区分
5. **请求格式**: 使用标准的Chat Completions格式

## 已修改的文件

- `backend/src/main/java/com/markglow/config/AIConfig.java` - 更新配置类
- `backend/src/main/java/com/markglow/service/ai/impl/ErnieAIService.java` - 更新API调用
- `backend/src/main/java/com/markglow/service/ai/impl/QwenAIService.java` - 更新API调用
- `backend/src/main/resources/application.yml` - 更新配置示例
- `AI_CONFIG.md` - 更新配置文档

