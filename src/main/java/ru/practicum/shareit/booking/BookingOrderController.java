package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingOrderCreateRequest;
import ru.practicum.shareit.booking.dto.BookingOrderResponse;

import javax.validation.Valid;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingOrderController {

    private static final String USER_HEADER = "X-Sharer-User-Id";
    private BookingService bookingService;

    @PostMapping
    public BookingOrderResponse createBookingOrder(@Valid @RequestBody BookingOrderCreateRequest dto,
                                                   @RequestHeader(USER_HEADER) long authorId) {
        return bookingService.createBookingOrder(dto, authorId);
    }

}
