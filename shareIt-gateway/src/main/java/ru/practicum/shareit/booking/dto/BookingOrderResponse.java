package ru.practicum.shareit.booking.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingOrderResponse {
    private long id;
    @JsonProperty(value = "booker")
    private UserDto author;
    private ItemDto item;
    private BookingStatus status;
    private LocalDateTime start;
    private LocalDateTime end;
}
