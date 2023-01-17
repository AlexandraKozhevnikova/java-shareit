package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import javax.validation.Valid;


@RestController
@AllArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemMapper itemMapper;
    private final ItemService itemService;

    @PostMapping
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") int id,
                              @Valid @RequestBody ItemDto itemDto) {
        Item item = itemMapper.dtoToItem(itemDto);
        item.setOwnerId(id);
        Item createdItem = itemService.createItem(item);
        return itemMapper.itemToDto(createdItem);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") int userId,
                              @PathVariable int itemId,
                              @RequestBody ItemDto itemDto) {
        Item updatedData = itemMapper.dtoToItem(itemDto);
        updatedData.setId(itemId);
        updatedData.setOwnerId(userId);
        Item updatedItem = itemService.updateItem(updatedData);
        return itemMapper.itemToDto(updatedItem);
    }
}
