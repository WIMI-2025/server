package com.wimi.miro.dto.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Collections;
import java.util.List;

/**
 * OpenAI API 요청을 위한 최상위 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatGPTRequest {
    private String model = "gpt-4o";
    private List<Message> messages;
    private boolean stream = false;
    private double temperature = 1;

    @JsonProperty("max_tokens")
    private Integer maxTokens;
}
