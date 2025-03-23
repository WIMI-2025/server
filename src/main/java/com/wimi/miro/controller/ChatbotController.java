package com.wimi.miro.controller;

import com.wimi.miro.dto.request.AnalysisRequest;
import com.wimi.miro.dto.request.ChatRequest;
import com.wimi.miro.dto.response.AnalysisResponse;
import com.wimi.miro.dto.response.ChatResponse;
import com.wimi.miro.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
public class ChatbotController {
    private final ChatbotService chatbotService;

    @PostMapping("/analysis")
    public ResponseEntity<AnalysisResponse> analysis(
            @RequestBody AnalysisRequest analysisRequest
    ) throws ExecutionException, InterruptedException {
        AnalysisResponse response = chatbotService.analysis(analysisRequest);
        return ResponseEntity.ok(response); // 200 OK와 함께 리턴
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @RequestBody ChatRequest chatRequest
    ) throws ExecutionException, InterruptedException {
        ChatResponse response = chatbotService.chat(chatRequest);
        return ResponseEntity.ok(response); // 200 OK와 함께 리턴
    }

}
