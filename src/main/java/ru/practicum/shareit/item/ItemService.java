package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class ItemService {

    private ItemRepository itemRepository;
    private UserService userService;

    public Item createItem(Item item) {
        userService.getUserById(item.getOwnerId());
        return itemRepository.createItem(item);
    }

    public Item updateItem(Item updatedData) {
        Item itemFromRep = checkItemBelongUser(updatedData);
        Item newItem = itemFromRep;

        if (updatedData.getTitle() != null && !updatedData.getTitle().equals(itemFromRep.getTitle())) {
            newItem.setTitle(updatedData.getTitle());
        }
        if (updatedData.getDescription() != null && !updatedData.getDescription().equals(itemFromRep.getDescription())) {
            newItem.setDescription(updatedData.getDescription());
        }
        if (updatedData.getIsAvailable() != null
                && !updatedData.getIsAvailable().equals(itemFromRep.getIsAvailable())) {
            newItem.setIsAvailable(updatedData.getIsAvailable());
        }

        return itemRepository.updateItem(newItem);
    }

    public Item getItem(int id) {
        Item itemFromRep = itemRepository.getItem(id);
        if (itemFromRep == null) {
            throw new NoSuchElementException("Вещь с id =  " + id + "  не найдена.");
        }
        return itemFromRep;
    }

    private Item checkItemBelongUser(Item unverifiedItem) {
        Item itemFromRep = checkItemIsExistInRep(unverifiedItem);
        if (itemFromRep.getOwnerId() != unverifiedItem.getOwnerId()) {
            throw new NoSuchElementException("У пользователя с id = " + unverifiedItem.getOwnerId() +
                    "  нет прав редактировать вещь с  id = " + unverifiedItem.getId() + " ");
        }
        return itemFromRep;
    }

    private Item checkItemIsExistInRep(Item unverifiedItem) {
        return getItem(unverifiedItem.getId());
    }
}
