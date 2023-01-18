package ru.practicum.shareit.user.dto;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.user.CreateUser;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@Getter
@Setter
public class UserDto {
    @Null
    private Integer id;
    @NotBlank(message = "'name' must not be blank", groups = CreateUser.class)
    private String name;
    @Email
    @NotNull(message = "'email' must not be nul", groups = CreateUser.class)
    private String email;
}
