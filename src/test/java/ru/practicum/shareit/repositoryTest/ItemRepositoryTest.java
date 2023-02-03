package ru.practicum.shareit.repositoryTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    private void addItems() {
        User user1 = new User();
        user1.setName("first");
        user1.setEmail("first@ya.ru");
        user1 = userRepository.save(user1);

        User user2 = new User();
        user2.setName("first2");
        user2.setEmail("first2@ya.ru");
        user2 = userRepository.save(user2);

        userRepository.save(user1);
        userRepository.save(user2);

        Item item = new Item();
        item.setTitle("title");
        item.setDescription("description");
        item.setOwner(user1);
        item.setIsAvailable(true);

        Item item2 = new Item();
        item2.setTitle("title2");
        item2.setDescription("description2");
        item2.setOwner(user2);
        item2.setIsAvailable(true);

        Item item3 = new Item();
        item3.setTitle("title3");
        item3.setDescription("description3");
        item3.setOwner(user1);
        item3.setIsAvailable(false);

        itemRepository.save(item);
        itemRepository.save(item2);
        itemRepository.save(item3);
    }

    @AfterEach
    private void deleteItems() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
    }


    @Test
    void findAllByOwnerIdOrderById_whenFindExist_thenReturnTwoWithSort() {
        List<Item> list = itemRepository.findAllByOwnerIdOrderById(1L);
        assertEquals(2, list.size());
        assertEquals(1, list.get(0).getId());
        assertEquals(3, list.get(1).getId());
    }

    @Test
    void findAllByOwnerIdOrderById_whenFindNotExist_thenReturnEmpty() {
        List<Item> list = itemRepository.findAllByOwnerIdOrderById(700L);
        assertTrue(list.isEmpty());
    }
}