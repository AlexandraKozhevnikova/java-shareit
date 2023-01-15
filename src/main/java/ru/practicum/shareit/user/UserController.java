package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import javax.validation.Valid;
import java.util.Arrays;

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
    public UserDto updateUser(@PathVariable int userId,
                              @Valid @RequestBody UserDto userDto) {
        User updateProperty = mapper.dtoToUser(userDto);
        updateProperty.setId(userId);
        User updatedUser = service.updateUser(updateProperty);
        return mapper.userToDto(updatedUser);
    }
}
