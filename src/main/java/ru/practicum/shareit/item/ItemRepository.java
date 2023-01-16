package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemRepository {

    Item createItem(Item item);

    Item updateItem(Item item);

    Collection<Item> getOwnersItems(int ownerId);

    Item getItem(int id);

    Collection<Item> searchItem(String text);
}
