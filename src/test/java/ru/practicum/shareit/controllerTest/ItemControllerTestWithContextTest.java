package ru.practicum.shareit.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserMapper;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class ItemControllerTestWithContextTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private ItemMapper itemMapper;
    @MockBean
    private UserMapper userMapper;
    @MockBean
    private BookingService bookingService;
    @MockBean
    private UserService UserService;
    @MockBean
    private ItemService itemService;

    private ItemDto itemDto;
    private Item item;

    @BeforeEach
    void setUp() {
        itemDto = new ItemDto();
        itemDto.setName("cycle");
        itemDto.setDescription("new sport cycle");
        itemDto.setIsAvailable(true);

        item = new Item();
        item.setTitle("cycle");
        item.setDescription("new sport cycle");
        item.setIsAvailable(true);
    }

    @Test
    @SneakyThrows
    void updateItem() {
        item.setId(4L);

        when(itemMapper.dtoToItem(any()))
                .thenReturn(item);

        item.setId(4L);
        when(itemService.updateItem(any(), anyLong()))
                .thenReturn(item);

        itemDto.setId(4L);
        when(itemMapper.itemToDto(any()))
                .thenReturn(itemDto);

        mvc.perform(patch("/items/{itemId}", 4)
                        .header("X-Sharer-User-Id", 2)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(4)));
    }

    @Test
    @SneakyThrows
    void createItem_whenResponseValid_thenReturnNewUser() throws Exception {
        when(itemMapper.dtoToItem(isA(ItemDto.class)))
                .thenReturn(item);

        item.setId(2L);
        when(itemService.createItem(any(), anyLong()))
                .thenReturn(item);

        itemDto.setId(2L);
        when(itemMapper.itemToDto(any()))
                .thenReturn(itemDto);

        String result = mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 2)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(itemDto))
                ).andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Assertions.assertEquals(mapper.writeValueAsString(itemDto), result);
    }
}

