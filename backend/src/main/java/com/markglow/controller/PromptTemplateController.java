package com.markglow.controller;

import com.markglow.dto.PromptTemplateDTO;
import com.markglow.dto.TemplateRenderRequest;
import com.markglow.service.PromptTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prompt-templates")
@CrossOrigin(origins = "*")
@Slf4j
public class PromptTemplateController {

    @Autowired
    private PromptTemplateService templateService;

    @GetMapping
    public ResponseEntity<List<PromptTemplateDTO>> getAllTemplates(
            @RequestParam(required = false) String category) {
        try {
            List<PromptTemplateDTO> templates = templateService.getAllTemplates(category);
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            log.error("获取模板列表失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromptTemplateDTO> getTemplateById(@PathVariable Long id) {
        try {
            PromptTemplateDTO template = templateService.getTemplateById(id);
            return ResponseEntity.ok(template);
        } catch (RuntimeException e) {
            log.error("获取模板失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("获取模板失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<PromptTemplateDTO> createTemplate(@RequestBody PromptTemplateDTO dto) {
        try {
            PromptTemplateDTO created = templateService.createTemplate(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("创建模板失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromptTemplateDTO> updateTemplate(
            @PathVariable Long id,
            @RequestBody PromptTemplateDTO dto) {
        try {
            PromptTemplateDTO updated = templateService.updateTemplate(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("更新模板失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("更新模板失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        try {
            templateService.deleteTemplate(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("删除模板失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("删除模板失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/render")
    public ResponseEntity<Map<String, String>> renderTemplate(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> variables) {
        try {
            TemplateRenderRequest request = new TemplateRenderRequest();
            request.setTemplateId(id);
            request.setVariables(variables != null ? variables : new HashMap<>());
            
            String rendered = templateService.renderTemplate(request);
            Map<String, String> response = new HashMap<>();
            response.put("rendered", rendered);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("渲染模板失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("渲染模板失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

