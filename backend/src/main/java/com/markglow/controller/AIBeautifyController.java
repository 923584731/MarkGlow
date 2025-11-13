package com.markglow.controller;

import com.markglow.dto.BeautifyRequest;
import com.markglow.dto.BeautifyResponse;
import com.markglow.service.AIBeautifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/beautify")
@CrossOrigin(origins = "*")
@Slf4j
public class AIBeautifyController {

    @Autowired
    private AIBeautifyService aiBeautifyService;

    @PostMapping
    public ResponseEntity<BeautifyResponse> beautifyMarkdown(@RequestBody BeautifyRequest request) {
        try {
            String beautified = aiBeautifyService.beautifyMarkdown(request.getContent());
            return ResponseEntity.ok(new BeautifyResponse(beautified));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BeautifyResponse("美化失败: " + e.getMessage()));
        }
    }
}

