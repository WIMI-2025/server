package com.wimi.miro.controller;

import com.wimi.miro.dto.request.AnalysisRequest;
import com.wimi.miro.dto.response.AnalysisResponse;
import com.wimi.miro.service.AnalysisService;
import com.wimi.miro.service.ChatContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;
    private final ChatContextService chatContextService;

    @Autowired
    public AnalysisController(AnalysisService analysisService, ChatContextService chatContextService) {
        this.analysisService = analysisService;
        this.chatContextService = chatContextService;
    }

    /**
     * 채팅 스크린샷 분석
     */
    @PostMapping
    public ResponseEntity<AnalysisResponse> analyzeChat(@RequestBody AnalysisRequest request) {
        AnalysisResponse response = analysisService.analyzeChat(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 채팅 스크린샷 분석 후 채팅 세션 생성
     */
    @PostMapping("/create-chat")
    public ResponseEntity<Map<String, Object>> analyzeChatAndCreateSession(
            @RequestBody AnalysisRequest request,
            @RequestParam("user_id") String userId) {

        try {
            // 채팅 스크린샷 분석
            AnalysisResponse analysis = analysisService.analyzeChat(request);

            // 분석 결과를 바탕으로 새 채팅 세션 생성
            String chatId = chatContextService.createChatFromAnalysis(
                    userId,
                    analysis.getName(),
                    analysis.getRelationship(),
                    analysis.getSituation(),
                    request.getImageUrl()
            );

            // 초기 AI 메시지 저장 (분석 결과에서 추천 답장)
            if (analysis.getMessageResponse() != null && !analysis.getMessageResponse().isEmpty()) {
                chatContextService.addAssistantMessage(chatId, analysis.getMessageResponse());
            }

            // 응답 구성
            Map<String, Object> response = new HashMap<>();
            response.put("chat_id", chatId);
            response.put("analysis", analysis);

            return ResponseEntity.ok(response);

        } catch (ExecutionException | InterruptedException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "분석 및 채팅 생성 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 디버깅용 이미지 정보 확인
     */
    @PostMapping("/debug")
    public ResponseEntity<Map<String, String>> debugAnalysis(@RequestParam("image") MultipartFile image) {
        try {
            Map<String, String> response = new HashMap<>();
            response.put("filename", image.getOriginalFilename());
            response.put("contentType", image.getContentType());
            response.put("size", String.valueOf(image.getSize()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}