package shareit.item;

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
import shareit.booking.BookingService;
import shareit.booking.dto.BookingInfoDto;
import shareit.item.dto.CommentDto;
import shareit.item.dto.ItemDto;
import shareit.item.dto.ItemMapper;
import shareit.item.dto.ItemWithOptionalBookingResponseDto;
import shareit.item.model.Comment;
import shareit.item.model.Item;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@AllArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private static final String USER_HEADER = "X-Sharer-User-Id";
    private final ItemMapper itemMapper;
    private final ItemService itemService;
    private final BookingService bookingService;

    @PostMapping
    public ItemDto createItem(@RequestHeader(USER_HEADER) Long ownerId, @RequestBody ItemDto itemDto) {
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
    public ItemWithOptionalBookingResponseDto getItem(@RequestHeader(USER_HEADER) Long userId, @PathVariable Long itemId) {
        Item item = itemService.getItemWithUserAccess(itemId, userId);
        ItemWithOptionalBookingResponseDto response = itemMapper.itemToDtoWithBookingInfo(item);
        if (userId.equals(item.getOwner().getId())) {
            bookingService.getNextBookingForItems(List.of(item.getId()))
                    .stream()
                    .findFirst()
                    .ifPresent(next -> response.setNextBooking(new BookingInfoDto(next.getId(), next.getAuthor()
                            .getId())));
            bookingService.getLastBookingForItems(List.of(item.getId()))
                    .stream()
                    .findFirst()
                    .ifPresent(next -> response.setLastBooking(new BookingInfoDto(next.getId(), next.getAuthor()
                            .getId())));
        }

        List<Comment> comments = itemService.getComment(itemId);
        response.setComments(comments.stream()
                .map(itemMapper::commentToDto)
                .collect(Collectors.toList()));
        return response;
    }

    @GetMapping
    public List<ItemWithOptionalBookingResponseDto> getOwnersItems(@RequestHeader(USER_HEADER) Long userId,
                                                                   @RequestParam(value = "from", required = false)
                                                                   Optional<Integer> from,
                                                                   @RequestParam(value = "size", required = false)
                                                                   Optional<Integer> size) {


        return itemService.getOwnersItems(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItem(@RequestParam String text,
                                    @RequestHeader(USER_HEADER) Long userId,
                                    @RequestParam(value = "from", required = false)
                                    Optional<Integer> from,
                                    @RequestParam(value = "size", required = false)
                                    Optional<Integer> size) {
        return itemService.findAllWithText(text, from, size).stream()
                .map(itemMapper::itemToDto)
                .collect(Collectors.toList());
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(USER_HEADER) Long userId, @PathVariable Long itemId,
                                 @RequestBody Map<String, String> text) {
        Comment comment = itemService.addComment(userId, itemId, text.get("text"));
        return itemMapper.commentToDto(comment);
    }
}