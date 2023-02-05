package ru.practicum.shareit.controllerEndpointTest;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestResponse;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.exparity.hamcrest.date.LocalDateTimeMatchers.within;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {
    @MockBean
    private ItemRequestService itemRequestService;
    @Autowired
    private MockMvc mvc;

    public static final String REQUEST = "/requests";
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Test
    void createItemRequest_whenWithoutUserHeader_then400MissingRequestHeaderException() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .post(REQUEST)
                        .content("{\n" +
                                "    \"description\": \"новый смартфон\"\n" +
                                "}")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error").value("Required request header " +
                        "'X-Sharer-User-Id' for method parameter type Long is not present"));
    }

    @Test
    void createItemRequest_whenInvalidJsonBody_then404HttpMessageNotReadableException() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .post(REQUEST)
                        .header(USER_HEADER, 1)
                        .content("{ \"description 12334\" }")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("error", containsString("JSON parse error: Unexpected character")));
    }


    @Test
    void createItemRequest_whenWithoutBody_then404HttpMessageNotReadableException() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .post(REQUEST)
                        .header(USER_HEADER, 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("error", containsString("Required request body is missing")));
    }

    @Test
    void createItemRequest_whenRequestValid_thenOkAndReturnItemRequestDto() throws Exception {
        ItemRequestResponse response = new ItemRequestResponse();
        response.setId(1L);
        response.setDescription("новый смартфон");
        response.setCreated(LocalDateTime.now());

        doReturn(response)
                .when(itemRequestService).createItemRequest(anyLong(), anyMap());

        String result = mvc.perform(MockMvcRequestBuilders
                        .post(REQUEST)
                        .header(USER_HEADER, 1)
                        .content("{\n" +
                                "    \"description\": \"новый смартфон\"\n" +
                                "}")
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(1)))
                .andExpect(jsonPath("description", is("новый смартфон")))
                .andExpect(jsonPath("created", startsWith(LocalDateTime.now().toString().substring(0, 10))))
                .andReturn().getResponse().getContentAsString();

        assertThat(LocalDateTime.parse(JsonPath.parse(result).read("created")),
                within(2, ChronoUnit.MINUTES, LocalDateTime.now()));

        verify(itemRequestService, times(1))
                .createItemRequest(1L, Map.of("description", "новый смартфон"));
    }

    @Test
    void createItemRequest_whenServiceReturnIllegalArgumentException_then400() throws Exception {
        doThrow(IllegalArgumentException.class)
                .when(itemRequestService).createItemRequest(anyLong(), anyMap());

        mvc.perform(MockMvcRequestBuilders
                        .post(REQUEST)
                        .header(USER_HEADER, 1)
                        .content("{\n" +
                                "    \"some else\": \"   123  \"\n" +
                                "}")
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errorInfo.description", is("object does not valid")));
    }
}
