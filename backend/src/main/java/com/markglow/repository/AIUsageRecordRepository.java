package com.markglow.repository;

import com.markglow.entity.AIUsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AIUsageRecordRepository extends JpaRepository<AIUsageRecord, Long> {
    List<AIUsageRecord> findByAction(String action);
    List<AIUsageRecord> findByProvider(String provider);
    List<AIUsageRecord> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // 显式查询方法，确保边界条件正确（包含边界）
    @Query("SELECT r FROM AIUsageRecord r WHERE r.createdAt >= :start AND r.createdAt <= :end ORDER BY r.createdAt DESC")
    List<AIUsageRecord> findByCreatedAtRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(r) FROM AIUsageRecord r WHERE r.createdAt >= :start AND r.createdAt <= :end")
    Long countByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT SUM(r.inputTokens + r.outputTokens) FROM AIUsageRecord r WHERE r.createdAt >= :start AND r.createdAt <= :end")
    Long sumTokensByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT SUM(r.cost) FROM AIUsageRecord r WHERE r.createdAt >= :start AND r.createdAt <= :end")
    Double sumCostByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT r.action, COUNT(r) as count FROM AIUsageRecord r WHERE r.createdAt >= :start AND r.createdAt <= :end GROUP BY r.action")
    List<Object[]> countByActionAndDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT r.provider, COUNT(r) as count FROM AIUsageRecord r WHERE r.createdAt >= :start AND r.createdAt <= :end GROUP BY r.provider")
    List<Object[]> countByProviderAndDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

