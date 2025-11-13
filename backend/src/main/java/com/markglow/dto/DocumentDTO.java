package com.markglow.dto;

import java.time.LocalDateTime;

public class DocumentDTO {
    private Long id;
    private String title;
    private String originalContent;
    private String beautifiedContent;
    private String theme;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DocumentDTO() {
    }

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

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalContent() {
        return originalContent;
    }

    public void setOriginalContent(String originalContent) {
        this.originalContent = originalContent;
    }

    public String getBeautifiedContent() {
        return beautifiedContent;
    }

    public void setBeautifiedContent(String beautifiedContent) {
        this.beautifiedContent = beautifiedContent;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

