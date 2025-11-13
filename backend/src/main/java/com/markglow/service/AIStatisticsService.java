package com.markglow.service;

import com.markglow.entity.AIUsageRecord;
import com.markglow.repository.AIUsageRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AIStatisticsService {

    @Autowired
    private AIUsageRecordRepository usageRecordRepository;

    // 模型定价（元/千tokens）- 简化版，实际应该从配置读取
    private static final Map<String, Map<String, Double>> MODEL_PRICING = new HashMap<>();
    static {
        // ERNIE模型定价
        Map<String, Double> erniePricing = new HashMap<>();
        erniePricing.put("input", 0.012); // 输入
        erniePricing.put("output", 0.012); // 输出
        MODEL_PRICING.put("ernie-4.5-turbo-128k", erniePricing);
        MODEL_PRICING.put("ernie-4.5", erniePricing);
        
        // Qwen模型定价
        Map<String, Double> qwenPricing = new HashMap<>();
        qwenPricing.put("input", 0.008);
        qwenPricing.put("output", 0.008);
        MODEL_PRICING.put("qwen-3-235b-a22b", qwenPricing);
        MODEL_PRICING.put("qwen-72b", qwenPricing);
    }

    public void recordUsage(String action, String provider, String model, 
                           Integer inputTokens, Integer outputTokens, Long duration) {
        try {
            AIUsageRecord record = new AIUsageRecord();
            record.setAction(action);
            record.setProvider(provider);
            record.setModel(model);
            record.setInputTokens(inputTokens != null ? inputTokens : 0);
            record.setOutputTokens(outputTokens != null ? outputTokens : 0);
            record.setDuration(duration != null ? duration : 0);
            
            // 计算成本
            double cost = calculateCost(model, record.getInputTokens(), record.getOutputTokens());
            record.setCost(cost);
            
            AIUsageRecord saved = usageRecordRepository.save(record);
            log.info("记录AI使用成功: id={}, action={}, provider={}, model={}, tokens={}/{}, cost={}, duration={}ms, createdAt={}", 
                    saved.getId(), action, provider, model, inputTokens, outputTokens, cost, duration, saved.getCreatedAt());
        } catch (Exception e) {
            log.error("记录AI使用失败: action={}, provider={}, error={}", action, provider, e.getMessage(), e);
        }
    }

    private double calculateCost(String model, int inputTokens, int outputTokens) {
        Map<String, Double> pricing = MODEL_PRICING.get(model);
        if (pricing == null) {
            // 默认定价
            return (inputTokens + outputTokens) / 1000.0 * 0.01;
        }
        
        double inputCost = (inputTokens / 1000.0) * pricing.getOrDefault("input", 0.01);
        double outputCost = (outputTokens / 1000.0) * pricing.getOrDefault("output", 0.01);
        return inputCost + outputCost;
    }

    public StatisticsSummary getSummary(String period) {
        LocalDateTime start;
        LocalDateTime end = LocalDateTime.now();
        
        switch (period) {
            case "today":
                start = LocalDateTime.of(end.toLocalDate(), LocalTime.MIN);
                break;
            case "week":
                start = end.minusDays(7);
                break;
            case "month":
                start = end.minusDays(30);
                break;
            default:
                start = LocalDateTime.of(end.toLocalDate(), LocalTime.MIN);
        }
        
        List<AIUsageRecord> records = usageRecordRepository.findByCreatedAtRange(start, end);
        
        StatisticsSummary summary = new StatisticsSummary();
        summary.setPeriod(period);
        summary.setStartTime(start);
        summary.setEndTime(end);
        summary.setTotalCalls((long) records.size());
        
        int totalInputTokens = records.stream().mapToInt(r -> r.getInputTokens() != null ? r.getInputTokens() : 0).sum();
        int totalOutputTokens = records.stream().mapToInt(r -> r.getOutputTokens() != null ? r.getOutputTokens() : 0).sum();
        summary.setTotalInputTokens(totalInputTokens);
        summary.setTotalOutputTokens(totalOutputTokens);
        summary.setTotalTokens(totalInputTokens + totalOutputTokens);
        
        double totalCost = records.stream().mapToDouble(r -> r.getCost() != null ? r.getCost() : 0).sum();
        summary.setTotalCost(Math.round(totalCost * 100.0) / 100.0);
        
        // 按action统计
        Map<String, Long> actionCounts = records.stream()
                .collect(Collectors.groupingBy(AIUsageRecord::getAction, Collectors.counting()));
        summary.setActionCounts(actionCounts);
        
        // 按provider统计
        Map<String, Long> providerCounts = records.stream()
                .collect(Collectors.groupingBy(AIUsageRecord::getProvider, Collectors.counting()));
        summary.setProviderCounts(providerCounts);
        
        return summary;
    }

    /**
     * 获取每次使用记录列表
     */
    public List<AIUsageRecord> getUsageRecords(LocalDateTime start, LocalDateTime end, int page, int size) {
        List<AIUsageRecord> allRecords = usageRecordRepository.findByCreatedAtRange(start, end);
        // 按创建时间倒序排列
        allRecords.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        // 简单分页
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, allRecords.size());
        if (fromIndex >= allRecords.size()) {
            return new ArrayList<>();
        }
        return allRecords.subList(fromIndex, toIndex);
    }

    /**
     * 获取总记录数
     */
    public long getUsageRecordsCount(LocalDateTime start, LocalDateTime end) {
        return usageRecordRepository.findByCreatedAtRange(start, end).size();
    }

    /**
     * 按分组获取统计（每次/每日/每月）
     */
    public List<GroupedStatistics> getGroupedStatistics(LocalDateTime start, LocalDateTime end, String groupBy) {
        log.info("获取分组统计: groupBy={}, start={}, end={}", groupBy, start, end);
        // 使用显式查询方法，确保边界条件正确
        List<AIUsageRecord> records = usageRecordRepository.findByCreatedAtRange(start, end);
        log.info("查询到记录数: {}", records.size());
        
        // 输出前几条记录的详细信息用于调试
        if (records.size() > 0) {
            log.info("前3条记录详情:");
            for (int i = 0; i < Math.min(3, records.size()); i++) {
                AIUsageRecord r = records.get(i);
                log.info("  记录[{}]: id={}, action={}, createdAt={}, inputTokens={}, outputTokens={}", 
                        i, r.getId(), r.getAction(), r.getCreatedAt(), r.getInputTokens(), r.getOutputTokens());
            }
        }
        
        Map<String, GroupedStatistics> groupedMap = new HashMap<>();
        
        for (AIUsageRecord record : records) {
            String key;
            switch (groupBy) {
                case "day":
                    key = record.getCreatedAt().toLocalDate().toString();
                    break;
                case "month":
                    key = record.getCreatedAt().toLocalDate().withDayOfMonth(1).toString();
                    break;
                case "each":
                default:
                    // 每次：使用记录ID作为key，每条记录单独显示
                    key = record.getId().toString();
                    break;
            }
            
            GroupedStatistics stats;
            if (groupBy.equals("each")) {
                // 每次统计：每条记录单独显示，不聚合
                stats = new GroupedStatistics();
                stats.setKey(key);
                stats.setDate(record.getCreatedAt().toString());
                stats.setAction(record.getAction());
                stats.setProvider(record.getProvider());
                stats.setModel(record.getModel());
                stats.setCalls(1); // 每次统计，调用次数始终为1
                stats.setInputTokens(record.getInputTokens() != null ? record.getInputTokens() : 0);
                stats.setOutputTokens(record.getOutputTokens() != null ? record.getOutputTokens() : 0);
                stats.setCost(record.getCost() != null ? record.getCost() : 0.0);
                stats.setTotalTokens(stats.getInputTokens() + stats.getOutputTokens());
                stats.setDuration(record.getDuration() != null ? record.getDuration() : 0L);
            } else {
                // 每日/每月统计：聚合数据
                stats = groupedMap.getOrDefault(key, new GroupedStatistics());
                stats.setKey(key);
                if (stats.getDate() == null) {
                    stats.setDate(key);
                }
                stats.setCalls(stats.getCalls() + 1);
                stats.setInputTokens(stats.getInputTokens() + (record.getInputTokens() != null ? record.getInputTokens() : 0));
                stats.setOutputTokens(stats.getOutputTokens() + (record.getOutputTokens() != null ? record.getOutputTokens() : 0));
                stats.setCost(stats.getCost() + (record.getCost() != null ? record.getCost() : 0));
                stats.setTotalTokens(stats.getInputTokens() + stats.getOutputTokens());
                if (record.getDuration() != null) {
                    stats.setDuration(stats.getDuration() + record.getDuration());
                }
            }
            groupedMap.put(key, stats);
        }
        
        List<GroupedStatistics> result = new ArrayList<>(groupedMap.values());
        // 按key倒序排列（最新的在前）
        result.sort((a, b) -> {
            if (groupBy.equals("each")) {
                return Long.compare(Long.parseLong(b.getKey()), Long.parseLong(a.getKey()));
            } else {
                return b.getKey().compareTo(a.getKey());
            }
        });
        
        log.info("分组统计完成: groupBy={}, 分组数={}, 总记录数={}", groupBy, result.size(), records.size());
        if (result.size() > 0) {
            log.info("前3个分组统计详情:");
            for (int i = 0; i < Math.min(3, result.size()); i++) {
                GroupedStatistics s = result.get(i);
                log.info("  分组[{}]: key={}, date={}, calls={}, tokens={}/{}, cost={}", 
                        i, s.getKey(), s.getDate(), s.getCalls(), s.getInputTokens(), s.getOutputTokens(), s.getCost());
            }
        }
        
        return result;
    }

    public List<DailyStatistics> getDailyStatistics(LocalDateTime start, LocalDateTime end) {
        List<AIUsageRecord> records = usageRecordRepository.findByCreatedAtRange(start, end);
        
        Map<String, DailyStatistics> dailyMap = new HashMap<>();
        
        for (AIUsageRecord record : records) {
            String date = record.getCreatedAt().toLocalDate().toString();
            DailyStatistics daily = dailyMap.getOrDefault(date, new DailyStatistics());
            daily.setDate(date);
            daily.setCalls(daily.getCalls() + 1);
            daily.setInputTokens(daily.getInputTokens() + (record.getInputTokens() != null ? record.getInputTokens() : 0));
            daily.setOutputTokens(daily.getOutputTokens() + (record.getOutputTokens() != null ? record.getOutputTokens() : 0));
            daily.setCost(daily.getCost() + (record.getCost() != null ? record.getCost() : 0));
            dailyMap.put(date, daily);
        }
        
        return new ArrayList<>(dailyMap.values());
    }

    public static class StatisticsSummary {
        private String period;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long totalCalls;
        private Integer totalInputTokens;
        private Integer totalOutputTokens;
        private Integer totalTokens;
        private Double totalCost;
        private Map<String, Long> actionCounts;
        private Map<String, Long> providerCounts;

        // Getters and Setters
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        
        public Long getTotalCalls() { return totalCalls; }
        public void setTotalCalls(Long totalCalls) { this.totalCalls = totalCalls; }
        
        public Integer getTotalInputTokens() { return totalInputTokens; }
        public void setTotalInputTokens(Integer totalInputTokens) { this.totalInputTokens = totalInputTokens; }
        
        public Integer getTotalOutputTokens() { return totalOutputTokens; }
        public void setTotalOutputTokens(Integer totalOutputTokens) { this.totalOutputTokens = totalOutputTokens; }
        
        public Integer getTotalTokens() { return totalTokens; }
        public void setTotalTokens(Integer totalTokens) { this.totalTokens = totalTokens; }
        
        public Double getTotalCost() { return totalCost; }
        public void setTotalCost(Double totalCost) { this.totalCost = totalCost; }
        
        public Map<String, Long> getActionCounts() { return actionCounts; }
        public void setActionCounts(Map<String, Long> actionCounts) { this.actionCounts = actionCounts; }
        
        public Map<String, Long> getProviderCounts() { return providerCounts; }
        public void setProviderCounts(Map<String, Long> providerCounts) { this.providerCounts = providerCounts; }
    }

    public static class DailyStatistics {
        private String date;
        private Integer calls;
        private Integer inputTokens;
        private Integer outputTokens;
        private Double cost;

        public DailyStatistics() {
            this.calls = 0;
            this.inputTokens = 0;
            this.outputTokens = 0;
            this.cost = 0.0;
        }

        // Getters and Setters
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public Integer getCalls() { return calls; }
        public void setCalls(Integer calls) { this.calls = calls; }
        
        public Integer getInputTokens() { return inputTokens; }
        public void setInputTokens(Integer inputTokens) { this.inputTokens = inputTokens; }
        
        public Integer getOutputTokens() { return outputTokens; }
        public void setOutputTokens(Integer outputTokens) { this.outputTokens = outputTokens; }
        
        public Double getCost() { return cost; }
        public void setCost(Double cost) { this.cost = cost; }
    }

    public static class GroupedStatistics {
        private String key;
        private String date;
        private String action;
        private String provider;
        private String model;
        private Integer calls;
        private Integer inputTokens;
        private Integer outputTokens;
        private Integer totalTokens;
        private Double cost;
        private Long duration;

        public GroupedStatistics() {
            this.calls = 0;
            this.inputTokens = 0;
            this.outputTokens = 0;
            this.totalTokens = 0;
            this.cost = 0.0;
            this.duration = 0L;
        }

        // Getters and Setters
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        
        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        
        public Integer getCalls() { return calls; }
        public void setCalls(Integer calls) { this.calls = calls; }
        
        public Integer getInputTokens() { return inputTokens; }
        public void setInputTokens(Integer inputTokens) { this.inputTokens = inputTokens; }
        
        public Integer getOutputTokens() { return outputTokens; }
        public void setOutputTokens(Integer outputTokens) { this.outputTokens = outputTokens; }
        
        public Integer getTotalTokens() { return totalTokens; }
        public void setTotalTokens(Integer totalTokens) { this.totalTokens = totalTokens; }
        
        public Double getCost() { return cost; }
        public void setCost(Double cost) { this.cost = cost; }
        
        public Long getDuration() { return duration; }
        public void setDuration(Long duration) { this.duration = duration; }
    }
}

