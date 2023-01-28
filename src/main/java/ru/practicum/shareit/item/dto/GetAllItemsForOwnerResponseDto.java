package ru.practicum.shareit.item.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingInfoDto;

@Data
public class GetAllItemsForOwnerResponseDto {
    private Long id;
    private String name;
    private String description;
    @JsonProperty("available")
    private Boolean isAvailable;
    private BookingInfoDto lastBooking;
    private BookingInfoDto nextBooking;
}
