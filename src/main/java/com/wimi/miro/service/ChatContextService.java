package com.wimi.miro.service;

import com.google.cloud.Timestamp;
import com.wimi.miro.model.Chat;
import com.wimi.miro.model.Message;
import com.wimi.miro.repository.ChatRepository;
import com.wimi.miro.repository.MessageRepository;
import com.wimi.miro.util.TimestampConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ChatContextService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    @Autowired
    public ChatContextService(ChatRepository chatRepository, MessageRepository messageRepository) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
    }

    /**
     * 새로운 채팅 세션 생성
     */
    public String createNewChat(String userId, String chatName, String initialContext) throws ExecutionException, InterruptedException {
        Chat chat = Chat.builder()
                .userId(userId)
                .chatName(chatName)
                .createdAt(TimestampConverter.now())
                .updatedAt(TimestampConverter.now())
                .build();

        String chatId = chatRepository.save(chat);

        if (initialContext != null && !initialContext.isEmpty()) {
            // 초기 맥락을 시스템 메시지로 저장
            Message systemMessage = Message.builder()
                    .content(initialContext)
                    .isUserMessage(false)
                    .timestamp(TimestampConverter.now())
                    .build();

            messageRepository.save(chatId, systemMessage);
        }

        return chatId;
    }

    /**
     * 채팅에 사용자 메시지 추가
     */
    public String addUserMessage(String chatId, String content) throws ExecutionException, InterruptedException {
        Message message = Message.builder()
                .content(content)
                .isUserMessage(true)
                .timestamp(TimestampConverter.now())
                .build();

        String messageId = messageRepository.save(chatId, message);

        // 채팅 업데이트 시간 갱신
        Chat chat = chatRepository.findById(chatId);
        chat.setUpdatedAt(TimestampConverter.now());
        chatRepository.update(chatId, chat);

        return messageId;
    }

    /**
     * 채팅에 AI 응답 메시지 추가
     */
    public String addAssistantMessage(String chatId, String content) throws ExecutionException, InterruptedException {
        Message message = Message.builder()
                .content(content)
                .isUserMessage(false)
                .timestamp(TimestampConverter.now())
                .build();

        String messageId = messageRepository.save(chatId, message);

        // 채팅 업데이트 시간 갱신
        Chat chat = chatRepository.findById(chatId);
        chat.setUpdatedAt(TimestampConverter.now());
        chatRepository.update(chatId, chat);

        return messageId;
    }

    /**
     * 특정 채팅의 모든 메시지 조회 (시간순)
     */
    public List<Message> getChatHistory(String chatId) throws ExecutionException, InterruptedException {
        return messageRepository.findByChatIdOrderByTimestamp(chatId);
    }

    /**
     * 채팅 맥락을 메시지 기록에서 문자열로 구성
     */
    public String buildChatContext(String chatId, int maxMessages) throws ExecutionException, InterruptedException {
        List<Message> messages = messageRepository.findByChatIdOrderByTimestamp(chatId);

        // 메시지가 너무 많은 경우 최신 메시지만 사용
        if (messages.size() > maxMessages) {
            messages = messages.subList(messages.size() - maxMessages, messages.size());
        }

        StringBuilder context = new StringBuilder();
        for (Message message : messages) {
            context.append(message.isUserMessage() ? "사용자: " : "미로: ");
            context.append(message.getContent());
            context.append("\n\n");
        }

        return context.toString();
    }

    /**
     * 분석 결과를 초기 맥락으로 하는 새 채팅 생성
     */
    public String createChatFromAnalysis(String userId, String name, String relationship,
                                         String situation, String imageUrl) throws ExecutionException, InterruptedException {
        String chatName = name + "와의 대화";

        // 분석 결과를 시스템 맥락으로 구성
        String initialContext = String.format(
                "대화 상대: %s\n관계: %s\n상황: %s\n이미지: %s\n",
                name, relationship, situation, imageUrl
        );

        return createNewChat(userId, chatName, initialContext);
    }
}