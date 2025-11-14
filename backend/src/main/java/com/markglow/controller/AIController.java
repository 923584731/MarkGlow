package com.markglow.controller;

import com.markglow.dto.AIRequest;
import com.markglow.dto.AIResponse;
import com.markglow.service.ai.EnhancedAIService;
import com.markglow.service.AIStatisticsService;
import com.markglow.service.ai.AIServiceFactory;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AIController {

    @Autowired
    private EnhancedAIService enhancedAIService;

    @Autowired
    private AIStatisticsService statisticsService;

    @Autowired
    private AIServiceFactory aiServiceFactory;

    /**
     * 统一记录AI使用统计的辅助方法
     */
    private void recordAIUsage(String action, String content, String result, 
                              String model, long duration) {
        try {
            String provider = aiServiceFactory.getCurrentProvider();
            String finalModel = model != null ? model : 
                (provider.equals("ernie") ? "ernie-4.5-turbo-128k" : "qwen-3-235b-a22b");
            // 估算token数：中文约1.5字符/token，英文约4字符/token，这里简化处理为字符数/2
            int inputTokens = content != null ? (int)(content.length() / 2.0) : 0;
            int outputTokens = result != null ? (int)(result.length() / 2.0) : 0;
            statisticsService.recordUsage(action, provider, finalModel, 
                                         inputTokens, outputTokens, duration);
        } catch (Exception e) {
            log.warn("记录统计信息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * AI美化Markdown
     */
    @PostMapping("/beautify")
    public ResponseEntity<AIResponse> beautify(@RequestBody AIRequest request) {
        log.info("收到AI美化请求，内容长度: {}", request.getContent() != null ? request.getContent().length() : 0);
        long startTime = System.currentTimeMillis();
        try {
            String result = enhancedAIService.beautifyMarkdown(
                    request.getContent(),
                    request.getTemperature(),
                    request.getMaxTokens(),
                    null,
                    request.getModel());
            long duration = System.currentTimeMillis() - startTime;
            log.info("AI美化请求处理成功");
            // 记录统计信息
            recordAIUsage("beautify", request.getContent(), result, request.getModel(), duration);
            return ResponseEntity.ok(new AIResponse(result, enhancedAIService.getCurrentProvider()));
        } catch (Exception e) {
            log.error("AI美化请求处理失败", e);
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
        long startTime = System.currentTimeMillis();
        String content = request.getTitle() + (request.getContext() != null ? "\n" + request.getContext() : "");
        try {
            String result = enhancedAIService.generateContent(
                    request.getTitle(),
                    request.getContext(),
                    request.getTemperature(),
                    request.getMaxTokens(),
                    request.getModel());
            long duration = System.currentTimeMillis() - startTime;
            recordAIUsage("generate", content, result, request.getModel(), duration);
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
        long startTime = System.currentTimeMillis();
        try {
            String result = enhancedAIService.improveContent(
                    request.getContent(),
                    request.getStyle(),
                    request.getTemperature(),
                    request.getMaxTokens(),
                    null,
                    request.getModel());
            long duration = System.currentTimeMillis() - startTime;
            recordAIUsage("improve", request.getContent(), result, request.getModel(), duration);
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
        log.info("收到语法检查请求，内容长度: {}", request.getContent() != null ? request.getContent().length() : 0);
        long startTime = System.currentTimeMillis();
        try {
            String result = enhancedAIService.checkGrammar(
                    request.getContent(),
                    request.getTemperature(),
                    request.getMaxTokens(),
                    null,
                    request.getModel());
            long duration = System.currentTimeMillis() - startTime;
            log.info("语法检查请求处理成功");
            recordAIUsage("checkGrammar", request.getContent(), result, request.getModel(), duration);
            return ResponseEntity.ok(new AIResponse(result, enhancedAIService.getCurrentProvider()));
        } catch (Exception e) {
            log.error("语法检查请求处理失败", e);
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
        log.info("收到生成摘要请求，内容长度: {}", request.getContent() != null ? request.getContent().length() : 0);
        long startTime = System.currentTimeMillis();
        try {
            String result = enhancedAIService.summarizeDocument(
                    request.getContent(),
                    request.getTemperature(),
                    request.getMaxTokens(),
                    null,
                    request.getModel());
            long duration = System.currentTimeMillis() - startTime;
            log.info("生成摘要请求处理成功");
            recordAIUsage("summarize", request.getContent(), result, request.getModel(), duration);
            return ResponseEntity.ok(new AIResponse(result, enhancedAIService.getCurrentProvider()));
        } catch (Exception e) {
            log.error("生成摘要请求处理失败", e);
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
        log.info("收到翻译请求，目标语言: {}，内容长度: {}", targetLang, request.getContent() != null ? request.getContent().length() : 0);
        long startTime = System.currentTimeMillis();
        try {
            String result = enhancedAIService.translateDocument(
                    request.getContent(),
                    targetLang,
                    request.getTemperature(),
                    request.getMaxTokens(),
                    null,
                    request.getModel());
            long duration = System.currentTimeMillis() - startTime;
            log.info("翻译请求处理成功");
            recordAIUsage("translate", request.getContent(), result, request.getModel(), duration);
            return ResponseEntity.ok(new AIResponse(result, enhancedAIService.getCurrentProvider()));
        } catch (Exception e) {
            log.error("翻译请求处理失败", e);
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
        log.info("收到解释代码请求，语言: {}，代码长度: {}", request.getLanguage(), request.getContent() != null ? request.getContent().length() : 0);
        long startTime = System.currentTimeMillis();
        try {
            String result = enhancedAIService.explainCode(
                    request.getContent(),
                    request.getLanguage(),
                    request.getTemperature(),
                    request.getMaxTokens(),
                    request.getModel());
            long duration = System.currentTimeMillis() - startTime;
            log.info("解释代码请求处理成功");
            recordAIUsage("explainCode", request.getContent(), result, request.getModel(), duration);
            return ResponseEntity.ok(new AIResponse(result, enhancedAIService.getCurrentProvider()));
        } catch (Exception e) {
            log.error("解释代码请求处理失败", e);
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
        log.info("收到智能补全请求，内容长度: {}", request.getContent() != null ? request.getContent().length() : 0);
        long startTime = System.currentTimeMillis();
        try {
            String result = enhancedAIService.suggestCompletion(
                    request.getContent(),
                    request.getTemperature(),
                    request.getMaxTokens(),
                    null,
                    request.getModel());
            long duration = System.currentTimeMillis() - startTime;
            log.info("智能补全请求处理成功");
            recordAIUsage("complete", request.getContent(), result, request.getModel(), duration);
            return ResponseEntity.ok(new AIResponse(result, enhancedAIService.getCurrentProvider()));
        } catch (Exception e) {
            log.error("智能补全请求处理失败", e);
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
        log.info("收到扩展段落请求，内容长度: {}", request.getContent() != null ? request.getContent().length() : 0);
        long startTime = System.currentTimeMillis();
        try {
            String result = enhancedAIService.expandParagraph(
                    request.getContent(),
                    request.getTemperature(),
                    request.getMaxTokens(),
                    null,
                    request.getModel());
            long duration = System.currentTimeMillis() - startTime;
            log.info("扩展段落请求处理成功");
            recordAIUsage("expand", request.getContent(), result, request.getModel(), duration);
            return ResponseEntity.ok(new AIResponse(result, enhancedAIService.getCurrentProvider()));
        } catch (Exception e) {
            log.error("扩展段落请求处理失败", e);
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
        log.info("收到生成列表请求，主题: {}", request.getTopic());
        long startTime = System.currentTimeMillis();
        String content = request.getTopic() != null ? request.getTopic() : "";
        try {
            String result = enhancedAIService.generateList(
                    request.getTopic(),
                    request.getTemperature(),
                    request.getMaxTokens(),
                    request.getModel());
            long duration = System.currentTimeMillis() - startTime;
            log.info("生成列表请求处理成功");
            recordAIUsage("generateList", content, result, request.getModel(), duration);
            return ResponseEntity.ok(new AIResponse(result, enhancedAIService.getCurrentProvider()));
        } catch (Exception e) {
            log.error("生成列表请求处理失败", e);
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
        log.info("收到优化标题请求，内容长度: {}", request.getContent() != null ? request.getContent().length() : 0);
        long startTime = System.currentTimeMillis();
        try {
            String result = enhancedAIService.optimizeTitles(
                    request.getContent(),
                    request.getTemperature(),
                    request.getMaxTokens(),
                    request.getModel());
            long duration = System.currentTimeMillis() - startTime;
            log.info("优化标题请求处理成功");
            recordAIUsage("optimizeTitles", request.getContent(), result, request.getModel(), duration);
            return ResponseEntity.ok(new AIResponse(result, enhancedAIService.getCurrentProvider()));
        } catch (Exception e) {
            log.error("优化标题请求处理失败", e);
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
        log.info("收到生成表格请求，描述长度: {}", request.getDescription() != null ? request.getDescription().length() : 0);
        long startTime = System.currentTimeMillis();
        String content = request.getDescription() != null ? request.getDescription() : "";
        try {
            String result = enhancedAIService.generateTable(
                    request.getDescription(),
                    request.getTemperature(),
                    request.getMaxTokens(),
                    request.getModel());
            long duration = System.currentTimeMillis() - startTime;
            log.info("生成表格请求处理成功");
            recordAIUsage("generateTable", content, result, request.getModel(), duration);
            return ResponseEntity.ok(new AIResponse(result, enhancedAIService.getCurrentProvider()));
        } catch (Exception e) {
            log.error("生成表格请求处理失败", e);
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
        log.info("收到文档问答请求，问题: {}，文档长度: {}", request.getQuestion(), request.getContent() != null ? request.getContent().length() : 0);
        long startTime = System.currentTimeMillis();
        String content = (request.getContent() != null ? request.getContent() : "") + 
                        (request.getQuestion() != null ? "\n问题: " + request.getQuestion() : "");
        try {
            String result = enhancedAIService.answerQuestion(
                    request.getContent(),
                    request.getQuestion(),
                    request.getTemperature(),
                    request.getMaxTokens(),
                    request.getModel());
            long duration = System.currentTimeMillis() - startTime;
            log.info("文档问答请求处理成功");
            recordAIUsage("qa", content, result, request.getModel(), duration);
            return ResponseEntity.ok(new AIResponse(result, enhancedAIService.getCurrentProvider()));
        } catch (Exception e) {
            log.error("文档问答请求处理失败", e);
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
        log.info("收到分析文档请求，内容长度: {}", request.getContent() != null ? request.getContent().length() : 0);
        long startTime = System.currentTimeMillis();
        try {
            Map<String, Object> analysis = enhancedAIService.analyzeDocument(
                    request.getContent(),
                    request.getTemperature(),
                    request.getMaxTokens(),
                    request.getModel());
            long duration = System.currentTimeMillis() - startTime;
            log.info("分析文档请求处理成功");
            // 对于analyze接口，result是Map，转换为字符串用于统计
            String resultStr = analysis != null ? analysis.toString() : "";
            recordAIUsage("analyze", request.getContent(), resultStr, request.getModel(), duration);
            AIResponse response = new AIResponse();
            response.setData(analysis);
            response.setProvider(enhancedAIService.getCurrentProvider());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("分析文档请求处理失败", e);
            AIResponse errorResponse = new AIResponse();
            errorResponse.setMessage("分析失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 切换AI服务提供商
     */
 /*   @PostMapping("/switch-provider")
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
    }*/

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
        String title = (String) requestBody.get("title");
        String context = (String) requestBody.get("context");
        log.info("收到流式请求 action={} 内容长度={} temp={} maxTokens={} model={} stream={} style={} targetLang={} title={}",
                action, content != null ? content.length() : 0, temperature, maxTokens, model, stream, style, targetLang, title);
        SseEmitter emitter = new SseEmitter(0L);
        CompletableFuture.runAsync(() -> {
            long start = System.currentTimeMillis();
            try {
                // 使用真正的流式传输，每收到一个chunk就立即发送给前端
                String result = enhancedAIService.routeActionStream(
                    action, content, temperature, maxTokens, model, style, targetLang, title, context,
                    chunk -> {
                        // 回调函数：每收到一个chunk就立即通过SSE发送给前端
                        try {
                            // 调试：检查chunk中的空格
                            boolean hasSpace = chunk.contains(" ");
                            boolean hasNewline = chunk.contains("\n");
                            boolean hasCarriageReturn = chunk.contains("\r");
                            
                            // 调试日志：如果包含#号或空格，记录详细信息
                            if (chunk.contains("#") || hasSpace) {
                                log.debug("后端发送chunk - 原始内容: [{}]", chunk);
                                log.debug("后端发送chunk - 包含空格: {}, 包含换行: {}, 包含回车: {}", 
                                    hasSpace, hasNewline, hasCarriageReturn);
                                log.debug("后端发送chunk - 长度: {}, JSON表示: {}", 
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
                                log.debug("后端发送chunk - JSON编码后: [{}]", encodedChunk);
                                log.debug("后端发送chunk - 编码前长度: {}, 编码后长度: {}", chunk.length(), encodedChunk.length());
                            }
                            
                            // 发送编码后的数据
                            emitter.send(SseEmitter.event().name("chunk").data(encodedChunk));
                        } catch (Exception e) {
                            log.error("发送chunk到前端失败", e);
                            throw new RuntimeException("发送chunk失败", e);
                        }
                    }
                );
                
                long cost = System.currentTimeMillis() - start;
                log.info("流式传输完成，总耗时: {} ms，最终内容长度: {} 字符", cost, result != null ? result.length() : 0);
                
                // 记录统计信息
                try {
                    String provider = aiServiceFactory.getCurrentProvider();
                    String finalModel = model != null ? model : (provider.equals("ernie") ? "ernie-4.5-turbo-128k" : "qwen-3-235b-a22b");
                    // 估算token数：中文约1.5字符/token，英文约4字符/token，这里简化处理
                    int inputTokens = content != null ? (int)(content.length() / 2.0) : 0;
                    int outputTokens = result != null ? (int)(result.length() / 2.0) : 0;
                    statisticsService.recordUsage(action, provider, finalModel, inputTokens, outputTokens, cost);
                } catch (Exception e) {
                    log.warn("记录统计信息失败", e);
                }
                
                emitter.send(SseEmitter.event().name("end").data("{\"done\":true,\"cost\":" + cost + "}"));
                emitter.complete();
            } catch (Exception e) {
                log.error("流式请求处理失败", e);
                try {
                    emitter.send(SseEmitter.event().name("error").data(e.getMessage()));
                } catch (Exception ignored) {}
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }
}

