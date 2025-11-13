package com.markglow.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * AI服务测试类
 * 用于直接测试AI接口调用
 * 
 * 使用方法：
 * 1. 修改下面的API_KEY、APP_ID和MODEL为您的实际值
 * 2. 运行main方法
 * 3. 输入要测试的内容，按回车执行
 * 4. 输入"exit"退出
 */
public class AIServiceTest {

    // ========== 配置区域 - 请修改为您的实际值 ==========
    // 文心一言配置
    private static final String ERNIE_API_KEY = "bce-v3/ALTAK-XwxIDe6sAFbZBhYojEIES/2251130425beddf0a0b3784a4715e049bb3b5ee3";
    private static final String ERNIE_APP_ID = "app-0BApSgld";
    private static final String ERNIE_MODEL = "ernie-4.5-turbo-128k";
    private static final String ERNIE_API_URL = "https://qianfan.baidubce.com/v2/chat/completions";
    
    // 通义千问配置
    private static final String QWEN_API_KEY = "bce-v3/ALTAK-XwxIDe6sAFbZBhYojEIES/2251130425beddf0a0b3784a4715e049bb3b5ee3";
    private static final String QWEN_APP_ID = "app-0BApSgld";
    private static final String QWEN_MODEL = "qwen-3-235b-a22b";
    private static final String QWEN_API_URL = "https://qianfan.baidubce.com/v2/chat/completions";
    
    // 当前使用的服务：ernie 或 qwen
    private static final String CURRENT_SERVICE = "ernie";
    // ==================================================

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("==========================================");
        System.out.println("AI服务接口测试工具");
        System.out.println("==========================================");
        System.out.println("当前服务: " + CURRENT_SERVICE);
        System.out.println("输入要测试的内容，按回车执行");
        System.out.println("输入 'exit' 退出");
        System.out.println("输入 'switch' 切换服务（ernie/qwen）");
        System.out.println("==========================================\n");
        
        String currentService = CURRENT_SERVICE;
        
        while (true) {
            System.out.print("请输入内容（或输入命令）: ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            if ("exit".equalsIgnoreCase(input)) {
                System.out.println("退出测试");
                break;
            }
            
            if ("switch".equalsIgnoreCase(input)) {
                currentService = currentService.equals("ernie") ? "qwen" : "ernie";
                System.out.println("已切换到: " + currentService);
                continue;
            }
            
            try {
                String result = callAIService(input, currentService);
                System.out.println("\n========== AI响应 ==========");
                System.out.println(result);
                System.out.println("============================\n");
            } catch (Exception e) {
                System.err.println("\n========== 错误 ==========");
                System.err.println("错误信息: " + e.getMessage());
                e.printStackTrace();
                System.err.println("==========================\n");
            }
        }
        
        scanner.close();
    }
    
    /**
     * 调用AI服务
     */
    private static String callAIService(String prompt, String service) throws Exception {
        String apiKey, appId, model, apiUrl;
        
        if ("ernie".equals(service)) {
            apiKey = ERNIE_API_KEY;
            appId = ERNIE_APP_ID;
            model = ERNIE_MODEL;
            apiUrl = ERNIE_API_URL;
            System.out.println("\n[使用文心一言服务]");
        } else {
            apiKey = QWEN_API_KEY;
            appId = QWEN_APP_ID;
            model = QWEN_MODEL;
            apiUrl = QWEN_API_URL;
            System.out.println("\n[使用通义千问服务]");
        }
        
        System.out.println("API URL: " + apiUrl);
        System.out.println("模型: " + model);
        System.out.println("App ID: " + appId);
        System.out.println("API Key: " + apiKey.substring(0, Math.min(30, apiKey.length())) + "...");
        System.out.println("提示词: " + prompt.substring(0, Math.min(50, prompt.length())) + (prompt.length() > 50 ? "..." : ""));
        
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, String>> messages = new ArrayList<>();
        
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        messages.add(userMsg);
        
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("stream", false);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_output_tokens", 2000);
        
        String jsonBody = JSON.toJSONString(requestBody);
        System.out.println("请求体大小: " + jsonBody.length() + " 字符");
        
        // 发送HTTP请求
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(apiUrl);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + apiKey);
        if (appId != null && !appId.isEmpty()) {
            httpPost.setHeader("appid", appId);
        }
        httpPost.setEntity(new StringEntity(jsonBody, "UTF-8"));
        
        System.out.println("发送HTTP请求...");
        long startTime = System.currentTimeMillis();
        
        CloseableHttpResponse response = httpClient.execute(httpPost);
        long requestTime = System.currentTimeMillis() - startTime;
        
        System.out.println("HTTP请求完成，耗时: " + requestTime + " ms");
        System.out.println("HTTP状态码: " + response.getStatusLine().getStatusCode());
        
        HttpEntity entity = response.getEntity();
        String responseBody = EntityUtils.toString(entity, "UTF-8");
        
        System.out.println("响应体大小: " + responseBody.length() + " 字符");
        System.out.println("响应体内容: " + responseBody);
        
        // 解析响应
        JSONObject jsonResponse = JSON.parseObject(responseBody);
        
        // 检查是否有错误
        if (jsonResponse.containsKey("error")) {
            JSONObject error = jsonResponse.getJSONObject("error");
            String errorMsg = "错误: " + error.getString("message");
            String errorCode = error.getString("code");
            System.err.println("错误代码: " + errorCode);
            throw new Exception(errorMsg);
        }
        
        // 解析响应内容
        if (jsonResponse.containsKey("choices") && jsonResponse.getJSONArray("choices").size() > 0) {
            JSONObject choice = jsonResponse.getJSONArray("choices").getJSONObject(0);
            JSONObject message = choice.getJSONObject("message");
            return message.getString("content");
        } else if (jsonResponse.containsKey("result")) {
            return jsonResponse.getString("result");
        }
        
        throw new Exception("无法解析响应: " + responseBody);
    }
}

