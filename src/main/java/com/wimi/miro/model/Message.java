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
public class Message {
    @DocumentId
    private String id;
    // chatId는 여기서 필드로 저장하지 않고 Firestore 경로에 의해 암시적으로 정의됨
    // 예: /chats/{chatId}/messages/{messageId}
    private String content;
    private Boolean isUserMessage;
    private Timestamp timestamp;

    public LocalDateTime getTimeStameAsLocalDateTime() {
        return TimestampConverter.toLocalDateTime(timestamp);
    }

    public boolean isUserMessage() {
        return isUserMessage != null && isUserMessage;
    }
}