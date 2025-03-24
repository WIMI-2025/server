package com.wimi.miro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class ImageUploadResponse {
    private String imageUrl;
}
