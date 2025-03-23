package com.wimi.miro.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.wimi.miro.model.Chat;
import com.wimi.miro.model.Message;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class ChatRepository {
    private static final String COLLECTION_NAME = "chats";

    private Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    // Chat methods
    public String saveChat(Chat chat) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getFirestore().collection(COLLECTION_NAME).document();
        String id = docRef.getId();
        chat.setId(id);
        chat.setCreatedAt(Timestamp.now());
        chat.setUpdatedAt(Timestamp.now());
        ApiFuture<WriteResult> result = docRef.set(chat);
        result.get(); // 완료될 때까지 대기
        return id;
    }

    public Chat findChatById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getFirestore().collection(COLLECTION_NAME).document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            return document.toObject(Chat.class);
        }
        return null;
    }

    public List<Chat> findChatsByUserId(String userId) throws ExecutionException, InterruptedException {
        Query query = getFirestore().collection(COLLECTION_NAME).whereEqualTo("userId", userId);
        ApiFuture<QuerySnapshot> future = query.get();
        List<Chat> chats = new ArrayList<>();

        for (DocumentSnapshot document : future.get().getDocuments()) {
            Chat chat = document.toObject(Chat.class);
            chats.add(chat);
        }

        return chats;
    }

    public void updateChat(Chat chat) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getFirestore().collection(COLLECTION_NAME).document(chat.getId());
        chat.setUpdatedAt(Timestamp.now());
        ApiFuture<WriteResult> result = docRef.set(chat);
        result.get();
    }

    public void deleteChat(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getFirestore().collection(COLLECTION_NAME).document(id);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }

    // Message methods - using subcollection
    private String getMessagesCollection(String chatId) {
        return COLLECTION_NAME + "/" + chatId + "/messages";
    }

    public void saveMessage(String chatId, Message message) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getFirestore().collection(getMessagesCollection(chatId)).document();
        String id = docRef.getId();
        message.setId(id);
        message.setCreatedAt(Timestamp.now());
        ApiFuture<WriteResult> result = docRef.set(message);
        result.get();
    }

    public List<Message> findMessagesByChatId(String chatId) throws ExecutionException, InterruptedException {
        Query query = getFirestore().collection(getMessagesCollection(chatId))
                .orderBy("timestamp", Query.Direction.ASCENDING);

        ApiFuture<QuerySnapshot> future = query.get();
        List<Message> messages = new ArrayList<>();

        for (DocumentSnapshot document : future.get().getDocuments()) {
            Message message = document.toObject(Message.class);
            messages.add(message);
        }

        return messages;
    }

    public void updateMessage(String chatId, Message message) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getFirestore().collection(getMessagesCollection(chatId)).document(message.getId());
        ApiFuture<WriteResult> result = docRef.set(message);
        result.get();
    }

    public void deleteMessage(String chatId, String messageId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getFirestore().collection(getMessagesCollection(chatId)).document(messageId);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}