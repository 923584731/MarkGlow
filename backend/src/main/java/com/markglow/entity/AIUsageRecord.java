package com.markglow.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ai_usage_records")
public class AIUsageRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action; // AI操作类型：beautify, improve, summarize等

    @Column(nullable = false)
    private String provider; // AI服务提供商：ernie, qwen

    @Column
    private String model; // 使用的模型

    @Column
    private Integer inputTokens; // 输入token数

    @Column
    private Integer outputTokens; // 输出token数

    @Column
    private Double cost; // 成本（元）

    @Column
    private Long duration; // 耗时（毫秒）

    @Column
    private String userId; // 用户ID（预留，当前可为空）

    @Column
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

