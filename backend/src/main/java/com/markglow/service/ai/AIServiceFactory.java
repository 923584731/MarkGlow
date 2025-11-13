package com.markglow.service.ai;

import com.markglow.config.AIConfig;
import com.markglow.service.ai.impl.ErnieAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * AI服务工厂类
 */
@Component
@Slf4j
public class AIServiceFactory {

    @Autowired
    private AIConfig aiConfig;

    @Autowired
    private ErnieAIService ernieAIService;

    private AIService defaultService;

    @PostConstruct
    public void init() {
        log.info("初始化AI服务工厂");
        defaultService = ernieAIService;
        log.info("使用百度千帆API（ErnieAIService）");
    }

    /**
     * 获取默认AI服务（统一使用百度千帆）
     */
    public AIService getDefaultService() {
        return defaultService;
    }

    /**
     * 获取当前使用的服务提供商（固定返回ernie，因为统一使用百度千帆）
     */
    public String getCurrentProvider() {
        return "ernie";
    }
}

