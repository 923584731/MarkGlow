package com.markglow.repository;

import com.markglow.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findAllByOrderByUpdatedAtDesc();
    
    // 按标题搜索（模糊匹配，忽略大小写）
    List<Document> findByTitleContainingIgnoreCaseOrderByUpdatedAtDesc(String keyword);
    
    // 按标题或内容搜索（全文搜索）
    @Query("SELECT d FROM Document d WHERE " +
           "LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.originalContent) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.beautifiedContent) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY d.updatedAt DESC")
    List<Document> searchByKeyword(@Param("keyword") String keyword);
}

