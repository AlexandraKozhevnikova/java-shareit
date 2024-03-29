package shareit.booking;

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
import shareit.booking.dto.BookingOrderCreateRequest;
import shareit.booking.dto.BookingOrderResponse;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingOrderController {

    private static final String USER_HEADER = "X-Sharer-User-Id";
    private BookingService bookingService;

    @PostMapping
    public BookingOrderResponse createBookingOrder(@RequestBody BookingOrderCreateRequest dto,
                                                   @RequestHeader(USER_HEADER) Long authorId) {
        return bookingService.createBookingOrder(dto, authorId);
    }

    @PatchMapping("/{bookingId}")
    public BookingOrderResponse reactBookingOrder(@RequestHeader(USER_HEADER) Long userId,
                                                  @PathVariable Long bookingId,
                                                  @RequestParam("approved") Boolean isApproved) {
        return bookingService.reactBookingOrder(userId, bookingId, isApproved);
    }

    @GetMapping("/{bookingId}")
    public BookingOrderResponse getBookingOrder(@RequestHeader(USER_HEADER) Long userId,
                                                @PathVariable Long bookingId) throws AccessDeniedException {
        return bookingService.getBookingOrderWithUserAccess(userId, bookingId);
    }

    @GetMapping
    public List<BookingOrderResponse> getAllAuthorBookingOrders(@RequestHeader(USER_HEADER) Long userId,
                                                                @RequestParam(name = "state", required = false,
                                                                        defaultValue = "ALL")
                                                                String state,
                                                                @RequestParam(value = "from", required = false)
                                                                Optional<Integer> from,
                                                                @RequestParam(value = "size", required = false)
                                                                Optional<Integer> size) {
        return bookingService.getAllAuthorBookingOrder(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingOrderResponse> getAllOwnerBookingOrders(@RequestHeader(USER_HEADER) Long userId,
                                                               @RequestParam(name = "state", required = false,
                                                                       defaultValue = "ALL") String state,
                                                               @RequestParam(value = "from", required = false)
                                                               Optional<Integer> from,
                                                               @RequestParam(value = "size", required = false)
                                                               Optional<Integer> size) {
        return bookingService.getAllOwnerBookingOrder(userId, state, from, size);
    }
}
