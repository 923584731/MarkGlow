package com.markglow.service;

import com.markglow.dto.DocumentDTO;
import com.markglow.entity.Document;
import com.markglow.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    /**
     * 获取所有文档（按更新时间倒序）
     * @return 文档DTO列表
     */
    public List<DocumentDTO> getAllDocuments() {
        return documentRepository.findAllByOrderByUpdatedAtDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取单个文档
     * @param id 文档ID
     * @return 文档DTO
     */
    public DocumentDTO getDocumentById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文档不存在: " + id));
        return convertToDTO(document);
    }

    @Transactional
    /**
     * 新建/保存文档
     * @param documentDTO 文档数据
     * @return 保存后的文档DTO
     */
    public DocumentDTO saveDocument(DocumentDTO documentDTO) {
        Document document = convertToEntity(documentDTO);
        if (document.getTitle() == null || document.getTitle().trim().isEmpty()) {
            document.setTitle("未命名文档");
        }
        Document saved = documentRepository.save(document);
        return convertToDTO(saved);
    }

    @Transactional
    /**
     * 更新文档
     * @param id 文档ID
     * @param documentDTO 新内容
     * @return 更新后的文档DTO
     */
    public DocumentDTO updateDocument(Long id, DocumentDTO documentDTO) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文档不存在: " + id));
        
        if (documentDTO.getTitle() != null) {
            document.setTitle(documentDTO.getTitle());
        }
        if (documentDTO.getOriginalContent() != null) {
            document.setOriginalContent(documentDTO.getOriginalContent());
        }
        if (documentDTO.getBeautifiedContent() != null) {
            document.setBeautifiedContent(documentDTO.getBeautifiedContent());
        }
        if (documentDTO.getTheme() != null) {
            document.setTheme(documentDTO.getTheme());
        }
        
        Document updated = documentRepository.save(document);
        return convertToDTO(updated);
    }

    @Transactional
    /**
     * 删除文档
     * @param id 文档ID
     */
    public void deleteDocument(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new RuntimeException("文档不存在: " + id);
        }
        documentRepository.deleteById(id);
    }

    /**
     * 搜索文档（按标题或内容）
     * @param keyword 搜索关键词
     * @return 匹配的文档列表
     */
    public List<DocumentDTO> searchDocuments(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllDocuments();
        }
        return documentRepository.searchByKeyword(keyword.trim())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 按标题搜索文档
     * @param keyword 搜索关键词
     * @return 匹配的文档列表
     */
    public List<DocumentDTO> searchByTitle(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllDocuments();
        }
        return documentRepository.findByTitleContainingIgnoreCaseOrderByUpdatedAtDesc(keyword.trim())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private DocumentDTO convertToDTO(Document document) {
        return new DocumentDTO(
                document.getId(),
                document.getTitle(),
                document.getOriginalContent(),
                document.getBeautifiedContent(),
                document.getTheme(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }

    private Document convertToEntity(DocumentDTO dto) {
        Document document = new Document();
        if (dto.getId() != null) {
            document.setId(dto.getId());
        }
        document.setTitle(dto.getTitle());
        document.setOriginalContent(dto.getOriginalContent());
        document.setBeautifiedContent(dto.getBeautifiedContent());
        document.setTheme(dto.getTheme());
        return document;
    }
}

