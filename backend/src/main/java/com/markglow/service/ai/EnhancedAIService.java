package com.markglow.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 增强的AI服务，提供各种AI功能
 */
@Service
public class EnhancedAIService {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedAIService.class);

    @Autowired
    private AIServiceFactory aiServiceFactory;

    private String invokeAI(String systemPrompt, String prompt,
                            Double temperature, Integer maxTokens,
                            Boolean stream, String model) {
        AIService aiService = aiServiceFactory.getDefaultService();
        return aiService.generateContent(prompt, systemPrompt, temperature, maxTokens, stream, model);
    }

    /**
     * 统一路由：根据 action 分发到对应功能；支持参数占位（当前版本记录日志，底层实现按默认参数调用）
     */
    public String routeAction(String action, String content, Double temperature, Integer maxTokens, String model, Boolean stream, String style, String targetLang) {
        String actionSafe = action != null ? action : "beautify";
        logger.info(">> 路由AI动作 action={}, temp={}, maxTokens={}, model={}, stream={}, style={}, targetLang={}", actionSafe, temperature, maxTokens, model, stream, style, targetLang);
        switch (actionSafe) {
            case "beautify":
                return beautifyMarkdown(content, temperature, maxTokens, stream, model);
            case "improve":
                return improveContent(content, style != null ? style : "专业、清晰、易读", temperature, maxTokens, stream, model);
            case "summarize":
                return summarizeDocument(content, temperature, maxTokens, stream, model);
            case "translate":
                return translateDocument(content, targetLang != null ? targetLang : "英文", temperature, maxTokens, stream, model);
            case "expand":
                return expandParagraph(content, temperature, maxTokens, stream, model);
            case "complete":
                return suggestCompletion(content, temperature, maxTokens, stream, model);
            default:
                logger.info("未知 action：{}，回退到 beautify", actionSafe);
                return beautifyMarkdown(content, temperature, maxTokens, stream, model);
        }
    }

    /**
     * 流式路由：根据 action 分发到对应功能，支持实时流式回调
     */
    public String routeActionStream(String action, String content, Double temperature, Integer maxTokens, 
                                    String model, String style, String targetLang, Consumer<String> chunkConsumer) {
        String actionSafe = action != null ? action : "beautify";
        logger.info(">> 流式路由AI动作 action={}, temp={}, maxTokens={}, model={}, style={}, targetLang={}", 
                actionSafe, temperature, maxTokens, model, style, targetLang);
        
        AIService aiService = aiServiceFactory.getDefaultService();
        String systemPrompt;
        String prompt;
        
        switch (actionSafe) {
            case "beautify":
                systemPrompt = "你是一个Markdown文档美化专家。请优化以下Markdown内容的格式、排版和结构，使其更加清晰易读。保持原有内容不变，只优化格式。";
                prompt = "请美化以下Markdown内容：\n\n" + content;
                break;
            case "improve":
                String styleDesc = style != null ? style : "专业、清晰、易读";
                systemPrompt = "你是一个专业的文本润色专家。请优化文本的表达方式，使其更加" + styleDesc + "。";
                prompt = "请润色以下内容，使其更加" + styleDesc + "：\n\n" + content;
                break;
            case "summarize":
                systemPrompt = "你是一个文档摘要专家。请为以下文档生成简洁明了的摘要，突出关键信息。";
                prompt = "请为以下文档生成摘要：\n\n" + content;
                break;
            case "translate":
                String target = targetLang != null ? targetLang : "英文";
                systemPrompt = "你是一个专业的翻译专家。请准确翻译文档内容，保持Markdown格式不变。";
                prompt = "请将以下内容翻译成" + target + "：\n\n" + content;
                break;
            case "expand":
                systemPrompt = "你是一个内容扩展专家。请将简短的段落扩展为更详细、更丰富的内容，保持主题不变。";
                prompt = "请扩展以下段落，使其更加详细：\n\n" + content;
                break;
            case "complete":
                systemPrompt = "你是一个文本补全助手。请根据上下文，补全用户输入的文本。";
                prompt = "请补全以下文本：\n\n" + content;
                break;
            default:
                logger.info("未知 action：{}，回退到 beautify", actionSafe);
                systemPrompt = "你是一个Markdown文档美化专家。请优化以下Markdown内容的格式、排版和结构，使其更加清晰易读。保持原有内容不变，只优化格式。";
                prompt = "请美化以下Markdown内容：\n\n" + content;
                break;
        }
        
        return aiService.generateContentStream(prompt, systemPrompt, temperature, maxTokens, model, chunkConsumer);
    }

    /**
     * AI美化Markdown内容
     */
    public String beautifyMarkdown(String content) {
        return beautifyMarkdown(content, null, null, null, null);
    }

    public String beautifyMarkdown(String content, Double temperature, Integer maxTokens, Boolean stream, String model) {
        logger.info(">>> 调用AI美化功能");
        logger.info("输入内容长度: {} 字符", content != null ? content.length() : 0);
        String systemPrompt = "你是一个Markdown文档美化专家。请优化以下Markdown内容的格式、排版和结构，使其更加清晰易读。保持原有内容不变，只优化格式。";
        String prompt = "请美化以下Markdown内容：\n\n" + content;
        String result = invokeAI(systemPrompt, prompt, temperature, maxTokens, stream, model);
        logger.info("美化完成，输出长度: {} 字符", result != null ? result.length() : 0);
        return result;
    }

    /**
     * AI写作助手 - 根据标题生成内容
     */
    public String generateContent(String title, String context) {
        logger.info(">>> 调用AI写作功能");
        logger.info("标题: {}", title);
        logger.info("上下文长度: {} 字符", context != null ? context.length() : 0);
        AIService aiService = aiServiceFactory.getDefaultService();
        String systemPrompt = "你是一个专业的文档写作助手。请根据用户提供的标题和上下文，生成高质量的Markdown文档内容。";
        String prompt = "标题: " + title + "\n\n" + 
                       (context != null && !context.isEmpty() ? "上下文: " + context + "\n\n" : "") +
                       "请生成完整的Markdown文档内容：";
        String result = aiService.generateContent(prompt, systemPrompt);
        logger.info("写作完成，输出长度: {} 字符", result != null ? result.length() : 0);
        return result;
    }

    /**
     * 语言润色
     */
    public String improveContent(String content, String style) {
        return improveContent(content, style, null, null, null, null);
    }

    public String improveContent(String content, String style,
                                 Double temperature, Integer maxTokens,
                                 Boolean stream, String model) {
        logger.info(">>> 调用语言润色功能");
        logger.info("输入内容长度: {} 字符", content != null ? content.length() : 0);
        logger.info("润色风格: {}", style);
        String styleDesc = style != null ? style : "专业、清晰、易读";
        String systemPrompt = "你是一个专业的文本润色专家。请优化文本的表达方式，使其更加" + styleDesc + "。";
        String prompt = "请润色以下内容，使其更加" + styleDesc + "：\n\n" + content;
        String result = invokeAI(systemPrompt, prompt, temperature, maxTokens, stream, model);
        logger.info("润色完成，输出长度: {} 字符", result != null ? result.length() : 0);
        return result;
    }

    /**
     * 语法检查
     */
    public String checkGrammar(String content) {
        AIService aiService = aiServiceFactory.getDefaultService();
        String systemPrompt = "你是一个语法检查专家。请检查以下Markdown内容的语法错误、拼写错误和格式问题，并提供修正建议。";
        String prompt = "请检查以下内容的语法和格式问题：\n\n" + content + "\n\n请指出错误并提供修正后的内容：";
        return aiService.generateContent(prompt, systemPrompt);
    }

    /**
     * 生成文档摘要
     */
    public String summarizeDocument(String content) {
        return summarizeDocument(content, null, null, null, null);
    }

    public String summarizeDocument(String content,
                                    Double temperature, Integer maxTokens,
                                    Boolean stream, String model) {
        String systemPrompt = "你是一个文档摘要专家。请为以下文档生成简洁明了的摘要，突出关键信息。";
        String prompt = "请为以下文档生成摘要：\n\n" + content;
        return invokeAI(systemPrompt, prompt, temperature, maxTokens, stream, model);
    }

    /**
     * 翻译文档
     */
    public String translateDocument(String content, String targetLang) {
        return translateDocument(content, targetLang, null, null, null, null);
    }

    public String translateDocument(String content, String targetLang,
                                    Double temperature, Integer maxTokens,
                                    Boolean stream, String model) {
        String systemPrompt = "你是一个专业的翻译专家。请准确翻译文档内容，保持Markdown格式不变。";
        String prompt = "请将以下内容翻译成" + targetLang + "：\n\n" + content;
        return invokeAI(systemPrompt, prompt, temperature, maxTokens, stream, model);
    }

    /**
     * 解释代码
     */
    public String explainCode(String code, String language) {
        AIService aiService = aiServiceFactory.getDefaultService();
        String systemPrompt = "你是一个代码解释专家。请详细解释代码的功能、逻辑和使用方法。";
        String prompt = "请解释以下" + (language != null ? language : "") + "代码：\n\n```" + 
                       (language != null ? language : "") + "\n" + code + "\n```";
        return aiService.generateContent(prompt, systemPrompt);
    }

    /**
     * 智能补全
     */
    public String suggestCompletion(String partialText) {
        return suggestCompletion(partialText, null, null, null, null);
    }

    public String suggestCompletion(String partialText,
                                    Double temperature, Integer maxTokens,
                                    Boolean stream, String model) {
        String systemPrompt = "你是一个文本补全助手。请根据上下文，补全用户输入的文本。";
        String prompt = "请补全以下文本：\n\n" + partialText;
        return invokeAI(systemPrompt, prompt, temperature, maxTokens, stream, model);
    }

    /**
     * 扩展段落
     */
    public String expandParagraph(String paragraph) {
        return expandParagraph(paragraph, null, null, null, null);
    }

    public String expandParagraph(String paragraph,
                                  Double temperature, Integer maxTokens,
                                  Boolean stream, String model) {
        String systemPrompt = "你是一个内容扩展专家。请将简短的段落扩展为更详细、更丰富的内容，保持主题不变。";
        String prompt = "请扩展以下段落，使其更加详细：\n\n" + paragraph;
        return invokeAI(systemPrompt, prompt, temperature, maxTokens, stream, model);
    }

    /**
     * 生成列表
     */
    public String generateList(String topic) {
        AIService aiService = aiServiceFactory.getDefaultService();
        String systemPrompt = "你是一个列表生成专家。请根据主题生成结构化的Markdown列表。";
        String prompt = "请为以下主题生成一个详细的Markdown列表：\n\n" + topic;
        return aiService.generateContent(prompt, systemPrompt);
    }

    /**
     * 优化标题
     */
    public String optimizeTitles(String content) {
        AIService aiService = aiServiceFactory.getDefaultService();
        String systemPrompt = "你是一个标题优化专家。请优化文档中的标题层级和标题文字，使其更加清晰和准确。";
        String prompt = "请优化以下文档的标题：\n\n" + content;
        return aiService.generateContent(prompt, systemPrompt);
    }

    /**
     * 生成表格
     */
    public String generateTable(String description) {
        AIService aiService = aiServiceFactory.getDefaultService();
        String systemPrompt = "你是一个表格生成专家。请根据描述生成Markdown格式的表格。";
        String prompt = "请根据以下描述生成Markdown表格：\n\n" + description;
        return aiService.generateContent(prompt, systemPrompt);
    }

    /**
     * 文档问答
     */
    public String answerQuestion(String document, String question) {
        AIService aiService = aiServiceFactory.getDefaultService();
        String systemPrompt = "你是一个文档问答助手。请根据提供的文档内容回答用户的问题。";
        String prompt = "文档内容：\n\n" + document + "\n\n问题：" + question + "\n\n请回答：";
        return aiService.generateContent(prompt, systemPrompt);
    }

    /**
     * 分析文档
     */
    public Map<String, Object> analyzeDocument(String content) {
        AIService aiService = aiServiceFactory.getDefaultService();
        String systemPrompt = "你是一个文档分析专家。请分析文档的结构、主题、关键词和阅读难度。";
        String prompt = "请分析以下文档：\n\n" + content + "\n\n请提供：1. 文档主题 2. 关键词 3. 文档结构 4. 阅读难度评估";
        
        String analysis = aiService.generateContent(prompt, systemPrompt);
        
        Map<String, Object> result = new HashMap<>();
        result.put("analysis", analysis);
        result.put("wordCount", content.length());
        result.put("estimatedReadingTime", calculateReadingTime(content));
        
        return result;
    }

    /**
     * 计算阅读时间（分钟）
     */
    private int calculateReadingTime(String content) {
        // 假设平均阅读速度为每分钟200字
        int wordCount = content.length();
        return Math.max(1, wordCount / 200);
    }

    /**
     * 切换AI服务提供商
     */
    public void switchProvider(String provider) {
        aiServiceFactory.switchProvider(provider);
    }

    /**
     * 获取当前AI服务提供商
     */
    public String getCurrentProvider() {
        return aiServiceFactory.getCurrentProvider();
    }
}

