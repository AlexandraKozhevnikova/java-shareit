package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestCreateResponse;
import ru.practicum.shareit.request.dto.ItemRequestGetResponse;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ItemRequestService {
    private ItemRequestRepository itemRequestRepository;
    private ItemRequestMapper itemRequestMapper;
    private UserService userService;
    private ItemService itemService;

    public ItemRequestCreateResponse createItemRequest(Long authorId, Map<String, String> body) {
        User user = userService.getUserById(authorId);
        String description = Optional.ofNullable(body.get("description"))
                .filter(it -> !it.isBlank())
                .orElseThrow(() -> new IllegalArgumentException("'text' must not be blank"));
        ItemRequest request = new ItemRequest();
        request.setAuthor(user);
        request.setDescription(description);
        return itemRequestMapper.entityToCreateDto(itemRequestRepository.save(request));
    }

    public ItemRequestGetResponse getItemRequest(Long requestId) {
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Запрос вещи с " + requestId + " не существует"));
        ItemRequestGetResponse response = itemRequestMapper.entityToGetDto(itemRequest);
        response.setItemOfferDtoList(itemService.getItemsByRequestId(requestId));
        return response;
    }

    public ItemRequest checkItemRequestIsExist(long requestId) {
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Запрос вещи с " + requestId + " не существует"));
    }
}

