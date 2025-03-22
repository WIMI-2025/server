package com.wimi.miro.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.wimi.miro.model.Message;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class MessageRepository extends FirestoreRepositoryImpl<Message> {

    // 메시지는 채팅의 하위 컬렉션으로 저장
    private String getCollectionPath(String chatId) {
        return "chats/" + chatId + "/messages";
    }

    public String save(String chatId, Message message) throws ExecutionException, InterruptedException {
        return super.save(getCollectionPath(chatId), message);
    }

    public Message findById(String chatId, String messageId) throws ExecutionException, InterruptedException {
        return super.findById(getCollectionPath(chatId), messageId);
    }

    public List<Message> findByChatId(String chatId) throws ExecutionException, InterruptedException {
        return super.findAll(getCollectionPath(chatId));
    }

    // 타임스탬프 기준으로 메시지 정렬해서 가져오기
    public List<Message> findByChatIdOrderByTimestamp(String chatId) throws ExecutionException, InterruptedException {
        Query query = getFirestore().collection(getCollectionPath(chatId))
                .orderBy("timestamp", Query.Direction.ASCENDING);

        ApiFuture<QuerySnapshot> future = query.get();
        List<Message> messages = new ArrayList<>();

        for (DocumentSnapshot document : future.get().getDocuments()) {
            Message message = document.toObject(Message.class);
            messages.add(message);
        }

        return messages;
    }

    public void update(String chatId, String messageId, Message message) throws ExecutionException, InterruptedException {
        super.update(getCollectionPath(chatId), messageId, message);
    }

    public void delete(String chatId, String messageId) throws ExecutionException, InterruptedException {
        super.delete(getCollectionPath(chatId), messageId);
    }
}