package com.markglow.dto;

public class BeautifyRequest {
    private String content;

    public BeautifyRequest() {
    }

    public BeautifyRequest(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

