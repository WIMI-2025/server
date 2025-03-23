package com.wimi.miro.dto.openai;

import lombok.Data;

@Data
public class OpenAIChatMessage {
    private String role;
    private String content;
}
