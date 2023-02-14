package shareit.user;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shareit.user.dto.UserDto;
import shareit.user.dto.UserMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserService service;
    private final UserMapper mapper;

    @PostMapping
    public UserDto createUser(@RequestBody UserDto userDto) {
        User userFromResponse = mapper.dtoToUser(userDto);
        User createdUser = service.createUser(userFromResponse);
        return mapper.userToDto(createdUser);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@PathVariable Long userId,
                              @RequestBody UserDto userDto) {
        User updateProperty = mapper.dtoToUser(userDto);
        updateProperty.setId(userId);
        User updatedUser = service.updateUser(updateProperty);
        return mapper.userToDto(updatedUser);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        return service.getAllUsers().stream()
                .map(mapper::userToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable(name = "userId") Long id) {
        User user = service.getUserById(id);
        return mapper.userToDto(user);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable(name = "userId") Long id) {
        service.deleteUser(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("message", "Пользователь с  id = " + id + " был успешно удален"));
    }
}
