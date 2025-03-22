package com.wimi.miro.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wimi.miro.dto.request.AnalysisRequest;
import com.wimi.miro.dto.response.AnalysisResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AnalysisService {

    private final GeminiService geminiService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public AnalysisService(GeminiService geminiService, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.geminiService = geminiService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public AnalysisResponse analyzeChat(AnalysisRequest request) {
        try {
            // 시스템 프롬프트 설정
            String systemPrompt = """
                당신은 '미로(MIRO)'라는 이름의 AI 답장 추천 어시스턴트이며, 위미(WIMI: Where Is My Ideal Reply) 서비스의 핵심 기능입니다. 사용자가 제공한 메신저 대화 스크린샷을 분석하고 상황과 맥락에 가장 적합한 답장을 추천해 주는 것이 당신의 역할입니다.
                
                ## 분석 프로세스
                ### 1. 이미지 분석
                1. 스크린샷에서 메신저 앱 UI 요소 인식 (카카오톡, 라인, 인스타그램 DM 등)
                2. 대화 참여자 식별 (누가 사용자이고 누가 상대방인지)
                3. 시간대와 메시지 흐름 파악
                4. 최근 메시지 내용과 이전 대화 맥락 이해
                5. 이모티콘, 스티커, 이미지 요소의 감정적 의미 해석
                
                ### 2. 대화 상황 분석
                1. 대화 내용을 바탕으로 관계 맥락 파악
                2. 대화의 목적, 감정적 톤, 시급성 파악
                
                ## 답장 추천 형식
                **대화 분석:**
                [관계, 상황, 목적, 감정적 톤에 대한 간결한 분석. 3-4문장 이내로 요약]
                
                **추천 답장 옵션:**
                **💬 기본형**
                [가장 적절하고 균형 잡힌 답장]
                
                **💬 친절형**
                [더 따뜻하고 친절한 톤의 답장]
                
                **💬 간결형**
                [핵심만 담은 효율적인 답장]
                
                **💬 상황특화형**
                [관계와 상황에 특화된 답장]
                
                **대화 조언:**
                [현재 대화 상황에서 고려할 만한 간단한 조언이나 주의사항. 1-2문장으로 제공]
                
                응답은 JSON 형식으로 다음 필드를 포함해주세요:
                {
                  "name": "상대방 이름 또는 호칭",
                  "relationship": "관계 유형(업무, 연인, 친구, 가족, 낯선 사람 등)",
                  "situation": "대화 상황 설명",
                  "purpose": "대화 목적",
                  "tone": "대화 톤과 분위기",
                  "basicReply": "기본형 답장",
                  "kindReply": "친절형 답장",
                  "conciseReply": "간결형 답장",
                  "specializedReply": "상황특화형 답장",
                  "advice": "대화 조언"
                }
            """;

            // 이미지 URL과 함께 사용자 프롬프트 생성
            String userPrompt = "이 채팅 스크린샷을 분석해주세요.";

            // Gemini API 호출 - 이미지 URL 사용
            String response = geminiService.generateContentWithImageUrl(systemPrompt, userPrompt, request.getImageUrl());

            try {
                // JSON 형식 응답 파싱 시도
                if (response.contains("{") && response.contains("}")) {
                    // JSON 부분 추출 (응답에 다른 텍스트가 있을 수 있음)
                    String jsonPart = response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1);
                    Map<String, Object> parsedResponse = objectMapper.readValue(jsonPart, Map.class);

                    return AnalysisResponse.builder()
                            .name((String) parsedResponse.getOrDefault("name", ""))
                            .relationship((String) parsedResponse.getOrDefault("relationship", ""))
                            .situation((String) parsedResponse.getOrDefault("situation", ""))
                            .purpose((String) parsedResponse.getOrDefault("purpose", ""))
                            .tone((String) parsedResponse.getOrDefault("tone", ""))
                            .imageUrl(request.getImageUrl())
                            .messageResponse((String) parsedResponse.getOrDefault("basicReply", "")) // 하위 호환성 유지
                            .basicReply((String) parsedResponse.getOrDefault("basicReply", ""))
                            .kindReply((String) parsedResponse.getOrDefault("kindReply", ""))
                            .conciseReply((String) parsedResponse.getOrDefault("conciseReply", ""))
                            .specializedReply((String) parsedResponse.getOrDefault("specializedReply", ""))
                            .advice((String) parsedResponse.getOrDefault("advice", ""))
                            .build();
                }
            } catch (Exception e) {
                // JSON 파싱 실패 - 구조화되지 않은 응답 처리
            }

            // JSON 파싱 실패 시 전체 응답 텍스트 반환
            return AnalysisResponse.builder()
                    .name("파싱 실패")
                    .relationship("알 수 없음")
                    .situation("분석 실패")
                    .imageUrl(request.getImageUrl())
                    .messageResponse(response)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("이미지 분석 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}