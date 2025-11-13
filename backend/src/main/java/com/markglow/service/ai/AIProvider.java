package com.markglow.service.ai;

/**
 * AI服务提供商枚举
 */
public enum AIProvider {
    ERNIE("ernie", "文心一言"),
    QWEN("qwen", "通义千问");

    private final String code;
    private final String name;

    AIProvider(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static AIProvider fromCode(String code) {
        for (AIProvider provider : values()) {
            if (provider.code.equalsIgnoreCase(code)) {
                return provider;
            }
        }
        return ERNIE; // 默认返回文心一言
    }
}

