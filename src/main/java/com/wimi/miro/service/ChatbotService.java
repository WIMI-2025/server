package com.wimi.miro.service;

import com.wimi.miro.dto.request.AnalysisRequest;
import com.wimi.miro.dto.request.ChatRequest;
import com.wimi.miro.dto.response.AnalysisResponse;
import com.wimi.miro.dto.response.ChatResponse;
import org.springframework.stereotype.Service;

@Service
public class ChatbotService {
    // ChatbotService 클래스의 analysis 메서드
    public AnalysisResponse analysis(AnalysisRequest analysisRequest) {
        // AnalysisRequest 객체를 이용하여 분석을 수행

        // GPT에게 보낼 chat request 구성 (System Message + AnalysisRequest)
        // GPT에게 요청 보내기
        // DB에 chat + GPT의 응답을 message로 구성해서 저장
        // GPT 응답 analysisResponse 구성해서 응답
        return null;
    }

    // ChatbotService 클래스의 chat 메서드
    public ChatResponse chat(ChatRequest chatRequest) {
        // ChatRequest 객체를 이용하여 채팅을 수행

        // DB에서 chatId를 가지고 이전에 했던 메시지 내역 쿼리
        // GPT에게 보낼 chat request 구성 (System Message + 과거 채팅 내역 + 새로 들어온 chat)
        // GPT에게 요청 보내기
        // DB에 chat + GPT의 응답을 message로 구성해서 저장
        // GPT 응답 chatResponse로 구성해서 응답

        return null;
    }
}
