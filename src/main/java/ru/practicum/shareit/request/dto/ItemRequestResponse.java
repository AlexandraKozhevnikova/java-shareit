package ru.practicum.shareit.request.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ItemRequestResponse {
    private Long id;
    private String description;
    private LocalDateTime created;
}
