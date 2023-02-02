package ru.practicum.shareit.item.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class ItemDto {
    private Long id;
    @NotBlank(message = "'name' must not be blank")
    private String name;
    @NotBlank(message = "'description' must not be blank")
    private String description;
    @NotNull(message = "'available' must not be blank")
    @JsonProperty("available")
    private Boolean isAvailable;
}
