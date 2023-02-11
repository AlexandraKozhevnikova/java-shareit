package ru.practicum.booking;

import ru.practicum.booking.dto.BookingOrderCreateRequest;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

/**
 * Время начала бронирования должно быть до времени окончания бронирования
 */
public class DateConsistencyValidator implements ConstraintValidator<DateConsistency, BookingOrderCreateRequest> {

    @Override
    public boolean isValid(BookingOrderCreateRequest value, ConstraintValidatorContext context) {
        return value.getStart() != null
                && value.getEnd() != null
                && value.getStart().isBefore(value.getEnd())
                && value.getStart().isAfter(LocalDateTime.now());
    }
}
