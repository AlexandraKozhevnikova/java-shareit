package ru.practicum.shareit.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private ItemMapper itemMapper;
    @MockBean
    private BookingService bookingService;
    @MockBean
    private ItemService itemService;
    private ItemDto itemRequestDto;
    private ItemDto itemResponseDto;
    private Item itemRequest;
    private Item itemResponse;

    @BeforeEach
    void setUp() {
        itemRequestDto = new ItemDto();
        itemRequestDto.setName("cycle");
        itemRequestDto.setDescription("new sport cycle");
        itemRequestDto.setIsAvailable(true);

        itemResponseDto = new ItemDto();
        itemResponseDto.setId(1L);
        itemResponseDto.setName("cycle");
        itemResponseDto.setDescription("new sport cycle");
        itemResponseDto.setIsAvailable(true);

        itemRequest = new Item();
        itemRequest.setTitle("cycle");
        itemRequest.setDescription("new sport cycle");
        itemRequest.setIsAvailable(true);

        itemResponse = new Item();
        itemResponse.setId(1L);
        itemResponse.setTitle("cycle");
        itemResponse.setDescription("new sport cycle");
        itemResponse.setIsAvailable(true);
    }

    @Test
    void createItem_whenRequestValid_thenReturnNewUser() throws Exception {
        doReturn(itemRequest)
                .when(itemMapper).dtoToItem(isA(ItemDto.class));
        doReturn(itemResponse)
                .when(itemService).createItem(isA(Item.class), anyLong());
        doReturn(itemResponseDto)
                .when(itemMapper).itemToDto(isA(Item.class));

        String result = mvc.perform(MockMvcRequestBuilders
                        .post("/items")
                        .header("X-Sharer-User-Id", 2)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(itemRequestDto))
                ).andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Assertions.assertEquals(mapper.writeValueAsString(itemResponseDto), result);
    }

    @Test
    void updateItem_whenValidRequest_thenReturnOkAndItem() throws Exception {
        doReturn(itemRequest)
                .when(itemMapper).dtoToItem(isA(ItemDto.class));
        doReturn(itemResponse)
                .when(itemService).updateItem(isA(Item.class), anyLong());
        doReturn(itemResponseDto)
                .when(itemMapper).itemToDto(isA(Item.class));

        mvc.perform(MockMvcRequestBuilders
                        .patch("/items/{itemId}", 4)
                        .header("X-Sharer-User-Id", 2)
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }
}

