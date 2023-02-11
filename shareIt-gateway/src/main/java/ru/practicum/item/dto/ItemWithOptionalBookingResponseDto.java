package ru.practicum.item.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.practicum.booking.dto.BookingInfoDto;

import java.util.List;

@Data
public class ItemWithOptionalBookingResponseDto {
    private Long id;
    private String name;
    private String description;
    @JsonProperty("available")
    private Boolean isAvailable;
    private BookingInfoDto lastBooking;
    private BookingInfoDto nextBooking;
    private List<CommentDto> comments;
}
