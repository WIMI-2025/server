package com.wimi.miro.dto.response;

import com.wimi.miro.model.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 챗봇 답장 추천 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    /**
     * 추천 답장 메시지
     */
    private String messageResponse;

    /**
     * 대화 ID (맥락 유지 채팅에서 사용)
     */
    private String chatId;
}