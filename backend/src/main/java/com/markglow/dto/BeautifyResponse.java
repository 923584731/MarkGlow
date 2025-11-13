package com.markglow.dto;

public class BeautifyResponse {
    private String beautifiedContent;

    public BeautifyResponse() {
    }

    public BeautifyResponse(String beautifiedContent) {
        this.beautifiedContent = beautifiedContent;
    }

    public String getBeautifiedContent() {
        return beautifiedContent;
    }

    public void setBeautifiedContent(String beautifiedContent) {
        this.beautifiedContent = beautifiedContent;
    }
}

