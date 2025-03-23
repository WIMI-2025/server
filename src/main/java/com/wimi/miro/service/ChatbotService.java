package com.wimi.miro.service;

import com.google.cloud.Timestamp;
import com.wimi.miro.config.OpenAIConfig;
import com.wimi.miro.dto.openai.*;
import com.wimi.miro.dto.request.AnalysisRequest;
import com.wimi.miro.dto.request.ChatRequest;
import com.wimi.miro.dto.response.AnalysisResponse;
import com.wimi.miro.dto.response.ChatResponse;
import com.wimi.miro.enums.MessageType;
import com.wimi.miro.model.Chat;
import com.wimi.miro.model.Message;
import com.wimi.miro.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class ChatbotService {
    private final ChatRepository chatRepository;
    private final OpenAIConfig openAIConfig;

    /**
     * 채팅 스크린샷 분석 메서드
     * 이미지를 분석하여 상황에 맞는 답장을 추천
     *
     * @param analysisRequest 분석 요청 객체
     * @return 분석 결과 응답
     */
    public AnalysisResponse analysis(AnalysisRequest analysisRequest) throws ExecutionException, InterruptedException {
        // 1. 새 Chat 생성
        Chat chat = Chat.builder()
                .chatName(analysisRequest.getRelationship() + " " + analysisRequest.getName() + "와(과)의 대화")
                .build();
        String chatId = chatRepository.saveChat(chat);

        // 2. 시스템 메시지 생성
        String systemPrompt = buildAnalysisSystemPrompt(analysisRequest);

        // 3. GPT 요청 생성
        ChatGPTRequest gptRequest = new ChatGPTRequest();
        List<com.wimi.miro.dto.openai.Message> messages = new ArrayList<>();

        // 시스템 메시지 추가
        messages.add(new TextMessage("system", systemPrompt));

        // 사용자 메시지 추가 (멀티모달 - 텍스트와 이미지)
        List<Content> userContents = new ArrayList<>();
        userContents.add(new TextContent("text", "스크린샷 분석"));
        userContents.add(new ImageContent("image_url", new ImageUrl(analysisRequest.getImageUrl())));

        messages.add(new MultimodalMessage("user", userContents));
        gptRequest.setMessages(messages);

        // 4. API 호출
        OpenAIChatDefaultResponse gptResponse = openAIConfig.OpenAiClient().post()
                .bodyValue(gptRequest)
                .retrieve()
                .bodyToMono(OpenAIChatDefaultResponse.class)
                .block();

        // 5. 응답 처리
        if (gptResponse == null) {
            throw new RuntimeException("GPT API 호출에 실패했습니다.");
        }
        String responseContent = gptResponse.getChoices().getFirst().getMessage().getContent();

        // 6. 메시지 저장
        // 사용자 메시지 저장
        Message userMsgEntity = Message.builder()
                .content("이미지 분석 요청: " + analysisRequest.getImageUrl())
                .type(MessageType.IMAGE)
                .isUserMessage(true)
                .build();
        chatRepository.saveMessage(chatId, userMsgEntity);

        // AI 응답 저장
        Message aiMsgEntity = Message.builder()
                .content(responseContent)
                .type(MessageType.TEXT)
                .isUserMessage(false)
                .build();
        chatRepository.saveMessage(chatId, aiMsgEntity);

        // 7. 응답 반환
        return AnalysisResponse.builder()
                .messageResponse(responseContent)
                .chatId(chatId)
                .build();
    }

    /**
     * 채팅 답장 추천 메서드
     * 이전 채팅 내역과 새로운 메시지를 분석하여 답장을 추천
     *
     * @param chatRequest 채팅 요청 객체
     * @return 채팅 응답
     */
    public ChatResponse chat(ChatRequest chatRequest) throws ExecutionException, InterruptedException {
        // 채팅 ID 확인
        String chatId = chatRequest.getChatId();
        Chat chat = chatRepository.findChatById(chatId);
        if (chat == null) {
            throw new IllegalArgumentException("존재하지 않는 대화 ID입니다: " + chatId);
        }

        // updatedAt 업데이트
        chatRepository.updateChat(chat);

        // 1. 이전 메시지 처리
        List<Message> previousMessages = chatRepository.findMessagesByChatId(chatId);

        // 2. 시스템 메시지 준비
        String systemPrompt = buildChatSystemPrompt(chatRequest);

        // 3. GPT 요청 생성
        ChatGPTRequest gptRequest = new ChatGPTRequest();
        gptRequest.setModel("gpt-4o"); // 멀티모달 모델 사용
        List<com.wimi.miro.dto.openai.Message> messages = new ArrayList<>();

        // 시스템 메시지 추가
        messages.add(new TextMessage("system", systemPrompt));

        // 이전 메시지 추가 (최대 10개)
        int startIdx = Math.max(0, previousMessages.size() - 10);
        for (int i = startIdx; i < previousMessages.size(); i++) {
            Message msg = previousMessages.get(i);

            if (msg.getType() == MessageType.TEXT) {
                // 텍스트 메시지 처리
                messages.add(new TextMessage(
                        msg.isUserMessage() ? "user" : "assistant",
                        msg.getContent()
                ));
            } else if (msg.getType() == MessageType.IMAGE && msg.isUserMessage()) {
                // 이미지 메시지 처리 (사용자 메시지만)
                List<Content> contents = new ArrayList<>();
                contents.add(new ImageContent("image_url", new ImageUrl(msg.getContent())));
                messages.add(new MultimodalMessage("user", contents));
            }
        }

        // 새 사용자 메시지 추가
        MessageType messageType = MessageType.valueOf(chatRequest.getMessageType().toUpperCase());

        if (messageType == MessageType.IMAGE) {
            // 이미지 메시지 처리
            List<Content> userContents = new ArrayList<>();

            // 이미지와 함께 텍스트도 추가 (있는 경우)
            if (chatRequest.getMessageRequest() != null && !chatRequest.getMessageRequest().isEmpty()) {
                userContents.add(new TextContent("text", chatRequest.getMessageRequest()));
            }

            userContents.add(new ImageContent("image_url", new ImageUrl(chatRequest.getImageUrl())));
            messages.add(new MultimodalMessage("user", userContents));
        } else {
            // 텍스트 메시지 처리
            messages.add(new TextMessage("user", chatRequest.getMessageRequest()));
        }

        gptRequest.setMessages(messages);

        // 4. API 호출
        OpenAIChatDefaultResponse gptResponse = openAIConfig.OpenAiClient().post()
                .bodyValue(gptRequest)
                .retrieve()
                .bodyToMono(OpenAIChatDefaultResponse.class)
                .block();

        // 5. 응답 처리
        if (gptResponse == null) {
            throw new RuntimeException("GPT API 호출에 실패했습니다.");
        }
        String responseContent = gptResponse.getChoices().getFirst().getMessage().getContent();

        // 6. 메시지 저장
        // 사용자 메시지 저장
        Message userMsgEntity = Message.builder()
                .content(messageType == MessageType.IMAGE ? chatRequest.getImageUrl() : chatRequest.getMessageRequest())
                .type(messageType)
                .isUserMessage(true)
                .build();
        chatRepository.saveMessage(chatId, userMsgEntity);

        // AI 응답 저장
        Message aiMsgEntity = Message.builder()
                .content(responseContent)
                .type(MessageType.TEXT)
                .isUserMessage(false)
                .build();
        chatRepository.saveMessage(chatId, aiMsgEntity);

        // 7. 응답 반환
        return ChatResponse.builder()
                .messageResponse(responseContent)
                .chatId(chatId)
                .build();
    }

    /**
     * 스크린샷 분석을 위한 시스템 프롬프트 구성
     *
     * @param request 분석 요청
     * @return 시스템 프롬프트
     */
    private String buildAnalysisSystemPrompt(AnalysisRequest request) {
        return String.format("""
            당신은 'WIMI(Replies That Fit with Me)'라는 메시지 답장 도우미 AI입니다.
            사용자가 제공한 채팅 스크린샷을 분석하고, 상황에 적절한 답장을 추천해주세요.
            
            상대방 이름: %s
            관계: %s
            상황: %s
            
            스크린샷을 분석하여 다음 정보를 파악하세요:
            1. 대화의 전반적인 맥락과 분위기
            2. 상대방의 마지막 메시지의 의도와 감정
            3. 상황과 관계에 맞는 적절한 답장 방향
            
            답장 추천 시 다음을 고려하세요:
            - 대화의 맥락과 흐름을 유지
            - 관계와 상황에 맞는 적절한 톤과 공손함 유지
            - 필요한 경우 3개 정도의 답장 옵션 제공 (격식/비격식, 긴/짧은 답장 등)
            
            답변 형식:
            [분석] 대화 맥락과 상대방 의도 분석
            [추천 답장] 상황에 맞는 답장 추천
            """,
                request.getName(),
                request.getRelationship(),
                request.getSituation()
        );
    }

    /**
     * 채팅 요청을 위한 시스템 프롬프트 구성
     *
     * @param request 채팅 요청
     * @return 시스템 프롬프트
     */
    private String buildChatSystemPrompt(ChatRequest request) {
        return String.format("""
            당신은 'WIMI(Replies That Fit with Me)'라는 메시지 답장 도우미 AI입니다.
            사용자가 제공한 메시지와 맥락을 바탕으로 상황에 적절한 답장을 추천해주세요.
            
            관계: %s
            
            답장 추천 시 다음을 고려하세요:
            - 대화의 맥락과 흐름을 유지
            - 관계와 상황에 맞는 적절한 톤과 공손함 유지
            - 필요한 경우 여러 답장 옵션 제공 (격식/비격식, 긴/짧은 답장 등)
            
            답변 형식:
            [분석] 대화 맥락과 상대방 의도 분석
            [추천 답장] 상황에 맞는 답장 추천
            """,
                request.getRelationship() != null ? request.getRelationship() : "일반적인 관계"
        );
    }
}