package ru.practicum.booking.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.booking.BookingStatus;
import ru.practicum.item.dto.ItemDto;
import ru.practicum.user.dto.UserDto;

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
