package com.wimi.miro.dto.openai;

import lombok.*;

/**
 * Text content type for OpenAI messages
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TextContent extends Content {
    private String text;

    public TextContent(String type, String text) {
        super(type);
        this.text = text;
    }
}
