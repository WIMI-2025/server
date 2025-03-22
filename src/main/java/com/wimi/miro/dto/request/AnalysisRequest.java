package com.wimi.miro.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 채팅 스크린샷 분석 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequest {
    /**
     * 분석할 이미지의 URL
     */
    private String imageUrl;

    /**
     * 분석에 도움이 될 부가 맥락 정보 (선택)
     */
    private String additionalContext;

    /**
     * 상대방과의 관계 정보 (선택)
     * 예: 업무 관계, 연인 관계, 친구 관계 등
     */
    private String relationship;
}