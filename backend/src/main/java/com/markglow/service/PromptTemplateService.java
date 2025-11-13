package com.markglow.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.markglow.dto.PromptTemplateDTO;
import com.markglow.dto.TemplateRenderRequest;
import com.markglow.entity.PromptTemplate;
import com.markglow.repository.PromptTemplateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PromptTemplateService {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    @Autowired
    private PromptTemplateRepository templateRepository;

    /**
     * 获取模板列表（可按分类过滤）
     * @param category 分类，可为空
     * @return 模板DTO列表
     */
    public List<PromptTemplateDTO> getAllTemplates(String category) {
        List<PromptTemplate> templates;
        if (category != null && !category.isEmpty()) {
            templates = templateRepository.findByCategory(category);
        } else {
            templates = templateRepository.findAll();
        }
        return templates.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * 根据ID获取模板
     * @param id 模板ID
     * @return 模板DTO
     */
    public PromptTemplateDTO getTemplateById(Long id) {
        PromptTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("模板不存在: " + id));
        return toDTO(template);
    }

    /**
     * 新建模板
     * @param dto 模板数据
     * @return 创建后的模板DTO
     */
    public PromptTemplateDTO createTemplate(PromptTemplateDTO dto) {
        PromptTemplate template = new PromptTemplate();
        template.setName(dto.getName());
        template.setDescription(dto.getDescription());
        template.setCategory(dto.getCategory());
        template.setContent(dto.getContent());
        template.setVariables(dto.getVariables() != null ? JSON.toJSONString(dto.getVariables()) : null);
        
        template = templateRepository.save(template);
        return toDTO(template);
    }

    /**
     * 更新模板
     * @param id 模板ID
     * @param dto 新内容
     * @return 更新后的模板DTO
     */
    public PromptTemplateDTO updateTemplate(Long id, PromptTemplateDTO dto) {
        PromptTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("模板不存在: " + id));
        
        template.setName(dto.getName());
        template.setDescription(dto.getDescription());
        template.setCategory(dto.getCategory());
        template.setContent(dto.getContent());
        template.setVariables(dto.getVariables() != null ? JSON.toJSONString(dto.getVariables()) : null);
        
        template = templateRepository.save(template);
        return toDTO(template);
    }

    /**
     * 删除模板
     * @param id 模板ID
     */
    public void deleteTemplate(Long id) {
        if (!templateRepository.existsById(id)) {
            throw new RuntimeException("模板不存在: " + id);
        }
        templateRepository.deleteById(id);
    }

    /**
     * 渲染模板内容（将 {{variable}} 替换为传入变量值）
     * @param request 渲染请求
     * @return 渲染后的文本
     */
    public String renderTemplate(TemplateRenderRequest request) {
        PromptTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new RuntimeException("模板不存在: " + request.getTemplateId()));
        
        String content = template.getContent();
        Map<String, String> variables = request.getVariables() != null ? request.getVariables() : new HashMap<>();
        
        // 替换所有 {{variable}} 为实际值
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String varName = matcher.group(1).trim();
            String value = variables.getOrDefault(varName, "");
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    /**
     * 从模板内容中提取变量名（{{var}}）
     */
    public List<String> extractVariables(String content) {
        Set<String> variables = new HashSet<>();
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        while (matcher.find()) {
            variables.add(matcher.group(1).trim());
        }
        return new ArrayList<>(variables);
    }

    private PromptTemplateDTO toDTO(PromptTemplate template) {
        PromptTemplateDTO dto = new PromptTemplateDTO();
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setDescription(template.getDescription());
        dto.setCategory(template.getCategory());
        dto.setContent(template.getContent());
        dto.setCreatedAt(template.getCreatedAt());
        dto.setUpdatedAt(template.getUpdatedAt());
        
        // 解析variables JSON
        if (template.getVariables() != null && !template.getVariables().isEmpty()) {
            try {
                JSONArray jsonArray = JSON.parseArray(template.getVariables());
                List<PromptTemplateDTO.VariableDefinition> varList = new ArrayList<>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    com.alibaba.fastjson.JSONObject obj = jsonArray.getJSONObject(i);
                    PromptTemplateDTO.VariableDefinition var = new PromptTemplateDTO.VariableDefinition();
                    var.setName(obj.getString("name"));
                    var.setDescription(obj.getString("description"));
                    var.setDefaultValue(obj.getString("defaultValue"));
                    varList.add(var);
                }
                dto.setVariables(varList);
            } catch (Exception e) {
                log.warn("解析variables失败: {}", e.getMessage());
            }
        } else {
            // 如果没有定义variables，从content中提取
            List<String> varNames = extractVariables(template.getContent());
            dto.setVariables(varNames.stream().map(name -> {
                PromptTemplateDTO.VariableDefinition var = new PromptTemplateDTO.VariableDefinition();
                var.setName(name);
                var.setDescription("变量: " + name);
                var.setDefaultValue("");
                return var;
            }).collect(Collectors.toList()));
        }
        
        return dto;
    }
}

