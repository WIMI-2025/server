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
public class Chat {
    @DocumentId
    private String id;
    private String chatName;
    // Firestore에서는 하위 컬렉션으로 메시지를 저장하므로 여기서는 참조하지 않음
    private Timestamp createdAt;
    private Timestamp updatedAt;

}
