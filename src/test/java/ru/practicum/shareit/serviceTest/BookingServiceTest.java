package ru.practicum.shareit.serviceTest;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QSort;
import ru.practicum.shareit.booking.BookingOrderRepository;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.ItemCanNotBeBookedByOwnerException;
import ru.practicum.shareit.booking.ItemNotAvailableForBookingException;
import ru.practicum.shareit.booking.dto.BookingOrderCreateRequest;
import ru.practicum.shareit.booking.dto.BookingOrderMapping;
import ru.practicum.shareit.booking.dto.BookingOrderMappingImpl;
import ru.practicum.shareit.booking.dto.BookingOrderResponse;
import ru.practicum.shareit.booking.model.BookingOrder;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.QBookingOrder;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @InjectMocks
    private BookingService bookingService;
    @Mock
    private ItemService itemService;
    @Spy
    private BookingOrderMapping bookingMapping = new BookingOrderMappingImpl();
    @Mock
    private UserService userService;
    @Mock
    private BookingOrderRepository bookingRepository;
    @Captor
    private ArgumentCaptor<BookingOrder> argumentCaptor;

    @Test
    void createBookingOrder_whenBookingIsAvailableIsFalse_thenItemNotAvailableForBookingException() {
        BookingOrderCreateRequest dto = new BookingOrderCreateRequest();
        dto.setItemId(22L);
        dto.setStart(LocalDateTime.parse("2040-01-31T19:53:19.363093"));
        dto.setEnd(LocalDateTime.parse("2041-01-31T19:53:19.363093"));

        User user = new User();
        user.setName("Sasha");
        user.setEmail("sa@ya.ru");
        user.setId(1L);

        User user2 = new User();
        user2.setName("Dima");
        user2.setEmail("dim@ya.ru");
        user2.setId(2L);

        Item item = new Item();
        item.setId(22L);
        item.setIsAvailable(true);
        item.setTitle("car");
        item.setDescription("sedan");
        item.setOwner(user2);
        item.setIsAvailable(false);

        doReturn(user)
                .when(userService).getUserById(anyLong());
        doReturn(item)
                .when(itemService).checkItemIsExistInRep(anyLong());


        assertThrows(ItemNotAvailableForBookingException.class,
                () -> bookingService.createBookingOrder(dto, 1L));

        verify(userService, times(1))
                .getUserById(1L);
        verify(itemService, times(1))
                .checkItemIsExistInRep(22L);
        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    void createBookingOrder_whenBookingByOwnerItem_thenItemCanNotBeBookedByOwnerException() {
        BookingOrderCreateRequest dto = new BookingOrderCreateRequest();
        dto.setItemId(22L);
        dto.setStart(LocalDateTime.parse("2040-01-31T19:53:19.363093"));
        dto.setEnd(LocalDateTime.parse("2041-01-31T19:53:19.363093"));

        User user = new User();
        user.setName("Sasha");
        user.setEmail("sa@ya.ru");
        user.setId(1L);

        Item item = new Item();
        item.setId(22L);
        item.setIsAvailable(true);
        item.setTitle("car");
        item.setDescription("sedan");
        item.setOwner(user);
        item.setIsAvailable(true);

        doReturn(user)
                .when(userService).getUserById(anyLong());
        doReturn(item)
                .when(itemService).checkItemIsExistInRep(anyLong());


        assertThrows(ItemCanNotBeBookedByOwnerException.class,
                () -> bookingService.createBookingOrder(dto, 1L));

        verify(userService, times(1))
                .getUserById(1L);
        verify(itemService, times(1))
                .checkItemIsExistInRep(22L);
        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    void reactBookingOrder_whenReactOnBookingNotOwner_thenAccessDeniedException() {
        User user = new User();
        user.setName("Sasha");
        user.setEmail("sa@ya.ru");
        user.setId(1L);

        User user2 = new User();
        user2.setName("Dima");
        user2.setEmail("dim@ya.ru");
        user2.setId(2L);

        Item item = new Item();
        item.setId(22L);
        item.setIsAvailable(true);
        item.setTitle("car");
        item.setDescription("sedan");
        item.setOwner(user);
        item.setIsAvailable(true);

        BookingOrder bookingOrder = new BookingOrder();
        bookingOrder.setId(333L);
        bookingOrder.setItem(item);
        bookingOrder.setStart(LocalDateTime.parse("2040-01-31T19:53:19.363093"));
        bookingOrder.setEnd(LocalDateTime.parse("2041-01-31T19:53:19.363093"));
        bookingOrder.setStatus(BookingStatus.WAITING);
        bookingOrder.setAuthor(user2);

        doReturn(Optional.of(bookingOrder))
                .when(bookingRepository).findById(anyLong());

        assertThrows(AccessDeniedException.class,
                () -> bookingService.reactBookingOrder(2L, 333L, true));
        verify(bookingRepository, times(1))
                .findById(333L);
    }

    @Test
    void reactBookingOrder_whenReactionOwnerIsReject_thenChangeBookingStatus() {
        User user = new User();
        user.setName("Sasha");
        user.setEmail("sa@ya.ru");
        user.setId(1L);

        User user2 = new User();
        user2.setName("Dima");
        user2.setEmail("dim@ya.ru");
        user2.setId(2L);

        Item item = new Item();
        item.setId(22L);
        item.setIsAvailable(true);
        item.setTitle("car");
        item.setDescription("sedan");
        item.setOwner(user);
        item.setIsAvailable(true);

        BookingOrder bookingOrder = new BookingOrder();
        bookingOrder.setId(333L);
        bookingOrder.setItem(item);
        bookingOrder.setStart(LocalDateTime.parse("2040-01-31T19:53:19.363093"));
        bookingOrder.setEnd(LocalDateTime.parse("2041-01-31T19:53:19.363093"));
        bookingOrder.setStatus(BookingStatus.WAITING);
        bookingOrder.setAuthor(user2);

        doReturn(Optional.of(bookingOrder))
                .when(bookingRepository).findById(anyLong());

        doReturn(bookingOrder)
                .when(bookingRepository).save(any(BookingOrder.class));

        BookingOrderResponse response = bookingService.reactBookingOrder(1L, 333L, false);

        verify(bookingRepository, times(1))
                .findById(333L);

        verify(bookingRepository).save(argumentCaptor.capture());
        BookingOrder savedBooking = argumentCaptor.getValue();

        assertEquals(BookingStatus.REJECTED, savedBooking.getStatus());
        assertEquals(2L, response.getAuthor().getId());
        assertEquals(333L, response.getId());
        assertEquals(LocalDateTime.parse("2040-01-31T19:53:19.363093"), response.getStart());
        assertEquals(LocalDateTime.parse("2041-01-31T19:53:19.363093"), response.getEnd());
        assertEquals(22L, response.getItem().getId());
        assertEquals(BookingStatus.REJECTED, response.getStatus());
    }

    @Test
    void reactBookingOrder_whenBookingOrderIsNotWaiting_thenItemNotAvailableForBookingException() {
        User user = new User();
        user.setName("Sasha");
        user.setEmail("sa@ya.ru");
        user.setId(1L);

        User user2 = new User();
        user2.setName("Dima");
        user2.setEmail("dim@ya.ru");
        user2.setId(2L);

        Item item = new Item();
        item.setId(22L);
        item.setIsAvailable(true);
        item.setTitle("car");
        item.setDescription("sedan");
        item.setOwner(user);
        item.setIsAvailable(true);

        BookingOrder bookingOrder = new BookingOrder();
        bookingOrder.setId(333L);
        bookingOrder.setItem(item);
        bookingOrder.setStart(LocalDateTime.parse("2040-01-31T19:53:19.363093"));
        bookingOrder.setEnd(LocalDateTime.parse("2041-01-31T19:53:19.363093"));
        bookingOrder.setStatus(BookingStatus.REJECTED);
        bookingOrder.setAuthor(user2);

        doReturn(Optional.of(bookingOrder))
                .when(bookingRepository).findById(anyLong());

        assertThrows(ItemNotAvailableForBookingException.class,
                () -> bookingService.reactBookingOrder(1L, 333L, true));
        verify(bookingRepository, times(1))
                .findById(333L);
    }

    @Test
    void getBookingOrderWithUserAccess_whenGetByAuthor_thenReturnBookingOrder() throws AccessDeniedException {
        User user = new User();
        user.setName("SashaOwner");
        user.setEmail("sa@ya.ru");
        user.setId(1L);

        User user2 = new User();
        user2.setName("DimaAuthor");
        user2.setEmail("dim@ya.ru");
        user2.setId(2L);

        Item item = new Item();
        item.setId(22L);
        item.setIsAvailable(true);
        item.setTitle("car");
        item.setDescription("sedan");
        item.setOwner(user);
        item.setIsAvailable(true);

        BookingOrder bookingOrder = new BookingOrder();
        bookingOrder.setId(333L);
        bookingOrder.setItem(item);
        bookingOrder.setStart(LocalDateTime.parse("2040-01-31T19:53:19.363093"));
        bookingOrder.setEnd(LocalDateTime.parse("2041-01-31T19:53:19.363093"));
        bookingOrder.setStatus(BookingStatus.APPROVED);
        bookingOrder.setAuthor(user2);

        doReturn(user)
                .when(userService).getUserById(anyLong());
        doReturn(Optional.of(bookingOrder))
                .when(bookingRepository).findById(anyLong());

        BookingOrderResponse response = bookingService.getBookingOrderWithUserAccess(2L, 333L);

        assertEquals(333L, response.getId());
        assertEquals(2L, response.getAuthor().getId());
        assertEquals(22L, response.getItem().getId());
        assertEquals(BookingStatus.APPROVED, response.getStatus());

        verify(userService, times(1))
                .getUserById(2L);
        verify(bookingRepository, times(1))
                .findById(333L);
    }

    @Test
    void getBookingOrderWithUserAccess_whenUserNotAuthorAndNotOwner_thenReturnAccessDeniedException() {
        User user = new User();
        user.setName("SashaOwner");
        user.setEmail("sa@ya.ru");
        user.setId(1L);

        User user2 = new User();
        user2.setName("DimaAuthor");
        user2.setEmail("dim@ya.ru");
        user2.setId(2L);

        Item item = new Item();
        item.setId(22L);
        item.setIsAvailable(true);
        item.setTitle("car");
        item.setDescription("sedan");
        item.setOwner(user);
        item.setIsAvailable(true);

        BookingOrder bookingOrder = new BookingOrder();
        bookingOrder.setId(333L);
        bookingOrder.setItem(item);
        bookingOrder.setStart(LocalDateTime.parse("2040-01-31T19:53:19.363093"));
        bookingOrder.setEnd(LocalDateTime.parse("2041-01-31T19:53:19.363093"));
        bookingOrder.setStatus(BookingStatus.APPROVED);
        bookingOrder.setAuthor(user2);

        doReturn(user)
                .when(userService).getUserById(anyLong());
        doReturn(Optional.of(bookingOrder))
                .when(bookingRepository).findById(anyLong());

        assertThrows(AccessDeniedException.class,
                () -> bookingService.getBookingOrderWithUserAccess(3L, 333L));


        verify(userService, times(1))
                .getUserById(3L);
        verify(bookingRepository, times(1))
                .findById(333L);
    }

    @Test
    void getAllAuthorBookingOrder_() {
        User user = new User();
        user.setName("SashaOwner");
        user.setEmail("sa@ya.ru");
        user.setId(1L);

        User user2 = new User();
        user2.setName("DimaAuthor");
        user2.setEmail("dim@ya.ru");
        user2.setId(2L);

        Item item = new Item();
        item.setId(22L);
        item.setIsAvailable(true);
        item.setTitle("car");
        item.setDescription("sedan");
        item.setOwner(user);
        item.setIsAvailable(true);

        BookingOrder bookingOrder = new BookingOrder();
        bookingOrder.setId(333L);
        bookingOrder.setItem(item);
        bookingOrder.setStart(LocalDateTime.parse("2040-01-31T19:53:19.363093"));
        bookingOrder.setEnd(LocalDateTime.parse("2041-01-31T19:53:19.363093"));
        bookingOrder.setStatus(BookingStatus.APPROVED);
        bookingOrder.setAuthor(user2);

        doReturn(user)
                .when(userService).getUserById(anyLong());
        doReturn(List.of(bookingOrder))
                .when(bookingRepository).findAll(any(BooleanExpression.class), any(Sort.class));


        List<BookingOrderResponse> list = bookingService.getAllAuthorBookingOrder(2L,
                BookingStatus.APPROVED.getApiValue());

        assertEquals(1, list.size());
        assertEquals(333L, list.get(0).getId());

        verify(userService, times(1))
                .getUserById(2L);
        verify(bookingRepository, times(1))
                .findAll(QBookingOrder.bookingOrder.author.id.eq(2L).and(QBookingOrder.bookingOrder.bookingStatusDbCode
                        .eq(200)), new QSort(QBookingOrder.bookingOrder.start.desc()));
    }

    @Test
    void getAllOwnerBookingOrder_() {
        User user = new User();
        user.setName("SashaOwner");
        user.setEmail("sa@ya.ru");
        user.setId(1L);

        User user2 = new User();
        user2.setName("DimaAuthor");
        user2.setEmail("dim@ya.ru");
        user2.setId(2L);

        Item item = new Item();
        item.setId(22L);
        item.setIsAvailable(true);
        item.setTitle("car");
        item.setDescription("sedan");
        item.setOwner(user);
        item.setIsAvailable(true);

        BookingOrder bookingOrder = new BookingOrder();
        bookingOrder.setId(333L);
        bookingOrder.setItem(item);
        bookingOrder.setStart(LocalDateTime.parse("2040-01-31T19:53:19.363093"));
        bookingOrder.setEnd(LocalDateTime.parse("2041-01-31T19:53:19.363093"));
        bookingOrder.setStatus(BookingStatus.APPROVED);
        bookingOrder.setAuthor(user2);

        doReturn(user)
                .when(userService).getUserById(anyLong());
        doReturn(List.of(bookingOrder))
                .when(bookingRepository).findAll(any(BooleanExpression.class), any(Sort.class));


        List<BookingOrderResponse> list = bookingService.getAllOwnerBookingOrder(1L,
                BookingStatus.FUTURE.getApiValue());

        assertEquals(1, list.size());
        assertEquals(333L, list.get(0).getId());

        verify(userService, times(1))
                .getUserById(1L);
    }
}
