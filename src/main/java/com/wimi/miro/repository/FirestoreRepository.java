package com.wimi.miro.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Firestore 리포지토리 인터페이스
 * @param <T> 엔티티 타입
 */
public interface FirestoreRepository<T> {

    /**
     * 문서 저장
     * @param collectionPath 컬렉션 경로
     * @param entity 저장할 엔티티
     * @return 저장된 문서 ID
     */
    String save(String collectionPath, T entity) throws ExecutionException, InterruptedException;

    /**
     * 문서 ID로 조회
     * @param collectionPath 컬렉션 경로
     * @param id 문서 ID
     * @return 조회된 엔티티
     */
    T findById(String collectionPath, String id) throws ExecutionException, InterruptedException;

    /**
     * 필드 조건으로 조회
     * @param collectionPath 컬렉션 경로
     * @param field 필드명
     * @param value 필드값
     * @return 조회된 엔티티 목록
     */
    List<T> findByField(String collectionPath, String field, Object value) throws ExecutionException, InterruptedException;

    /**
     * 전체 문서 조회
     * @param collectionPath 컬렉션 경로
     * @return 전체 엔티티 목록
     */
    List<T> findAll(String collectionPath) throws ExecutionException, InterruptedException;

    /**
     * 문서 업데이트
     * @param collectionPath 컬렉션 경로
     * @param id 문서 ID
     * @param entity 업데이트할 엔티티
     */
    void update(String collectionPath, String id, T entity) throws ExecutionException, InterruptedException;

    /**
     * 문서 삭제
     * @param collectionPath 컬렉션 경로
     * @param id 문서 ID
     */
    void delete(String collectionPath, String id) throws ExecutionException, InterruptedException;
}