package com.markglow.service;

import com.markglow.service.ai.EnhancedAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AIBeautifyService {

    @Autowired
    private EnhancedAIService enhancedAIService;

    /**
     * AI美化Markdown内容 - 使用真实的AI服务
     */
    public String beautifyMarkdown(String content) {
        if (content == null || content.trim().isEmpty()) {
            return content;
        }

        // 调用真实的AI服务进行美化
        return enhancedAIService.beautifyMarkdown(content);
    }
}

