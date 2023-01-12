package ru.practicum.shareit.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class ApiError {
    String type;
    String description;
}
