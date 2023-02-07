package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import org.springframework.data.querydsl.QSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestCreateResponse;
import ru.practicum.shareit.request.dto.ItemRequestGetResponse;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ItemRequestService {
    private ItemRequestRepository itemRequestRepository;
    private ItemRequestMapper itemRequestMapper;
    private UserService userService;
    private ItemService itemService;

    @Transactional
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

    @Transactional(readOnly = true)
    public ItemRequestGetResponse getItemRequestById(Long requestId) {
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Запрос вещи с " + requestId + " не существует"));
        ItemRequestGetResponse response = itemRequestMapper.entityToGetDto(itemRequest);
        response.setItemOfferDtoList(itemService.getItemsByRequestId(requestId));
        return response;
    }

    @Transactional(readOnly = true)
    public ItemRequest checkItemRequestIsExist(long requestId) {
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Запрос вещи с " + requestId + " не существует"));
    }

    @Transactional(readOnly = true)
    public List<ItemRequestGetResponse> getItemRequestByAuthor(Long userId) {
        List<ItemRequest> itemRequests = (List<ItemRequest>) itemRequestRepository.findAll(
                QItemRequest.itemRequest.author.id.eq(userId), new QSort(QItemRequest.itemRequest.created.desc()));
        return itemRequests.stream()
                .map(itemRequestMapper::entityToGetDto)
                .peek(it -> it.setItemOfferDtoList(itemService.getItemsByRequestId(it.getId())))
                .collect(Collectors.toList());
    }

}

