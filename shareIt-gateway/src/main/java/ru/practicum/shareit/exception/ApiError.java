package ru.practicum.shareit.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class ApiError {
    private String type;
    private String description;
}
