package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {

    Item createItem(Item item);

    Item updateItem(Item item);

    List<Item> getOwnersItems(int ownerId);

    Item getItem(int id);

    List<Item> searchItem(String text);
}
