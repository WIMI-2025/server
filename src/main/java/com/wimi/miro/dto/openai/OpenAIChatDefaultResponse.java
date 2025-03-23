package com.wimi.miro.dto.openai;

import lombok.Data;

import java.util.List;

@Data
public class OpenAIChatDefaultResponse {
    // Getter, Setter
    private List<Choice> choices;

    // Choice 내부 클래스
    @Data
    public static class Choice {
        private OpenAIChatMessage message;

    }
}
