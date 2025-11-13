package com.markglow.dto;

import lombok.Data;
import java.util.Map;

@Data
public class TemplateRenderRequest {
    private Long templateId;
    private Map<String, String> variables; // 变量名 -> 变量值
}

