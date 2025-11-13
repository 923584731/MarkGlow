package com.markglow.dto;

import lombok.Data;

@Data
public class BeautifyResponse {
    private String beautifiedContent;


    public BeautifyResponse(String beautifiedContent) {
        this.beautifiedContent = beautifiedContent;
    }


}

