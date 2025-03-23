package com.wimi.miro.dto.openai;

import lombok.Data;

import java.util.List;

@Data
public class OpenAIChatRequest {
    private String model = "gpt-4o";
    private List<OpenAIChatMessage> messages;
    private boolean stream = false;
    private double temperature = 1;
}
