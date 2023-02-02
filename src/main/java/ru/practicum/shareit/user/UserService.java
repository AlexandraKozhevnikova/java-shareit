package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
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
        return userRepository.save(user);
    }

    public User updateUser(User updateProperty) {
        User savedUser = getUserById(updateProperty.getId());

        if (updateProperty.getEmail() != null && !updateProperty.getEmail().equals(savedUser.getEmail())) {
            savedUser.setEmail(updateProperty.getEmail());
        }

        if (updateProperty.getName() != null && !updateProperty.getName().equals(savedUser.getName())) {
            savedUser.setName((updateProperty.getName()));
        }

        return userRepository.save(savedUser);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElseThrow(() -> new NoSuchElementException("Пользователя с 'id' = " + id + " не существует"));
    }

    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }
}

