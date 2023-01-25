package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@RestController
@Validated
@RequestMapping(path = "/users")
public class UserController {
    private final UserService service;
    private final UserMapper mapper;

    @PostMapping
    @Validated(CreateUser.class)
    public UserDto createUser(@RequestBody @Valid UserDto userDto) {
        User userFromResponse = mapper.dtoToUser(userDto);
        User createdUser = service.createUser(userFromResponse);
        return mapper.userToDto(createdUser);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@PathVariable long userId,
                              @Valid @RequestBody UserDto userDto) {
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
    public UserDto getUserById(@PathVariable(name = "userId") int id) {
        User user = service.getUserById(id);
        return mapper.userToDto(user);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable(name = "userId") int id) {
        service.deleteUser(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("message", "Пользователь с  id = " + id + " был успешно удален"));
    }
}
