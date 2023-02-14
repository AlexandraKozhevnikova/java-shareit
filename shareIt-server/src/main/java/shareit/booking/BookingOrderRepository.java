package shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import shareit.booking.model.BookingOrder;

import java.util.List;

public interface BookingOrderRepository extends JpaRepository<BookingOrder, Long>,
        QuerydslPredicateExecutor<BookingOrder> {
    @Query(
            value = "SELECT DISTINCT first_value(id) OVER (PARTITION BY item_id ORDER BY start_time DESC )\n" +
                    "FROM (\n" +
                    "         SELECT  id, item_id, start_time\n" +
                    "         FROM booking_order\n" +
                    "         WHERE item_id IN (:itemsIds)\n" +
                    "         AND status = 200\n" +
                    "         AND start_time > now()\n" +
                    "         ORDER BY  start_time ASC\n" +
                    "     ) AS root",
            nativeQuery = true
    )
    List<Long> getNextBookingForItem(@Param("itemsIds") List<Long> itemsIds);

    @Query(
            value = "SELECT DISTINCT first_value(id) OVER (PARTITION BY item_id ORDER BY start_time DESC )\n" +
                    "FROM (\n" +
                    "         SELECT  id, item_id, start_time\n" +
                    "         FROM booking_order\n" +
                    "         WHERE item_id IN (:itemsIds)\n" +
                    "         AND status = 200   \n" +
                    "         AND start_time < now()\n" +
                    "         ORDER BY  start_time DESC\n" +
                    "     ) AS root",
            nativeQuery = true
    )
    List<Long> getLastBookingForItem(@Param("itemsIds") List<Long> itemsIds);
}
