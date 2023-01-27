package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
                                                   @RequestHeader(USER_HEADER) Long authorId) {
        return bookingService.createBookingOrder(dto, authorId);
    }

    @PatchMapping("/{bookingId}")
    public BookingOrderResponse reactBookingOrder(@RequestHeader(USER_HEADER) Long userId,
                                                  @PathVariable Long bookingId,
                                                  @RequestParam("approved") Boolean isApproved){
        return bookingService.reactBootingOrder(userId, bookingId, isApproved);
    }
    @GetMapping("/{bookingId}")
    public BookingOrderResponse getBookingOrder(@RequestHeader(USER_HEADER) Long userId,
                                                  @PathVariable Long bookingId){
        return bookingService.getBookingOrder(userId, bookingId);
    }



}
