package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

@Service
@AllArgsConstructor
public class ItemService {

    private ItemRepository itemRepository;
    private UserService userService;

    public Item createItem(Item item) {
        userService.getUserById(item.getOwnerId());
        return itemRepository.createItem(item);
    }
}
