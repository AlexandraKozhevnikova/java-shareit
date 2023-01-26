package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@AllArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemMapper itemMapper;
    private final ItemService itemService;
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto createItem(@RequestHeader(USER_HEADER) long ownerId,
                              @Valid @RequestBody ItemDto itemDto) {
        Item item = itemMapper.dtoToItem(itemDto);
        Item createdItem = itemService.createItem(item, ownerId);
        return itemMapper.itemToDto(createdItem);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(USER_HEADER) int userId,
                              @PathVariable long itemId,
                              @RequestBody ItemDto itemDto) {
        Item updatedData = itemMapper.dtoToItem(itemDto);
        updatedData.setId(itemId);
        Item updatedItem = itemService.updateItem(updatedData, userId);
        return itemMapper.itemToDto(updatedItem);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@RequestHeader(USER_HEADER) int userId,
                           @PathVariable int itemId) {
        Item item = itemService.getItemWithUserAccess(itemId, userId);
        return itemMapper.itemToDto(item);
    }

    @GetMapping
    public List<ItemDto> getOwnersItems(@RequestHeader(USER_HEADER) int userId) {
        return itemService.getOwnersItems(userId).stream()
                .map(itemMapper::itemToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    public List<ItemDto> searchItem(@RequestParam String text) {
        return itemService.searchItem(text).stream()
                .map(itemMapper::itemToDto)
                .collect(Collectors.toList());
    }
}
