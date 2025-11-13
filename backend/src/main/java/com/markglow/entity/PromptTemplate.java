package com.markglow.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "prompt_templates")
public class PromptTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String category; // 分类：如 "写作助手", "内容优化", "代码相关", "分析工具"

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 模板内容，支持 {{variable}} 变量

    @Column(columnDefinition = "TEXT")
    private String variables; // JSON格式存储变量定义，如 [{"name":"topic","description":"主题","default":""}]

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

