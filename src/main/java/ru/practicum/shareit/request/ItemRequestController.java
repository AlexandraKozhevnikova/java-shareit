package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestResponse;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private static final String USER_HEADER = "X-Sharer-User-Id";
    private ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestResponse createItemRequest(@RequestHeader(USER_HEADER) Long authorId,
                                                 @RequestBody Map<String, String> body) {
        return itemRequestService.createItemRequest(authorId, body);
    }

}
