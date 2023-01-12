package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.HashSet;

@Repository
public class InMemoryRepository implements UserRepository {
    private HashMap<Integer, User> users = new HashMap<>();
    private HashSet<String> emails = new HashSet<>();
    private static int counterId;

    @Autowired
    public InMemoryRepository() {
    }

    @Override
    public User createUser(User user) {
        if (!emails.contains(user.getEmail())) {
            int id = getNexId();
            user.setId(id);
            users.put(user.getId(), user);
            emails.add(user.getEmail());
            return users.get(id);
        } else {
            throw new DuplicateKeyException("Добавление пользователя с  email '" + user.getEmail() +
                    "' невозможно. Попробуйте другой email");
        }
    }

    private int getNexId() {
        return ++counterId;
    }
}
