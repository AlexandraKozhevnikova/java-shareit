package shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(User updateProperty) {
        User savedUser = getUserById(updateProperty.getId());

        if (updateProperty.getEmail() != null) {
            savedUser.setEmail(updateProperty.getEmail());
        }

        if (updateProperty.getName() != null) {
            savedUser.setName((updateProperty.getName()));
        }

        return savedUser;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(()
                -> new NoSuchElementException("Пользователя с 'id' = " + id + " не существует"));
    }

    public User checkUserExist(Long id) {
        return getUserById(id);
    }

    @Transactional
    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }
}

