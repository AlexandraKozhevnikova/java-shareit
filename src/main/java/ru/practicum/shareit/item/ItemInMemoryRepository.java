package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository
public class ItemInMemoryRepository implements ItemRepository {

    private Map<Integer, Item> items = new HashMap<>();
    private int counter;

    @Override
    public Item createItem(Item item) {
        item.setId(getNextId());
        items.put(item.getId(), item);
        return items.get(item.getId());
    }

    @Override
    public Item updateItem(Item item) {
        return null;
    }

    @Override
    public Collection<Item> getOwnersItems(int ownerId) {
        return null;
    }

    @Override
    public Item getItem(int id) {
        return null;
    }

    @Override
    public Collection<Item> searchItem(String text) {
        return null;
    }

    private int getNextId() {
        return ++counter;
    }
}
