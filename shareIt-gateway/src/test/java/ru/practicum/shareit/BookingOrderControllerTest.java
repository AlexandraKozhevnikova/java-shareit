package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingOrderController;
import ru.practicum.shareit.booking.dto.BookingOrderCreateRequest;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingOrderController.class)
public class BookingOrderControllerTest {
    @MockBean
    private BookingClient bookingClient;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    private BookingOrderCreateRequest bookingCreateRequest;


    @BeforeEach
    void setUpData() {
        bookingCreateRequest = new BookingOrderCreateRequest();
        bookingCreateRequest.setItemId(2L);
        bookingCreateRequest.setStart(LocalDateTime.parse("2030-01-31T19:53:19.363093"));
        bookingCreateRequest.setEnd(LocalDateTime.parse("2030-02-02T19:53:19.363129"));
    }

    @Test
    public void createBookingOrder_whenEmptyBody_thenBadRequest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .post("/bookings")
                        .content(mapper.writeValueAsString(new BookingOrderCreateRequest()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 3)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createBookingOrder_whenEmptyUserHeader_thenBadRequest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .post("/bookings")
                        .content(mapper.writeValueAsString(bookingCreateRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    @Test
    public void reactBookingOrder_whenRequestWithoutParams_thenBadRequest() throws Exception {
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
    public void getAllAuthorBookingOrders_whenRequestWithoutState_thenOkAndUseAll() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/bookings")
                        .header("X-Sharer-User-Id", 3)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingClient, times(1))
                .getAllAuthorBookingOrder(3L, "ALL", Optional.empty(), Optional.empty());
    }

    @Test
    public void getAllOwnerBookingOrders_whenRequestWithoutState_thenOkAndUseAll() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/bookings/owner")
                        .header("X-Sharer-User-Id", 3)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingClient, times(1))
                .getAllOwnerBookingOrder(3L, "ALL", Optional.empty(), Optional.empty());
    }
}
