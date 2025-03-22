package com.wimi.miro.repository;

import com.wimi.miro.model.User;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository extends FirestoreRepositoryImpl<User> {
    private static final String COLLECTION_NAME = "users";

    public String save(User user) throws Exception {
        return super.save(COLLECTION_NAME, user);
    }

    public User findById(String id) throws Exception {
        return super.findById(COLLECTION_NAME, id);
    }

    public User findByEmail(String email) throws Exception {
        var users = super.findByField(COLLECTION_NAME, "email", email);
        return users.isEmpty() ? null : users.getFirst();
    }

    public void update(String id, User user) throws Exception {
        super.update(COLLECTION_NAME, id, user);
    }

    public void delete(String id) throws Exception {
        super.delete(COLLECTION_NAME, id);
    }
}