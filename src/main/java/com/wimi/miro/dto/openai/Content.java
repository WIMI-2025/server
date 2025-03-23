package com.wimi.miro.dto.openai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Base class for message content items
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Content {
    private String type;
}
