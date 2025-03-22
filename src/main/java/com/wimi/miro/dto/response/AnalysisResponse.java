package com.wimi.miro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResponse {
    private String name;
    private String relationship;
    private String situation;
    private String imageUrl;
    private String messageResponse;
}