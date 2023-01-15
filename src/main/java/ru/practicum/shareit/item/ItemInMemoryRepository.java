package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        items.put(item.getId(), item);
        return items.get(item.getId());
    }

    @Override
    public List<Item> getOwnersItems(int ownerId) {
        return items.values().stream()
                .filter(it -> it.getOwnerId().equals(ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public Item getItem(int id) {
        return items.get(id);
    }

    @Override
    public List<Item> searchItem(String text) {
        return items.values().stream()
                .filter(it -> it.getIsAvailable().equals(true))
                .filter(it -> it.getDescription().toLowerCase().contains(text.toLowerCase())
                        || it.getTitle().toLowerCase().contains(text.toLowerCase()))
                .collect(Collectors.toList());
    }

    private int getNextId() {
        return ++counter;
    }
}
