package com.wimi.miro.dto.openai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIChatDefaultResponse {
    // Getter, Setter
    private List<Choice> choices;

    // Choice 내부 클래스
    @Data
    public static class Choice {
        private OpenAIChatMessage message;

    }
}
