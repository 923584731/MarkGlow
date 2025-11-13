package com.markglow.repository;

import com.markglow.entity.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {
    List<PromptTemplate> findByCategory(String category);
    List<PromptTemplate> findByNameContainingIgnoreCase(String name);
}

