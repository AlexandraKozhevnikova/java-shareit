package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;

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

            if(emails.add(updateProperty.getEmail())){
                emails.remove(oldUser.getEmail());
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
    public List<User> getAllUsers(){
        return new ArrayList<>(users.values());
    }

    private int getNexId() {
        return ++counterId;
    }
}
