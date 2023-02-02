package ru.practicum.shareit.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.BookingOrderController;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingOrderCreateRequest;
import ru.practicum.shareit.booking.dto.BookingOrderResponse;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;

@ExtendWith({MockitoExtension.class})
public class BookingOrderControllerTest {
    @Mock
    private BookingService bookingService;
    @InjectMocks
    private BookingOrderController controller;
    private final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mvc;
    private BookingOrderResponse bookingOrderResponse;
    private BookingOrderCreateRequest bookingCreateRequest;
    private UserDto userDto;
//    private final ApplicationContext applicationContext;
//    private final ApplicationConversionService conversionService;
//    private final ExceptionApiHandler exceptionApiHandler;
//
//    public BookingOrderControllerTest(ApplicationContext applicationContext, ApplicationConversionService conversionService, ExceptionApiHandler exceptionApiHandler) {
//        this.applicationContext = applicationContext;
//        this.conversionService = conversionService;
//        this.exceptionApiHandler = exceptionApiHandler;
//    }

    @BeforeEach
    void setUp1() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

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

//    @BeforeEach
//    public void setUp() {
//        applicationContext.getBeansOfType(Formatter.class).values().forEach(conversionService::addFormatter);
//
//        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new
//                MappingJackson2HttpMessageConverter();
//        mappingJackson2HttpMessageConverter.setObjectMapper(getMapper());
//
//
//        mvc = MockMvcBuilders.standaloneSetup(controller)
//                .setControllerAdvice(exceptionApiHandler)
//                .setConversionService(conversionService)
//                .setMessageConverters(mappingJackson2HttpMessageConverter)
//                .build();
//    }

    private static ObjectMapper getMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    @Test
    void createBooking() throws Exception {
        Mockito.when(bookingService.createBookingOrder(Mockito.any(), Mockito.anyLong()))
                .thenReturn(bookingOrderResponse);
        mapper.registerModule(new JavaTimeModule());
        mvc.perform(MockMvcRequestBuilders
                        .post("/bookings")
                        .content(mapper.writeValueAsString(bookingCreateRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 3)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", is(BookingStatus.WAITING.getApiValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.item.name", is("cycle")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.item.id", is(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.booker.id", is(3)));
    }
}
