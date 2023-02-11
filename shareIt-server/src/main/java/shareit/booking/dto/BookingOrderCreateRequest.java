package shareit.booking.dto;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingOrderCreateRequest {
    private Long itemId;
    private LocalDateTime start;
    private LocalDateTime end;
}
