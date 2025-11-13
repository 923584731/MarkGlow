package com.markglow.service;

import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DocumentAnalysisService {
    
    // 阅读速度：中文300字/分钟，英文200词/分钟
    private static final int CHINESE_READING_SPEED = 300;
    private static final int ENGLISH_READING_SPEED = 200;

    /**
     * 分析文档内容，输出统计与复杂度指标
     * @param content Markdown 文本内容
     * @return 分析结果
     */
    public AnalysisResult analyzeDocument(String content) {
        if (content == null || content.isEmpty()) {
            return new AnalysisResult();
        }

        AnalysisResult result = new AnalysisResult();
        
        // 字数统计
        result.setTotalChars(content.length());
        result.setChineseChars(countChineseChars(content));
        result.setEnglishWords(countEnglishWords(content));
        result.setTotalWords(result.getChineseChars() + result.getEnglishWords());
        
        // 阅读时间估算
        double chineseMinutes = result.getChineseChars() / (double) CHINESE_READING_SPEED;
        double englishMinutes = result.getEnglishWords() / (double) ENGLISH_READING_SPEED;
        result.setReadingTimeMinutes(Math.max(chineseMinutes, englishMinutes) + Math.min(chineseMinutes, englishMinutes) * 0.5);
        
        // 复杂度分析
        ComplexityMetrics complexity = analyzeComplexity(content);
        result.setComplexity(complexity);
        
        return result;
    }

    /**
     * 统计中文字符数量
     */
    private int countChineseChars(String content) {
        int count = 0;
        for (char c : content.toCharArray()) {
            if (c >= 0x4E00 && c <= 0x9FFF) { // 中文字符范围
                count++;
            }
        }
        return count;
    }

    /**
     * 统计英文单词数量（忽略 Markdown 语法）
     */
    private int countEnglishWords(String content) {
        // 移除Markdown语法标记
        String text = content.replaceAll("```[\\s\\S]*?```", ""); // 代码块
        text = text.replaceAll("`[^`]+`", ""); // 行内代码
        text = text.replaceAll("#+\\s*", ""); // 标题标记
        text = text.replaceAll("\\*+|_+", ""); // 粗体/斜体
        text = text.replaceAll("\\[.*?\\]\\(.*?\\)", ""); // 链接
        text = text.replaceAll("!\\[.*?\\]\\(.*?\\)", ""); // 图片
        
        // 统计英文单词
        Pattern pattern = Pattern.compile("\\b[a-zA-Z]+\\b");
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * 分析文档复杂度（标题层级、段落、代码、链接、列表、可读性评分）
     */
    private ComplexityMetrics analyzeComplexity(String content) {
        ComplexityMetrics metrics = new ComplexityMetrics();
        
        // 标题层级深度
        Pattern headingPattern = Pattern.compile("^(#{1,6})\\s+", Pattern.MULTILINE);
        Matcher headingMatcher = headingPattern.matcher(content);
        int maxHeadingLevel = 0;
        int headingCount = 0;
        while (headingMatcher.find()) {
            headingCount++;
            int level = headingMatcher.group(1).length();
            maxHeadingLevel = Math.max(maxHeadingLevel, level);
        }
        metrics.setHeadingDepth(maxHeadingLevel);
        metrics.setHeadingCount(headingCount);
        
        // 段落统计
        String[] paragraphs = content.split("\\n\\n+");
        int paragraphCount = 0;
        int totalParagraphLength = 0;
        for (String para : paragraphs) {
            String trimmed = para.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#") && !trimmed.startsWith("-") && !trimmed.startsWith("*") && !trimmed.startsWith("```")) {
                paragraphCount++;
                totalParagraphLength += trimmed.length();
            }
        }
        metrics.setParagraphCount(paragraphCount);
        metrics.setAverageParagraphLength(paragraphCount > 0 ? totalParagraphLength / paragraphCount : 0);
        
        // 代码块数量
        Pattern codeBlockPattern = Pattern.compile("```[\\s\\S]*?```");
        Matcher codeBlockMatcher = codeBlockPattern.matcher(content);
        int codeBlockCount = 0;
        while (codeBlockMatcher.find()) {
            codeBlockCount++;
        }
        metrics.setCodeBlockCount(codeBlockCount);
        
        // 链接数量
        Pattern linkPattern = Pattern.compile("\\[([^\\]]+)\\]\\([^\\)]+\\)");
        Matcher linkMatcher = linkPattern.matcher(content);
        int linkCount = 0;
        while (linkMatcher.find()) {
            linkCount++;
        }
        metrics.setLinkCount(linkCount);
        
        // 列表项数量
        Pattern listPattern = Pattern.compile("^[\\s]*[-*+]\\s+", Pattern.MULTILINE);
        Matcher listMatcher = listPattern.matcher(content);
        int listItemCount = 0;
        while (listMatcher.find()) {
            listItemCount++;
        }
        metrics.setListItemCount(listItemCount);
        
        // 可读性评分（简化版Flesch-Kincaid）
        // 基于：平均段落长度、标题层级、代码块比例
        double readabilityScore = calculateReadabilityScore(metrics, content.length());
        metrics.setReadabilityScore(readabilityScore);
        
        return metrics;
    }

    /**
     * 计算可读性分数（简化版）
     */
    private double calculateReadabilityScore(ComplexityMetrics metrics, int totalLength) {
        double score = 100.0;
        
        // 段落长度影响（越长越难读）
        if (metrics.getAverageParagraphLength() > 500) {
            score -= 20;
        } else if (metrics.getAverageParagraphLength() > 300) {
            score -= 10;
        }
        
        // 标题层级影响（层级越深越复杂）
        if (metrics.getHeadingDepth() > 4) {
            score -= 15;
        } else if (metrics.getHeadingDepth() > 3) {
            score -= 8;
        }
        
        // 代码块比例（代码多可能更技术性）
        double codeRatio = totalLength > 0 ? (metrics.getCodeBlockCount() * 100.0) / (totalLength / 100.0) : 0;
        if (codeRatio > 10) {
            score -= 10;
        }
        
        return Math.max(0, Math.min(100, score));
    }

    public static class AnalysisResult {
        private int totalChars;
        private int chineseChars;
        private int englishWords;
        private int totalWords;
        private double readingTimeMinutes;
        private ComplexityMetrics complexity;

        // Getters and Setters
        public int getTotalChars() { return totalChars; }
        public void setTotalChars(int totalChars) { this.totalChars = totalChars; }
        
        public int getChineseChars() { return chineseChars; }
        public void setChineseChars(int chineseChars) { this.chineseChars = chineseChars; }
        
        public int getEnglishWords() { return englishWords; }
        public void setEnglishWords(int englishWords) { this.englishWords = englishWords; }
        
        public int getTotalWords() { return totalWords; }
        public void setTotalWords(int totalWords) { this.totalWords = totalWords; }
        
        public double getReadingTimeMinutes() { return readingTimeMinutes; }
        public void setReadingTimeMinutes(double readingTimeMinutes) { this.readingTimeMinutes = readingTimeMinutes; }
        
        public ComplexityMetrics getComplexity() { return complexity; }
        public void setComplexity(ComplexityMetrics complexity) { this.complexity = complexity; }
    }

    public static class ComplexityMetrics {
        private int headingDepth;
        private int headingCount;
        private int paragraphCount;
        private double averageParagraphLength;
        private int codeBlockCount;
        private int linkCount;
        private int listItemCount;
        private double readabilityScore;

        // Getters and Setters
        public int getHeadingDepth() { return headingDepth; }
        public void setHeadingDepth(int headingDepth) { this.headingDepth = headingDepth; }
        
        public int getHeadingCount() { return headingCount; }
        public void setHeadingCount(int headingCount) { this.headingCount = headingCount; }
        
        public int getParagraphCount() { return paragraphCount; }
        public void setParagraphCount(int paragraphCount) { this.paragraphCount = paragraphCount; }
        
        public double getAverageParagraphLength() { return averageParagraphLength; }
        public void setAverageParagraphLength(double averageParagraphLength) { this.averageParagraphLength = averageParagraphLength; }
        
        public int getCodeBlockCount() { return codeBlockCount; }
        public void setCodeBlockCount(int codeBlockCount) { this.codeBlockCount = codeBlockCount; }
        
        public int getLinkCount() { return linkCount; }
        public void setLinkCount(int linkCount) { this.linkCount = linkCount; }
        
        public int getListItemCount() { return listItemCount; }
        public void setListItemCount(int listItemCount) { this.listItemCount = listItemCount; }
        
        public double getReadabilityScore() { return readabilityScore; }
        public void setReadabilityScore(double readabilityScore) { this.readabilityScore = readabilityScore; }
    }
}

