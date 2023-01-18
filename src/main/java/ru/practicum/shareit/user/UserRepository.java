package ru.practicum.shareit.user;

import java.util.List;

public interface UserRepository {
    User createUser(User user);

    User updateUser(User updateProperty);

    List<User> getAllUsers();

    User getUserById(int id);

    void deleteUser(int id);
}
