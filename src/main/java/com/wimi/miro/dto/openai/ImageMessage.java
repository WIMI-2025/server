package com.wimi.miro.dto.openai;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ImageMessage extends Message {
    private List<Content> content;

    public ImageMessage(String role, List<Content> content) {
        super(role);
        this.content = content;
    }
}