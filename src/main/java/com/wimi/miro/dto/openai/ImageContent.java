package com.wimi.miro.dto.openai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Image content type for OpenAI messages
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageContent extends Content {
    private ImageUrl image_url;


    public ImageContent(String type, ImageUrl image_url) {
        super(type);
        this.image_url = image_url;
    }

}
