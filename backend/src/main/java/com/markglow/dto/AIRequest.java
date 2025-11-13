package com.markglow.dto;

import lombok.Data;

@Data
public class AIRequest {
    private String content;
    private String prompt;
    private String title;
    private String context;
    private String style;
    private String targetLang;
    private String language;
    private String topic;
    private String question;
    private String description;
    private String provider; // AI服务提供商
    private Double temperature;
    private Integer maxTokens;
    private String model;


}

