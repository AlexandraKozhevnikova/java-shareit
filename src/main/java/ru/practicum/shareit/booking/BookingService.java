package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingOrderCreateRequest;
import ru.practicum.shareit.booking.dto.BookingOrderMapping;
import ru.practicum.shareit.booking.dto.BookingOrderResponse;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

@Service
@AllArgsConstructor
public class BookingService {
    private BookingOrderMapping bookingMapping;
    private UserService userService;
    private ItemService itemService;
    private BookingOrderRepository bookingRepository;


    @SneakyThrows
    @Transactional
    public BookingOrderResponse createBookingOrder(BookingOrderCreateRequest dto, long authorId) {
        BookingOrder booking = bookingMapping.dtoToEntity(dto);
        User author = userService.getUserById(authorId);
        booking.setAuthor(author);
        Item item = itemService.checkItemIsExistInRep(dto.getItemId());
        booking.setItem(item);
        if (booking.getItem().getIsAvailable() == false) {
            throw new ItemNotAvailableForBookingException("Вещь с id = " + booking.getItem().getId() +
                    " не доступна для бронирования");
        }
        booking.setStatus(BookingStatus.WAITING);
        BookingOrder savedBooking = bookingRepository.save(booking);

        return bookingMapping.entityToDto(savedBooking);
    }
}
