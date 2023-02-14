package ru.practicum.shareit.customValidationTest;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingOrderCreateRequest;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataConsistencyTest {
    @Test
    void dataConsistency_whenStartBeforeNow_returnError() {
        BookingOrderCreateRequest bookingOrder = new BookingOrderCreateRequest();
        bookingOrder.setItemId(1L);
        bookingOrder.setStart(LocalDateTime.parse("2023-01-31T19:53:19.363093"));
        bookingOrder.setEnd(LocalDateTime.parse("2030-02-02T19:53:19.363129"));

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<BookingOrderCreateRequest>> violations = validator.validate(bookingOrder);
        assertEquals(violations.size(), 1);
        assertEquals("start time must be before end time",
                violations.stream().findFirst().get().getMessage());
    }

    @Test
    void dataConsistency_whenStartAfterEnd_returnError() {
        BookingOrderCreateRequest bookingOrder = new BookingOrderCreateRequest();
        bookingOrder.setItemId(1L);
        bookingOrder.setStart(LocalDateTime.parse("2030-01-31T19:53:19.363093"));
        bookingOrder.setEnd(LocalDateTime.parse("2025-02-02T19:53:19.363129"));

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<BookingOrderCreateRequest>> violations = validator.validate(bookingOrder);
        assertEquals(violations.size(), 1);
        assertEquals("start time must be before end time",
                violations.stream().findFirst().get().getMessage());
    }

    @Test
    void dataConsistency_whenStartAfterNowAndStartBeforeEnd_returnEmptyViolationsList() {
        BookingOrderCreateRequest bookingOrder = new BookingOrderCreateRequest();
        bookingOrder.setItemId(1L);
        bookingOrder.setStart(LocalDateTime.parse("2030-01-31T19:53:19.363093"));
        bookingOrder.setEnd(LocalDateTime.parse("2030-02-02T19:53:19.363129"));

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<BookingOrderCreateRequest>> violations = validator.validate(bookingOrder);
        assertTrue(violations.isEmpty());
    }
}
