package com.wimi.miro.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 챗봇 답장 추천 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    /**
     * 메시지 유형
     * "text": 텍스트 메시지
     * "image": 이미지 메시지
     */
    private String messageType;

    /**
     * 메시지 내용
     * 텍스트 메시지일 경우: 사용자의 메시지 텍스트
     * 이미지 메시지일 경우: 이미지에 대한 설명 또는 맥락
     */
    private String messageRequest;

    /**
     * 채팅 ID (맥락 유지 채팅에서 사용)
     */
    private String chatId;

    /**
     * 이미지 URL (이미지 메시지일 경우)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String imageUrl;
}