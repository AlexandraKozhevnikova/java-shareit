package shareit.serviceTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import shareit.item.CommentRepository;
import shareit.item.ItemRepository;
import shareit.item.ItemService;
import shareit.item.model.Item;
import shareit.request.ItemRequest;
import shareit.request.ItemRequestService;
import shareit.user.User;
import shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    @InjectMocks
    private ItemService itemService;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserService userService;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestService itemRequestService;
    @Captor
    private ArgumentCaptor<Item> argumentCaptor;

    User userWithId;
    Item itemWithId;
    Item itemWithId2;
    Item itemWithId3;

    @BeforeEach
    void setup() {
        userWithId = new User();
        userWithId.setId(6L);
        userWithId.setEmail("ol@ya.ru");
        userWithId.setName("Olesya");

        itemWithId = new Item();
        itemWithId.setId(2L);
        itemWithId.setTitle("title");
        itemWithId.setDescription("description");
        itemWithId.setOwner(userWithId);
        itemWithId.setIsAvailable(true);

        itemWithId2 = new Item();
        itemWithId2.setId(2L);
        itemWithId2.setTitle("title2");
        itemWithId2.setDescription("description2");
        itemWithId2.setIsAvailable(false);

        itemWithId3 = new Item();
        itemWithId3.setId(2L);
        itemWithId3.setTitle("title");
        itemWithId3.setDescription("description");
        itemWithId3.setIsAvailable(true);
    }

    @Test
    void updateItem_whenAllNew_then() {
        doReturn(userWithId)
                .when(userService).getUserById(anyLong());
        doReturn(Optional.of(itemWithId))
                .when(itemRepository).findById(anyLong());
        doReturn(itemWithId)
                .when(itemRepository).save(any(Item.class));

        Item updatedItem = itemService.updateItem(itemWithId2, 6L);

        Assertions.assertEquals(itemWithId2.getTitle(), updatedItem.getTitle());
        Assertions.assertEquals(itemWithId2.getDescription(), updatedItem.getDescription());
        Assertions.assertEquals(6L, updatedItem.getOwner().getId());
        Assertions.assertEquals(itemWithId2.getIsAvailable(), updatedItem.getIsAvailable());
    }

    @Test
    void updateItem_whenAllTheSame_then() {
        doReturn(userWithId)
                .when(userService).getUserById(anyLong());
        doReturn(Optional.of(itemWithId))
                .when(itemRepository).findById(anyLong());
        doReturn(itemWithId)
                .when(itemRepository).save(any(Item.class));

        Item updatedItem = itemService.updateItem(itemWithId3, 6L);

        Assertions.assertEquals(itemWithId.getTitle(), updatedItem.getTitle());
        Assertions.assertEquals(itemWithId.getDescription(), updatedItem.getDescription());
        Assertions.assertEquals(6L, updatedItem.getOwner().getId());
        Assertions.assertEquals(itemWithId.getIsAvailable(), updatedItem.getIsAvailable());
    }

    @Test
    void updateItem_whenAllNull_then() {
        Item itemEmpty = new Item();
        itemEmpty.setId(2L);
        doReturn(userWithId)
                .when(userService).getUserById(anyLong());
        doReturn(Optional.of(itemWithId))
                .when(itemRepository).findById(anyLong());
        doReturn(itemWithId)
                .when(itemRepository).save(any(Item.class));

        Item updatedItem = itemService.updateItem(itemEmpty, 6L);

        Assertions.assertEquals(itemWithId.getTitle(), updatedItem.getTitle());
        Assertions.assertEquals(itemWithId.getDescription(), updatedItem.getDescription());
        Assertions.assertEquals(6L, updatedItem.getOwner().getId());
        Assertions.assertEquals(itemWithId.getIsAvailable(), updatedItem.getIsAvailable());
    }

    @Test
    void checkItemBelongUser_whenItemNotBelongUse_thenException() {
        User otherUser = new User();
        otherUser.setId(100L);

        Item unverifiedItem = new Item();
        unverifiedItem.setId(2L);
        unverifiedItem.setTitle("title");
        unverifiedItem.setDescription("description");
        unverifiedItem.setIsAvailable(true);
        unverifiedItem.setOwner(otherUser);

        doReturn(Optional.of(itemWithId))
                .when(itemRepository).findById(anyLong());

        assertThrows(NoSuchElementException.class,
                () -> itemService.checkItemBelongUser(unverifiedItem));
    }

    @Test
    void createItem_whenRequestIdIsExist() {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(7777L);

        Item interimItem = new Item();
        interimItem.setTitle("cycle");
        interimItem.setDescription("new sport cycle");
        interimItem.setIsAvailable(true);
        interimItem.setItemRequest(itemRequest);

        User requestAuthor = new User();
        requestAuthor.setId(2L);

        ItemRequest persistItemRequest = new ItemRequest();
        persistItemRequest.setId(7777L);
        persistItemRequest.setDescription("description");
        persistItemRequest.setAuthor(requestAuthor);
        persistItemRequest.setCreated(LocalDateTime.of(2020, 2, 22, 2, 44));

        doReturn(userWithId)
                .when(userService).getUserById(anyLong());
        doReturn(persistItemRequest)
                .when(itemRequestService).checkItemRequestIsExist(anyLong());

        itemService.createItem(interimItem, 6L);

        verify(userService, times(1))
                .getUserById(6L);
        verify(itemRequestService, times(1))
                .checkItemRequestIsExist(7777L);
        verify(itemRepository).save(argumentCaptor.capture());
        Item result = argumentCaptor.getValue();

        assertNull(result.getId());
        assertEquals("cycle", result.getTitle());
        assertEquals("new sport cycle", result.getDescription());
        assertTrue(result.getIsAvailable());
        assertEquals(7777L, result.getItemRequest().getId());
        assertEquals("description", result.getItemRequest().getDescription());
        assertEquals(6L, result.getOwner().getId());
    }
}
