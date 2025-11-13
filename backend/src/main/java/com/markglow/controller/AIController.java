package com.markglow.controller;

import com.markglow.dto.AIRequest;
import com.markglow.dto.AIResponse;
import com.markglow.service.ai.EnhancedAIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    private static final Logger logger = LoggerFactory.getLogger(AIController.class);

    @Autowired
    private EnhancedAIService enhancedAIService;

    /**
     * AI美化Markdown
     */
    @PostMapping("/beautify")
    public ResponseEntity<AIResponse> beautify(@RequestBody AIRequest request) {
        logger.info("收到AI美化请求，内容长度: {}", request.getContent() != null ? request.getContent().length() : 0);
        try {
            String result = enhancedAIService.beautifyMarkdown(request.getContent());
            logger.info("AI美化请求处理成功");
            return ResponseEntity.ok(new AIResponse(result, enhancedAIService.getCurrentProvider()));
        } catch (Exception e) {
            logger.error("AI美化请求处理失败", e);
            AIResponse errorResponse = new AIResponse();
            errorResponse.setMessage("美化失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * AI写作 - 根据标题生成内容
     */
    @PostMapping("/generate")
    public ResponseEntity<AIResponse> generate(@RequestBody AIRequest request) {
        try {
            String result = enhancedAIService.generateContent(request.getTitle(), request.getContext());
            return ResponseEntity.ok(new AIResponse(result, enhancedAIService.getCurrentProvider()));
        } catch (Exception e) {
            AIResponse errorResponse = new AIResponse();
            errorResponse.setMessage("生成失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 语言润色
     */
    @PostMapping("/improve")
    public ResponseEntity<AIResponse> improve(@RequestBody AIRequest request) {
        try {
            String result = enhancedAIService.improveContent(request.getContent(), request.getStyle());
            return ResponseEntity.ok(new AIResponse(result, enhancedAIService.getCurrentProvider()));
        } catch (Exception e) {
            AIResponse errorResponse = new AIResponse();
            errorResponse.setMessage("润色失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 语法检查
     */
    @PostMapping("/check-grammar")
    public ResponseEntity<AIResponse> checkGrammar(@RequestBody AIRequest request) {
        logger.info("收到语法检查请求，内容长度: {}", request.getContent() != null ? request.getContent().length() : 0);
        try {
            String result = enhancedAIService.checkGrammar(request.getContent());
            logger.info("语法检查请求处理成功");
            return ResponseEntity.ok(new AIResponse(result, enhancedAIService.getCurrentProvider()));
        } catch (Exception e) {
            logger.error("语法检查请求处理失败", e);
            AIResponse errorResponse = new AIResponse();
            errorResponse.setMessage("检查失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 生成摘要
     */
    @PostMapping("/summarize")
    public ResponseEntity<AIResponse> summarize(@RequestBody AIRequest request) {
        logger.info("收到生成摘要请求，内容长度: {}", request.getContent() != null ? request.getContent().length() : 0);
        try {
            String result = enhancedAIService.summarizeDocument(request.getContent());
            logger.info("生成摘要请求处理成功");
            return ResponseEntity.ok(new AIResponse(result, enhancedAIService.getCurrentProvider()));
        } catch (Exception e) {
            logger.error("生成摘要请求处理失败", e);
            AIResponse errorResponse = new AIResponse();
            errorResponse.setMessage("生成摘要失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 翻译文档
     */
    @PostMapping("/translate")
    public ResponseEntity<AIResponse> translate(@RequestBody AIRequest request) {
        String targetLang = request.getTargetLang() != null ? request.getTargetLang() : "英文";
        logger.info("收到翻译请求，目标语言: {}，内容长度: {}", targetLang, request.getContent() != null ? request.getContent().length() : 0);
        try {
            String result = enhancedAIService.translateDocument(request.getContent(), targetLang);
            logger.info("翻译请求处理成功");
            return ResponseEntity.ok(new AIResponse(result, enhancedAIService.getCurrentProvider()));
        } catch (Exception e) {
            logger.error("翻译请求处理失败", e);
            AIResponse errorResponse = new AIResponse();
            errorResponse.setMessage("翻译失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 解释代码
     */
    @PostMapping("/explain-code")
    public ResponseEntity<AIResponse> explainCode(@RequestBody AIRequest request) {
        logger.info("收到解释代码请求，语言: {}，代码长度: {}", request.getLanguage(), request.getContent() != null ? request.getContent().length() : 0);
        try {
            String result = enhancedAIService.explainCode(request.getContent(), request.getLanguage());
            logger.info("解释代码请求处理成功");
            return ResponseEntity.ok(new AIResponse(result, enhancedAIService.getCurrentProvider()));
        } catch (Exception e) {
            logger.error("解释代码请求处理失败", e);
            AIResponse errorResponse = new AIResponse();
            errorResponse.setMessage("解释失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 智能补全
     */
    @PostMapping("/complete")
    public ResponseEntity<AIResponse> complete(@RequestBody AIRequest request) {
        logger.info("收到智能补全请求，内容长度: {}", request.getContent() != null ? request.getContent().length() : 0);
        try {
            String result = enhancedAIService.suggestCompletion(request.getContent());
            logger.info("智能补全请求处理成功");
            return ResponseEntity.ok(new AIResponse(result, enhancedAIService.getCurrentProvider()));
        } catch (Exception e) {
            logger.error("智能补全请求处理失败", e);
            AIResponse errorResponse = new AIResponse();
            errorResponse.setMessage("补全失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 扩展段落
     */
    @PostMapping("/expand")
    public ResponseEntity<AIResponse> expand(@RequestBody AIRequest request) {
        logger.info("收到扩展段落请求，内容长度: {}", request.getContent() != null ? request.getContent().length() : 0);
        try {
            String result = enhancedAIService.expandParagraph(request.getContent());
            logger.info("扩展段落请求处理成功");
            return ResponseEntity.ok(new AIResponse(result, enhancedAIService.getCurrentProvider()));
        } catch (Exception e) {
            logger.error("扩展段落请求处理失败", e);
            AIResponse errorResponse = new AIResponse();
            errorResponse.setMessage("扩展失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 生成列表
     */
    @PostMapping("/generate-list")
    public ResponseEntity<AIResponse> generateList(@RequestBody AIRequest request) {
        logger.info("收到生成列表请求，主题: {}", request.getTopic());
        try {
            String result = enhancedAIService.generateList(request.getTopic());
            logger.info("生成列表请求处理成功");
            return ResponseEntity.ok(new AIResponse(result, enhancedAIService.getCurrentProvider()));
        } catch (Exception e) {
            logger.error("生成列表请求处理失败", e);
            AIResponse errorResponse = new AIResponse();
            errorResponse.setMessage("生成列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 优化标题
     */
    @PostMapping("/optimize-titles")
    public ResponseEntity<AIResponse> optimizeTitles(@RequestBody AIRequest request) {
        logger.info("收到优化标题请求，内容长度: {}", request.getContent() != null ? request.getContent().length() : 0);
        try {
            String result = enhancedAIService.optimizeTitles(request.getContent());
            logger.info("优化标题请求处理成功");
            return ResponseEntity.ok(new AIResponse(result, enhancedAIService.getCurrentProvider()));
        } catch (Exception e) {
            logger.error("优化标题请求处理失败", e);
            AIResponse errorResponse = new AIResponse();
            errorResponse.setMessage("优化标题失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 生成表格
     */
    @PostMapping("/generate-table")
    public ResponseEntity<AIResponse> generateTable(@RequestBody AIRequest request) {
        logger.info("收到生成表格请求，描述长度: {}", request.getDescription() != null ? request.getDescription().length() : 0);
        try {
            String result = enhancedAIService.generateTable(request.getDescription());
            logger.info("生成表格请求处理成功");
            return ResponseEntity.ok(new AIResponse(result, enhancedAIService.getCurrentProvider()));
        } catch (Exception e) {
            logger.error("生成表格请求处理失败", e);
            AIResponse errorResponse = new AIResponse();
            errorResponse.setMessage("生成表格失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 文档问答
     */
    @PostMapping("/qa")
    public ResponseEntity<AIResponse> answerQuestion(@RequestBody AIRequest request) {
        logger.info("收到文档问答请求，问题: {}，文档长度: {}", request.getQuestion(), request.getContent() != null ? request.getContent().length() : 0);
        try {
            String result = enhancedAIService.answerQuestion(request.getContent(), request.getQuestion());
            logger.info("文档问答请求处理成功");
            return ResponseEntity.ok(new AIResponse(result, enhancedAIService.getCurrentProvider()));
        } catch (Exception e) {
            logger.error("文档问答请求处理失败", e);
            AIResponse errorResponse = new AIResponse();
            errorResponse.setMessage("问答失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 分析文档
     */
    @PostMapping("/analyze")
    public ResponseEntity<AIResponse> analyze(@RequestBody AIRequest request) {
        logger.info("收到分析文档请求，内容长度: {}", request.getContent() != null ? request.getContent().length() : 0);
        try {
            Map<String, Object> analysis = enhancedAIService.analyzeDocument(request.getContent());
            logger.info("分析文档请求处理成功");
            AIResponse response = new AIResponse();
            response.setData(analysis);
            response.setProvider(enhancedAIService.getCurrentProvider());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("分析文档请求处理失败", e);
            AIResponse errorResponse = new AIResponse();
            errorResponse.setMessage("分析失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 切换AI服务提供商
     */
    @PostMapping("/switch-provider")
    public ResponseEntity<AIResponse> switchProvider(@RequestBody AIRequest request) {
        logger.info("收到切换AI服务请求，目标服务: {}", request.getProvider());
        try {
            enhancedAIService.switchProvider(request.getProvider());
            Map<String, Object> data = new HashMap<>();
            data.put("provider", enhancedAIService.getCurrentProvider());
            logger.info("AI服务切换成功，当前服务: {}", enhancedAIService.getCurrentProvider());
            AIResponse response = new AIResponse();
            response.setData(data);
            response.setMessage("切换成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("AI服务切换失败", e);
            AIResponse errorResponse = new AIResponse();
            errorResponse.setMessage("切换失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取当前AI服务提供商
     */
    @GetMapping("/provider")
    public ResponseEntity<AIResponse> getCurrentProvider() {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("provider", enhancedAIService.getCurrentProvider());
            AIResponse response = new AIResponse();
            response.setData(data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            AIResponse errorResponse = new AIResponse();
            errorResponse.setMessage("获取失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 流式输出（SSE）：用于长文本的打字机效果，真正的实时流式传输
     * 使用 POST 请求避免 URL 长度限制
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestBody Map<String, Object> requestBody) {
        String action = (String) requestBody.get("action");
        String content = (String) requestBody.get("content");
        Double temperature = requestBody.get("temperature") != null ? 
            (requestBody.get("temperature") instanceof Number ? 
                ((Number) requestBody.get("temperature")).doubleValue() : 
                Double.parseDouble(requestBody.get("temperature").toString())) : null;
        Integer maxTokens = requestBody.get("maxTokens") != null ? 
            (requestBody.get("maxTokens") instanceof Number ? 
                ((Number) requestBody.get("maxTokens")).intValue() : 
                Integer.parseInt(requestBody.get("maxTokens").toString())) : null;
        String model = (String) requestBody.get("model");
        Boolean stream = requestBody.get("stream") != null ? 
            (Boolean) requestBody.get("stream") : true;
        String style = (String) requestBody.get("style");
        String targetLang = (String) requestBody.get("targetLang");
        logger.info("收到流式请求 action={} 内容长度={} temp={} maxTokens={} model={} stream={} style={} targetLang={}",
                action, content != null ? content.length() : 0, temperature, maxTokens, model, stream, style, targetLang);
        SseEmitter emitter = new SseEmitter(0L);
        CompletableFuture.runAsync(() -> {
            long start = System.currentTimeMillis();
            try {
                // 使用真正的流式传输，每收到一个chunk就立即发送给前端
                String result = enhancedAIService.routeActionStream(
                    action, content, temperature, maxTokens, model, style, targetLang,
                    chunk -> {
                        // 回调函数：每收到一个chunk就立即通过SSE发送给前端
                        try {
                            // 调试：检查chunk中的空格
                            boolean hasSpace = chunk.contains(" ");
                            boolean hasNewline = chunk.contains("\n");
                            boolean hasCarriageReturn = chunk.contains("\r");
                            
                            // 调试日志：如果包含#号或空格，记录详细信息
                            if (chunk.contains("#") || hasSpace) {
                                logger.debug("后端发送chunk - 原始内容: [{}]", chunk);
                                logger.debug("后端发送chunk - 包含空格: {}, 包含换行: {}, 包含回车: {}", 
                                    hasSpace, hasNewline, hasCarriageReturn);
                                logger.debug("后端发送chunk - 长度: {}, JSON表示: {}", 
                                    chunk.length(), chunk.replace(" ", "·")); // 用·表示空格便于查看
                            }
                            
                            String encodedChunk;
                            // 为了确保空格不被丢失，所有chunk都使用JSON编码
                            // 这样可以保证所有字符（包括空格）都能正确传输
                            encodedChunk = chunk
                                .replace("\\", "\\\\")  // 先转义反斜杠
                                .replace("\"", "\\\"")  // 转义双引号
                                .replace("\n", "\\n")   // 转义换行符
                                .replace("\r", "\\r")   // 转义回车符
                                .replace("\t", "\\t");  // 转义制表符
                            // 包装在JSON字符串中，前端需要解析
                            encodedChunk = "\"" + encodedChunk + "\"";
                            
                            if (chunk.contains("#") || hasSpace) {
                                logger.debug("后端发送chunk - JSON编码后: [{}]", encodedChunk);
                                logger.debug("后端发送chunk - 编码前长度: {}, 编码后长度: {}", chunk.length(), encodedChunk.length());
                            }
                            
                            // 发送编码后的数据
                            emitter.send(SseEmitter.event().name("chunk").data(encodedChunk));
                        } catch (Exception e) {
                            logger.error("发送chunk到前端失败", e);
                            throw new RuntimeException("发送chunk失败", e);
                        }
                    }
                );
                
                long cost = System.currentTimeMillis() - start;
                logger.info("流式传输完成，总耗时: {} ms，最终内容长度: {} 字符", cost, result != null ? result.length() : 0);
                emitter.send(SseEmitter.event().name("end").data("{\"done\":true,\"cost\":" + cost + "}"));
                emitter.complete();
            } catch (Exception e) {
                logger.error("流式请求处理失败", e);
                try {
                    emitter.send(SseEmitter.event().name("error").data(e.getMessage()));
                } catch (Exception ignored) {}
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }
}

