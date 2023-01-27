package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingOrderRepository extends JpaRepository<BookingOrder, Long> {
}
