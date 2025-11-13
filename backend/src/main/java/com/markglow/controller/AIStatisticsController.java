package com.markglow.controller;

import com.markglow.service.AIStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*")
@Slf4j
public class AIStatisticsController {

    @Autowired
    private AIStatisticsService statisticsService;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(
            @RequestParam(required = false, defaultValue = "today") String period) {
        try {
            AIStatisticsService.StatisticsSummary summary = statisticsService.getSummary(period);
            
            Map<String, Object> response = new HashMap<>();
            response.put("period", summary.getPeriod());
            response.put("totalCalls", summary.getTotalCalls());
            response.put("totalInputTokens", summary.getTotalInputTokens());
            response.put("totalOutputTokens", summary.getTotalOutputTokens());
            response.put("totalTokens", summary.getTotalTokens());
            response.put("totalCost", summary.getTotalCost());
            response.put("actionCounts", summary.getActionCounts());
            response.put("providerCounts", summary.getProviderCounts());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取统计摘要失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/usage")
    public ResponseEntity<Map<String, Object>> getUsageStatistics(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(required = false, defaultValue = "day") String groupBy) {
        try {
            LocalDateTime startTime;
            LocalDateTime endTime;
            
            // 处理时区转换：前端发送的是ISO字符串（可能包含时区信息）
            if (start == null || start.isEmpty()) {
                startTime = LocalDateTime.now().minusDays(7);
            } else {
                // 尝试解析ISO格式的日期时间字符串
                try {
                    // 如果包含时区信息（如 'Z' 或 '+08:00'），使用ZonedDateTime解析
                    if (start.contains("Z") || start.contains("+") || start.contains("-") && start.length() > 19) {
                        ZonedDateTime zonedStart = ZonedDateTime.parse(start);
                        startTime = zonedStart.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
                    } else {
                        // 纯日期时间字符串，直接解析为LocalDateTime
                        startTime = LocalDateTime.parse(start);
                    }
                } catch (Exception e) {
                    log.warn("解析start时间失败: {}, 使用默认值", start, e);
                    startTime = LocalDateTime.now().minusDays(7);
                }
            }
            
            if (end == null || end.isEmpty()) {
                endTime = LocalDateTime.now();
            } else {
                try {
                    if (end.contains("Z") || end.contains("+") || (end.contains("-") && end.length() > 19)) {
                        ZonedDateTime zonedEnd = ZonedDateTime.parse(end);
                        endTime = zonedEnd.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
                    } else {
                        endTime = LocalDateTime.parse(end);
                    }
                } catch (Exception e) {
                    log.warn("解析end时间失败: {}, 使用默认值", end, e);
                    endTime = LocalDateTime.now();
                }
            }
            
            log.info("获取使用统计: groupBy={}, start={} (原始: {}), end={} (原始: {})", 
                    groupBy, startTime, start, endTime, end);
            
            List<AIStatisticsService.GroupedStatistics> groupedStats = statisticsService.getGroupedStatistics(startTime, endTime, groupBy);
            
            log.info("返回统计数据: 记录数={}", groupedStats.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("startTime", startTime);
            response.put("endTime", endTime);
            response.put("groupBy", groupBy);
            response.put("statistics", groupedStats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取使用统计失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/records")
    public ResponseEntity<Map<String, Object>> getUsageRecords(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime = parseDateTimeParameter(start, now.minusDays(7));
            LocalDateTime endTime = parseDateTimeParameter(end, now);

            log.info("获取使用记录: start={} (原始: {}), end={} (原始: {}), page={}, size={}",
                    startTime, start, endTime, end, page, size);
            
            List<com.markglow.entity.AIUsageRecord> records = statisticsService.getUsageRecords(startTime, endTime, page, size);
            long total = statisticsService.getUsageRecordsCount(startTime, endTime);
            
            Map<String, Object> response = new HashMap<>();
            response.put("startTime", startTime);
            response.put("endTime", endTime);
            response.put("records", records);
            response.put("total", total);
            response.put("page", page);
            response.put("size", size);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取使用记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private LocalDateTime parseDateTimeParameter(String value, LocalDateTime defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            if (value.contains("Z") || value.contains("+") || (value.contains("-") && value.length() > 19)) {
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(value);
                return zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
            }
            return LocalDateTime.parse(value);
        } catch (Exception e) {
            log.warn("解析时间参数失败: {}, 使用默认值 {}", value, defaultValue, e);
            return defaultValue;
        }
    }

    @GetMapping("/cost")
    public ResponseEntity<Map<String, Object>> getCostStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            if (start == null) {
                start = LocalDateTime.now().minusDays(30);
            }
            if (end == null) {
                end = LocalDateTime.now();
            }
            
            List<AIStatisticsService.DailyStatistics> dailyStats = statisticsService.getDailyStatistics(start, end);
            
            double totalCost = dailyStats.stream().mapToDouble(s -> s.getCost() != null ? s.getCost() : 0).sum();
            
            Map<String, Object> response = new HashMap<>();
            response.put("startTime", start);
            response.put("endTime", end);
            response.put("totalCost", Math.round(totalCost * 100.0) / 100.0);
            response.put("dailyCost", dailyStats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取成本统计失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

