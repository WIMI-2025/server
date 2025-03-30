package com.wimi.miro.service;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
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
                .userUid(analysisRequest.getUserUid())
                .build();
        String chatId = chatRepository.saveChat(chat);

        // 2. 시스템 메시지 생성
        String systemPrompt = buildAnalysisSystemPrompt(analysisRequest);

        // 3. GPT 요청 생성
        ChatGPTRequest gptRequest = new ChatGPTRequest();
        List<ChatMessage> messages = new ArrayList<>();

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
                .content(analysisRequest.getImageUrl())
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
        String systemPrompt = buildChatSystemPrompt();

        // 3. GPT 요청 생성
        ChatGPTRequest gptRequest = new ChatGPTRequest();
        List<ChatMessage> messages = new ArrayList<>();

        // 시스템 메시지 추가
        messages.add(new TextMessage("system", systemPrompt));

        // 이전 메시지 추가 (최대 10개)
        log.info("이전 메시지 수: {}", previousMessages.size());

        // 앞의 두 메시지 (항상 포함): 이미지 요청 + 그에 대한 assistant 응답
        if (previousMessages.size() >= 2) {
            Message imageRequest = previousMessages.get(0);
            Message assistantReply = previousMessages.get(1);

            // 1. 이미지 메시지 (user)
            if (imageRequest.getType() == MessageType.IMAGE && imageRequest.isUserMessage()) {
                List<Content> contents = new ArrayList<>();
                contents.add(new ImageContent("image_url", new ImageUrl(imageRequest.getContent())));
                messages.add(new MultimodalMessage("user", contents));
            }

            // 2. assistant의 텍스트 응답
            if (assistantReply.getType() == MessageType.TEXT && !assistantReply.isUserMessage()) {
                messages.add(new TextMessage("assistant", assistantReply.getContent()));
            }
        }


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
                String imageUrl = msg.getContent();
                List<Content> contents = new ArrayList<>();
                contents.add(new ImageContent("image_url", new ImageUrl(imageUrl)));
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
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            log.info("OpenAI 요청: {}", mapper.writeValueAsString(gptRequest));
//        } catch (Exception e) {
//            log.error("로깅 실패", e);
//        }

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
   당신은 'WIMI(Replies That Fit with Me)'라는 맞춤형 메시지 답장 도우미 AI입니다.
        사용자가 제공한 채팅 스크린샷을 분석하고, 상황과 관계에 적절한 답장을 추천해주세요.
       \s
        ## 기본 정보
        - 상대방 이름: %s
        - 관계: %s
        - 상황: %s
       \s
        ## 분석 지침
        1. 대화의 맥락과 분위기를 깊이 이해하세요
        2. 상대방의 마지막 메시지에 담긴 의도와 감정을 세밀히 파악하세요
        3. 메시지에 담긴 명시적/암시적 질문이나 요청을 식별하세요
        4. 대화의 친밀도와 격식 수준을 고려하세요
       \s
        ## 답장 생성 방식
        당신은 감성형과 이성형을 균형있게 조합한 답변을 제공해야 합니다:
       \s
        ### 감성형 접근 (공감과 정서적 연결)
        - 상대방의 감정에 공감하고 정서적 연결을 중시합니다
        - 따뜻하고 친근한 어조를 사용합니다
        - 상대방의 말에 공감을 표현하고 정서적 지지를 제공합니다
        - 예시 요소: "그랬구나, 많이 힘들었겠다", "너의 기분 이해해", "함께 기뻐해"
       \s
        ### 이성형 접근 (분석과 문제 해결)
        - 상황을 객관적으로 분석하고 논리적으로 접근합니다
        - 정제된 문장과 명확한 표현을 사용합니다
        - 필요시 해결책이나 조언을 제공합니다
        - 예시 요소: "상황을 고려하면", "이렇게 접근하는 것이 효과적일 수 있어", "다음 단계로는"
       \s
        ## 최종 답변 구성
        [분석]
        대화 맥락과 상대방의 의도 분석 (간략하게 3-4줄)
       \s
        [감성 요소]
        공감과 정서적 연결을 표현하는 요소 (1-2개 문장)
       \s
        [이성 요소]
        상황에 대한 객관적 분석과 제안 (1-2개 문장)
       \s
        [추천 답장]
        다음 유형의 답장을 1-3개 제안하고, 각 답장의 감정 톤과 이모티콘 사용을 간략히 설명하세요:
       \s
        1. 😊 보다 감성적인 답장 (공감 중심)
        2. 👍 균형잡힌 답장 (감성과 이성의 조화)
        3. 💡 보다 이성적인 답장 (명확한 의사소통 중심)
       \s
        각 답장은 자연스러운 대화체로 작성하고, 이모티콘은 문장의 맥락에 맞게 조심스럽게 활용하세요. 이모티콘은 감정을 강화하되, 과하지 않도록 조절하는 것이 중요합니다.
       \s
        각 옵션은 자연스러운 대화체로 작성하고, 한국어 문화와 정서에 맞게 조정하세요.
       \s""",
                request.getName(),
                request.getRelationship(),
                request.getSituation()
        );
    }

    /**
     * 채팅 요청을 위한 시스템 프롬프트 구성
     *
     * @return 시스템 프롬프트
     */
    private String buildChatSystemPrompt() {
        return """
        ## 분석 지침
        1. 대화의 전체 맥락과 흐름을 파악하세요
        2. 이전 메시지들의 패턴과 어조를 분석하세요
        3. 상대방의 마지막 메시지에 담긴 의도와 감정을 파악하세요
        4. 사용자와 상대방 간의 관계 역학을 고려하세요
        """;
    }
}