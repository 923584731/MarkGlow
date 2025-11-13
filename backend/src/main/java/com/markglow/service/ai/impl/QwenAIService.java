package com.markglow.service.ai.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.markglow.config.AIConfig;
import com.markglow.service.ai.AIProvider;
import com.markglow.service.ai.AIService;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 通义千问AI服务实现（百度千帆平台）
 * 使用通义千问3-235B-A22B模型
 */
@Service
public class QwenAIService implements AIService {

    private static final Logger logger = LoggerFactory.getLogger(QwenAIService.class);

    @Autowired
    private AIConfig aiConfig;

    @Override
    public String generateContent(String prompt, String systemPrompt) {
        return generateContent(prompt, systemPrompt, null, null, Boolean.FALSE, null);
    }

    @Override
    public String generateContent(String prompt, String systemPrompt,
                                  Double temperature, Integer maxTokens,
                                  Boolean stream, String model) {
        boolean streamEnabled = stream != null ? stream : false;
        
        if (streamEnabled) {
            return generateContentStream(prompt, systemPrompt, temperature, maxTokens, model);
        } else {
            return generateContentNonStream(prompt, systemPrompt, temperature, maxTokens, model);
        }
    }

    /**
     * 非流式生成内容
     */
    private String generateContentNonStream(String prompt, String systemPrompt,
                                           Double temperature, Integer maxTokens,
                                           String model) {
        long startTime = System.currentTimeMillis();
        logger.info("========== 通义千问 AI 调用开始（非流式）==========");
        String defaultModel = aiConfig.getQwen().getModel();
        double tempValue = temperature != null ? temperature : 0.7;
        int maxTokenValue = maxTokens != null ? maxTokens : 2000;
        String targetModel = (model != null && !model.isEmpty()) ? model : defaultModel;

        logger.info("模型: {}", targetModel);
        logger.info("系统提示词: {}", systemPrompt != null ? systemPrompt.substring(0, Math.min(100, systemPrompt.length())) + "..." : "无");
        logger.info("用户提示词长度: {} 字符", prompt != null ? prompt.length() : 0);
        logger.info("temperature={}, maxTokens={}", tempValue, maxTokenValue);
        
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        
        try {
            String url = aiConfig.getQwen().getApiUrl();
            String apiKey = aiConfig.getQwen().getApiKey();
            String appId = aiConfig.getQwen().getAppId();
            
            logger.info("API URL: {}", url);
            logger.info("App ID: {}", appId);
            logger.info("模型名称: {}", targetModel);

            Map<String, Object> requestBody = buildRequestBody(prompt, systemPrompt, targetModel, tempValue, maxTokenValue, false);

            String jsonBody = JSON.toJSONString(requestBody);
            logger.info("请求体大小: {} 字符", jsonBody.length());
            logger.debug("请求体内容: {}", jsonBody);

            httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            if (appId != null && !appId.isEmpty()) {
                httpPost.setHeader("appid", appId);
            }
            httpPost.setEntity(new StringEntity(jsonBody, "UTF-8"));

            logger.info("发送HTTP请求...");
            long requestStartTime = System.currentTimeMillis();
            response = httpClient.execute(httpPost);
            long requestTime = System.currentTimeMillis() - requestStartTime;
            logger.info("HTTP请求完成，耗时: {} ms", requestTime);
            
            int statusCode = response.getStatusLine().getStatusCode();
            logger.info("HTTP状态码: {}", statusCode);
            
            if (statusCode != 200) {
                String errorBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                logger.error("HTTP请求失败，状态码: {}, 响应: {}", statusCode, errorBody);
                return "HTTP请求失败: " + statusCode + " - " + errorBody;
            }
            
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            logger.info("响应体大小: {} 字符", responseBody.length());
            logger.debug("响应体内容: {}", responseBody);

            JSONObject jsonResponse = JSON.parseObject(responseBody);

            if (jsonResponse.containsKey("error")) {
                JSONObject error = jsonResponse.getJSONObject("error");
                String errorMsg = "错误: " + error.getString("message");
                logger.error("AI调用失败: {}", errorMsg);
                logger.error("错误详情: {}", error.toJSONString());
                return errorMsg;
            }

            String result = null;
            if (jsonResponse.containsKey("choices") && jsonResponse.getJSONArray("choices").size() > 0) {
                JSONObject choice = jsonResponse.getJSONArray("choices").getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                if (message != null) {
                    result = message.getString("content");
                }
            } else if (jsonResponse.containsKey("result")) {
                result = jsonResponse.getString("result");
            }

            if (result != null) {
                long totalTime = System.currentTimeMillis() - startTime;
                logger.info("AI调用成功，返回内容长度: {} 字符", result.length());
                logger.info("总耗时: {} ms", totalTime);
                logger.info("========== 通义千问 AI 调用结束 ==========");
                return result;
            } else {
                logger.error("无法解析响应: {}", responseBody);
                return "未知错误: " + responseBody;
            }

        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            logger.error("AI调用异常，耗时: {} ms", totalTime, e);
            logger.error("异常信息: {}", e.getMessage());
            return "调用AI服务失败: " + e.getMessage();
        } finally {
            closeResources(httpClient, response);
        }
    }

    /**
     * 流式生成内容
     */
    private String generateContentStream(String prompt, String systemPrompt,
                                        Double temperature, Integer maxTokens,
                                        String model) {
        long startTime = System.currentTimeMillis();
        logger.info("========== 通义千问 AI 调用开始（流式）==========");
        String defaultModel = aiConfig.getQwen().getModel();
        double tempValue = temperature != null ? temperature : 0.7;
        int maxTokenValue = maxTokens != null ? maxTokens : 2000;
        String targetModel = (model != null && !model.isEmpty()) ? model : defaultModel;

        logger.info("模型: {}", targetModel);
        logger.info("系统提示词: {}", systemPrompt != null ? systemPrompt.substring(0, Math.min(100, systemPrompt.length())) + "..." : "无");
        logger.info("用户提示词长度: {} 字符", prompt != null ? prompt.length() : 0);
        logger.info("temperature={}, maxTokens={}", tempValue, maxTokenValue);
        
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        BufferedReader reader = null;
        
        try {
            String url = aiConfig.getQwen().getApiUrl();
            String apiKey = aiConfig.getQwen().getApiKey();
            String appId = aiConfig.getQwen().getAppId();
            
            logger.info("API URL: {}", url);
            logger.info("App ID: {}", appId);
            logger.info("模型名称: {}", targetModel);

            Map<String, Object> requestBody = buildRequestBody(prompt, systemPrompt, targetModel, tempValue, maxTokenValue, true);

            String jsonBody = JSON.toJSONString(requestBody);
            logger.info("请求体大小: {} 字符", jsonBody.length());
            logger.debug("请求体内容: {}", jsonBody);

            httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            if (appId != null && !appId.isEmpty()) {
                httpPost.setHeader("appid", appId);
            }
            httpPost.setEntity(new StringEntity(jsonBody, "UTF-8"));

            logger.info("发送HTTP请求（流式）...");
            long requestStartTime = System.currentTimeMillis();
            response = httpClient.execute(httpPost);
            long requestTime = System.currentTimeMillis() - requestStartTime;
            logger.info("HTTP请求完成，耗时: {} ms", requestTime);
            
            int statusCode = response.getStatusLine().getStatusCode();
            logger.info("HTTP状态码: {}", statusCode);
            
            if (statusCode != 200) {
                String errorBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                logger.error("HTTP请求失败，状态码: {}, 响应: {}", statusCode, errorBody);
                return "HTTP请求失败: " + statusCode + " - " + errorBody;
            }
            
            HttpEntity entity = response.getEntity();
            logger.info("开始读取流式响应...");
            
            StringBuilder buffer = new StringBuilder();
            int chunkCount = 0;
            reader = new BufferedReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8));
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                
                // 处理 SSE 格式: data: {...}
                if (!line.startsWith("data:")) {
                    logger.debug("忽略非 data 行: {}", line);
                    continue;
                }
                
                String payload = line.substring(5).trim(); // 移除 "data:" 前缀
                if (payload.isEmpty()) {
                    continue;
                }
                
                // 检查结束标记
                if ("[DONE]".equalsIgnoreCase(payload)) {
                    logger.info("接收到 [DONE]，结束读取");
                    break;
                }
                
                try {
                    JSONObject chunk = JSON.parseObject(payload);
                    chunkCount++;
                    
                    // 检查错误
                    if (chunk.containsKey("error")) {
                        JSONObject error = chunk.getJSONObject("error");
                        String errorMsg = "错误: " + error.getString("message");
                        logger.error("AI流式调用失败: {}", errorMsg);
                        logger.error("错误详情: {}", error.toJSONString());
                        return errorMsg;
                    }
                    
                    // 解析 choices
                    if (chunk.containsKey("choices")) {
                        for (int i = 0; i < chunk.getJSONArray("choices").size(); i++) {
                            JSONObject choice = chunk.getJSONArray("choices").getJSONObject(i);
                            if (choice == null) {
                                continue;
                            }
                            
                            // 优先使用 delta.content（流式增量）
                            JSONObject delta = choice.getJSONObject("delta");
                            if (delta != null) {
                                String deltaContent = delta.getString("content");
                                if (deltaContent != null && !deltaContent.isEmpty()) {
                                    buffer.append(deltaContent);
                                }
                            }
                            
                            // 如果有完整的 message，也提取（某些情况下会发送完整消息）
                            JSONObject messageObj = choice.getJSONObject("message");
                            if (messageObj != null) {
                                String messageContent = messageObj.getString("content");
                                if (messageContent != null && !messageContent.isEmpty()) {
                                    // 避免重复追加
                                    if (!buffer.toString().contains(messageContent)) {
                                        buffer.append(messageContent);
                                        logger.debug("收到完整消息，内容长度: {}", messageContent.length());
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    logger.warn("解析流式数据失败，chunk #{}: {}", chunkCount, payload, ex);
                    // 继续处理下一行，不中断
                }
            }
            
            String result = buffer.toString();
            long totalTime = System.currentTimeMillis() - startTime;
            logger.info("流式读取完成，共处理 {} 个 chunk，最终内容长度: {} 字符", chunkCount, result.length());
            logger.info("总耗时: {} ms", totalTime);
            logger.info("========== 通义千问 AI 调用结束 ==========");
            
            if (result.isEmpty()) {
                logger.warn("流式响应为空，可能未正确解析");
                return "流式响应为空";
            }
            
            return result;

        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            logger.error("AI流式调用异常，耗时: {} ms", totalTime, e);
            logger.error("异常信息: {}", e.getMessage(), e);
            return "调用AI服务失败: " + e.getMessage();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    logger.warn("关闭流式读取器失败", e);
                }
            }
            closeResources(httpClient, response);
        }
    }

    /**
     * 构建请求体
     */
    private Map<String, Object> buildRequestBody(String prompt, String systemPrompt,
                                                 String model, double temperature,
                                                 int maxTokens, boolean stream) {
        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, String>> messages = new ArrayList<>();

        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);
        }

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        messages.add(userMsg);

        requestBody.put("model", model != null ? model : "qwen-3-235b-a22b");
        requestBody.put("messages", messages);
        requestBody.put("stream", stream);
        requestBody.put("temperature", temperature);
        requestBody.put("max_output_tokens", maxTokens);

        return requestBody;
    }

    /**
     * 流式生成内容，每收到一个chunk就调用回调函数
     */
    @Override
    public String generateContentStream(String prompt, String systemPrompt,
                                        Double temperature, Integer maxTokens,
                                        String model, Consumer<String> chunkConsumer) {
        long startTime = System.currentTimeMillis();
        logger.info("========== 通义千问 AI 流式调用开始（实时回调）==========");
        String defaultModel = aiConfig.getQwen().getModel();
        double tempValue = temperature != null ? temperature : 0.7;
        int maxTokenValue = maxTokens != null ? maxTokens : 2000;
        String targetModel = (model != null && !model.isEmpty()) ? model : defaultModel;

        logger.info("模型: {}", targetModel);
        logger.info("系统提示词: {}", systemPrompt != null ? systemPrompt.substring(0, Math.min(100, systemPrompt.length())) + "..." : "无");
        logger.info("用户提示词长度: {} 字符", prompt != null ? prompt.length() : 0);
        logger.info("temperature={}, maxTokens={}", tempValue, maxTokenValue);
        
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        BufferedReader reader = null;
        
        try {
            String url = aiConfig.getQwen().getApiUrl();
            String apiKey = aiConfig.getQwen().getApiKey();
            String appId = aiConfig.getQwen().getAppId();
            
            logger.info("API URL: {}", url);
            logger.info("App ID: {}", appId);
            logger.info("模型名称: {}", targetModel);

            Map<String, Object> requestBody = buildRequestBody(prompt, systemPrompt, targetModel, tempValue, maxTokenValue, true);

            String jsonBody = JSON.toJSONString(requestBody);
            logger.info("请求体大小: {} 字符", jsonBody.length());
            logger.debug("请求体内容: {}", jsonBody);

            httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            if (appId != null && !appId.isEmpty()) {
                httpPost.setHeader("appid", appId);
            }
            httpPost.setEntity(new StringEntity(jsonBody, "UTF-8"));

            logger.info("发送HTTP请求（流式回调）...");
            long requestStartTime = System.currentTimeMillis();
            response = httpClient.execute(httpPost);
            long requestTime = System.currentTimeMillis() - requestStartTime;
            logger.info("HTTP请求完成，耗时: {} ms", requestTime);
            
            int statusCode = response.getStatusLine().getStatusCode();
            logger.info("HTTP状态码: {}", statusCode);
            
            if (statusCode != 200) {
                String errorBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                logger.error("HTTP请求失败，状态码: {}, 响应: {}", statusCode, errorBody);
                return "HTTP请求失败: " + statusCode + " - " + errorBody;
            }
            
            HttpEntity entity = response.getEntity();
            logger.info("开始读取流式响应（实时回调）...");
            
            StringBuilder buffer = new StringBuilder();
            int chunkCount = 0;
            reader = new BufferedReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8));
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                
                // 处理 SSE 格式: data: {...}
                if (!line.startsWith("data:")) {
                    logger.debug("忽略非 data 行: {}", line);
                    continue;
                }
                
                String payload = line.substring(5).trim(); // 移除 "data:" 前缀
                if (payload.isEmpty()) {
                    continue;
                }
                
                // 检查结束标记
                if ("[DONE]".equalsIgnoreCase(payload)) {
                    logger.info("接收到 [DONE]，结束读取");
                    break;
                }
                
                try {
                    JSONObject chunk = JSON.parseObject(payload);
                    chunkCount++;
                    
                    // 检查错误
                    if (chunk.containsKey("error")) {
                        JSONObject error = chunk.getJSONObject("error");
                        String errorMsg = "错误: " + error.getString("message");
                        logger.error("AI流式调用失败: {}", errorMsg);
                        logger.error("错误详情: {}", error.toJSONString());
                        return errorMsg;
                    }
                    
                    // 解析 choices
                    if (chunk.containsKey("choices")) {
                        for (int i = 0; i < chunk.getJSONArray("choices").size(); i++) {
                            JSONObject choice = chunk.getJSONArray("choices").getJSONObject(i);
                            if (choice == null) {
                                continue;
                            }
                            
                            // 优先使用 delta.content（流式增量）
                            JSONObject delta = choice.getJSONObject("delta");
                            if (delta != null) {
                                String deltaContent = delta.getString("content");
                                if (deltaContent != null && !deltaContent.isEmpty()) {
                                    // 确保保留原始格式，包括空格和换行符
                                    // fastjson 的 getString() 会自动解析 JSON 转义字符（\n, \t 等）
                                    buffer.append(deltaContent);
                                    // 立即调用回调函数，实现真正的流式传输
                                    if (chunkConsumer != null) {
                                        try {
                                            chunkConsumer.accept(deltaContent);
                                        } catch (Exception e) {
                                            logger.warn("回调函数执行失败", e);
                                        }
                                    }
                                }
                            }
                            
                            // 如果有完整的 message，也提取（某些情况下会发送完整消息）
                            JSONObject messageObj = choice.getJSONObject("message");
                            if (messageObj != null) {
                                String messageContent = messageObj.getString("content");
                                if (messageContent != null && !messageContent.isEmpty()) {
                                    // 避免重复追加
                                    if (!buffer.toString().contains(messageContent)) {
                                        buffer.append(messageContent);
                                        // 如果之前没有通过delta发送，现在发送完整消息
                                        if (chunkConsumer != null && !buffer.toString().equals(messageContent)) {
                                            try {
                                                chunkConsumer.accept(messageContent);
                                            } catch (Exception e) {
                                                logger.warn("回调函数执行失败", e);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    logger.warn("解析流式数据失败，chunk #{}: {}", chunkCount, payload, ex);
                    // 继续处理下一行，不中断
                }
            }
            
            String result = buffer.toString();
            long totalTime = System.currentTimeMillis() - startTime;
            logger.info("流式读取完成，共处理 {} 个 chunk，最终内容长度: {} 字符", chunkCount, result.length());
            logger.info("总耗时: {} ms", totalTime);
            logger.info("========== 通义千问 AI 流式调用结束 ==========");
            
            if (result.isEmpty()) {
                logger.warn("流式响应为空，可能未正确解析");
                return "流式响应为空";
            }
            
            return result;

        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            logger.error("AI流式调用异常，耗时: {} ms", totalTime, e);
            logger.error("异常信息: {}", e.getMessage(), e);
            return "调用AI服务失败: " + e.getMessage();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    logger.warn("关闭流式读取器失败", e);
                }
            }
            closeResources(httpClient, response);
        }
    }

    /**
     * 关闭HTTP资源
     */
    private void closeResources(CloseableHttpClient httpClient, CloseableHttpResponse response) {
        if (response != null) {
            try {
                response.close();
            } catch (Exception e) {
                logger.warn("关闭HTTP响应失败", e);
            }
        }
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (Exception e) {
                logger.warn("关闭HTTP客户端失败", e);
            }
        }
    }

    @Override
    public AIProvider getProvider() {
        return AIProvider.QWEN;
    }
}

