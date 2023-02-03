package ru.practicum.shareit.controllerTest;

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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.BookingOrderController;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingOrderCreateRequest;
import ru.practicum.shareit.booking.dto.BookingOrderResponse;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ExceptionApiHandler;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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
        Mockito.when(bookingService.createBookingOrder(Mockito.any(), Mockito.anyLong()))
                .thenReturn(bookingOrderResponse);

        mvc.perform(MockMvcRequestBuilders.post("/bookings")
                        .content(mapper.writeValueAsString(bookingCreateRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 3)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is(BookingStatus.WAITING.getApiValue())))
                .andExpect(jsonPath("$.item.name", is("cycle")))
                .andExpect(jsonPath("$.item.id", is(2)))
                .andExpect(jsonPath("$.start", is("2030-01-31T19:53:19.363093")))
                .andExpect(jsonPath("$.start", is("2030-01-31T19:53:19.363093")))
                .andExpect(jsonPath("$.booker.id", is(3)));
    }
}
