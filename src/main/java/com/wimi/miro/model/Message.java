package com.wimi.miro.model;

import com.google.cloud.Date;
import com.google.cloud.firestore.annotation.DocumentId;
import com.wimi.miro.enums.MessageType;
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
    private MessageType type;
    // chatId는 여기서 필드로 저장하지 않고 Firestore 경로에 의해 암시적으로 정의됨
    // 예: /chats/{chatId}/messages/{messageId}
    private String content;
    private Boolean isUserMessage;
    private Date createdAt;

    public boolean isUserMessage() {
        return isUserMessage != null && isUserMessage;
    }
}