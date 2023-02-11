package ru.practicum.request;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Map;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private static final String USER_HEADER = "X-Sharer-User-Id";
    private ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createItemRequest(@RequestHeader(USER_HEADER) Long authorId,
                                                    @RequestBody Map<String, String> body) {
        return itemRequestClient.createRequest(authorId, body);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequestById(@RequestHeader(USER_HEADER) Long userId,
                                                     @PathVariable Long requestId) {
        return itemRequestClient.getRequest(userId, requestId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemRequestByAuthor(@RequestHeader(USER_HEADER) Long userId) {
        return itemRequestClient.getItemRequestByAuthor(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllOtherItemRequest(@RequestHeader(USER_HEADER) Long userId,
                                                         @PositiveOrZero
                                                         @RequestParam(value = "from", required = false)
                                                         Optional<Integer> from,
                                                         @Positive
                                                         @RequestParam(value = "size", required = false)
                                                         Optional<Integer> size) {
        return itemRequestClient.getAllOtherItemRequest(userId, from, size);
    }
}
