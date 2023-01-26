//package ru.practicum.shareit.item;
//
//import org.apache.commons.lang3.BooleanUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.stereotype.Repository;
//import ru.practicum.shareit.item.model.Item;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//public class Repository {
//
//    private Map<Integer, Item> items = new HashMap<>();
//    private int counter;
//
//    public Item createItem(Item item) {
//        item.setId(getNextId());
//        items.put(item.getId(), item);
//        return items.get(item.getId());
//    }
//
//    public Item updateItem(Item item) {
//        items.put(item.getId(), item);
//        return items.get(item.getId());
//    }
//
//    public List<Item> getOwnersItems(int ownerId) {
//        return items.values().stream()
//                .filter(it -> it.getOwnerId().equals(ownerId))
//                .collect(Collectors.toList());
//    }
//
//    public Item getItem(int id) {
//        return items.get(id);
//    }
//
//    @Override
//    public List<Item> searchItem(String text) {
//        return items.values().stream()
//                .filter(it -> BooleanUtils.isTrue(it.getIsAvailable()))
//                .filter(it -> StringUtils.containsIgnoreCase(it.getDescription(), text)
//                        || StringUtils.containsIgnoreCase(it.getTitle(), text))
//                .collect(Collectors.toList());
//    }
//
//    private int getNextId() {
//        return ++counter;
//    }
//}
