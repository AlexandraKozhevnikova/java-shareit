package ru.practicum.shareit.controllerMethodTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingOrderController;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingOrderResponse;

import java.nio.file.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingOrderControllerUnitTest {
    @InjectMocks
    private BookingOrderController bookingOrderController;
    @Mock
    private BookingService bookingService;

    @Test
    void reactBookingOrder_whenAcceptBookingByOwner_thenReturnTheBooking() {
        BookingOrderResponse expectedResponse = new BookingOrderResponse();
        when(bookingService.reactBookingOrder(anyLong(), anyLong(), eq(true)))
                .thenReturn(expectedResponse);

        BookingOrderResponse actualResponse =
                bookingOrderController.reactBookingOrder(1L, 1L, true);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void getBookingOrder_whenByAuthorOrOwner_thenReturnTheBooking() throws AccessDeniedException {
        BookingOrderResponse expectedResponse = new BookingOrderResponse();
        when(bookingService.getBookingOrderWithUserAccess(anyLong(), anyLong()))
                .thenReturn(expectedResponse);

        BookingOrderResponse actualResponse = bookingOrderController.getBookingOrder(anyLong(), anyLong());

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void getBookingOrder_whenByNotAuthorAndNotOwner_thenThrowAccessDeniedException() throws AccessDeniedException {
        when(bookingService.getBookingOrderWithUserAccess(anyLong(), anyLong()))
                .thenThrow(AccessDeniedException.class);

        assertThrows(AccessDeniedException.class, () -> bookingOrderController.getBookingOrder(anyLong(), anyLong()));
    }
}
