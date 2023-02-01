//package ru.practicum.shareit.controllerTest;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import ru.practicum.shareit.booking.BookingService;
//import ru.practicum.shareit.item.ItemController;
//import ru.practicum.shareit.item.ItemService;
//import ru.practicum.shareit.item.dto.ItemDto;
//import ru.practicum.shareit.item.dto.ItemMapper;
//import ru.practicum.shareit.item.dto.ItemMapperImpl;
//import ru.practicum.shareit.item.model.Item;
//
//import java.nio.charset.StandardCharsets;
//import java.sql.SQLException;
//
//import static org.hamcrest.Matchers.is;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(controllers = ItemController.class)
//public class itemControllerTestWithContext {
//    @Autowired
//    ObjectMapper mapper;
//    @MockBean
//    ItemMapper itemMapper;
//
//    @MockBean
//    BookingService bookingService;
//    @MockBean
//    ItemService itemService;
//    @Autowired(
//    private MockMvc mvc;
//    private ItemDto itemDto;
//
//    @BeforeEach
//    void setUp() {
//        itemDto = new ItemDto();
//        itemDto.setId(2);
//        itemDto.setName("cycle");
//        itemDto.setDescription("new sport cycle");
//        itemDto.setIsAvailable(true);
//
//        itemMapper = new ItemMapperImpl();
//    }
//
//    @Test
//    void updateItem() throws Exception {
//        when(itemService.updateItem(any(), any()))
//                .thenThrow(SQLException.class);
//
//        when(itemMapper.dtoToItem(any()))
//                .thenReturn(new Item());
//
//        when(itemMapper.itemToDto(any()))
//                .thenReturn(itemDto);
//
//        mvc.perform(post("/users")
//                        .content(mapper.writeValueAsString(itemDto))
//                        .characterEncoding(StandardCharsets.UTF_8)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class));
//    }
//
//
//}
//
