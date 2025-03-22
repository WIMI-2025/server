package com.wimi.miro.dto.request;

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
public class ChatbotRequest {
    /**
     * 메시지 유형
     * "text": 텍스트 메시지
     * "image": 이미지 메시지 (이미지는 별도 multipart로 전송)
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
     * 관계 정보 (선택)
     * 예: 업무 관계, 연인 관계, 친구 관계 등
     */
    private String relationship;
}