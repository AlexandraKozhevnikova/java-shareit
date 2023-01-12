package ru.practicum.shareit.user.dto;

import org.mapstruct.Mapper;
import ru.practicum.shareit.user.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User dtoToUser(UserDto dto);

    UserDto userToDto(User user);
}
