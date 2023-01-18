package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@Repository
@Slf4j
public class UserInMemoryRepository implements UserRepository {
    private Map<Integer, User> users = new HashMap<>();
    private Set<String> emails = new HashSet<>();
    private static int counterId;

    @Autowired
    public UserInMemoryRepository() {
    }

    @Override
    public User createUser(User user) {
        if (emails.contains(user.getEmail())) {
            throw new DuplicateKeyException("Добавление пользователя с  email '" + user.getEmail() +
                    "' невозможно. Попробуйте другой email");
        }

        int id = getNexId();
        user.setId(id);
        users.put(user.getId(), user);
        emails.add(user.getEmail());
        return users.get(id);
    }

    @Override
    public User updateUser(User updateProperty) {
        if (!users.containsKey(updateProperty.getId())) {
            throw new NoSuchElementException("Пользователя с 'id' = " + updateProperty.getId() + " не существует");
        }

        User oldUser = users.get(updateProperty.getId());

        User newUser = new User();
        newUser.setId(updateProperty.getId());

        if (updateProperty.getEmail() != null && !updateProperty.getEmail().equals(oldUser.getEmail())) {
            newUser.setEmail(updateProperty.getEmail());

            if (emails.add(updateProperty.getEmail())) {
                deleteEmail(oldUser.getEmail());
            } else {
                throw new DuplicateKeyException("Обновление пользователя с  id = '" + updateProperty.getId() +
                        " неуспешно. Попробуйте другой email");
            }
        } else {
            newUser.setEmail(oldUser.getEmail());
        }

        if (updateProperty.getName() != null && !updateProperty.getName().equals(oldUser.getName())) {
            newUser.setName((updateProperty.getName()));
        } else {
            newUser.setName(oldUser.getName());
        }

        users.put(newUser.getId(), newUser);

        return newUser;
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(int id) {
        return users.get(id);
    }

    @Override
    public void deleteUser(int id) {
        if (users.containsKey(id)) {
            deleteEmail(users.get(id).getEmail());
            users.remove(id);
        } else {
            throw new NoSuchElementException("Пользователя с 'id' = " + id + " не существует");
        }
    }

    private int getNexId() {
        return ++counterId;
    }

    private void deleteEmail(String email) {
        if (emails.remove(email)) {
            //do nothing
        } else {
            log.warn("Из служебного набора не была удалена почта. Требуется ручной разбор");
        }
    }
}
