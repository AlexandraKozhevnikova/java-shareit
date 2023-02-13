package shareit.controllerEndpointTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import shareit.booking.BookingService;
import shareit.item.ItemController;
import shareit.item.ItemService;
import shareit.item.dto.ItemMapper;
import shareit.item.dto.ItemMapperImpl;
import shareit.item.dto.ItemWithOptionalBookingResponseDto;
import shareit.item.model.Item;
import shareit.request.ItemRequest;
import shareit.user.User;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(ItemMapperImpl.class)
@WebMvcTest(ItemController.class)
public class ItemControllerTest {
    @Autowired
    private MockMvc mvc;
    @SpyBean
    private ItemMapper itemMapper;
    @MockBean
    private BookingService bookingService;
    @MockBean
    private ItemService itemService;
    private Item persistItem;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(100L);

        persistItem = new Item();
        persistItem.setId(1L);
        persistItem.setTitle("cycle");
        persistItem.setDescription("new sport cycle");
        persistItem.setIsAvailable(true);
        persistItem.setOwner(user);
    }

    @Test
    void createItem_whenRequestValidWithoutItemRequestId_thenReturnNewItem() throws Exception {
        doReturn(persistItem)
                .when(itemService).createItem(isA(Item.class), anyLong());

        mvc.perform(MockMvcRequestBuilders
                        .post("/items")
                        .header("X-Sharer-User-Id", 2)
                        .contentType("application/json")
                        .content("{" +
                                "\"name\":\"cycle\"," +
                                "\"description\":\"new sport cycle\"," +
                                "\"requestId\":null," +
                                "\"available\":true " +
                                "}")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(1))
                .andExpect(jsonPath("name").value("cycle"))
                .andExpect(jsonPath("description").value("new sport cycle"))
                .andExpect(jsonPath("requestId").value(nullValue()))
                .andExpect(jsonPath("available").value(true));
    }

    @Test
    void createItem_whenRequestValidWithItemRequestId_thenReturnNewItem() throws Exception {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(7777L);

        persistItem.setItemRequest(itemRequest);
        doReturn(persistItem)
                .when(itemService).createItem(isA(Item.class), anyLong());

        mvc.perform(MockMvcRequestBuilders
                        .post("/items")
                        .header("X-Sharer-User-Id", 2)
                        .contentType("application/json")
                        .content("{" +
                                "\"name\":\"cycle\"," +
                                "\"description\":\"new sport cycle\"," +
                                "\"requestId\":7777," +
                                "\"available\":true " +
                                "}")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(1))
                .andExpect(jsonPath("name").value("cycle"))
                .andExpect(jsonPath("description").value("new sport cycle"))
                .andExpect(jsonPath("requestId").value(7777L))
                .andExpect(jsonPath("available").value(true));
    }

    @Test
    void updateItem_whenValidRequest_thenReturnOkAndItem() throws Exception {
        doReturn(persistItem)
                .when(itemService).updateItem(isA(Item.class), anyLong());

        mvc.perform(MockMvcRequestBuilders
                        .patch("/items/{itemId}", 4)
                        .header("X-Sharer-User-Id", 2)
                        .content("{" +
                                "\"name\":\"cycle\"," +
                                "\"description\":\"new sport cycle\"," +
                                "\"requestId\":null," +
                                "\"available\":true " +
                                "}")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void getItem_whenItemWithoutCommentAndGetByNotOwner() throws Exception {
        ItemWithOptionalBookingResponseDto getAllDto = new ItemWithOptionalBookingResponseDto();
        getAllDto.setId(1L);
        getAllDto.setName("book");
        getAllDto.setDescription("about java");
        getAllDto.setIsAvailable(true);

        doReturn(persistItem)
                .when(itemService).getItemWithUserAccess(anyLong(), anyLong());

        mvc.perform(MockMvcRequestBuilders
                        .get("/items/{itemId}", 1)
                        .header("X-Sharer-User-Id", 2))
                .andExpect(status().isOk())
                .andDo(print());

        verify(itemService, times(1))
                .getItemWithUserAccess(1L, 2L);
    }

    @Test
    void addComment_whenTextNull_Return() throws Exception {
        doThrow(IllegalArgumentException.class)
                .when(itemService).addComment(anyLong(), anyLong(), any());

        mvc.perform(MockMvcRequestBuilders
                        .post("/items/{itemId}/comment", 4)
                        .header("X-Sharer-User-Id", 2)
                        .content("{ \"text\": null }")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorInfo.type")
                        .value("validation"))
                .andExpect(jsonPath("$.errorInfo.description")
                        .value("object does not valid"));
    }

    @Test
    void addComment_whenText_ReturnOK() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .post("/items/{itemId}/comment", 4)
                        .header("X-Sharer-User-Id", 2)
                        .content(" { \"text\" : \"some data\" } ")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }
}
