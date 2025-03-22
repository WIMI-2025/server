package com.wimi.miro.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@DependsOn("firebaseConfig")
public class FirestoreRepositoryImpl<T> implements FirestoreRepository<T> {

    private final Class<T> entityClass;

    @SuppressWarnings("unchecked")
    public FirestoreRepositoryImpl() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    protected Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    @Override
    public String save(String collectionPath, T entity) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getFirestore().collection(collectionPath).document();
        ApiFuture<WriteResult> result = docRef.set(entity);
        result.get(); // 완료될 때까지 대기
        return docRef.getId();
    }

    @Override
    public T findById(String collectionPath, String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getFirestore().collection(collectionPath).document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            return document.toObject(entityClass);
        }

        return null;
    }

    @Override
    public List<T> findByField(String collectionPath, String field, Object value)
            throws ExecutionException, InterruptedException {
        Query query = getFirestore().collection(collectionPath).whereEqualTo(field, value);
        ApiFuture<QuerySnapshot> future = query.get();
        List<T> entities = new ArrayList<>();

        for (DocumentSnapshot document : future.get().getDocuments()) {
            T entity = document.toObject(entityClass);
            entities.add(entity);
        }

        return entities;
    }

    @Override
    public List<T> findAll(String collectionPath) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getFirestore().collection(collectionPath).get();
        List<T> entities = new ArrayList<>();

        for (DocumentSnapshot document : future.get().getDocuments()) {
            T entity = document.toObject(entityClass);
            entities.add(entity);
        }

        return entities;
    }

    @Override
    public void update(String collectionPath, String id, T entity)
            throws ExecutionException, InterruptedException {
        DocumentReference docRef = getFirestore().collection(collectionPath).document(id);
        ApiFuture<WriteResult> result = docRef.set(entity);
        result.get(); // 완료될 때까지 대기
    }

    @Override
    public void delete(String collectionPath, String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getFirestore().collection(collectionPath).document(id);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get(); // 완료될 때까지 대기
    }
}