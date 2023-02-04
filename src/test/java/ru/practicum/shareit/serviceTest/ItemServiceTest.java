package ru.practicum.shareit.serviceTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
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
    void addComment_textIsBlank_thenException() {
        assertThrows(IllegalArgumentException.class,
                () -> itemService.addComment(1L, 1L, ""));

        assertThrows(IllegalArgumentException.class,
                () -> itemService.addComment(1L, 1L, "       "));

        verify(commentRepository, never())
                .save(any());
    }
}
