package ru.practicum.shareit.serviceTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestCreateResponse;
import ru.practicum.shareit.request.dto.ItemRequestGetResponse;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestMapperImpl;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class})
public class ItemRequestServiceTest {
    @InjectMocks
    private ItemRequestService itemRequestService;
    @Mock
    private UserService userService;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private ItemService itemService;
    @Spy
    private ItemRequestMapper itemRequestMapper = new ItemRequestMapperImpl();
    @Captor
    private ArgumentCaptor<ItemRequest> argumentCaptor;

    @Test
    void createItemRequest_whenUserExistAndValidArgs_thenReturnItemResponseDto() {
        User user = doDataPreparation_createUser(1L);
        Mockito.doReturn(user)
                .when(userService).getUserById(anyLong());
        Mockito.doReturn(doDataPreparation_createItemRequest(2L, user, "the car"))
                .when(itemRequestRepository).save(any(ItemRequest.class));

        ItemRequestCreateResponse response = itemRequestService.createItemRequest(1L,
                Map.of("description", "the car"));

        assertEquals(2L, response.getId());
        assertEquals("the car", response.getDescription());
        assertEquals(LocalDateTime.of(2020, 2, 22, 2, 44),
                response.getCreated());

        verify(userService, times(1))
                .getUserById(1L);
        verify(itemRequestRepository).save(argumentCaptor.capture());
        ItemRequest itemRequestForSave = argumentCaptor.getValue();

        assertNull(itemRequestForSave.getId());
        assertEquals("the car", itemRequestForSave.getDescription());
        assertNull(itemRequestForSave.getCreated());
        assertEquals(1L, itemRequestForSave.getAuthor().getId());
    }

    @Test
    void getItemRequest_whenRequestExist_whenReturnItemRequest() {
        User user = doDataPreparation_createUser(1L);
        ItemRequest itemRequest = doDataPreparation_createItemRequest(6666L, user, "lorem ipsum");
        ItemDto itemDto = doDataPreparation_createItemDto(22L, 6666L);
        doReturn(Optional.of(itemRequest))
                .when(itemRequestRepository).findById(anyLong());
        doReturn(List.of(itemDto))
                .when(itemService).getItemsByRequestId(anyLong());

        ItemRequestGetResponse result = itemRequestService.getItemRequest(6666L);

        assertEquals(6666L, result.getId());
        assertEquals("lorem ipsum", result.getDescription());
        assertEquals(LocalDateTime.of(2020, 2, 22, 2, 44), result.getCreated());
        assertEquals(1, result.getItemOfferDtoList().size());
        assertEquals(22L, result.getItemOfferDtoList().get(0).getId());
        assertEquals("book22", result.getItemOfferDtoList().get(0).getName());
        assertEquals("new", result.getItemOfferDtoList().get(0).getDescription());
        assertEquals(true, result.getItemOfferDtoList().get(0).getIsAvailable());
        assertEquals(6666L, result.getItemOfferDtoList().get(0).getRequestId());

        verify(itemRequestRepository, times(1))
                .findById(6666L);
        verify(itemService, times(1))
                .getItemsByRequestId(6666L);

    }

    private ItemRequest doDataPreparation_createItemRequest(long itemRequestId, User user, String description) {
        ItemRequest request = new ItemRequest();
        request.setId(itemRequestId);
        request.setDescription(description);
        request.setAuthor(user);
        request.setCreated(LocalDateTime.of(2020, 2, 22, 2, 44));
        return request;
    }

    private User doDataPreparation_createUser(long userId) {
        User user = new User();
        user.setName("Sasha");
        user.setEmail(RandomUtils.nextLong() + "sa@ya.ru");
        user.setId(userId);
        return user;
    }

    private Item doDataPreparation_createItem(long itemId, User user, ItemRequest itemRequest) {
        Item item = new Item();
        item.setId(itemId);
        item.setTitle("book" + itemId);
        item.setDescription("new");
        item.setOwner(user);
        item.setIsAvailable(true);
        item.setItemRequest(itemRequest);
        return item;
    }

    private ItemDto doDataPreparation_createItemDto(long itemId, long itemRequestId) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(itemId);
        itemDto.setName("book" + itemId);
        itemDto.setDescription("new");
        itemDto.setIsAvailable(true);
        itemDto.setRequestId(itemRequestId);
        return itemDto;
    }


}
