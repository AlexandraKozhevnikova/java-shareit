package shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import shareit.booking.model.BookingOrder;

public interface BookingOrderRepository extends JpaRepository<BookingOrder, Long>,
        QuerydslPredicateExecutor<BookingOrder> {
}
