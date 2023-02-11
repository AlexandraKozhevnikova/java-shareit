package ru.practicum.booking.dto;


import lombok.Getter;
import lombok.Setter;
import ru.practicum.booking.DateConsistency;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@DateConsistency
public class BookingOrderCreateRequest {
    @NotNull
    private Long itemId;
    @NotNull
    private LocalDateTime start;
    @NotNull
    private LocalDateTime end;
}
