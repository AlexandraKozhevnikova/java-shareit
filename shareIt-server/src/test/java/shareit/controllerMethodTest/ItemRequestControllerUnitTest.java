package shareit.controllerMethodTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import shareit.request.ItemRequestController;
import shareit.request.ItemRequestService;
import shareit.user.UserService;

import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ItemRequestControllerUnitTest {
    @InjectMocks
    private ItemRequestController itemRequestController;
    @Mock
    private ItemRequestService itemRequestService;
    @Mock
    private UserService userService;

    @Test
    void createItemRequest_whenValidRequest_thenReturnNewItemRequest() {
        itemRequestController.createItemRequest(1L, Map.of("description", "new phone"));

        verify(itemRequestService, times(1))
                .createItemRequest(1L, Map.of("description", "new phone"));
    }

    @Test
    void getItemRequestById_whenValidRequest_thenReturnItemRequest() {
        itemRequestController.getItemRequestById(1L, 4444L);

        verify(userService, times(1))
                .checkUserExist(1L);
        verify(itemRequestService, times(1))
                .getItemRequestById(4444L);
    }
}
