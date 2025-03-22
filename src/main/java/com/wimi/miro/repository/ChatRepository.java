package com.wimi.miro.repository;

import com.wimi.miro.model.Chat;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class ChatRepository extends FirestoreRepositoryImpl<Chat> {
    private static final String COLLECTION_NAME = "chats";

    public String save(Chat chat) throws ExecutionException, InterruptedException {
        return super.save(COLLECTION_NAME, chat);
    }

    public Chat findById(String id) throws ExecutionException, InterruptedException {
        return super.findById(COLLECTION_NAME, id);
    }

    public List<Chat> findByUserId(String userId) throws ExecutionException, InterruptedException {
        return super.findByField(COLLECTION_NAME, "userId", userId);
    }

    public void update(String id, Chat chat) throws ExecutionException, InterruptedException {
        super.update(COLLECTION_NAME, id, chat);
    }

    public void delete(String id) throws ExecutionException, InterruptedException {
        super.delete(COLLECTION_NAME, id);
    }
}