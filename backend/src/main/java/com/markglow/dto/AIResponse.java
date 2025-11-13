package com.markglow.dto;

import java.util.Map;

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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

