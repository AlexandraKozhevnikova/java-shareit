package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestCreateResponse;
import ru.practicum.shareit.request.dto.ItemRequestGetResponse;
import ru.practicum.shareit.user.UserService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private static final String USER_HEADER = "X-Sharer-User-Id";
    private ItemRequestService itemRequestService;
    private UserService userService;

    @PostMapping
    public ItemRequestCreateResponse createItemRequest(@RequestHeader(USER_HEADER) Long authorId,
                                                       @RequestBody Map<String, String> body) {
        return itemRequestService.createItemRequest(authorId, body);
    }

    @GetMapping("/{requestId}")
    public ItemRequestGetResponse getItemRequestById(@RequestHeader(USER_HEADER) Long userId,
                                                     @PathVariable Long requestId) {
        userService.checkUserExist(userId);
        return itemRequestService.getItemRequestById(requestId);
    }

    @GetMapping
    public List<ItemRequestGetResponse> getItemRequestByAuthor(@RequestHeader(USER_HEADER) Long userId) {
        userService.checkUserExist(userId);
        return itemRequestService.getItemRequestByAuthor(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestGetResponse> getAllOtherItemRequest(@RequestHeader(USER_HEADER) Long userId,
                                                               @PositiveOrZero
                                                               @RequestParam(value = "from", required = false)
                                                               Optional<Integer> from,
                                                               @Positive
                                                               @RequestParam(value = "size", required = false)
                                                               Optional<Integer> size) {
        userService.checkUserExist(userId);
        return itemRequestService.getAllOtherItemRequest(userId, from, size);
    }


}
