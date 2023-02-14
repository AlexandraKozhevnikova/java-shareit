package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
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

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Map;
import java.util.Optional;


@RestController
@AllArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private static final String USER_HEADER = "X-Sharer-User-Id";
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(USER_HEADER) Long ownerId, @Valid @RequestBody ItemDto itemDto) {
        return itemClient.createItem(ownerId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(USER_HEADER) Long userId, @PathVariable Long itemId,
                                             @RequestBody ItemDto itemDto) {
        return itemClient.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@RequestHeader(USER_HEADER) Long userId, @PathVariable Long itemId) {
        return itemClient.getItem(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnersItems(@RequestHeader(USER_HEADER) Long userId,
                                                 @PositiveOrZero
                                                 @RequestParam(value = "from", required = false)
                                                 Optional<Integer> from,
                                                 @Positive
                                                 @RequestParam(value = "size", required = false)
                                                 Optional<Integer> size) {
        return itemClient.getOwnersItems(userId, from, size);

    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItem(@RequestParam String text,
                                             @RequestHeader(USER_HEADER) Long userId,
                                             @PositiveOrZero
                                             @RequestParam(value = "from", required = false)
                                             Optional<Integer> from,
                                             @Positive
                                             @RequestParam(value = "size", required = false)
                                             Optional<Integer> size) {
        return itemClient.searchItem(userId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(USER_HEADER) Long userId, @PathVariable Long itemId,
                                             @RequestBody Map<String, String> body) {
        return itemClient.addComment(userId, itemId, body);
    }
}