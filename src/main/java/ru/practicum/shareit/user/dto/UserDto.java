package ru.practicum.shareit.user.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@Getter
@Setter
public class UserDto {
    @Null
    Integer id;
    @NotBlank(message = "'name' must not be blank")
    String name;
    @Email
    @NotNull(message = "'email' must not be nul")
    String email;
}
