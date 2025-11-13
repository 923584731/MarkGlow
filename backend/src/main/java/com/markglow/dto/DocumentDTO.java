package com.markglow.dto;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class DocumentDTO {
    private Long id;
    private String title;
    private String originalContent;
    private String beautifiedContent;
    private String theme;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public DocumentDTO(Long id, String title, String originalContent, String beautifiedContent, 
                      String theme, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.originalContent = originalContent;
        this.beautifiedContent = beautifiedContent;
        this.theme = theme;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}

