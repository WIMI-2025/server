package com.wimi.miro.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    // Firestore는 LocalDateTime을 직접 지원하지 않으므로 변환 메서드 추가
    public java.time.LocalDateTime getCreatedAtAsLocalDateTime() {
        return createdAt != null ?
                java.time.LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochSecond(
                                createdAt.getSeconds(),
                                createdAt.getNanos()
                        ),
                        java.time.ZoneId.systemDefault()
                ) : null;
    }

    public java.time.LocalDateTime getUpdatedAtAsLocalDateTime() {
        return updatedAt != null ?
                java.time.LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochSecond(
                                updatedAt.getSeconds(),
                                updatedAt.getNanos()
                        ),
                        java.time.ZoneId.systemDefault()
                ) : null;
    }
}

