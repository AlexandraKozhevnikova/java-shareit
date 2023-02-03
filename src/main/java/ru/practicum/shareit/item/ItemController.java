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
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.GetAllItemsForOwnerResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@AllArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemMapper itemMapper;
    private final ItemService itemService;
    private final BookingService bookingService;
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto createItem(@RequestHeader(USER_HEADER) Long ownerId, @Valid @RequestBody ItemDto itemDto) {
        Item item = itemMapper.dtoToItem(itemDto);
        Item createdItem = itemService.createItem(item, ownerId);
        return itemMapper.itemToDto(createdItem);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(USER_HEADER) Long userId, @PathVariable Long itemId,
                              @RequestBody ItemDto itemDto) {
        Item updatedData = itemMapper.dtoToItem(itemDto);
        updatedData.setId(itemId);
        Item updatedItem = itemService.updateItem(updatedData, userId);
        return itemMapper.itemToDto(updatedItem);
    }

    @GetMapping("/{itemId}")
    public GetAllItemsForOwnerResponseDto getItem(@RequestHeader(USER_HEADER) Long userId, @PathVariable Long itemId) {
        Item item = itemService.getItemWithUserAccess(itemId, userId);
        GetAllItemsForOwnerResponseDto response = itemMapper.itemToDtoWithBookingInfo(item);
        if (userId.equals(item.getOwner().getId())) {
            response.setNextBooking(bookingService.getNextBookingForItem(item.getId(), userId));
            response.setLastBooking(bookingService.getLastBookingForItem(item.getId(), userId));
        }

        List<Comment> comments = itemService.getComment(itemId);
        response.setComments(comments.stream().map(itemMapper::commentToDto).collect(Collectors.toList()));
        return response;
    }

    @GetMapping
    public List<GetAllItemsForOwnerResponseDto> getOwnersItems(@RequestHeader(USER_HEADER) Long userId) {
        return itemService.getOwnersItems(userId).stream().map(itemMapper::itemToDtoWithBookingInfo).peek(it -> {
            it.setLastBooking(bookingService.getLastBookingForItem(it.getId(), userId));
            it.setNextBooking(bookingService.getNextBookingForItem(it.getId(), userId));
        }).collect(Collectors.toList());
    }

    @GetMapping("/search")
    public List<ItemDto> searchItem(@RequestParam String text) {
        return itemService.findAllWithText(text).stream().map(itemMapper::itemToDto).collect(Collectors.toList());
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(USER_HEADER) Long userId, @PathVariable Long itemId, @RequestBody @Valid @NotEmpty @NotNull Map<String, String> text) {
        Comment comment = itemService.addComment(userId, itemId, text.get("text"));
        return itemMapper.commentToDto(comment);
    }
}