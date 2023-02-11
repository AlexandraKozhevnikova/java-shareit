package shareit.request;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.querydsl.QPageRequest;
import org.springframework.data.querydsl.QSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shareit.item.ItemService;
import shareit.request.dto.ItemRequestCreateResponse;
import shareit.request.dto.ItemRequestGetResponse;
import shareit.request.dto.ItemRequestMapper;
import shareit.user.User;
import shareit.user.UserService;

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
    private static final int DEFAULT_FROM = 1;
    private static final int DEFAULT_SIZE = 100;

    @Transactional
    public ItemRequestCreateResponse createItemRequest(Long authorId, Map<String, String> body) {
        User user = userService.getUserById(authorId);
        String description = Optional.ofNullable(body.get("description"))
                .filter(StringUtils::isNoneBlank)
                .orElseThrow(() -> new IllegalArgumentException("'text' must not be blank")); //todo
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

    @Transactional(readOnly = true)
    public List<ItemRequestGetResponse> getAllOtherItemRequest(Long userId, Optional<Integer> from, Optional<Integer> size) {
        Page<ItemRequest> itemRequests = itemRequestRepository.findAll(QItemRequest.itemRequest.author.id.ne(userId),
                QPageRequest.of(from.orElse(DEFAULT_FROM), size.orElse(DEFAULT_SIZE))
                        .withSort(new QSort(QItemRequest.itemRequest.created.desc())));

        return itemRequests.stream()
                .map(itemRequestMapper::entityToGetDto)
                .peek(it -> it.setItemOfferDtoList(itemService.getItemsByRequestId(it.getId())))
                .collect(Collectors.toList());
    }
}

