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
     * 상대방 이름
     */
    private String name;
    /**
     * 분석할 이미지의 URL
     */
    private String imageUrl;

    /**
     * 상대방과의 관계 정보
     * 예: 업무 관계, 연인 관계, 친구 관계 등
     */
    private String relationship;
    /**
     * 상황 정보
     * 예: 업무 상황, 연애 상황, 친구 상황 등
     */
    private String situation;
}