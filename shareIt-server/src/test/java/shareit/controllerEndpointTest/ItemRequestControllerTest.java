package shareit.controllerEndpointTest;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import shareit.item.dto.ItemDto;
import shareit.request.ItemRequestController;
import shareit.request.ItemRequestService;
import shareit.request.dto.ItemRequestCreateResponse;
import shareit.request.dto.ItemRequestGetResponse;
import shareit.user.UserService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.exparity.hamcrest.date.LocalDateTimeMatchers.within;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
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
    public static final String REQUEST = "/requests";
    private static final String USER_HEADER = "X-Sharer-User-Id";
    @MockBean
    private ItemRequestService itemRequestService;
    @MockBean
    private UserService userService;
    @Autowired
    private MockMvc mvc;

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
        ItemRequestCreateResponse response = new ItemRequestCreateResponse();
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

    @Test
    void getItemRequestById_whenRequestValid_thenReturnItemRequest() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(11L);
        itemDto.setName("cycle");
        itemDto.setDescription("new sport cycle");
        itemDto.setIsAvailable(true);
        itemDto.setRequestId(5555L);

        ItemRequestGetResponse response = new ItemRequestGetResponse();
        response.setId(5555L);
        response.setDescription("новый велосипед");
        response.setCreated(LocalDateTime.of(2020, 2, 22, 2, 44));
        response.setItemOfferDtoList(List.of(itemDto));

        doReturn(response)
                .when(itemRequestService).getItemRequestById(anyLong());

        mvc.perform(MockMvcRequestBuilders
                        .get(REQUEST + "/{requestId}", 5555)
                        .header(USER_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(5555))
                .andExpect(jsonPath("description").value("новый велосипед"))
                .andExpect(jsonPath("created")
                        .value("2020-02-22T02:44:00"))
                .andExpect(jsonPath("items").value(hasSize(1)))
                .andExpect(jsonPath("items.[0].id").value(11))
                .andExpect(jsonPath("items.[0].name").value("cycle"))
                .andExpect(jsonPath("items.[0].description").value("new sport cycle"))
                .andExpect(jsonPath("items.[0].available").value(true))
                .andExpect(jsonPath("items.[0].requestId").value(5555));

        verify(userService, times(1))
                .checkUserExist(1L);
        verify(itemRequestService, times(1))
                .getItemRequestById(5555L);
    }

    @Test
    void getItemRequestByAuthor_whenRequestValid_thenReturnItemRequests() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(11L);
        itemDto.setName("cycle");
        itemDto.setDescription("new sport cycle");
        itemDto.setIsAvailable(true);
        itemDto.setRequestId(5555L);

        ItemRequestGetResponse response = new ItemRequestGetResponse();
        response.setId(5555L);
        response.setDescription("новый велосипед");
        response.setCreated(LocalDateTime.of(2020, 2, 22, 2, 44));
        response.setItemOfferDtoList(List.of(itemDto));

        doReturn(List.of(response))
                .when(itemRequestService).getItemRequestByAuthor(anyLong());

        mvc.perform(MockMvcRequestBuilders
                        .get(REQUEST)
                        .header(USER_HEADER, 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].id").value(5555))
                .andExpect(jsonPath("[0].description").value("новый велосипед"))
                .andExpect(jsonPath("[0].created")
                        .value("2020-02-22T02:44:00"))
                .andExpect(jsonPath("[0].items").value(hasSize(1)))
                .andExpect(jsonPath("[0].items.[0].id").value(11))
                .andExpect(jsonPath("[0].items.[0].name").value("cycle"))
                .andExpect(jsonPath("[0].items.[0].description").value("new sport cycle"))
                .andExpect(jsonPath("[0].items.[0].available").value(true))
                .andExpect(jsonPath("[0].items.[0].requestId").value(5555));

        verify(userService, times(1))
                .checkUserExist(1L);
        verify(itemRequestService, times(1))
                .getItemRequestByAuthor(1L);
    }
}
