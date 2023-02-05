package ru.practicum.shareit.controllerMethodTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestService;

import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ItemRequestControllerUnitTest {
    @InjectMocks
    private ItemRequestController itemRequestController;
    @Mock
    private ItemRequestService itemRequestService;

    @Test
    void createItemRequest_whenValidRequest_thenReturnNewItemRequest() {
        itemRequestController.createItemRequest(1L, Map.of("description", "new phone"));

        verify(itemRequestService, times(1))
                .createItemRequest(1L, Map.of("description", "new phone"));
    }
}
