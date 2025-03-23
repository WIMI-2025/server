package com.wimi.miro.service;

import com.google.cloud.Timestamp;
import com.wimi.miro.config.OpenAIConfig;
import com.wimi.miro.dto.openai.OpenAIChatDefaultResponse;
import com.wimi.miro.dto.openai.OpenAIChatMessage;
import com.wimi.miro.dto.openai.OpenAIChatRequest;
import com.wimi.miro.dto.request.AnalysisRequest;
import com.wimi.miro.dto.request.ChatRequest;
import com.wimi.miro.dto.response.AnalysisResponse;
import com.wimi.miro.dto.response.ChatResponse;
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

    // ChatbotService 클래스의 analysis 메서드
    public AnalysisResponse analysis(AnalysisRequest analysisRequest) throws ExecutionException, InterruptedException {
        // AnalysisRequest 객체를 이용하여 분석을 수행
        // 1. 새 Chat 생성
        Chat chat = Chat.builder()
                .chatName(analysisRequest.getRelationship() + analysisRequest.getName() + "와의 대화")
                .build();
        String chatId = chatRepository.saveChat(chat);

        // GPT에게 보낼 chat request 구성 (System Message + AnalysisRequest)
        // 2. 시스템 메시지 생성
        String systemPrompt = buildAnalysisSystemPrompt(analysisRequest);
        // 3. GPT 요청 생성
        OpenAIChatRequest gptRequest = new OpenAIChatRequest();
        List<OpenAIChatMessage> messages = new ArrayList<>();

        // 시스템 메시지 추가
        OpenAIChatMessage systemMessage = new OpenAIChatMessage();
        systemMessage.setRole("system");
        systemMessage.setContent(systemPrompt);
        messages.add(systemMessage);

        // 사용자 메시지 추가 (이미지 URL 포함)
        OpenAIChatMessage userMessage = new OpenAIChatMessage();
        userMessage.setRole("user");
        userMessage.setContent("스크린샷을 분석해주세요: " + analysisRequest.getImageUrl());
        messages.add(userMessage);

        gptRequest.setMessages(messages);

        // GPT에게 요청 보내기
        // 4. API 호출
        OpenAIChatDefaultResponse gptResponse = openAIConfig.OpenAiClient().post()
                .bodyValue(gptRequest)
                .retrieve()
                .bodyToMono(OpenAIChatDefaultResponse.class)
                .block();

        // DB에 chat + GPT의 응답을 message로 구성해서 저장
        // 5. 응답 처리
        if (gptResponse == null) {
            throw new RuntimeException("GPT API 호출에 실패했습니다.");
        }
        String responseContent = gptResponse.getChoices().getFirst().getMessage().getContent();

        // 6. 메시지 저장
        // 사용자 메시지 저장
        Message userMsgEntity = Message.builder()
                .content("이미지 분석 요청: " + analysisRequest.getImageUrl())
                .isUserMessage(true)
                .build();
        chatRepository.saveMessage(chatId, userMsgEntity);

        // AI 응답 저장
        Message aiMsgEntity = Message.builder()
                .content(responseContent)
                .isUserMessage(false)
                .build();
        chatRepository.saveMessage(chatId, aiMsgEntity);

        // GPT 응답 analysisResponse 구성해서 응답
        // 7. 응답 반환
        return AnalysisResponse.builder()
                .messageResponse(responseContent)
                .chatId(chatId)
                .build();
    }

    // ChatbotService 클래스의 chat 메서드
    public ChatResponse chat(ChatRequest chatRequest) throws ExecutionException, InterruptedException {
        // ChatRequest 객체를 이용하여 채팅을 수행
        String chatId = chatRequest.getChatId();
        Chat chat = chatRepository.findChatById(chatId);
        if (chat == null) {
            throw new IllegalArgumentException("존재하지 않는 대화 ID입니다: " + chatId);
        }

        // updatedAt 업데이트
        chatRepository.updateChat(chat);

        // DB에서 chatId를 가지고 이전에 했던 메시지 내역 쿼리
        // 1. 이전 메시지 처리
        List<Message> previousMessages = chatRepository.findMessagesByChatId(chatId);


        // GPT에게 보낼 chat request 구성 (System Message + 과거 채팅 내역 + 새로 들어온 chat)
        // 2. 시스템 메시지 준비
        String systemPrompt = buildChatSystemPrompt(chatRequest);

        // 3. GPT 요청 생성
        OpenAIChatRequest gptRequest = new OpenAIChatRequest();

        List<OpenAIChatMessage> messages = new ArrayList<>();

        // 시스템 메시지 추가
        OpenAIChatMessage systemMessage = new OpenAIChatMessage();
        systemMessage.setRole("system");
        systemMessage.setContent(systemPrompt);
        messages.add(systemMessage);

        // 이전 메시지 추가 (최대 10개)
        int startIdx = Math.max(0, previousMessages.size() - 10);
        for (int i = startIdx; i < previousMessages.size(); i++) {
            Message msg = previousMessages.get(i);
            OpenAIChatMessage chatMsg = new OpenAIChatMessage();
            chatMsg.setContent(msg.getContent());
            chatMsg.setRole(msg.isUserMessage() ? "user" : "assistant");
            messages.add(chatMsg);
        }

        // 새 사용자 메시지 추가
        OpenAIChatMessage userMessage = new OpenAIChatMessage();
        userMessage.setRole("user");
        userMessage.setContent(chatRequest.getMessageRequest());
        messages.add(userMessage);

        gptRequest.setMessages(messages);

        // GPT에게 요청 보내기
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

        // DB에 chat + GPT의 응답을 message로 구성해서 저장
        // 6. 메시지 저장
        // 사용자 메시지 저장
        Message userMsgEntity = Message.builder()
                .content(chatRequest.getMessageRequest())
                .isUserMessage(true)
                .build();
        chatRepository.saveMessage(chatId, userMsgEntity);

        // AI 응답 저장
        Message aiMsgEntity = Message.builder()
                .content(responseContent)
                .isUserMessage(false)
                .build();
        chatRepository.saveMessage(chatId, aiMsgEntity);

        // GPT 응답 chatResponse로 구성해서 응답
        // 7. 응답 반환
        return ChatResponse.builder()
                .messageResponse(responseContent)
                .chatId(chatId)
                .build();
    }

    // 스크린샷 분석을 위한 시스템 프롬프트 구성
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

    // 채팅 요청을 위한 시스템 프롬프트 구성
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
