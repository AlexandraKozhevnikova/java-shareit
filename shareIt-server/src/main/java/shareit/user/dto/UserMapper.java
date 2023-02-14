package shareit.user.dto;

import org.mapstruct.Mapper;
import shareit.user.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User dtoToUser(UserDto dto);

    UserDto userToDto(User user);
}
