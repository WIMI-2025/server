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
public class Message {
    @DocumentId
    private String id;
    // chatId는 여기서 필드로 저장하지 않고 Firestore 경로에 의해 암시적으로 정의됨
    // 예: /chats/{chatId}/messages/{messageId}
    private String content;
    private Boolean isUserMessage;
    private Timestamp timestamp;

    // Firestore는 LocalDateTime을 직접 지원하지 않으므로 변환 메서드 추가
    public java.time.LocalDateTime getTimestampAsLocalDateTime() {
        return timestamp != null ?
                java.time.LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochSecond(
                                timestamp.getSeconds(),
                                timestamp.getNanos()
                        ),
                        java.time.ZoneId.systemDefault()
                ) : null;
    }
}