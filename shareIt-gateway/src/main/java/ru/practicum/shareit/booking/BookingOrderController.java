package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
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

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingOrderController {

    private static final String USER_HEADER = "X-Sharer-User-Id";
    private BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> createBookingOrder(@Valid @RequestBody BookingOrderCreateRequest bookingOrderCreateRequest,
                                                     @RequestHeader(USER_HEADER) Long authorId) {
        return bookingClient.createBookingOrder(bookingOrderCreateRequest, authorId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> reactBookingOrder(@RequestHeader(USER_HEADER) Long userId,
                                                    @PathVariable Long bookingId,
                                                    @RequestParam("approved") Boolean isApproved) {
        return bookingClient.reactBookingOrder(userId, bookingId, isApproved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingOrder(@RequestHeader(USER_HEADER) Long userId,
                                                  @PathVariable Long bookingId) {
        return bookingClient.getBookingOrderWithUserAccess(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllAuthorBookingOrders(@RequestHeader(USER_HEADER) Long userId,
                                                            @RequestParam(name = "state", required = false,
                                                                    defaultValue = "ALL")
                                                            String state,
                                                            @PositiveOrZero
                                                            @RequestParam(value = "from", required = false)
                                                            Optional<Integer> from,
                                                            @Positive
                                                            @RequestParam(value = "size", required = false)
                                                            Optional<Integer> size) {
        return bookingClient.getAllAuthorBookingOrder(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllOwnerBookingOrders(@RequestHeader(USER_HEADER) Long userId,
                                                           @RequestParam(name = "state", required = false,
                                                                   defaultValue = "ALL") String state,
                                                           @PositiveOrZero
                                                           @RequestParam(value = "from", required = false)
                                                           Optional<Integer> from,
                                                           @Positive
                                                           @RequestParam(value = "size", required = false)
                                                           Optional<Integer> size) {
        return bookingClient.getAllOwnerBookingOrder(userId, state, from, size);
    }
}
