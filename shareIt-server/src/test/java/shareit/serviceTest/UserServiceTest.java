package shareit.serviceTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import shareit.item.model.Item;
import shareit.user.User;
import shareit.user.UserRepository;
import shareit.user.UserService;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;

    @Captor
    private ArgumentCaptor<User> argumentCaptor;

    @Test
    void createUser_whenAddUser_thenReturnUserWithId() {
        User expectedUser = new User();
        when(userRepository.save(any()))
                .thenReturn(expectedUser);

        User actualUser = userService.createUser(new User());

        assertEquals(expectedUser, actualUser);
    }


    @Test
    void getUserById_whenUserExist_thenReturnFoundUser() {
        User expectedUser = new User();
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(expectedUser));

        User actualUser = userService.getUserById(anyLong());

        assertEquals(expectedUser, actualUser);
    }

    @Test
    void getUserById_whenUserNotExist_thenReturnException() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.getUserById(anyLong()));
    }

    @Test
    void updateUser_whenUpdateNameAndEmailForExistUser_thenReturnUserWithUpdatedNameAndEmail() {
        User oldUser = new User();
        oldUser.setName("Sasha");
        oldUser.setEmail("sa@ya.ru");
        oldUser.setId(1L);

        User updateProperty = new User();
        updateProperty.setName("Olesya");
        updateProperty.setEmail("ol@ya.ru");
        updateProperty.setId(6L);
        updateProperty.setItems(Set.of(new Item()));

        when(userRepository.save(any()))
                .thenReturn(updateProperty);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(oldUser));

        userService.updateUser(updateProperty);

        verify(userRepository).save(argumentCaptor.capture());
        User savedUser = argumentCaptor.getValue();

        assertEquals(oldUser.getId(), savedUser.getId());
        assertEquals(updateProperty.getName(), savedUser.getName());
        assertEquals(updateProperty.getEmail(), savedUser.getEmail());
        assertTrue(savedUser.getItems().isEmpty());
    }

    @Test
    void updateUser_whenUpdateOnlyEmailForExistUser_thenReturnUserWithUpdatedEmail() {
        User oldUser = new User();
        oldUser.setName("Sasha");
        oldUser.setEmail("sa@ya.ru");
        oldUser.setId(1L);

        User updateProperty = new User();
        updateProperty.setEmail("ol@ya.ru");
        updateProperty.setId(1L);

        when(userRepository.save(any()))
                .thenReturn(updateProperty);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(oldUser));

        userService.updateUser(updateProperty);

        verify(userRepository).save(argumentCaptor.capture());
        User savedUser = argumentCaptor.getValue();

        assertEquals(oldUser.getId(), savedUser.getId());
        assertEquals(oldUser.getName(), savedUser.getName());
        assertEquals(updateProperty.getEmail(), savedUser.getEmail());
        assertTrue(oldUser.getItems().isEmpty());
    }

    @Test
    void updateUser_whenUpdateOnlyNameForExistUser_thenReturnUserWithUpdatedName() {
        User oldUser = new User();
        oldUser.setName("Sasha");
        oldUser.setEmail("sa@ya.ru");
        oldUser.setId(1L);

        User updateProperty = new User();
        updateProperty.setName("Diana");
        updateProperty.setId(1L);

        when(userRepository.save(any()))
                .thenReturn(updateProperty);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(oldUser));

        userService.updateUser(updateProperty);

        verify(userRepository).save(argumentCaptor.capture());
        User savedUser = argumentCaptor.getValue();

        assertEquals(oldUser.getId(), savedUser.getId());
        assertEquals(updateProperty.getName(), savedUser.getName());
        assertEquals(oldUser.getEmail(), savedUser.getEmail());
        assertTrue(oldUser.getItems().isEmpty());
    }

    @Test
    void updateUser_whenUpdateWithTheSameNameAndEmailForExistUser_thenOldUser() {
        User oldUser = new User();
        oldUser.setName("Sasha");
        oldUser.setEmail("sa@ya.ru");
        oldUser.setId(1L);

        User updateProperty = new User();
        updateProperty.setName("Sasha");
        updateProperty.setEmail("sa@ya.ru");
        updateProperty.setId(1L);

        when(userRepository.save(any()))
                .thenReturn(updateProperty);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(oldUser));

        userService.updateUser(updateProperty);

        verify(userRepository).save(argumentCaptor.capture());
        User savedUser = argumentCaptor.getValue();

        assertEquals(oldUser.getId(), savedUser.getId());
        assertEquals(oldUser.getName(), savedUser.getName());
        assertEquals(oldUser.getEmail(), savedUser.getEmail());
        assertTrue(savedUser.getItems().isEmpty());
    }

    @Test
    void deleteUser_whenUserExist_thenReturnNothing() {
        userService.deleteUser(1L);

        verify(userRepository, times(1))
                .deleteById(1L);
    }
}
