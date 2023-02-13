package shareit.serviceTest;

import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import shareit.booking.BookingOrderRepository;
import shareit.booking.BookingService;
import shareit.booking.ItemCanNotBeBookedByOwnerException;
import shareit.booking.ItemNotAvailableForBookingException;
import shareit.booking.dto.BookingOrderCreateRequest;
import shareit.booking.dto.BookingOrderMapping;
import shareit.booking.dto.BookingOrderMappingImpl;
import shareit.booking.dto.BookingOrderResponse;
import shareit.booking.model.BookingOrder;
import shareit.booking.model.BookingStatus;
import shareit.item.ItemService;
import shareit.item.model.Item;
import shareit.user.User;
import shareit.user.UserService;

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

        BookingOrderResponse response = bookingService.reactBookingOrder(1L, 333L, false);

        verify(bookingRepository, times(1))
                .findById(333L);

        assertEquals(BookingStatus.REJECTED, response.getStatus());
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
        doReturn(new PageImpl<>(List.of(bookingOrder)))
                .when(bookingRepository).findAll(any(Predicate.class), any(Pageable.class));

        List<BookingOrderResponse> list = bookingService.getAllAuthorBookingOrder(
                2L,
                BookingStatus.APPROVED.getApiValue(),
                Optional.empty(),
                Optional.empty()
        );

        assertEquals(1, list.size());
        assertEquals(333L, list.get(0).getId());

        verify(userService, times(1))
                .getUserById(2L);
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
        doReturn(new PageImpl<>(List.of(bookingOrder)))
                .when(bookingRepository).findAll(any(Predicate.class), any(Pageable.class));


        List<BookingOrderResponse> list = bookingService.getAllOwnerBookingOrder(
                1L,
                BookingStatus.FUTURE.getApiValue(),
                Optional.empty(),
                Optional.empty());

        assertEquals(1, list.size());
        assertEquals(333L, list.get(0).getId());

        verify(userService, times(1))
                .getUserById(1L);
    }
}
