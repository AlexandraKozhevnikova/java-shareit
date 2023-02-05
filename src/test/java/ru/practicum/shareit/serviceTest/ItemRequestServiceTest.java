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
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestMapperImpl;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class})
public class ItemRequestServiceTest {
    @InjectMocks
    private ItemRequestService itemRequestService;
    @Mock
    private UserService userService;
    @Mock
    private ItemRequestRepository itemRequestRepository;
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

        ItemRequestResponse response = itemRequestService.createItemRequest(1L,
                Map.of("description", "the car"));

        assertEquals(2L, response.getId());
        assertEquals("the car", response.getDescription());
        assertEquals(LocalDateTime.of(2020, 2, 22, 2, 44),
                response.getCreated());

        Mockito.verify(userService, Mockito.times(1))
                .getUserById(1L);
        verify(itemRequestRepository).save(argumentCaptor.capture());
        ItemRequest itemRequestForSave = argumentCaptor.getValue();

        assertNull(itemRequestForSave.getId());
        assertEquals("the car", itemRequestForSave.getDescription());
        assertNull(itemRequestForSave.getCreated());
        assertEquals(1L, itemRequestForSave.getAuthor().getId());
    }

    private ItemRequest doDataPreparation_createItemRequest(long itemRequestId, User user, String description) {
        ItemRequest request = new ItemRequest();
        request.setId(itemRequestId);
        request.setAuthor(user);
        request.setDescription(description);
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


}
