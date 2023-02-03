package ru.practicum.shareit.controllerEndpointTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.format.Formatter;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.BookingOrderController;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.ItemCanNotBeBookedByOwnerException;
import ru.practicum.shareit.booking.ItemNotAvailableForBookingException;
import ru.practicum.shareit.booking.dto.BookingOrderCreateRequest;
import ru.practicum.shareit.booking.dto.BookingOrderResponse;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ExceptionApiHandler;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingOrderController.class)
public class BookingOrderControllerTest {
    @MockBean
    private BookingService bookingService;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private BookingOrderController controller;
    @Autowired
    private MockMvc mvc;
    private BookingOrderResponse bookingOrderResponse;
    private BookingOrderCreateRequest bookingCreateRequest;
    private UserDto userDto;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private final FormattingConversionService conversionService = new FormattingConversionService();
    @Autowired
    private ExceptionApiHandler exceptionApiHandler;


    @BeforeEach
    void setUpData() {

        userDto = new UserDto();
        userDto.setId(3);
        userDto.setEmail("randomUtils@ya.ru");
        userDto.setName("Antony");

        ItemDto itemDto = new ItemDto();
        itemDto.setId(2L);
        itemDto.setName("cycle");
        itemDto.setDescription("new sport cycle");
        itemDto.setIsAvailable(true);

        bookingCreateRequest = new BookingOrderCreateRequest();
        bookingCreateRequest.setItemId(2L);
        bookingCreateRequest.setStart(LocalDateTime.parse("2030-01-31T19:53:19.363093"));
        bookingCreateRequest.setEnd(LocalDateTime.parse("2030-02-02T19:53:19.363129"));

        bookingOrderResponse = new BookingOrderResponse();
        bookingOrderResponse.setId(1);
        bookingOrderResponse.setAuthor(userDto);
        bookingOrderResponse.setItem(itemDto);
        bookingOrderResponse.setStatus(BookingStatus.WAITING);
        bookingOrderResponse.setStart(LocalDateTime.parse("2030-01-31T19:53:19.363093"));
        bookingOrderResponse.setEnd(LocalDateTime.parse("2030-02-02T19:53:19.363129"));
    }

    @BeforeEach
    public void setUpConfForDataTimeFormat() {
        applicationContext.getBeansOfType(Formatter.class).values().forEach(conversionService::addFormatter);

        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setObjectMapper(mapper);

        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(exceptionApiHandler)
                .setConversionService(conversionService)
                .setMessageConverters(mappingJackson2HttpMessageConverter)
                .build();
    }

    @Test
    void createBookingOrder_whenValidRequest_thenOkAndReturnTheBooking() throws Exception {
        Mockito.when(bookingService.createBookingOrder(Mockito.any(), anyLong()))
                .thenReturn(bookingOrderResponse);

        mvc.perform(MockMvcRequestBuilders.post("/bookings")
                        .content(mapper.writeValueAsString(bookingCreateRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 3)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is(BookingStatus.WAITING.getApiValue())))
                .andExpect(jsonPath("$.item.name", is("cycle")))
                .andExpect(jsonPath("$.item.id", is(2)))
                .andExpect(jsonPath("$.start", is("2030-01-31T19:53:19.363093")))
                .andExpect(jsonPath("$.start", is("2030-01-31T19:53:19.363093")))
                .andExpect(jsonPath("$.booker.id", is(3)));
    }

    @Test
    void createBookingOrder_whenEmptyBody_thenBadRequest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/bookings")
                        .content(mapper.writeValueAsString(new BookingOrderCreateRequest()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 3)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBookingOrder_whenEmptyUserHeader_thenBadRequest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/bookings")
                        .content(mapper.writeValueAsString(bookingCreateRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBookingOrder_whenItemIsNotAvailable_thenBadRequest() throws Exception {
        Mockito.when(bookingService.createBookingOrder(Mockito.any(), anyLong()))
                .thenThrow(ItemNotAvailableForBookingException.class);

        mvc.perform(MockMvcRequestBuilders.post("/bookings")
                        .content(mapper.writeValueAsString(bookingCreateRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 3)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.errorInfo.type").value("logic"))
                .andExpect(jsonPath("$.errorInfo.description")
                        .value("object does not available for booking"));
    }

    @Test
    void createBookingOrder_whenOwnerBookOwnItem_thenNotFound() throws Exception {
        Mockito.when(bookingService.createBookingOrder(Mockito.any(), anyLong()))
                .thenThrow(ItemCanNotBeBookedByOwnerException.class);

        mvc.perform(MockMvcRequestBuilders.post("/bookings")
                        .content(mapper.writeValueAsString(bookingCreateRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 3)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.errorInfo.type").value("logic"))
                .andExpect(jsonPath("$.errorInfo.description")
                        .value("item can not be booked by owner"));
    }

    @Test
    void reactBookingOrder_whenValidRequest_thenOKt() throws Exception {
        Mockito.when(bookingService.reactBookingOrder(anyLong(), anyLong(), eq(true)))
                .thenReturn(bookingOrderResponse);

        bookingOrderResponse.setStatus(BookingStatus.APPROVED);

        mvc.perform(MockMvcRequestBuilders.patch("/bookings/{bookingIs}", 1)
                        .header("X-Sharer-User-Id", 3)
                        .param("approved", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.booker").isNotEmpty());
    }

    @Test
    void reactBookingOrder_whenItemHasNotStatusWaiting_thenBadRequest() throws Exception {
        Mockito.when(bookingService.reactBookingOrder(anyLong(), anyLong(), eq(true)))
                .thenThrow(ItemNotAvailableForBookingException.class);

        mvc.perform(MockMvcRequestBuilders.patch("/bookings/{bookingIs}", 1)
                        .header("X-Sharer-User-Id", 3)
                        .param("approved", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.errorInfo.type").value("logic"))
                .andExpect(jsonPath("$.errorInfo.description")
                        .value("object does not available for booking"));
    }

    @Test
    void reactBookingOrder_whenReactNotOwner_thenNotFound() throws Exception {
        Mockito.when(bookingService.reactBookingOrder(anyLong(), anyLong(), eq(true)))
                .thenAnswer(invocation -> {
                    throw new AccessDeniedException("");
                });

        mvc.perform(MockMvcRequestBuilders.patch("/bookings/{bookingIs}", 1)
                        .header("X-Sharer-User-Id", 3)
                        .param("approved", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(""))
                .andExpect(jsonPath("$.errorInfo.type").value("common"))
                .andExpect(jsonPath("$.errorInfo.description").value("no detailed exception"));
    }

    @Test
    void reactBookingOrder_whenRequestWithoutParams_thenBadRequest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .patch("/bookings/{bookingIs}", 1)
                        .header("X-Sharer-User-Id", 3)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Required request parameter 'approved' for method parameter type Boolean is not present"))
                .andExpect(jsonPath("$.errorInfo.type").value("validation"))
                .andExpect(jsonPath("$.errorInfo.description").value("request parameter is missing"));
    }

    @Test
    void getAllAuthorBookingOrders_whenRequestWithoutState_thenOkAndUseAll() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/bookings")
                        .header("X-Sharer-User-Id", 3)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingService, times(1))
                .getAllAuthorBookingOrder(3L, "ALL");
    }

    @Test
    void getAllOwnerBookingOrders_whenRequestWithoutState_thenOkAndUseAll() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/bookings/owner")
                        .header("X-Sharer-User-Id", 3)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingService, times(1))
                .getAllOwnerBookingOrder(3L, "ALL");
    }
}
