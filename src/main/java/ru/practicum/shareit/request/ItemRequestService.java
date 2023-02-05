package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ItemRequestService {
    private ItemRequestRepository itemRequestRepository;
    private ItemRequestMapper itemRequestMapper;
    private UserService userService;

    public ItemRequestResponse createItemRequest(Long authorId, Map<String, String> body) {
        User user = userService.getUserById(authorId);
        String description = Optional.ofNullable(body.get("description"))
                .filter(it -> !it.isBlank())
                .orElseThrow(() -> new IllegalArgumentException("'text' must not be blank"));
        ItemRequest request = new ItemRequest();
        request.setAuthor(user);
        request.setDescription(description);
        return itemRequestMapper.entityToDto(itemRequestRepository.save(request));
    }
}

