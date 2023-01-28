package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface BookingOrderRepository extends JpaRepository<BookingOrder, Long>,
        QuerydslPredicateExecutor<BookingOrder> {
}
