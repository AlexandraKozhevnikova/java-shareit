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

import java.nio.file.AccessDeniedException;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class BookingService {
    private BookingOrderMapping bookingMapping;
    private UserService userService;
    private ItemService itemService;
    private BookingOrderRepository bookingRepository;


    @Transactional
    public BookingOrderResponse createBookingOrder(BookingOrderCreateRequest dto, Long authorId) {
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

    public BookingOrderResponse reactBootingOrder(Long userId, Long bookingId, Boolean isApproved) {
        return null;
    }


    @SneakyThrows
    public BookingOrderResponse getBookingOrder(Long userId, Long bookingId) {
        User user = userService.getUserById(userId);
        BookingOrder booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new NoSuchElementException("Бронирование с  id = " + bookingId + "   не существует"));

        if (userId.equals(booking.getAuthor().getId()) || userId.equals(booking.getItem().getOwner().getId())) {
            return bookingMapping.entityToDto(booking);
        } else {
            throw new AccessDeniedException("Доступ запрещен");
        }
    }
}
