package com.wimi.miro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 채팅 스크린샷 분석 결과 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResponse {
    /**
     * 대화 상대방의 이름 또는 호칭
     */
    private String name;

    /**
     * 관계 유형(업무, 연인, 친구, 가족, 낯선 사람 등)
     */
    private String relationship;

    /**
     * 대화 상황 설명
     */
    private String situation;

    /**
     * 대화 목적
     */
    private String purpose;

    /**
     * 대화 톤과 분위기
     */
    private String tone;

    /**
     * 분석한 이미지의 URL
     */
    private String imageUrl;

    /**
     * 기본 답장 추천 (하위 호환성 유지)
     */
    private String messageResponse;

    /**
     * 기본형 답장 - 가장 적절하고 균형 잡힌 답장
     */
    private String basicReply;

    /**
     * 친절형 답장 - 더 따뜻하고 친절한 톤의 답장
     */
    private String kindReply;

    /**
     * 간결형 답장 - 핵심만 담은 효율적인 답장
     */
    private String conciseReply;

    /**
     * 상황특화형 답장 - 관계와 상황에 특화된 답장
     */
    private String specializedReply;

    /**
     * 대화 조언 - 현재 대화 상황에서 고려할 만한 조언
     */
    private String advice;
}