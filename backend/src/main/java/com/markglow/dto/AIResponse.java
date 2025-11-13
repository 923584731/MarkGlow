package com.markglow.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AIResponse {
    private String result;
    private Map<String, Object> data;
    private String provider;
    private String message;

    public AIResponse() {
    }

    public AIResponse(String result) {
        this.result = result;
    }

    public AIResponse(String result, String provider) {
        this.result = result;
        this.provider = provider;
    }

    public AIResponse(String result, String provider, String message) {
        this.result = result;
        this.provider = provider;
        this.message = message;
    }


}

