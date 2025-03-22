package com.wimi.miro.controller;

import com.wimi.miro.dto.request.ChatbotRequest;
import com.wimi.miro.dto.response.ChatbotResponse;
import com.wimi.miro.model.Message;
import com.wimi.miro.service.ChatContextService;
import com.wimi.miro.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final ChatContextService chatContextService;

    @Autowired
    public ChatbotController(ChatbotService chatbotService, ChatContextService chatContextService) {
        this.chatbotService = chatbotService;
        this.chatContextService = chatContextService;
    }

    /**
     * 기존 메서드: 단순 답장 추천 (맥락 없음)
     */
    @PostMapping("/reply")
    public ResponseEntity<ChatbotResponse> getReplyRecommendation(@RequestBody ChatbotRequest request) {
        ChatbotResponse response = chatbotService.getReplyRecommendation(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 이미지 기반 답장 추천 (맥락 없음)
     */
    @PostMapping(value = "/reply/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatbotResponse> getReplyRecommendationWithImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "context", required = false, defaultValue = "") String context,
            @RequestParam(value = "relationship", required = false, defaultValue = "알 수 없음") String relationship) {

        ChatbotResponse response = chatbotService.getReplyRecommendationWithImage(image, context, relationship);
        return ResponseEntity.ok(response);
    }

    /**
     * 맥락 유지 채팅 - 메시지 전송
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> sendChatMessage(
            @RequestParam("chat_id") String chatId,
            @RequestParam("user_id") String userId,
            @RequestBody ChatbotRequest request) {

        try {
            // 사용자 메시지 저장
            chatContextService.addUserMessage(chatId, request.getMessageRequest());

            // 채팅 맥락 가져오기 (최근 10개 메시지)
            String chatContext = chatContextService.buildChatContext(chatId, 10);

            // 맥락을 포함한 요청 처리
            ChatbotResponse response = chatbotService.getChatReplyWithContext(chatContext, request.getMessageRequest());

            // 응답 저장
            String responseText = response.getMessageResponse();
            chatContextService.addAssistantMessage(chatId, responseText);

            // 최신 대화 기록 반환
            List<Message> chatHistory = chatContextService.getChatHistory(chatId);

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("message_response", responseText);
            responseMap.put("chat_history", chatHistory);

            return ResponseEntity.ok(responseMap);

        } catch (ExecutionException | InterruptedException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "채팅 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 새 채팅 세션 생성
     */
    @PostMapping("/chat/new")
    public ResponseEntity<Map<String, Object>> createNewChat(
            @RequestParam("user_id") String userId,
            @RequestParam("chat_name") String chatName) {

        try {
            String chatId = chatContextService.createNewChat(userId, chatName, "");

            Map<String, Object> response = new HashMap<>();
            response.put("chat_id", chatId);
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (ExecutionException | InterruptedException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "채팅 생성 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 채팅 기록 조회
     */
    @GetMapping("/chat/{chatId}/history")
    public ResponseEntity<Map<String, Object>> getChatHistory(
            @PathVariable("chatId") String chatId) {

        try {
            List<Message> chatHistory = chatContextService.getChatHistory(chatId);

            Map<String, Object> response = new HashMap<>();
            response.put("chat_id", chatId);
            response.put("messages", chatHistory);

            return ResponseEntity.ok(response);

        } catch (ExecutionException | InterruptedException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "채팅 기록 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 디버깅용 엔드포인트
     */
    @PostMapping("/debug")
    public ResponseEntity<Map<String, String>> debugChatbot(@RequestBody ChatbotRequest request) {
        Map<String, String> debugInfo = new HashMap<>();
        debugInfo.put("messageType", request.getMessageType());
        debugInfo.put("messageRequest", request.getMessageRequest());
        debugInfo.put("status", "요청이 성공적으로 수신되었습니다.");
        return ResponseEntity.ok(debugInfo);
    }
}