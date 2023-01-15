package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        return userRepository.createUser(user);
    }

    public User updateUser(User updateProperty) {
        return userRepository.updateUser(updateProperty);
    }

    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    public User getUserById(int id) {
        Optional<User> user = Optional.ofNullable(userRepository.getUserById(id));
        return user.orElseThrow(() -> new NoSuchElementException("Пользователя с 'id' = " + id + " не существует"));
    }

    public void deleteUser(int id){
        userRepository.deleteUser(id);
    }
}

