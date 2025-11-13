package com.markglow.controller;

import com.markglow.dto.DocumentDTO;
import com.markglow.service.DocumentService;
import com.markglow.service.DocumentAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
@Slf4j
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentAnalysisService analysisService;

    @GetMapping
    public ResponseEntity<List<DocumentDTO>> getAllDocuments() {
        try {
            List<DocumentDTO> documents = documentService.getAllDocuments();
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<DocumentDTO>> searchDocuments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "all") String type) {
        try {
            List<DocumentDTO> documents;
            if (keyword == null || keyword.trim().isEmpty()) {
                documents = documentService.getAllDocuments();
            } else {
                if ("title".equals(type)) {
                    documents = documentService.searchByTitle(keyword);
                } else {
                    documents = documentService.searchDocuments(keyword);
                }
            }
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDTO> getDocumentById(@PathVariable Long id) {
        try {
            DocumentDTO document = documentService.getDocumentById(id);
            return ResponseEntity.ok(document);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<DocumentDTO> createDocument(@RequestBody DocumentDTO documentDTO) {
        try {
            DocumentDTO saved = documentService.saveDocument(documentDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentDTO> updateDocument(@PathVariable Long id, 
                                                      @RequestBody DocumentDTO documentDTO) {
        try {
            DocumentDTO updated = documentService.updateDocument(id, documentDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        try {
            documentService.deleteDocument(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/analyze")
    public ResponseEntity<Map<String, Object>> analyzeDocument(@PathVariable Long id) {
        try {
            DocumentDTO document = documentService.getDocumentById(id);
            String content = document.getOriginalContent() != null ? 
                document.getOriginalContent() : document.getBeautifiedContent();
            
            DocumentAnalysisService.AnalysisResult result = analysisService.analyzeDocument(content);
            
            Map<String, Object> response = new HashMap<>();
            response.put("documentId", id);
            response.put("totalChars", result.getTotalChars());
            response.put("chineseChars", result.getChineseChars());
            response.put("englishWords", result.getEnglishWords());
            response.put("totalWords", result.getTotalWords());
            response.put("readingTimeMinutes", Math.round(result.getReadingTimeMinutes() * 10.0) / 10.0);
            response.put("readingTimeFormatted", formatReadingTime(result.getReadingTimeMinutes()));
            
            if (result.getComplexity() != null) {
                Map<String, Object> complexity = new HashMap<>();
                complexity.put("headingDepth", result.getComplexity().getHeadingDepth());
                complexity.put("headingCount", result.getComplexity().getHeadingCount());
                complexity.put("paragraphCount", result.getComplexity().getParagraphCount());
                complexity.put("averageParagraphLength", Math.round(result.getComplexity().getAverageParagraphLength()));
                complexity.put("codeBlockCount", result.getComplexity().getCodeBlockCount());
                complexity.put("linkCount", result.getComplexity().getLinkCount());
                complexity.put("listItemCount", result.getComplexity().getListItemCount());
                complexity.put("readabilityScore", Math.round(result.getComplexity().getReadabilityScore() * 10.0) / 10.0);
                response.put("complexity", complexity);
            }
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeContent(@RequestBody Map<String, String> request) {
        try {
            String content = request.get("content");
            if (content == null) {
                return ResponseEntity.badRequest().build();
            }
            
            DocumentAnalysisService.AnalysisResult result = analysisService.analyzeDocument(content);
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalChars", result.getTotalChars());
            response.put("chineseChars", result.getChineseChars());
            response.put("englishWords", result.getEnglishWords());
            response.put("totalWords", result.getTotalWords());
            response.put("readingTimeMinutes", Math.round(result.getReadingTimeMinutes() * 10.0) / 10.0);
            response.put("readingTimeFormatted", formatReadingTime(result.getReadingTimeMinutes()));
            
            if (result.getComplexity() != null) {
                Map<String, Object> complexity = new HashMap<>();
                complexity.put("headingDepth", result.getComplexity().getHeadingDepth());
                complexity.put("headingCount", result.getComplexity().getHeadingCount());
                complexity.put("paragraphCount", result.getComplexity().getParagraphCount());
                complexity.put("averageParagraphLength", Math.round(result.getComplexity().getAverageParagraphLength()));
                complexity.put("codeBlockCount", result.getComplexity().getCodeBlockCount());
                complexity.put("linkCount", result.getComplexity().getLinkCount());
                complexity.put("listItemCount", result.getComplexity().getListItemCount());
                complexity.put("readabilityScore", Math.round(result.getComplexity().getReadabilityScore() * 10.0) / 10.0);
                response.put("complexity", complexity);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String formatReadingTime(double minutes) {
        if (minutes < 1) {
            return "不到1分钟";
        } else if (minutes < 60) {
            return Math.round(minutes) + "分钟";
        } else {
            int hours = (int) (minutes / 60);
            int mins = (int) (minutes % 60);
            if (mins == 0) {
                return hours + "小时";
            } else {
                return hours + "小时" + mins + "分钟";
            }
        }
    }
}

