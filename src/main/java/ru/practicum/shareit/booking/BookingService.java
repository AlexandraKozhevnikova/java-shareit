package ru.practicum.shareit.booking;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.querydsl.QSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.booking.dto.BookingOrderCreateRequest;
import ru.practicum.shareit.booking.dto.BookingOrderMapping;
import ru.practicum.shareit.booking.dto.BookingOrderResponse;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@AllArgsConstructor
public class BookingService {
    private BookingOrderMapping bookingMapping;
    private UserService userService;
    private ItemService itemService;
    private BookingOrderRepository bookingRepository;

    private final JPAQueryFactory jpaQueryFactory;


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


    @Transactional(readOnly = true)
    public List<BookingOrderResponse> getAllAuthorBookingOrder(Long authorId, String state) {
        userService.getUserById(authorId);

        BooleanExpression byStatus = getFilterByState(state);
        BooleanExpression byAuthor = QBookingOrder.bookingOrder.author.id.eq(authorId);
        Iterable<BookingOrder> orders = bookingRepository.findAll(byAuthor.and(byStatus),
                new QSort(QBookingOrder.bookingOrder.start.desc()));

        return StreamSupport.stream(orders.spliterator(), false)
                .map(it -> bookingMapping.entityToDto(it))
                .collect(Collectors.toList());
    }

    public List<BookingOrderResponse> getAllOwnerBookingOrder(Long ownerId, String state) {
        userService.getUserById(ownerId);
        BooleanExpression byStatus = getFilterByState(state);
        BooleanExpression byOwner = QBookingOrder.bookingOrder.item.owner.id.eq(ownerId);
        Iterable<BookingOrder> orders = bookingRepository.findAll(byOwner.and(byStatus),
                new QSort(QBookingOrder.bookingOrder.start.desc()));

        return StreamSupport.stream(orders.spliterator(), false)
                .map(it -> bookingMapping.entityToDto(it))
                .collect(Collectors.toList());
    }

    public BookingInfoDto getNextBookingForItem(Long itemId, Long authorId) {
        BookingOrder order = jpaQueryFactory
                .selectFrom(QBookingOrder.bookingOrder)
                .where(QBookingOrder.bookingOrder.bookingStatusDbCode
                        .eq(BookingStatus.APPROVED.getDbCode()))
                .where(QBookingOrder.bookingOrder.item.id
                        .eq(itemId))
                .where(QBookingOrder.bookingOrder.start
                        .after(LocalDateTime.now()))
                .orderBy(QBookingOrder.bookingOrder.start.asc())
                .fetchFirst();
        return order == null ? null : new BookingInfoDto(order.getId(), order.getAuthor().getId());
    }

    public BookingInfoDto getLastBookingForItem(Long itemId, Long authorId) {
        BookingOrder order = jpaQueryFactory
                .selectFrom(QBookingOrder.bookingOrder)
                .where(QBookingOrder.bookingOrder.bookingStatusDbCode
                        .eq(BookingStatus.APPROVED.getDbCode()))
                .where(QBookingOrder.bookingOrder.item.id
                        .eq(itemId))
                .where(QBookingOrder.bookingOrder.start
                        .before(LocalDateTime.now()))
                .orderBy(QBookingOrder.bookingOrder.start.desc())
                .fetchFirst();
        return order == null ? null : new BookingInfoDto(order.getId(), order.getAuthor().getId());
    }

    private BooleanExpression getFilterByState(String state) {
        BookingStatus status = BookingStatus.ofApiValue(state);
        BooleanExpression byStatus = Expressions.asBoolean(true).isTrue();

        if (status == BookingStatus.ALL) {
            byStatus = Expressions.asBoolean(true).isTrue();
        } else if (status == BookingStatus.APPROVED || status == BookingStatus.REJECTED || status == BookingStatus.WAITING) {
            byStatus = QBookingOrder.bookingOrder.bookingStatusDbCode
                    .eq(status.getDbCode());
        } else if (status == BookingStatus.CURRENT) {
            byStatus = Expressions.asDateTime(LocalDateTime.now())
                    .between(QBookingOrder.bookingOrder.start, QBookingOrder.bookingOrder.end);
        } else if (status == BookingStatus.FUTURE) {
            byStatus = QBookingOrder.bookingOrder.start.after(LocalDateTime.now());
        } else if (status == BookingStatus.PAST) {
            byStatus = QBookingOrder.bookingOrder.end.before(LocalDateTime.now());
        } else if (status == BookingStatus.UNDERFUND) {
            throw new IllegalArgumentException("Unknown state: " + state);
        }
        return byStatus;
    }

    public User checkUserHadItemBooking(long userId, long itemId) {
        BookingOrder order = jpaQueryFactory
                .selectFrom(QBookingOrder.bookingOrder)
                .where(QBookingOrder.bookingOrder.item.id.eq(itemId))
                .where(QBookingOrder.bookingOrder.author.id.eq(userId))
                .where(QBookingOrder.bookingOrder.start.before(LocalDateTime.now()))
                .where(QBookingOrder.bookingOrder.bookingStatusDbCode.ne(BookingStatus.REJECTED.getDbCode()))
                .fetchFirst();

        if (order == null) {
            throw new IllegalArgumentException("Пользователь не может оставить отзыва об этой вещи");
        }
        return order.getAuthor();
    }
}

