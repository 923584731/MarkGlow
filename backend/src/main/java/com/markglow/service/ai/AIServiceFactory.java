package com.markglow.service.ai;

import com.markglow.config.AIConfig;
import com.markglow.service.ai.impl.ErnieAIService;
import com.markglow.service.ai.impl.QwenAIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * AI服务工厂类
 */
@Component
public class AIServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(AIServiceFactory.class);

    @Autowired
    private AIConfig aiConfig;

    @Autowired
    private ErnieAIService ernieAIService;

    @Autowired
    private QwenAIService qwenAIService;

    private Map<String, AIService> serviceMap = new HashMap<>();
    private AIService defaultService;

    @PostConstruct
    public void init() {
        logger.info("初始化AI服务工厂");
        serviceMap.put("ernie", ernieAIService);
        serviceMap.put("qwen", qwenAIService);
        defaultService = getService(aiConfig.getProvider());
        logger.info("默认AI服务: {}", aiConfig.getProvider());
    }

    /**
     * 获取指定提供商的AI服务
     */
    public AIService getService(String provider) {
        AIService service = serviceMap.get(provider.toLowerCase());
        return service != null ? service : defaultService;
    }

    /**
     * 获取默认AI服务
     */
    public AIService getDefaultService() {
        return defaultService;
    }

    /**
     * 切换AI服务提供商
     */
    public void switchProvider(String provider) {
        logger.info("切换AI服务提供商: {} -> {}", aiConfig.getProvider(), provider);
        AIService service = serviceMap.get(provider.toLowerCase());
        if (service != null) {
            defaultService = service;
            aiConfig.setProvider(provider.toLowerCase());
            logger.info("AI服务切换成功: {}", provider);
        } else {
            logger.warn("无效的AI服务提供商: {}", provider);
        }
    }

    /**
     * 获取当前使用的服务提供商
     */
    public String getCurrentProvider() {
        return aiConfig.getProvider();
    }
}

