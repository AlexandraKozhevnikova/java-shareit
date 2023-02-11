package shareit.booking;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.querydsl.QPageRequest;
import org.springframework.data.querydsl.QSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shareit.booking.dto.BookingInfoDto;
import shareit.booking.dto.BookingOrderCreateRequest;
import shareit.booking.dto.BookingOrderMapping;
import shareit.booking.dto.BookingOrderResponse;
import shareit.booking.model.BookingOrder;
import shareit.booking.model.BookingStatus;
import shareit.booking.model.QBookingOrder;
import shareit.item.ItemService;
import shareit.item.model.Item;
import shareit.user.User;
import shareit.user.UserService;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import static shareit.booking.model.BookingStatus.APPROVED;
import static shareit.booking.model.BookingStatus.REJECTED;
import static shareit.booking.model.BookingStatus.WAITING;


@Service
@AllArgsConstructor
public class BookingService {
    private final BookingOrderMapping bookingMapping;
    private final UserService userService;
    private final ItemService itemService;
    private final BookingOrderRepository bookingRepository;
    private final JPAQueryFactory jpaQueryFactory;
    private static final int DEFAULT_FROM = 0;
    private static final int DEFAULT_SIZE = 100;


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
            throw new ItemCanNotBeBookedByOwnerException("Владелец не может бронировать свои вещи");
        }
        booking.setStatus(WAITING);
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

        if (booking.getStatus() != WAITING) {
            throw new ItemNotAvailableForBookingException("Бронирование не в статусе WAITING");
        }

        if (isApproved) {
            booking.setStatus(APPROVED);
        } else {
            booking.setStatus(REJECTED);
        }

        BookingOrder savedBooking = bookingRepository.save(booking);

        return bookingMapping.entityToDto(savedBooking);
    }

    @Transactional(readOnly = true)
    public BookingOrderResponse getBookingOrderWithUserAccess(Long userId, Long bookingId) throws AccessDeniedException {
        userService.getUserById(userId);
        BookingOrder booking = getBookingById(bookingId);

        if (!userId.equals(booking.getAuthor().getId()) && !userId.equals(booking.getItem().getOwner().getId())) {
            throw new AccessDeniedException("Доступ запрещен");
        }
        return bookingMapping.entityToDto(booking);
    }

    @Transactional(readOnly = true)
    public BookingOrder getBookingById(long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Бронирование с  id = " + bookingId + "   не существует"));
    }

    @Transactional(readOnly = true)
    public List<BookingOrderResponse> getAllAuthorBookingOrder(
            Long authorId,
            String state,
            Optional<Integer> from,
            Optional<Integer> size) {
        userService.getUserById(authorId);

        BooleanExpression byStatus = getFilterByState(state);
        BooleanExpression byAuthor = QBookingOrder.bookingOrder.author.id.eq(authorId);

        return bookingRepository.findAll(byAuthor.and(byStatus),
                        QPageRequest.of(from.orElse(DEFAULT_FROM), size.orElse(DEFAULT_SIZE)).withSort(
                                new QSort(QBookingOrder.bookingOrder.start.desc()))
                ).stream()
                .map(bookingMapping::entityToDto)
                .collect(Collectors.toList());
    }

    public List<BookingOrderResponse> getAllOwnerBookingOrder(Long ownerId, String state,
                                                              Optional<Integer> from,
                                                              Optional<Integer> size) {
        userService.getUserById(ownerId);
        BooleanExpression byStatus = getFilterByState(state);
        BooleanExpression byOwner = QBookingOrder.bookingOrder.item.owner.id.eq(ownerId);
        return bookingRepository.findAll(byOwner.and(byStatus),
                        QPageRequest.of(from.orElse(DEFAULT_FROM), size.orElse(DEFAULT_SIZE))
                                .withSort(new QSort(QBookingOrder.bookingOrder.start.desc()))
                ).stream()
                .map(bookingMapping::entityToDto)
                .collect(Collectors.toList());
    }

    public BookingInfoDto getNextBookingForItem(Long itemId, Long authorId) {
        BookingOrder order = jpaQueryFactory
                .selectFrom(QBookingOrder.bookingOrder)
                .where(QBookingOrder.bookingOrder.bookingStatusDbCode
                        .eq(APPROVED.getDbCode()))
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
                        .eq(APPROVED.getDbCode()))
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
        BooleanExpression byStatus;

        switch (status) {
            case ALL:
                byStatus = Expressions.asBoolean(true).isTrue();
                break;
            case APPROVED:
            case REJECTED:
            case WAITING:
                byStatus = QBookingOrder.bookingOrder.bookingStatusDbCode
                        .eq(status.getDbCode());
                break;
            case CURRENT:
                byStatus = Expressions.asDateTime(LocalDateTime.now())
                        .between(QBookingOrder.bookingOrder.start, QBookingOrder.bookingOrder.end);
                break;
            case FUTURE:
                byStatus = QBookingOrder.bookingOrder.start.after(LocalDateTime.now());
                break;
            case PAST:
                byStatus = QBookingOrder.bookingOrder.end.before(LocalDateTime.now());
                break;
            default:
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
                .where(QBookingOrder.bookingOrder.bookingStatusDbCode.ne(REJECTED.getDbCode()))
                .fetchFirst();

        if (order == null) {
            throw new IllegalArgumentException("Пользователь не может оставить отзыва об этой вещи");
        }
        return order.getAuthor();
    }
}
