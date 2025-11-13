package com.markglow.service.ai;

import java.util.function.Consumer;

/**
 * AI服务接口
 */
public interface AIService {
    /**
     * 调用AI服务生成内容
     * @param prompt 提示词
     * @param systemPrompt 系统提示词（可选）
     * @return AI生成的内容
     */
    String generateContent(String prompt, String systemPrompt);

    /**
     * 调用AI服务生成内容（带参数版本）
     * @param prompt 提示词
     * @param systemPrompt 系统提示词
     * @param temperature 温度
     * @param maxTokens 最大输出Token
     * @param stream 是否启用流式输出
     * @param model 指定模型
     * @return AI生成的内容
     */
    default String generateContent(String prompt, String systemPrompt,
                                   Double temperature, Integer maxTokens,
                                   Boolean stream, String model) {
        return generateContent(prompt, systemPrompt);
    }
    
    /**
     * 流式生成内容，每收到一个chunk就调用回调函数
     * @param prompt 提示词
     * @param systemPrompt 系统提示词
     * @param temperature 温度
     * @param maxTokens 最大输出Token
     * @param model 指定模型
     * @param chunkConsumer chunk回调函数，每收到一个chunk就调用
     * @return 完整内容（用于兼容性）
     */
    default String generateContentStream(String prompt, String systemPrompt,
                                        Double temperature, Integer maxTokens,
                                        String model, Consumer<String> chunkConsumer) {
        // 默认实现：调用普通方法，然后模拟流式
        String result = generateContent(prompt, systemPrompt, temperature, maxTokens, true, model);
        if (chunkConsumer != null && result != null) {
            // 简单分段发送
            int chunkSize = 10;
            for (int i = 0; i < result.length(); i += chunkSize) {
                int end = Math.min(i + chunkSize, result.length());
                chunkConsumer.accept(result.substring(i, end));
            }
        }
        return result;
    }
    
    /**
     * 获取服务提供商
     * @return 服务提供商
     */
    AIProvider getProvider();
}

