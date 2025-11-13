package com.markglow.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PromptTemplateDTO {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String content;
    private List<VariableDefinition> variables;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class VariableDefinition {
        private String name;
        private String description;
        private String defaultValue;
    }
}

