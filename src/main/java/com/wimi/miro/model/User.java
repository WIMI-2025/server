package com.wimi.miro.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.wimi.miro.util.TimestampConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @DocumentId
    private String id;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public LocalDateTime getCreatedAt() {
        return TimestampConverter.toLocalDateTime(createdAt);
    }

    public LocalDateTime getUpdatedAt() {
        return TimestampConverter.toLocalDateTime(updatedAt);
    }
}

