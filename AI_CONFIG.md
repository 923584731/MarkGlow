# AI服务配置指南

本项目支持两种AI服务：文心一言（百度）和通义千问（阿里云）。您可以根据需要选择使用其中一个或两个。

## 配置方式

### 方式一：环境变量（推荐）

在启动后端服务前，设置环境变量：

**Windows (PowerShell):**
```powershell
$env:ERNIE_API_KEY="bce-v3/ALTAK-xxx/xxx"
$env:ERNIE_APP_ID="app-xxxxx"
$env:ERNIE_MODEL="ernie-4.5-turbo-128k"
$env:QWEN_API_KEY="bce-v3/ALTAK-xxx/xxx"
$env:QWEN_APP_ID="app-xxxxx"
$env:QWEN_MODEL="qwen-3-235b-a22b"
```

**Linux/Mac:**
```bash
export ERNIE_API_KEY="bce-v3/ALTAK-xxx/xxx"
export ERNIE_APP_ID="app-xxxxx"
export ERNIE_MODEL="ernie-4.5-turbo-128k"
export QWEN_API_KEY="bce-v3/ALTAK-xxx/xxx"
export QWEN_APP_ID="app-xxxxx"
export QWEN_MODEL="qwen-3-235b-a22b"
```

### 方式二：配置文件

直接修改 `backend/src/main/resources/application.yml` 文件，填入您的API密钥。

## 文心一言配置（ERNIE 4.5 Turbo）

### 1. 获取API Key和App ID

1. 访问 [百度智能云千帆控制台](https://console.bce.baidu.com/qianfan/ais/console/applicationConsole/application)
2. 登录您的百度账号
3. 创建应用，获取 API Key 和 App ID
4. 确保开通了千帆大模型服务
5. 选择 ERNIE 4.5 Turbo 模型

### 2. 配置参数

在 `application.yml` 中配置：

```yaml
ai:
  provider: ernie
  ernie:
    api-key: your-ernie-api-key  # Bearer Token格式，如：bce-v3/ALTAK-xxx/xxx
    app-id: your-app-id          # 应用ID，如：app-0BApSgld
    model: ernie-4.5-turbo-128k  # 模型名称
    api-url: https://qianfan.baidubce.com/v2/chat/completions
```

### 3. 注意事项

- API Key 是 Bearer Token 格式，直接使用，不需要获取 access_token
- App ID 是应用的唯一标识
- 确保账户有足够的额度
- 模型名称：`ernie-4.5-turbo-128k`


### 3. 注意事项

- API Key 是 Bearer Token 格式，直接使用，不需要获取 access_token
- App ID 是应用的唯一标识
- 确保账户有足够的额度
- 模型名称：`qwen-3-235b-a22b`
- 通义千问也是通过百度千帆平台调用

## 切换AI服务


## API调用说明

### 文心一言 API（ERNIE 4.5 Turbo）

- 使用Bearer Token认证（直接使用API Key）
- 支持流式和非流式响应（当前实现为非流式）
- 模型：ERNIE 4.5 Turbo 128K
- API端点：`https://qianfan.baidubce.com/v2/chat/completions`
- 需要 `appid` header


## 常见问题

### 1. API调用失败

**问题**: 提示"获取访问令牌失败"或"调用AI服务失败"

**解决方法**:
- 检查API Key和Secret是否正确
- 检查网络连接是否正常
- 检查账户是否有足够的额度
- 查看后端日志获取详细错误信息

### 2. 响应速度慢

**问题**: AI功能响应速度较慢

**解决方法**:
- 检查网络连接
- 尝试切换到另一个AI服务
- 减少请求内容长度
- 检查API服务状态

### 3. 额度不足

**问题**: 提示额度不足

**解决方法**:
- 登录相应的控制台查看额度
- 充值或升级服务套餐
- 切换到另一个AI服务

## 测试配置

配置完成后，可以通过以下方式测试：

1. 启动后端服务
2. 在前端点击"🤖 AI 功能"
3. 尝试使用任意AI功能
4. 如果配置正确，应该能看到AI生成的内容

## 安全建议

1. **不要将API密钥提交到代码仓库**
   - 使用环境变量或配置文件
   - 将 `application.yml` 添加到 `.gitignore`

2. **定期更换API密钥**
   - 如果密钥泄露，立即更换
   - 定期检查API使用情况

3. **限制API调用频率**
   - 在生产环境中添加速率限制
   - 监控API使用情况

## 费用说明

- **文心一言**: 按调用次数和Token数量计费，具体价格请查看百度智能云官网
- **通义千问**: 按调用次数和Token数量计费，具体价格请查看阿里云DashScope官网

建议在使用前了解各服务的计费方式，避免产生意外费用。

