package com.wimi.miro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class ImageUploadResponse {
    private String imageUrl;
}
