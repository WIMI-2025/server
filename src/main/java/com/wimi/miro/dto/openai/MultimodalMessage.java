package com.wimi.miro.dto.openai;

import lombok.Data;

import java.util.List;

@Data
public class MultimodalMessage extends ChatMessage {
    private List<Content> content;

    public MultimodalMessage(String role, List<Content> content) {
        super(role);
        this.content = content;
    }
}
