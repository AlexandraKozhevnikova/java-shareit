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
    @SneakyThrows
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
        if (booking.getItem().getOwner().getId().equals(authorId)) {
            throw new AccessDeniedException("Владелец не может бронировать свои вещи");
        }
        booking.setStatus(BookingStatus.WAITING);
        BookingOrder savedBooking = bookingRepository.save(booking);

        return bookingMapping.entityToDto(savedBooking);
    }

    @SneakyThrows
    @Transactional
    public BookingOrderResponse reactBookingOrder(Long userId, Long bookingId, Boolean isApproved) {
        BookingOrder booking = getBookingById(bookingId);

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Доступ запрещен");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ItemNotAvailableForBookingException("Бронирование не в статусе WAITING");
        }

        if (isApproved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        BookingOrder savedBooking = bookingRepository.saveAndFlush(booking);

        return bookingMapping.entityToDto(savedBooking);
    }

    @SneakyThrows
    @Transactional(readOnly = true)
    public BookingOrderResponse getBookingOrderWithUserAccess(Long userId, Long bookingId) {
        userService.getUserById(userId);
        BookingOrder booking = getBookingById(bookingId);

        if (!userId.equals(booking.getAuthor().getId()) && !userId.equals(booking.getItem().getOwner().getId())) {
            throw new AccessDeniedException("Доступ запрещен");
        }
        return bookingMapping.entityToDto(booking);
    }

    @Transactional(readOnly = true)
    public BookingOrder getBookingById(long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() ->
                new NoSuchElementException("Бронирование с  id = " + bookingId + "   не существует"));
    }
}
